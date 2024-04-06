package com.pehrs.vespa.yql.plugin;

import static nl.altindag.ssl.util.internal.ValidationUtils.requireNotNull;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.pehrs.vespa.yql.plugin.settings.VespaClusterConfig;
import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsState;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;
import nl.altindag.ssl.util.TrustManagerUtils;
import nl.altindag.ssl.util.internal.IOUtils;
import nl.altindag.ssl.util.internal.ValidationUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.pem.util.PemUtils;

/**
 * Static facade for global functions and constants
 */
public class YQL implements StartupActivity {

  private static final Logger log = LoggerFactory.getLogger(YQL.class);

  public YQL() {
  }

  public static List<String> YQL_KEYWORDS = List.of(
      // Basic keywords
      "select",
      "from",
      "where",
      "order by",
      "limit",
      "offset",
      "timeout",
      // where keywords
      "nearestNeighbor",
      "weightedSet",
      "predicate",
      "dotProduct",
      "userQuery",
      "nonEmpty",
      "userInput",
      "geoLocation",
      "sameElement",
      "matches",
      "range",
      "contains",
      "weakAnd",
      "phrase",
      "fuzzy",
      "equiv",
      "onear",
      "wand",
      "true",
      "false",
      "rank",
      "near",
      "and",
      "not",
      "uri",
      "or"
  );

  public static SSLFactory createSslFactory(String caPemFile, String clientCertFile,
      String clientKeyFile) {
    log.trace("createSslFactory(" + caPemFile + ", " + clientCertFile + ", " + clientKeyFile + ")");
    X509ExtendedTrustManager trustManager;
    trustManager = createTrustManager(caPemFile);

    X509ExtendedKeyManager keyManager;
    keyManager = createKeyManager(clientCertFile, clientKeyFile);

    SSLFactory sslFactory = SSLFactory.builder()
        .withTrustMaterial(trustManager)
        .withIdentityMaterial(keyManager)
        .build();
    return sslFactory;
  }

  private static X509ExtendedKeyManager createKeyManager(String clientCertFile,
      String clientKeyFile) {
    X509ExtendedKeyManager keyManager;
    try (FileInputStream certFile = new FileInputStream(clientCertFile);
        FileInputStream keyFile = new FileInputStream(clientKeyFile)) {
      String certificateChainContent = IOUtils.getContent(certFile);
      String privateKeyContent = IOUtils.getContent(keyFile);
      keyManager = PemUtils.parseIdentityMaterial(certificateChainContent,
          privateKeyContent, null);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
    return keyManager;
  }

  private static X509ExtendedTrustManager createTrustManager(String caPemFile) {
    X509ExtendedTrustManager trustManager;
    try (FileInputStream file = new FileInputStream(caPemFile)) {
      String caPemContent = IOUtils.getContent(file);
      List<X509Certificate> cert = PemUtils.parseCertificate(caPemContent);
      trustManager = TrustManagerUtils.createTrustManager(cert);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
    return trustManager;
  }


  public static YqlResult executeQuery(String yqlQueryRequest)
      throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();

    Optional<VespaClusterConfig> configOpt = settings.getCurrentClusterConfig();
    VespaClusterConfig config = configOpt.orElseThrow(() ->
        new RuntimeException("Could not get current connection configuration!")
    );

    HttpPost request = new HttpPost(config.queryEndpoint);
    request.addHeader("content-type", "application/json;charset=UTF-8");
    StringEntity entity = new StringEntity(yqlQueryRequest, ContentType.APPLICATION_JSON);
    request.setEntity(entity);

    HttpClientBuilder httpClientBuilder = HttpClients.custom();
    if (settings.sslAllowAll) {
      log.warn("Allowing all server TLS certificates");
      allowAll(httpClientBuilder);
    }
    if (config.sslUseClientCert) {
      SSLFactory sslFactory = YQL.createSslFactory(
          config.sslCaCert,
          config.sslClientCert,
          config.sslClientKey);
      LayeredConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
          sslFactory.getSslContext(),
          sslFactory.getSslParameters().getProtocols(),
          sslFactory.getSslParameters().getCipherSuites(),
          sslFactory.getHostnameVerifier()
      );
      httpClientBuilder.setSSLSocketFactory(socketFactory);
    }
    try (CloseableHttpClient httpClient = httpClientBuilder.build()) {
      CloseableHttpResponse response = httpClient.execute(request);
      try {
        String responseString = EntityUtils.toString(response.getEntity());
        log.debug("HTTP Response Status: " + response.getStatusLine());
        log.trace("HTTP Response: " + responseString);
        return new YqlResult(responseString);
      } finally {
        response.close();
      }
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  public static void allowAll(HttpClientBuilder httpClientBuilder)
      throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
    TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
    SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
        .build();
    SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
        NoopHostnameVerifier.INSTANCE);

    Registry<ConnectionSocketFactory> socketFactoryRegistry =
        RegistryBuilder.<ConnectionSocketFactory>create()
            .register("https", sslsf)
            .register("http", new PlainConnectionSocketFactory())
            .build();

    BasicHttpClientConnectionManager connectionManager =
        new BasicHttpClientConnectionManager(socketFactoryRegistry);
    httpClientBuilder.setConnectionManager(connectionManager);
  }

  private static FileSystem jarFs = null;

  public static String getResource(String resourceName) {
    try {

      URL url;
      url = YQL.class.getResource(resourceName);
      // Workaround for java.nio.file.FileSystemNotFoundException issue in Intellij plugins
      final Map<String, String> env = new HashMap<>();
      final String[] array = url.toURI().toString().split("!");
      if (array.length > 1) {
        if (jarFs == null) {
          jarFs = FileSystems.newFileSystem(URI.create(array[0]), env);
        }
        final Path path = jarFs.getPath(array[1]);
        return Files.readString(path, Charset.forName("utf-8"));
      } else {
        return Files.readString(Paths.get(url.toURI()), Charset.forName("utf-8"));
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private static Properties getBuildInfoProperties() throws IOException {
    String propsStr = YQL.getResource("/build-info.properties");
    Properties properties = new Properties();
    try (StringReader reader = new StringReader(propsStr)) {
      properties.load(reader);
      return properties;
    }
  }

  public static String getBuildTimestamp() {
    try {
      Properties properties = getBuildInfoProperties();
      String ts = properties.getProperty("build-timestamp");
      if (ts == null) {
        return "";
      }
      return "Build: " + ts;
    } catch (Exception ex) {
      return "";
    }
  }

  public static String getBuiltByUser() {
    try {
      Properties properties = getBuildInfoProperties();
      String user = properties.getProperty("built-by");
      if (user == null) {
        return "";
      }
      return "BuiltBy: " + user;
    } catch (Exception ex) {
      return "";
    }
  }


  public static String getDefaultBrowserScript() {
    String OS = System.getProperty("os.name", "linux").toLowerCase(Locale.ENGLISH);
    if ((OS.contains("mac")) || (OS.contains("darwin"))) {
      return "open";
    } else if (OS.contains("win")) {
      return "start";
    } else {
      return "/usr/bin/xdg-open";
    }
  }

  @Override
  public void runActivity(@NotNull Project project) {
    // Run once at startup :-)

    // NOT NEEDED for now:
    // Set properties for graphstream UI lib
    // System.setProperty("org.graphstream.ui", "swing");
  }
}

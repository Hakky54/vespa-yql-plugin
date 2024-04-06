package com.pehrs.vespa.yql.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pehrs.vespa.yql.plugin.settings.VespaClusterConfig;
import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsState;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;
import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.pem.util.PemUtils;
import nl.altindag.ssl.util.TrustManagerUtils;
import nl.altindag.ssl.util.internal.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VespaClusterConnection {

  private static final Logger log = LoggerFactory.getLogger(VespaClusterConnection.class);

  final static ObjectMapper objectMapper = new ObjectMapper();

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

  static X509ExtendedKeyManager createKeyManager(String clientCertFile,
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

  static X509ExtendedTrustManager createTrustManager(String caPemFile) {
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
      SSLFactory sslFactory = createSslFactory(
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

  public static JsonNode jsonGet(VespaClusterConfig config, String url) {
    String res = get(config, url);
    try {
      return objectMapper.readTree(res);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static String get(VespaClusterConfig config, String url) {
    HttpGet request = new HttpGet(url);
    return execute(config, request);
  }

  public static String execute(VespaClusterConfig config, HttpRequestBase requestBase) {

    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
//
//    Optional<VespaClusterConfig> configOpt = settings.getCurrentClusterConfig();
//    VespaClusterConfig config = configOpt.orElseThrow(() ->
//        new RuntimeException("Could not get current connection configuration!")
//    );

    SocketConfig socketConfig = SocketConfig.custom()
        .setSoTimeout(10_000)
        .build();
    HttpClientBuilder httpClientBuilder = HttpClients.custom()
        .setDefaultSocketConfig(socketConfig);

    URI uri = requestBase.getURI();
    if (uri.getScheme().equals("https")) {
      // NOTE: We cannot use the sslAllowAll for the config api, it typically requires the client cert to work
      if (settings.sslAllowAll && uri.toString().equals(config.queryEndpoint)) {
        log.warn("Allowing all server TLS certificates");
        try {
          allowAll(httpClientBuilder);
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
          throw new RuntimeException(e);
        }
      }
      if (config.sslUseClientCert) {
        SSLFactory sslFactory = VespaClusterConnection.createSslFactory(
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
    }
    try (CloseableHttpClient httpClient = httpClientBuilder.build();
        CloseableHttpResponse response = httpClient.execute(requestBase)) {
      String responseString = EntityUtils.toString(response.getEntity());
      log.trace("HTTP Response Status: " + response.getStatusLine());
      log.trace("HTTP Response: " + responseString);
      return responseString;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

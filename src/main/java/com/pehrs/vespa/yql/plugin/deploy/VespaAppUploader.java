package com.pehrs.vespa.yql.plugin.deploy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.progress.ProgressIndicator;
import com.pehrs.vespa.yql.plugin.VespaClusterConnection;
import com.pehrs.vespa.yql.plugin.settings.VespaClusterConfig;
import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsState;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import nl.altindag.ssl.SSLFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class VespaAppUploader {

  private static final Logger log = LoggerFactory.getLogger(VespaAppUploader.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static PrepareAndDeployResponse packagePrepareAndActivate(String canonicalPath, ProgressIndicator indicator)
      throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

    File dir = new File(canonicalPath);

    if(!dir.isDirectory()) {
      throw new RuntimeException("" + canonicalPath + " is not a directory");
    }
    indicator.setFraction(0.2);
    indicator.setText("Packaging application");
    // FIXME: More validations here?
    log.info("src dir: " + dir);

    File zipFile = File.createTempFile("yql-vespa-upload", ".zip");
    zipFile.deleteOnExit();
    zipDirectory(dir, zipFile);
    log.info("ZIP DONE: " + zipFile.getAbsolutePath());

    indicator.setFraction(0.6);
    indicator.setText("Uploading...");

    JsonNode json = packagePrepareAndActivate(zipFile);
    return objectMapper.treeToValue(json, PrepareAndDeployResponse.class);
  }

  private static JsonNode packagePrepareAndActivate(File zipFile)
      throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();

    Optional<VespaClusterConfig> configOpt = settings.getCurrentClusterConfig();
    VespaClusterConfig config = configOpt.orElseThrow(() ->
        new RuntimeException("Could not get current connection configuration!")
    );


    String url = String.format("%s/application/v2/tenant/%s/prepareandactivate",
        config.configEndpoint,
        settings.tenant);
    HttpPost request = new HttpPost(url);
    request.addHeader("content-type", "application/zip");
    FileEntity entity = new FileEntity(zipFile);
    request.setEntity(entity);

    SocketConfig socketConfig = SocketConfig.custom()
        .setSoTimeout(10_000)
        .build();
    HttpClientBuilder httpClientBuilder = HttpClients.custom()
        .setDefaultSocketConfig(socketConfig);

    // NOTE: We cannot use the sslAllowAll for the config api, it typically requires the client cert to work
//    if (settings.sslAllowAll) {
//      log.warn("Allowing all server TLS certificates");
//      YQL.allowAll(httpClientBuilder);
//    }
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
    try (CloseableHttpClient httpClient = httpClientBuilder
        .build()) {
      CloseableHttpResponse response = httpClient.execute(request);
      try {
        String responseString = EntityUtils.toString(response.getEntity());
        log.info("HTTP Response Status: " + response.getStatusLine());
        log.info("HTTP Response: " + responseString);

        JsonNode responseJson = objectMapper.readTree(
            responseString);
        if(responseJson.has("error-code")) {
          // We have an error!!
          throw new RuntimeException(responseJson.get("message").asText());
        }
        return responseJson;
      } finally {
        response.close();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void populateFilesList(List<String> filesListInDir, File dir) throws IOException {
    File[] files = dir.listFiles();
    for(File file : files){
      if(file.isFile()) filesListInDir.add(file.getAbsolutePath());
      else populateFilesList(filesListInDir, file);
    }
  }


  private static void zipDirectory(File dir, File zipFile) {
    try {
      List<String> filesListInDir = new ArrayList<>();
      populateFilesList(filesListInDir, dir);


      //now zip files one by one
      //create ZipOutputStream to write to the zip file
      FileOutputStream fos = new FileOutputStream(zipFile);
      ZipOutputStream zos = new ZipOutputStream(fos);
      for(String filePath : filesListInDir){
        log.trace("Zipping "+filePath);
        //for ZipEntry we need to keep only relative file path, so we used substring on absolute path
        ZipEntry ze = new ZipEntry(filePath.substring(dir.getAbsolutePath().length()+1, filePath.length()));
        zos.putNextEntry(ze);
        //read the file and write to ZipOutputStream
        FileInputStream fis = new FileInputStream(filePath);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = fis.read(buffer)) > 0) {
          zos.write(buffer, 0, len);
        }
        zos.closeEntry();
        fis.close();
      }
      zos.close();
      fos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}

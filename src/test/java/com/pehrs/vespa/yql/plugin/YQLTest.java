package com.pehrs.vespa.yql.plugin;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import nl.altindag.ssl.SSLFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Ignore;

public class YQLTest {


  /**
   * This test can only be run if you have started the test cluster (via docker-compose)
   * as described in the README
   */
  @Ignore
  //@Test
  public void testSSL() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

    HttpClientBuilder httpClientBuilder = HttpClients.custom();

    // YQL.allowAll(httpClientBuilder);

    HttpGet request = new HttpGet("https://localhost:8443/state/v1/health");

    String root = ".";
    String caPemFile = root + "/vespa-cluster/pki/vespa/ca-vespa.pem";
    String clientCertFile = root + "/vespa-cluster/pki/client/client.pem";
    String clientKeyFile = root + "/vespa-cluster/pki/client/client.key";
    SSLFactory sslFactory = YQL.createSslFactory(caPemFile, clientCertFile,
        clientKeyFile);
    LayeredConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
        sslFactory.getSslContext(),
        sslFactory.getSslParameters().getProtocols(),
        sslFactory.getSslParameters().getCipherSuites(),
        sslFactory.getHostnameVerifier()
    );
    httpClientBuilder.setSSLSocketFactory(socketFactory);

    try(CloseableHttpClient httpClient = httpClientBuilder.build()) {
      CloseableHttpResponse response = httpClient.execute(request);
      try {
        String responseString = EntityUtils.toString(response.getEntity());
        System.out.println("HTTP Response Status: " + response.getStatusLine());
        System.out.println("HTTP Response: " + responseString);
      } finally {
        response.close();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

}
package com.pehrs.vespa.yql.plugin.settings;

import com.intellij.util.xmlb.annotations.Tag;

public class VespaClusterConfig {

  public String name;
  public String queryEndpoint;
  public String configEndpoint;

  @Tag("ssl-use-client-cert")
  public boolean sslUseClientCert = false;

  @Tag("ssl-ca-cert")
  public String sslCaCert = ""; // root + "/vespa-cluster/pki/vespa/ca-vespa.pem";

  @Tag("ssl-client-cert")
  public String sslClientCert = ""; // root + "/vespa-cluster/pki/client/client.pem";
  @Tag("ssl-client-key")
  public String sslClientKey = ""; // root + "/vespa-cluster/pki/client/client.key";


  public VespaClusterConfig() {}

  public VespaClusterConfig(
      String name,
      String queryEndpoint,
      String configEndpoint,
      Boolean useClientCert,
      String caCert,
      String clientCert,
      String clientKey) {
    this.name = name;
    this.queryEndpoint = queryEndpoint;
    this.configEndpoint = configEndpoint;
    this.sslUseClientCert = useClientCert;
    this.sslCaCert = caCert;
    this.sslClientCert = clientCert;
    this.sslClientKey = clientKey;
  }

  @Override
  public VespaClusterConfig clone() {
    return new VespaClusterConfig(
        this.name,
        this.queryEndpoint,
        this.configEndpoint,
        this.sslUseClientCert,
        this.sslCaCert,
        this.sslClientCert,
        this.sslClientKey
    );
  }

  @Override
  public String toString() {
    return "" + name;
  }
}

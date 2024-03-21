package com.pehrs.vespa.yql.plugin.settings;

public class VespaClusterConfig {

  public String name;
  public String queryEndpoint;
  public String configEndpoint;

  public VespaClusterConfig() {}

  public VespaClusterConfig(String name, String queryEndpoint, String configEndpoint) {
    this.name = name;
    this.queryEndpoint = queryEndpoint;
    this.configEndpoint = configEndpoint;
  }

  @Override
  public String toString() {
    return "" + name;
  }
}

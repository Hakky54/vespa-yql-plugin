package com.pehrs.vespa.yql.plugin.trace;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ZipkinEndpoint {
  String serviceName;

  @Override
  public String toString() {
    return "ZipkinEndpoint{" +
        "serviceName='" + serviceName + '\'' +
        '}';
  }

  public static ZipkinEndpoint QUERY_CONTAINER_ENDPOINT = ZipkinEndpoint.builder()
      .serviceName("Vespa Query Container")
      .build();
  public static ZipkinEndpoint PROTON_SERVER_ENDPOINT = ZipkinEndpoint.builder()
      .serviceName("Vespa Proton Server")
      .build();
}

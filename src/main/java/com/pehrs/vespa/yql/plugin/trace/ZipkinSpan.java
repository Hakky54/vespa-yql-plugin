package com.pehrs.vespa.yql.plugin.trace;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ZipkinSpan {
  String traceId;
  String id;
  String parentId;
  String name;
  String kind;
  Long timestamp; // Microseconds
  Long duration; // Microseconds
  ZipkinEndpoint localEndpoint;
  ZipkinEndpoint remoteEndpoint;
  Map<String, Object> tags;

  @Override
  public String toString() {
    return "ZipkinSpan{" +
        "traceId='" + traceId + '\'' +
        ", id='" + id + '\'' +
        ", parentId='" + parentId + '\'' +
        ", name='" + name + '\'' +
        ", kind='" + kind + '\'' +
        ", timestamp=" + timestamp +
        ", duration=" + duration +
        ", localEndpoint=" + localEndpoint +
        ", remoteEndpoint=" + remoteEndpoint +
        ", tags=" + tags +
        '}';
  }
}

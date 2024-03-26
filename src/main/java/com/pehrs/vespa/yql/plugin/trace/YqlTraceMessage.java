package com.pehrs.vespa.yql.plugin.trace;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class YqlTraceMessage extends YqlTraceNodeBase {

  public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS zzz",
      Locale.ENGLISH);

  String documentType;
  Integer distributionKey;
  String startTime;
  Double durationNs;

  @Builder
  public YqlTraceMessage(YqlTraceNodeBase parent,
      String name,
      List<YqlTraceNodeBase> children,
      Double relativeEndTsMs,
      String documentType,
      Integer distributionKey,
      String startTime,
      Double durationNs) {
    super(parent, name, children, relativeEndTsMs);
    this.documentType = documentType;
    this.distributionKey = distributionKey;
    this.startTime = startTime;
    this.durationNs = durationNs;
  }

  public Long getStartTimeNs() {
    try {
      Date date = YqlTraceMessage.sdf.parse(startTime);
      return 1_000_000L * date.getTime();
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  @JsonIgnore
  public YqlTraceMessage getParentMessage() {
    return this;
  }

  public Long getStartTimeMicros() {
    return getStartTimeNs() / 1000L;
  }

  public Long getDurationMicros() {
    return (long) (getDurationNs() / 1000L);
  }

  @Override
  public String documentType() {
    return documentType;
  }

  @Override
  public Integer distributionKey() {
    return distributionKey;
  }

  @Override
  public String toString() {
    return "YqlTraceMessage{" +
        "documentType='" + documentType + '\'' +
        ", distributionKey=" + distributionKey +
        ", startTime='" + startTime + '\'' +
        ", durationNs=" + durationNs +
        ", parent=" + (parent == null ? "null" : parent.name) +
        ", name='" + name + '\'' +
        ", children=" + children +
        ", relativeEndTsMs=" + relativeEndTsMs +
        '}';
  }

  public List<ZipkinSpan> createZipkinSpans(String traceId) {
    ZipkinSpan msgSpan = ZipkinSpan.builder()
        .traceId(traceId)
        .id(generateId())
        .kind("CLIENT")
        .name(name)
        .duration(this.getDurationMicros())
        .timestamp(this.getStartTimeMicros())
        .localEndpoint(ZipkinEndpoint.QUERY_CONTAINER_ENDPOINT)
        .remoteEndpoint(ZipkinEndpoint.PROTON_SERVER_ENDPOINT)
        .tags(Map.of(
            "documentType", this.documentType,
            "distributionKey", this.distributionKey,
            "className", this.getClass().getSimpleName(),
            "durationNs", this.durationNs,
            "startTime", this.startTime,
            "name", this.name
        ))
        .build();

    List<ZipkinSpan> spans = new ArrayList<>();
    spans.add(msgSpan);
    spans.addAll(this.children.stream()
        .flatMap(child -> child.createZipkinSpans(msgSpan).stream())
        .toList());
    return spans;
  }

}

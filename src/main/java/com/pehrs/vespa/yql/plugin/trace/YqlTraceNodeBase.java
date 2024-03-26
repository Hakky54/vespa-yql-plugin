package com.pehrs.vespa.yql.plugin.trace;


import com.fasterxml.jackson.annotation.JsonIgnore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class YqlTraceNodeBase {

  @JsonIgnore // Workaround for debug printing to work
  YqlTraceNodeBase parent;
  String name;
  @JsonIgnore // Workaround for debug printing to work
  List<YqlTraceNodeBase> children;
  Double relativeEndTsMs;

  @JsonIgnore
  public String documentType() {
    if (parent != null) {
      return parent.documentType();
    }
    return null;
  }

  @JsonIgnore
  public Integer distributionKey() {
    if (parent != null) {
      return parent.distributionKey();
    }
    return null;
  }

  @Override
  public String toString() {
    return "YqlTraceNodeBase{" +
        "parent=" + (parent == null ? "null" : parent.name) +
        ", name='" + name + '\'' +
        ", children=" + children +
        ", relativeEndTsMs=" + relativeEndTsMs +
        '}';
  }

  public YqlTraceMessage getParentMessage() {
    if (this.parent == null) {
      throw new RuntimeException("This should not happen!!!!");
    }
    if (this.parent instanceof YqlTraceMessage) {
      YqlTraceMessage msg = (YqlTraceMessage) this.parent;
      return msg;
    } else {
      return parent.getParentMessage();
    }
  }

  public Optional<YqlTraceNodeBase> getPreviousNodeOf(YqlTraceNodeBase node) {
    for (int i = 0; i < children.size(); i++) {
      if (children.get(i).equals(node)) {
        if (i > 0) {
          return Optional.of(children.get(i - 1));
        } else {
          return Optional.empty();
        }
      }
    }
    return Optional.empty();
  }

  static SecureRandom rnd = new SecureRandom();

  static String generateId() {
    return String.format("%08x", rnd.nextLong());
  }

  protected List<ZipkinSpan> createZipkinSpans(ZipkinSpan parentZip) {
    Optional<YqlTraceNodeBase> prev = this.parent.getPreviousNodeOf(this);
    YqlTraceMessage msg = getParentMessage();

    long duration = prev
        .map(p -> (long) ((this.relativeEndTsMs - p.relativeEndTsMs) * 1000d))
        .orElseGet(() -> {
          if (parent instanceof YqlTraceMessage) {
            return (long) (this.relativeEndTsMs * 1000d);
          } else if (this instanceof YqlTraceThread) {
            return parentZip.getDuration();
          } else {

            long start = parentZip.timestamp - msg.getStartTimeMicros();
            long end = (long) (this.relativeEndTsMs * 1000d);

            return end - start;
            // return (long) ((this.relativeEndTsMs * 1000d) - parentZip.getDuration());
          }
        });

    long timestamp = prev
        .map(p -> msg.getStartTimeMicros() + (long) (p.relativeEndTsMs * 1000d))
        // .orElseGet(() -> msg.getStartTimeMicros());
        .orElseGet(() -> parentZip.timestamp);

    ZipkinSpan span = ZipkinSpan.builder()
        .traceId(parentZip.traceId)
        .id(generateId())
        .kind("CLIENT")
        .parentId(parentZip.id)
        .name(name)
        .duration(duration)
        .timestamp(timestamp)
        .localEndpoint(ZipkinEndpoint.PROTON_SERVER_ENDPOINT)
        .remoteEndpoint(ZipkinEndpoint.PROTON_SERVER_ENDPOINT)
        .tags(Map.of(
            "documentType", this.documentType(),
            "distributionKey", this.distributionKey(),
            "className", this.getClass().getSimpleName(),
            "relativeEndTsMs", this.relativeEndTsMs,
            "name", this.name
        ))
        .build();

    List<ZipkinSpan> spans = new ArrayList<>();
    spans.add(span);
    spans.addAll(this.children.stream()
        .flatMap(child -> child.createZipkinSpans(span).stream())
        .toList());
    return spans;
  }
}

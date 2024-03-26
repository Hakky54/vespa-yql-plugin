package com.pehrs.vespa.yql.plugin.traceorg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
@Deprecated
record TraceThread(TraceMessage message, TraceHolder parentHolder, String documentType, Integer distributionKey, List<Trace> traces) implements TraceHolder{

  public List<TraceRow> toTraceRows() {
    return traces.stream().flatMap(
        trace -> trace.toTraceRows(documentType, distributionKey).stream()
    ).collect(Collectors.toList());
  }

  public Optional<Trace> getPrevTraceOf(Trace trace) {
    for (int i = 0; i < traces.size(); i++) {
      if (traces.get(i).equals(trace)) {
        if (i > 0) {
          return Optional.of(traces.get(i - 1));
        } else {
          return Optional.empty();
        }
      }
    }
    return Optional.empty();
  }


  //  public List<ZipkinSpan> toZipkinSpans(ZipkinSpan parent) {
//    return traces.stream().flatMap(
//        trace -> trace.toZipkinSpans(parent, documentType, distributionKey).stream()
//    ).collect(Collectors.toList());
//  }

  public List<ZipkinSpan> toZipkinSpans(TraceMessage traceMessage,
      ZipkinSpan parentZip,
      String documentType,
      Integer distributionKey) {

    long duration = parentZip.duration();
    long timestamp = parentZip.timestamp();

    ZipkinSpan thisZip = ZipkinSpan.create(parentZip.traceId(), "Thread")
        .withParentId(parentZip.id())
        .withTimestamp(timestamp)
        .withDuration(duration)
        .withTags(Map.of(
            "docType", documentType,
            "distKey", distributionKey
        ));

    List<ZipkinSpan> spans = new ArrayList<>();
    spans.add(thisZip);
    spans.addAll(traces.stream().flatMap(trace -> {
      return trace.toZipkinSpans(traceMessage, thisZip, documentType, distributionKey).stream();
    }).toList());

    return spans;
  }
}

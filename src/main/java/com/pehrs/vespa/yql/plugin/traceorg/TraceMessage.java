package com.pehrs.vespa.yql.plugin.traceorg;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
@Deprecated
public record TraceMessage(Long tsNs,
                           List<Trace> traces,
                           Integer distributionKey,
                           String documentType,
                           Double durationNs) implements TraceHolder{

  public TraceMessage withTraces(List<Trace> traces) {
    return new TraceMessage(this.tsNs, traces, this.distributionKey, this.documentType, this.durationNs);
  }

  public List<TraceRow> toTraceRows() {
    // "2024-03-21 20:56:36.604 UTC"
    List<TraceRow> rows = new ArrayList<>();
    String msg = String.format("%s: docType:%s, distKey: %d - duration: %.3fms",
        TraceUtils.sdf.format(new Date(tsNs / 1_000_000L)),
        documentType,
        distributionKey,
        durationNs / 1_000_000d);
    TraceRow first = new TraceRow(tsNs, 0.0d, documentType, distributionKey, msg);
    List<TraceRow> theTraces = this.traces.stream().flatMap(trace -> {
      // TraceRow first = new TraceRow(trace.timestampMs, trace.tag);
      return trace.toTraceRows(documentType, distributionKey).stream();
    }).collect(Collectors.toList());
    rows.add(first);
    rows.addAll(theTraces);
    return rows;
  }

  public List<ZipkinSpan> toZipkinSpans(ZipkinSpan root) {
    String name = String.format("%s: docType:%s, distKey: %d - duration: %.3fms",
        TraceUtils.sdf.format(new Date(tsNs / 1_000_000L)),
        documentType,
        distributionKey,
        durationNs / 1_000_000d);
    long tsMicros = tsNs / 1000L; // start_time
    ZipkinSpan msgZip = ZipkinSpan.create(root.traceId(), name)
        .withParentId(root.id())
        .withTimestamp(tsMicros)
        .withDuration((long)(durationNs / 1_000d)) // Microseconds
        .withTags(Map.of(
            "docType", documentType,
            "distKey", distributionKey
        ));

    List<ZipkinSpan> theSpans = new ArrayList<>();
    theSpans.add(msgZip);
    theSpans.addAll(this.traces.stream().flatMap(trace -> {
      // TraceRow first = new TraceRow(trace.timestampMs, trace.tag);
      return trace.toZipkinSpans(this, msgZip, documentType, distributionKey).stream();
    }).collect(Collectors.toList()));


    return theSpans;
  }

  public TraceThread getThread() {
    return new TraceThread(this, this, documentType, distributionKey, this.traces);
  }


  @Override
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

//  public List<ZipkinSpan> toZipkinSpans(ZipkinSpan root) {
//    long tsMicro = tsNs / 1000L; // start_time
//
//    List<ZipkinSpan> rows = new ArrayList<>();
//    String msg = String.format("%s: docType:%s, distKey: %d - duration: %.3fms",
//        TraceUtils.sdf.format(new Date(tsNs / 1_000_000L)),
//        documentType,
//        distributionKey,
//        durationNs / 1_000_000d);
//    // TraceRow first = new TraceRow(tsNs, 0.0d, documentType, distributionKey, msg);
//    ZipkinSpan first = ZipkinSpan.create(root.traceId(), msg)
//        .withParentId(root.id())
//        .withTimestamp(tsMicro)
//        .withDuration((long)(durationNs / 1_000d)) // Microseconds
//        .withTags(Map.of(
//            "docType", documentType,
//            "distKey", distributionKey
//        ));
//    List<ZipkinSpan> theSpans = new ArrayList<>();
//    theSpans.add(first);
//    theSpans.addAll(this.traces.stream().flatMap(trace -> {
//      // TraceRow first = new TraceRow(trace.timestampMs, trace.tag);
//      return trace.toZipkinSpans(first, documentType, distributionKey).stream();
//    }).collect(Collectors.toList()));
//
//    // Adjust duration and timestamp
////    List<ZipkinSpan> spansWithDuration = theSpans.stream()
////        .map(span -> span
////            .adjustDuration(theTraces)
////            .adjustTimestamp(theTraces)).collect(Collectors.toList()
////        );
//
//    // Make sure the top level duration is correct :-)
//    // rows.add(first.withDuration((long)(durationNs / 1_000d))); // Microseconds
//    // rows.addAll(spansWithDuration);
//
//    rows.add(first);
//    rows.addAll(theSpans);
//
//    return rows;
//
//  }
}

package com.pehrs.vespa.yql.plugin.traceorg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Deprecated
record Trace(TraceMessage message, TraceHolder parentHolder, Double relativeTsNs, String documentType, Integer distributionKey,
             String tag, List<TraceThread> threads) implements TraceHolder {

  public TraceRow toTraceRow() {
    return new TraceRow(message.tsNs(), relativeTsNs, documentType, distributionKey, tag);
  }

  public Trace withParentHolder(TraceHolder traceHolder) {
    return new Trace(message, traceHolder, relativeTsNs, documentType, distributionKey, tag, threads);
  }

  public List<TraceRow> toTraceRows(String documentType, Integer distributionKey) {
    List<TraceRow> rows = new ArrayList<>();
    TraceRow first = new TraceRow((long) (message.tsNs() + relativeTsNs), relativeTsNs, documentType,
        distributionKey, tag);
//    List<TraceRow> subTraces = this.traces.stream().flatMap(
//            trace -> trace.toTraceRows(documentType, distributionKey).stream())
//        .collect(Collectors.toList()
//        );
    List<TraceRow> threadTraces = this.threads.stream().flatMap(
        thread -> thread.toTraceRows().stream()
    ).collect(Collectors.toList());
    rows.add(first);
    // rows.addAll(subTraces);
    rows.addAll(threadTraces);
    return rows;
  }

  public List<ZipkinSpan> toZipkinSpans(TraceMessage traceMessage,
      ZipkinSpan parentZip,
      String documentType,
      Integer distributionKey) {

    // Optional<Trace> prev = traceMessage.getThread().getPrevTraceOf(this);
    Optional<Trace> prev = parentHolder.getPrevTraceOf(this);

    long duration = prev
        .map(p -> (long)((p.relativeTsNs - this.relativeTsNs) / 1000d))
        .orElseGet(() -> (long)(this.relativeTsNs / 1000d));

    long timestamp = prev
        .map(p ->  (long)((traceMessage.tsNs() + p.relativeTsNs) / 1000d))
        .orElseGet(() -> traceMessage.tsNs() / 1000l);

    ZipkinSpan thisZip = ZipkinSpan.create(parentZip.traceId(), this.tag)
        .withParentId(parentZip.id())
        .withTimestamp(timestamp)
        .withDuration(duration)
        .withTags(Map.of(
            "docType", documentType,
            "distKey", distributionKey
        ));

    List<ZipkinSpan> spans = new ArrayList<>();
    spans.add(thisZip);
    spans.addAll(this.threads.stream().flatMap(
        t -> t.toZipkinSpans(traceMessage, thisZip, documentType, distributionKey).stream()
    ).toList());

    return spans;
  }

  @Override
  public Optional<Trace> getPrevTraceOf(Trace trace) {
    return this.threads.stream()
        .map(t -> t.getPrevTraceOf(trace))
        .filter(opt -> opt.isPresent())
        .findFirst()
        .orElseGet(() -> Optional.empty());
  }
//
//  public List<ZipkinSpan> toZipkinSpans(ZipkinSpan parent,
//      String documentType,
//      Integer distributionKey) {
//
//    List<ZipkinSpan> rows = new ArrayList<>();
////    TraceRow first = new TraceRow((long) (tsNs + timestampNs), timestampNs, documentType,
////        distributionKey, tag);
//    long tsMicro = (long) (message.tsNs() + relativeTsNs) / 1000L;
//    ZipkinSpan first = ZipkinSpan.create(parent.traceId(), tag)
//        .withParentId(parent.id())
//        .withTimestamp(tsMicro)
//        .withTags(Map.of(
//            "docType", documentType,
//            "distKey", distributionKey
//        ));
////    List<ZipkinSpan> subTraces = this.traces.stream().flatMap(
////            trace -> trace.toZipkinSpans(first, documentType, distributionKey).stream())
////        .collect(Collectors.toList()
////        );
//    List<ZipkinSpan> threadTraces = this.threads.stream().flatMap(
//        thread -> thread.toZipkinSpans(first).stream()
//    ).collect(Collectors.toList());
//    rows.add(first);
//    // rows.addAll(subTraces);
//    rows.addAll(threadTraces);
//    return rows;
//  }
}

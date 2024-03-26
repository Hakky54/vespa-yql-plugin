package com.pehrs.vespa.yql.plugin.traceorg;

import org.jetbrains.annotations.NotNull;
@Deprecated
public record TraceRow(Long tsNs,
                       Double relativeTsNs,
                       String docType,
                       Integer distKey,
                       String message) implements Comparable<TraceRow> {

  @Override
  public int compareTo(@NotNull TraceRow other) {
    // return (int) (1_000_000d * (this.relativeTs - other.relativeTs));
    int diff = this.docType.compareTo(other.docType);
    if (diff == 0) {
      diff = this.distKey - other.distKey;
    }
    if (diff == 0) {
      diff = (int) (this.tsNs - other.tsNs);
    }
    return diff;
  }
}

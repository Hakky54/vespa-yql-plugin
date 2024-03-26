package com.pehrs.vespa.yql.plugin.traceorg;

import java.util.Optional;
@Deprecated
public interface TraceHolder {

  Optional<Trace> getPrevTraceOf(Trace trace);

}

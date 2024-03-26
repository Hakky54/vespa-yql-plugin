package com.pehrs.vespa.yql.plugin.trace;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class YqlTraceThread extends YqlTraceNodeBase {

  @Builder
  public YqlTraceThread(YqlTraceNodeBase parent, String name, List<YqlTraceNodeBase> children, Double relativeEndTsMs) {
    super(parent, name, children, relativeEndTsMs);
  }

  @Override
  public String toString() {
    return "YqlTraceThread{" +
        "parent=" + (parent == null?"null":parent.name) +
        ", name='" + name + '\'' +
        ", children=" + children +
        ", relativeEndTsMs=" + relativeEndTsMs +
        '}';
  }
}

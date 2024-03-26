package com.pehrs.vespa.yql.plugin.util;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.NotNull;

public class JsonUtils {

  @NotNull
  public static List<JsonNode> toList(JsonNode traces) {
    if(traces==null) {
      return List.of();
    }
    return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(traces.iterator(), Spliterator.ORDERED),
            false)
        .collect(Collectors.toList());
  }
}

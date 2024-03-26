package com.pehrs.vespa.yql.plugin.traceorg;

import com.fasterxml.jackson.databind.JsonNode;
import com.pehrs.vespa.yql.plugin.YqlResult;
import com.pehrs.vespa.yql.plugin.util.JsonUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class TraceUtils {

  public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS zzz",
      Locale.ENGLISH);


  public static List<TraceRow> getTraceRows(YqlResult result) {

    List<JsonNode> messageNodes = getTraceRoots(result);
    List<TraceMessage> messages = messageNodes.stream().flatMap(
        node -> getTraceMessages(node).stream()
    ).collect(Collectors.toList());

    // Collections.sort mutates the list so
    // let's make sure we have a mutable List to sort
    List<TraceRow> res = new ArrayList<>(messages.stream().flatMap(
        message -> message.toTraceRows().stream()
    ).collect(Collectors.toList()));
    Collections.sort(res);
    return res;
  }

  public static List<JsonNode> getTraceRoots(JsonNode trace) {
    JsonNode message = trace.get("message");
    if (message != null && message.isArray()) {
      // We have a match
      return List.of(trace);
    }
    JsonNode children = trace.get("children");
    if (children != null) {
      return JsonUtils.toList(children).stream()
          .flatMap(child -> getTraceRoots(child).stream())
          .collect(Collectors.toList());
    }
    return List.of();

  }

  static List<JsonNode> getTraceRoots(YqlResult result) {
    return result.getTrace()
        .map(TraceUtils::getTraceRoots)
        .orElse(List.of());
  }

  private static List<Trace> getTraces(TraceMessage msg, JsonNode node, String documentType,
      Integer distributionKey) {
    return JsonUtils.toList(node.get("traces"))
        .stream()
        .map(traceNode -> {
          String tag = "";
          JsonNode tagNode = traceNode.get("tag");
          if (tagNode != null) {
            tag = tagNode.asText();
          }
          JsonNode eventNode = traceNode.get("event");
          if (eventNode != null) {
            tag = eventNode.asText();
          }
          Double timstampNs = 1_000_000d * traceNode.get("timestamp_ms").asDouble();
          List<TraceThread> threads = getTraceThreads(msg, traceNode, documentType, distributionKey);
          // return new Trace(tag, timstampMs, traces, threads);
          return new Trace(msg, msg, timstampNs, documentType, distributionKey, tag, threads);
        }).collect(Collectors.toList());
  }

  private static List<TraceThread> getTraceThreads(TraceMessage msg, JsonNode traceNode,
      String documentType,
      Integer distributionKey) {
    @NotNull List<Trace> traces = getTraces(msg, traceNode, documentType, distributionKey);
    if(traces.size()> 0) {
      // Single thread trace
      return List.of(new TraceThread(msg, msg, documentType, distributionKey, traces));
    }
    @NotNull List<JsonNode> threadNodes = JsonUtils.toList(traceNode.get("threads"));
    return threadNodes.stream().map(thread -> {
      List<Trace> threadTraces = getTraces(msg, thread, documentType, distributionKey);
      return new TraceThread(msg, msg, documentType, distributionKey, threadTraces);
    }).collect(Collectors.toList());
  }

  static List<TraceMessage> getTraceMessages(JsonNode root) {
    @NotNull List<JsonNode> nodes = JsonUtils.toList(
        root.get("message"));

    return nodes.stream().map(node -> {
      System.out.println(node);
      String startTime = node.get("start_time").asText();
      Date date = null;
      try {
        date = sdf.parse(startTime);
      } catch (ParseException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
      long tsNs = 1_000_000L * date.getTime();

      Integer distributionKey = node.get("distribution-key").asInt();
      String documentType = node.get("document-type").asText();
      Double durationMs = node.get("duration_ms").asDouble();
      Double durationNs = 1_000_000d * durationMs;
      TraceMessage msg = new TraceMessage(tsNs, List.of(), distributionKey, documentType, durationNs);
      List<Trace> traces = getTraces(msg, node, documentType, distributionKey);
      return  msg.withTraces(traces);
    }).collect(Collectors.toList());
  }
}

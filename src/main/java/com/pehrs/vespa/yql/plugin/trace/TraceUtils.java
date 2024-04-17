package com.pehrs.vespa.yql.plugin.trace;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.jcef.JBCefApp;
import com.pehrs.vespa.yql.plugin.YqlResult;
import com.pehrs.vespa.yql.plugin.results.ZipkinBrowserPanel;
import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsState;
import com.pehrs.vespa.yql.plugin.util.BrowserUtils;
import com.pehrs.vespa.yql.plugin.util.JsonUtils;
import com.pehrs.vespa.yql.plugin.util.NotificationUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraceUtils {

  private static final Logger log = LoggerFactory.getLogger(TraceUtils.class);

  public static List<YqlTraceMessage> getYqlTraceMessages(YqlResult result) {
    return getTraceRoots(result)
        .stream().flatMap(root -> getYqlTraceMessages(root).stream())
        .collect(Collectors.toList());
  }

  public static List<JsonNode> getTraceRoots(YqlResult result) {
    return result.getTrace()
        .map(TraceUtils::getTraceRoots)
        .orElse(List.of());
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


  static List<YqlTraceMessage> getYqlTraceMessages(JsonNode root) {
    @NotNull List<JsonNode> nodes = JsonUtils.toList(
        root.get("message"));

    return nodes.stream().map(node -> {
      String startTime = node.get("start_time").asText();
      Integer distributionKey = node.get("distribution-key").asInt();
      String documentType = node.get("document-type").asText();
      Double durationMs = node.get("duration_ms").asDouble();
      Double durationNs = 1_000_000d * durationMs;
      String name = String.format("%s: docType:%s, distKey: %d - duration: %.3fms",
          startTime,
          documentType,
          distributionKey,
          durationNs / 1_000_000d);
      // YqlTraceMessage msg = new YqlTraceMessage(tsNs, List.of(), distributionKey, documentType, durationNs);
      YqlTraceMessage msg = YqlTraceMessage.builder()
          .name(name)
          .startTime(startTime)
          .distributionKey(distributionKey)
          .documentType(documentType)
          .durationNs(durationNs)
          .build();
      List<YqlTraceNodeBase> traces = getMessageTraces(msg, node);
      msg.setChildren(traces);
      return msg;
    }).collect(Collectors.toList());
  }

  private static List<YqlTraceNodeBase> getMessageTraces(YqlTraceMessage parent, JsonNode parentNode) {
    return JsonUtils.toList(parentNode.get("traces"))
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
          Double timestampMs = traceNode.get("timestamp_ms").asDouble();
          // List<TraceThread> threads = getTraceThreads(msg, traceNode, documentType, distributionKey);
          // return new Trace(msg, msg, timstampNs, documentType, distributionKey, tag, threads);
          YqlTraceNode trace = YqlTraceNode.builder()
              .name(tag)
              .parent(parent)
              .relativeEndTsMs(timestampMs)
              .build();
          trace.setChildren(getYqlTraceThreads(trace, traceNode));
          return trace;
        }).collect(Collectors.toList());
  }

  private static List<YqlTraceNodeBase> getYqlTraceThreads(YqlTraceNodeBase parent, JsonNode traceNode) {

    if(traceNode.has("traces")) {
      YqlTraceThread thread = YqlTraceThread.builder()
          .name("Thread")
          .parent(parent)
          .relativeEndTsMs(parent.relativeEndTsMs)
          .build();
      @NotNull List<YqlTraceNodeBase> traces = getTraces(thread, traceNode);
      if (traces.size() > 0) {
        // Single thread trace
        // return List.of(new YqlTraceThread(msg, msg, documentType, distributionKey, traces));
        thread.setChildren(traces);
        return List.of(thread);
      }
    }

    if(traceNode.has("threads")) {
      @NotNull List<JsonNode> threadNodes = JsonUtils.toList(traceNode.get("threads"));
      return threadNodes.stream().map(threadNode -> {
        YqlTraceThread thread = YqlTraceThread.builder()
            .name("Thread")
            .parent(parent)
            .relativeEndTsMs(parent.relativeEndTsMs)
            .build();
        List<YqlTraceNodeBase> threadTraces = getTraces(thread, threadNode);
        thread.setChildren(threadTraces);
        return thread;
      }).collect(Collectors.toList());
    }

    return List.of();
  }

  private static List<YqlTraceNodeBase> getTraces(YqlTraceNodeBase parent, JsonNode parentNode) {
    return JsonUtils.toList(parentNode.get("traces"))
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
          Double timstampMs = traceNode.get("timestamp_ms").asDouble();
          YqlTraceNodeBase trace = YqlTraceNode.builder()
              .parent(parent)
              .name(tag)
              .relativeEndTsMs(timstampMs)
              .build();
          List<YqlTraceNodeBase> threads = getYqlTraceThreads(trace, traceNode);
          trace.setChildren(threads);
          return trace;
        }).collect(Collectors.toList());
  }

  private static Long getTimestamp(List<ZipkinSpan> msgSpans) {
    return msgSpans.stream().mapToLong(span -> span.getTimestamp())
        .min().orElse(0L);
  }

  private static Long getDurationMicros(List<ZipkinSpan> msgSpans) {
    Long start = getTimestamp(msgSpans);

    Long maxVal = msgSpans.stream()
        .mapToLong(span ->  span.getTimestamp() + span.getDuration())
        .max().orElse(start);

    return maxVal - start;
  }

  public static List<ZipkinSpan> getZipkinSpans(YqlResult result) {
    String traceId = YqlTraceNodeBase.generateId();

    ZipkinSpan root = ZipkinSpan.builder()
        .traceId(traceId)
        .id(YqlTraceNodeBase.generateId())
        .kind("CLIENT")
        .name("/search/")
        .localEndpoint(ZipkinEndpoint.QUERY_CONTAINER_ENDPOINT)
        .remoteEndpoint(ZipkinEndpoint.QUERY_CONTAINER_ENDPOINT)
        .build();

    List<YqlTraceMessage> tracesMessages = TraceUtils.getYqlTraceMessages(result);

    List<ZipkinSpan> msgSpans = tracesMessages.stream()
        .flatMap(msg -> msg.createZipkinSpans(traceId).stream())
        .collect(Collectors.toList());
    Long tsMicro = getTimestamp(msgSpans);
    Long durationMicros = getDurationMicros(msgSpans);
    root.setDuration(durationMicros);
    root.setTimestamp(tsMicro);

    List<ZipkinSpan> spans = new ArrayList<>();
    spans.add(root);
    spans.addAll(msgSpans);
    return spans;
  }


  public static String uploadToZipkin() throws JsonProcessingException {
    YqlResult result = YqlResult.getYqlResult();

    List<ZipkinSpan> spans = getZipkinSpans(result);

    ObjectMapper mapper = new ObjectMapper();
    String zipkinJson = mapper
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(spans);

    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();

    HttpPost request = new HttpPost(settings.getState().zipkinEndpoint + "/api/v2/spans");
    request.addHeader("content-type", "application/json;charset=UTF-8");
    StringEntity entity = new StringEntity(zipkinJson, ContentType.APPLICATION_JSON);
    request.setEntity(entity);

    try (CloseableHttpClient httpClient = HttpClients.custom()
        .build()) {
      CloseableHttpResponse response = httpClient.execute(request);
      try {
        String responseString = EntityUtils.toString(response.getEntity());
        log.debug("HTTP Response Status: " + response.getStatusLine());
        log.trace("HTTP Response: " + responseString);
        return spans.get(0).getTraceId(); // Should be the same for all of them :-)
      } finally {
        response.close();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void openTrace(Project project, String traceId, ZipkinBrowserPanel zipkinPanel)
      throws URISyntaxException, IOException {

    URI uri = getZipkinUri(traceId);

    if (JBCefApp.isSupported()) {
      zipkinPanel.loadURL(traceId);
    } else {
      // Workaround is to run xdg-open ...
      try {
        BrowserUtils.openBrowser(uri);
      } catch (IOException  ex) {
        NotificationUtils.showNotification(project, NotificationType.ERROR,
            "Could not open browser: " + ex.getMessage());
      }
    }
  }

  @NotNull
  public static URI getZipkinUri(String traceID) throws URISyntaxException {
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    URI uri = new URI(String.format("%s/zipkin/traces/%s", settings.zipkinEndpoint, traceID));
    return uri;
  }
}

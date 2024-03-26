package com.pehrs.vespa.yql.plugin.traceorg;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.jcef.JBCefApp;
import com.pehrs.vespa.yql.plugin.YqlResult;
import com.pehrs.vespa.yql.plugin.results.ZipkinBrowserPanel;
import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsState;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
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

@Deprecated
public class ZipkinUtils {

  static Logger log = LoggerFactory.getLogger(ZipkinUtils.class);

  static SecureRandom rnd = new SecureRandom();

  public static String generateId() {
    return String.format("%08x", rnd.nextLong());
  }

  private static Long getTimestamp(List<ZipkinSpan> msgSpans) {
    return msgSpans.stream().mapToLong(span -> span.timestamp())
        .min().orElse(0L);
  }

  private static Long getDurationMicros(List<ZipkinSpan> msgSpans) {
    Long start = getTimestamp(msgSpans);

    Long maxVal = msgSpans.stream()
        .mapToLong(span -> span.timestamp() + span.duration())
        .max().orElse(start);

    return maxVal - start;
  }

  public static List<ZipkinSpan> getZipkinSpans(YqlResult result) {
    List<JsonNode> messageNodes = TraceUtils.getTraceRoots(result);
    List<TraceMessage> messages = messageNodes.stream().flatMap(
        node -> TraceUtils.getTraceMessages(node).stream()
    ).collect(Collectors.toList());

    String traceId = ZipkinUtils.generateId();
    ZipkinSpan root = ZipkinSpan.create(traceId, "/search/");
    List<ZipkinSpan> res = new ArrayList<>();

    List<ZipkinSpan> msgTraces = messages.stream().flatMap(
        message -> message.toZipkinSpans(root).stream()
    ).collect(Collectors.toList());

    Long tsMicro = getTimestamp(msgTraces);
    Long durationMicros = getDurationMicros(msgTraces);

    res.add(
        root.withTimestamp(tsMicro)
            .withDuration(durationMicros) // Microseconds
            .withLocalEndpoint(new ZipkinEndpoint(ZipkinSpan.VESPA_QUERY_SERVICE))
            .withRemoteEndpoint(new ZipkinEndpoint(ZipkinSpan.VESPA_PROTON_SERVICE))
    );
    res.addAll(msgTraces);
    // Collections.sort(res);
    return res;
  }


  public static String uploadToZipkin() throws JsonProcessingException {
    YqlResult result = YqlResult.getYqlResult();

    List<ZipkinSpan> spans = ZipkinUtils.getZipkinSpans(result);

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
        return spans.get(0).traceId(); // Should be the same for all of them :-)
      } finally {
        response.close();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void openTrace(Project project, String traceId, ZipkinBrowserPanel zipkinPanel)
      throws URISyntaxException, IOException {

    URI uri = getUri(traceId);

    if (JBCefApp.isSupported()) {
      zipkinPanel.loadURL(traceId);
    } else {
//      if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
//        Desktop.getDesktop().browse(uri);
//      }
      // Workaround is to run xdg-open ...
      try {
        YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
        Process p = Runtime.getRuntime().exec(
            String.format("%s %s", settings.browserScript, uri.toString()));
      } catch (IOException ex) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Vespa YQL")
            .createNotification("Could not open browser: " + ex.getMessage(),
                NotificationType.ERROR)
            .notify(project);
      }
    }
  }

  @NotNull
  public static URI getUri(String traceID) throws URISyntaxException {
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    URI uri = new URI(String.format("%s/zipkin/traces/%s", settings.zipkinEndpoint, traceID));
    return uri;
  }
}

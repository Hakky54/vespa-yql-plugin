package com.pehrs.vespa.yql.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.intellij.openapi.progress.ProgressIndicator;
import com.pehrs.vespa.yql.plugin.YqlResult.YqlResultListener;
import com.pehrs.vespa.yql.plugin.settings.VespaClusterConfig;
import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsState;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import org.apache.http.client.methods.HttpGet;
import org.eclipse.jetty.client.ProtocolHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VespaClusterChecker {

  private static final Logger log = LoggerFactory.getLogger(VespaClusterChecker.class);


  public interface StatusListener {
    void vespaClusterStatusUpdated();
  }

  public enum Status {
    UP, DOWN, INITIALIZING, FAIL,
  }

//  public final static String STATUS_UP = "up";
//  public final static String STATUS_DOWN = "down";
//  public final static String STATUS_INITIALIZING = "initializing";
//  public final static String STATUS_FAIL = "fail";

  static List<StatusListener> listeners = new ArrayList();

  @Getter
  static Map<VespaClusterConfig, Status> queryEndpointStatus = new HashMap<>();
  @Getter
  static Map<VespaClusterConfig, Status> configEndpointStatus = new HashMap<>();
  @Getter
  static Map<VespaClusterConfig, String> appName = new HashMap<>();

  @Getter
  static Map<VespaClusterConfig, JsonNode> appStatus = new HashMap<>();

  static Status zipkinStatus = Status.FAIL;

  public static Optional<String> getAppGeneration(VespaClusterConfig config) {
    return Optional.ofNullable(appStatus.get(config))
        .flatMap(nodeRoot -> {
          if(nodeRoot.has("application")) {
            JsonNode application = nodeRoot.get("application");
            if(application.has("meta")) {
              JsonNode meta = application.get("meta");
              if(meta.has("generation")) {
                return Optional.of(meta.get("generation").asText());
              }
            }
          }
          return Optional.empty();
        });
  }

  public static void checkVespaClusters() {
    checkVespaClusters(null);
  }

  public static void checkVespaClusters(ProgressIndicator indicator) {
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    if (settings != null) {

      List<VespaClusterConfig> clusterConfigs = settings.clusterConfigs;
      // FIXME: Create thread pool and run all the checks in parallel
      for (int i = 0; i < clusterConfigs.size(); i++) {
        VespaClusterConfig clusterConfig = clusterConfigs.get(i);
        if (indicator != null) {
          indicator.setFraction(i / clusterConfigs.size());
          indicator.setText("Checking Vespa cluster " + clusterConfig.name);
          indicator.setText("Vespa: " + clusterConfig.queryEndpoint);
        }
        URI configHostUri = clusterConfig.getConfigUri().resolve("/");
        Status configCode = getHealthStatusCode(configHostUri.toString(), clusterConfig);
        log.trace("[" + clusterConfig.name + "]" + clusterConfig.configEndpoint + ": " + configCode);
        URI queryHostUri = clusterConfig.getQueryUri().resolve("/");
        Status queryCode = getHealthStatusCode(queryHostUri.toString(), clusterConfig);
        log.trace("[" + clusterConfig.name + "]" + clusterConfig.queryEndpoint + ": " + queryCode);

        getApplicationClusterName(configHostUri.toString(),
            clusterConfig).ifPresentOrElse((name) -> appName.put(clusterConfig, name),
            () -> appName.remove(clusterConfig));

        getApplicationStatus(queryHostUri.toString(), clusterConfig)
            .ifPresentOrElse(as -> appStatus.put(clusterConfig, as),
                () -> appStatus.remove(clusterConfig));

        zipkinStatus = getZipkinStatus();

        queryEndpointStatus.put(clusterConfig, queryCode);
        configEndpointStatus.put(clusterConfig, configCode);
      }
      notifyStatusListeners();
    }
  }

  private static Status getHealthStatusCode(String endpoint, VespaClusterConfig clusterConfig) {
    String url = String.format("%s/state/v1/health", endpoint);
    try {
      JsonNode healthResponse =
          VespaClusterConnection.jsonGet(clusterConfig, url);
      if(healthResponse.has("status")) {
        if(healthResponse.get("status").has("code")) {
          String code = healthResponse.get("status").get("code").asText();
          try {
            return Status.valueOf(code.toUpperCase());
          } catch (Exception ex) {
            return Status.FAIL;
          }
        }
      }
      return Status.DOWN;
    } catch (Exception ex) {
      return Status.FAIL;
    }
  }

  private static Optional<String> getApplicationClusterName(String endpoint, VespaClusterConfig clusterConfig) {
    String url = String.format("%s/config/v2/tenant/default/application/default/cloud.config.cluster-list", endpoint);
    try {
      JsonNode clusterListRes =
          VespaClusterConnection.jsonGet(clusterConfig, url);
      if(clusterListRes.has("storage")) {
        JsonNode storage = clusterListRes.get("storage");
        if(storage.isArray() && storage.size() > 0) {
          JsonNode first = storage.get(0);
          if(first.has("name")) {
            return Optional.of(first.get("name").asText());
          }
        }
      }
      return Optional.empty();
    } catch (Exception ex) {
      return Optional.empty();
    }
  }


  private static Status getZipkinStatus() {
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();

    String url = String.format("%s/api/v2/traces", settings.getZipkinEndpoint());
    try {
      JsonNode clusterListRes = VespaClusterConnection.jsonGet(url);
      return Status.UP;
    } catch (Exception ex) {
      return Status.FAIL;
    }
  }


  private static Optional<JsonNode> getApplicationStatus(String endpoint, VespaClusterConfig clusterConfig) {
    // http://localhost:8080/ApplicationStatus
    String url = String.format("%s/ApplicationStatus", endpoint);
    try {
      JsonNode appStatus =
          VespaClusterConnection.jsonGet(clusterConfig, url);
      return Optional.of(appStatus);
    } catch (Exception ex) {
      return Optional.empty();
    }
  }


  public static void addStatusListener(StatusListener listener) {
    if(!listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  public void removeStatusListener(StatusListener listener) {
    if(YqlResult.listeners.contains(listener)) {
      YqlResult.listeners.remove(listener);
    }
  }
  private static void notifyStatusListeners() {
    synchronized (listeners) {
      List<StatusListener> toBeRemoved = listeners.stream().flatMap(listener -> {
        try {
          listener.vespaClusterStatusUpdated();
        } catch (Exception ex) {
          ex.printStackTrace();
          return Stream.of(listener);
        }
        return Stream.empty();
      }).collect(Collectors.toList());
      listeners.removeAll(toBeRemoved);
    }
  }

}

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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VespaClusterChecker {

  private static final Logger log = LoggerFactory.getLogger(VespaClusterChecker.class);

  public static interface StatusListener {
    void vespaClusterStatusUpdated();
  }

  static List<StatusListener> listeners = new ArrayList();

  @Getter
  static Map<VespaClusterConfig, String> queryEndpointStatus = new HashMap<>();
  @Getter
  static Map<VespaClusterConfig, String> configEndpointStatus = new HashMap<>();

  public static void checkVespaClusters() {
    checkVespaClusters(null);
  }

  public static void checkVespaClusters(ProgressIndicator indicator) {
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    if (settings != null) {

      List<VespaClusterConfig> clusterConfigs = settings.clusterConfigs;
      for (int i = 0; i < clusterConfigs.size(); i++) {
        VespaClusterConfig clusterConfig = clusterConfigs.get(i);
        if (indicator != null) {
          indicator.setFraction(i / clusterConfigs.size());
          indicator.setText("Checking Vespa cluster " + clusterConfig.name);
          indicator.setText("Vespa: " + clusterConfig.queryEndpoint);
        }
        URI configHostUri = clusterConfig.getConfigUri().resolve("/");
        String configCode = getHealthStatusCode(configHostUri.toString(), clusterConfig);
        log.trace("[" + clusterConfig.name + "]" + clusterConfig.configEndpoint + ": " + configCode);
        URI queryHostUri = clusterConfig.getQueryUri().resolve("/");
        String queryCode = getHealthStatusCode(queryHostUri.toString(), clusterConfig);
        log.trace("[" + clusterConfig.name + "]" + clusterConfig.queryEndpoint + ": " + queryCode);

        queryEndpointStatus.put(clusterConfig, queryCode);
        configEndpointStatus.put(clusterConfig, configCode);
      }
      notifyStatusListeners();
    }
  }

  public final static String STATUS_UP = "up";
  public final static String STATUS_DOWN = "down";
  public final static String STATUS_INITIALIZING = "initializing";
  public final static String STATUS_FAIL = "fail";

  private static String getHealthStatusCode(String endpoint, VespaClusterConfig clusterConfig) {
    String url = String.format("%s/state/v1/health", endpoint);
    try {
      JsonNode healthResponse =
          VespaClusterConnection.jsonGet(clusterConfig, url);
      if(healthResponse.has("status")) {
        if(healthResponse.get("status").has("code")) {
          String code = healthResponse.get("status").get("code").asText();
          return code;
        }
      }
      return STATUS_DOWN;
    } catch (Exception ex) {
      return STATUS_FAIL;
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

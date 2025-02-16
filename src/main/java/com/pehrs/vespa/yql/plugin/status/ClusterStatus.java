package com.pehrs.vespa.yql.plugin.status;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.pehrs.vespa.yql.plugin.VespaClusterChecker;
import com.pehrs.vespa.yql.plugin.settings.VespaClusterConfig;
import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsState;
import com.pehrs.vespa.yql.plugin.util.BrowserUtils;
import com.pehrs.vespa.yql.plugin.util.NotificationUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

public class ClusterStatus {


  public static String getAppName(Project project) {
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    Optional<VespaClusterConfig> configOpt = settings.getCurrentClusterConfig();
    VespaClusterConfig config = configOpt.orElseThrow(() ->
        new RuntimeException("Could not get current connection configuration!")
    );
    Map<VespaClusterConfig, String> appNameMap = VespaClusterChecker.getAppName();
    return appNameMap.getOrDefault(config, "-");
  }

  public static void openInBrowser(Project project) {
    openInBrowser(project, getAppName(project));
  }

  public static void openInBrowser(Project project, String appName) {
    try {
      URI uri = new URI(getClusterStatusUrl(appName));
      BrowserUtils.openBrowser(uri);
    } catch (IOException | URISyntaxException ex) {
      String msg = "Could not open browser: " + ex.getMessage();
      NotificationUtils.showNotification(project, NotificationType.ERROR, msg);
    }
  }
  public static String getClusterStatusUrl(Project project) {
    return getClusterStatusUrl(getAppName(project));
  }

  public static String getClusterStatusUrl(String appName) {
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    Optional<VespaClusterConfig> configOpt = settings.getCurrentClusterConfig();
    VespaClusterConfig config = configOpt.orElseThrow(() ->
        new RuntimeException("Could not get current connection configuration!")
    );
    URI controllerUri = config.getControllerUri();
    // https://localhost:19050/clustercontroller-status/v1/llm
    String statusUrl = controllerUri.toString();
    return String.format("%s/clustercontroller-status/v1/%s", statusUrl, appName);
  }

}

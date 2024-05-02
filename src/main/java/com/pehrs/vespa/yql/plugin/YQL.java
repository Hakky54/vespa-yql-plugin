package com.pehrs.vespa.yql.plugin;

import static nl.altindag.ssl.util.internal.ValidationUtils.requireNotNull;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.RegisterToolWindowTaskBuilder;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static facade for global functions and constants
 */
public class YQL implements StartupActivity {

  private static final Logger log = LoggerFactory.getLogger(YQL.class);

  public final static String TOOL_WINDOW_ID = "Vespa Results";

  public YQL() {
  }

  public static List<String> YQL_KEYWORDS = List.of(
      // Basic keywords
      "select",
      "from",
      "where",
      "order by",
      "limit",
      "offset",
      "timeout",
      // where keywords
      "nearestNeighbor",
      "weightedSet",
      "predicate",
      "dotProduct",
      "userQuery",
      "nonEmpty",
      "userInput",
      "geoLocation",
      "sameElement",
      "matches",
      "range",
      "contains",
      "weakAnd",
      "phrase",
      "fuzzy",
      "equiv",
      "onear",
      "wand",
      "true",
      "false",
      "rank",
      "near",
      "and",
      "not",
      "uri",
      "or"
  );


  private static FileSystem jarFs = null;

  public static String getResource(String resourceName) {
    try {

      URL url;
      url = YQL.class.getResource(resourceName);
      // Workaround for java.nio.file.FileSystemNotFoundException issue in Intellij plugins
      final Map<String, String> env = new HashMap<>();
      final String[] array = url.toURI().toString().split("!");
      if (array.length > 1) {
        if (jarFs == null) {
          jarFs = FileSystems.newFileSystem(URI.create(array[0]), env);
        }
        final Path path = jarFs.getPath(array[1]);
        return Files.readString(path, Charset.forName("utf-8"));
      } else {
        return Files.readString(Paths.get(url.toURI()), Charset.forName("utf-8"));
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private static Properties getBuildInfoProperties() throws IOException {
    String propsStr = YQL.getResource("/build-info.properties");
    Properties properties = new Properties();
    try (StringReader reader = new StringReader(propsStr)) {
      properties.load(reader);
      return properties;
    }
  }

  public static String getBuildTimestamp() {
    try {
      Properties properties = getBuildInfoProperties();
      String ts = properties.getProperty("build-timestamp");
      if (ts == null) {
        return "";
      }
      return "Build: " + ts;
    } catch (Exception ex) {
      return "";
    }
  }

  public static String getBuiltByUser() {
    try {
      Properties properties = getBuildInfoProperties();
      String user = properties.getProperty("built-by");
      if (user == null) {
        return "";
      }
      return "BuiltBy: " + user;
    } catch (Exception ex) {
      return "";
    }
  }


  public static String getDefaultBrowserScript() {
    String OS = System.getProperty("os.name", "linux").toLowerCase(Locale.ENGLISH);
    if ((OS.contains("mac")) || (OS.contains("darwin"))) {
      return "open";
    } else if (OS.contains("win")) {
      return "start";
    } else {
      return "/usr/bin/xdg-open";
    }
  }

  @NotNull
  public static ToolWindow getVespaToolWindow(Project project) {
    ToolWindowManager mgr = ToolWindowManager.getInstance(project);
    return mgr.getToolWindow(TOOL_WINDOW_ID);
  }

  @Override
  public void runActivity(@NotNull Project project) {
    // Run once at startup :-)
    log.debug("vespa-yql-plugin STARTUP!");

    ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
    ToolWindow toolWindow = toolWindowManager.getToolWindow(TOOL_WINDOW_ID);
    if(toolWindow != null) {
      toolWindow.setAutoHide(true);
      toolWindow.hide();
      // toolWindow.remove();
    }

    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      for (; ; ) { // ever
        try {
          log.debug("VESPA-YQL-PLUGIN BACKGROUND CHECK...");

          boolean showProgress = false;
          if (showProgress) {
            ProgressManager.getInstance().run(new Task.Backgroundable(project,
                "Vespa Cluster Checker", false) {
              @Override
              public void run(@NotNull ProgressIndicator indicator) {
                VespaClusterChecker.checkVespaClusters(indicator);
              }
            });
          } else {
            VespaClusterChecker.checkVespaClusters();
          }

          // Check every minute
          Thread.sleep(60_000L);

        } catch (InterruptedException e) {
          log.error("Interrupted!!!");
          throw new RuntimeException(e);
        }
      }
    });

  }

}

package com.pehrs.vespa.yql.plugin.status;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.pehrs.vespa.yql.plugin.YQL;
import com.pehrs.vespa.yql.plugin.xml.VespaServicesXml;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterStatusDockFactory implements ToolWindowFactory, DumbAware {

  private static final Logger log = LoggerFactory.getLogger(ClusterStatusDockFactory.class);

  public static final String TOOL_WINDOW_ID = "Vespa - Cluster Status";

  private static ClusterStatusBrowserPanel servicesPanel;

  private static ClusterStatusDockFactory factory = new ClusterStatusDockFactory();

  @Override
  public void init(@NotNull ToolWindow toolWindow) {
    // Hide on start...
  }

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
//    this.project = project;

    servicesPanel = new ClusterStatusBrowserPanel(project);
    ContentManager contentManager = toolWindow.getContentManager();
    Content content = contentManager.getFactory().createContent(servicesPanel, "Cluster Status", false);
    contentManager.addContent(content);
    contentManager.setSelectedContent(content);
  }

  public static void openClusterStatus(Project project) {

    ToolWindow toolWindow = YQL.getVespaToolWindow(project);
    if (toolWindow != null) {
      final ToolWindow win = toolWindow;
      @NotNull ContentManager contentManager = win.getContentManager();
      getClusterStatusContent(contentManager.getContents())
          .ifPresentOrElse(contentManager::setSelectedContent,
              () -> {
                factory.createToolWindowContent(project, win);
              });
      win.activate(() -> {
        win.show(() -> {
          log.debug("show cluster status window");
        });
      });
    }
  }


  private static Optional<Content> getClusterStatusContent(Content[] contents) {
    for (Content content : contents) {
      if (content.getComponent() == servicesPanel) {
        return Optional.of(content);
      }
    }
    return Optional.empty();
  }
}

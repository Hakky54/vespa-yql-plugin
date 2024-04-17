package com.pehrs.vespa.yql.plugin.logserver;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.pehrs.vespa.yql.plugin.YQL;
import com.pehrs.vespa.yql.plugin.actions.OpenLogsAction;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VespaLogsViewFactory implements ToolWindowFactory {

  private static final Logger log = LoggerFactory.getLogger(OpenLogsAction.class);

  // private static VespaLogContent logWatcher;

  private static VespaLogsViewFactory factory = new VespaLogsViewFactory();

  @Override
  public void init(@NotNull ToolWindow toolWindow) {

  }

  public boolean isApplicable(@NotNull Project project) {
    // FIXME: Check if project is a Java or Vespa project!!!
    return project != null;
  }


  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    // Create an instance of your custom content component
    ContentManager contentManager = toolWindow.getContentManager();

    VespaLogsView view = new VespaLogsView(project);

    Content content = contentManager.getFactory()
        .createContent(view, "Cluster Logs", false);
    contentManager.addContent(content);
    contentManager.setSelectedContent(content);
  }

  public static void openLogs(Project project) {

    // FIXME: Check to see if we Should open the log view here!

    ToolWindow toolWindow = YQL.getVespaToolWindow(project, factory);
    if (toolWindow != null) {
      final ToolWindow win = toolWindow;
      @NotNull ContentManager contentManager = win.getContentManager();

      // factory.createToolWindowContent(project, win);
      getVespaLogsViewContent(contentManager.getContents())
          .ifPresentOrElse(contentManager::setSelectedContent,
              () -> {
                factory.createToolWindowContent(project, win);
              });
      win.activate(() -> {
        win.show(() -> {
          log.debug("show logs window");
        });
      });
    }
  }

  private static Optional<Content> getVespaLogsViewContent(Content[] contents) {
    for (Content content : contents) {
      if (content.getComponent() instanceof VespaLogsView) {
        return Optional.of(content);
      }
    }
    return Optional.empty();
  }
}


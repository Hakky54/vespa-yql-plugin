package com.pehrs.vespa.yql.plugin.results;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.pehrs.vespa.yql.plugin.YQL;
import com.pehrs.vespa.yql.plugin.logserver.VespaLogsView;
import java.util.Optional;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YqlResultsFactory implements ToolWindowFactory { // , DumbAware, YqlResultListener {

  private static final Logger log = LoggerFactory.getLogger(YqlResultsFactory.class);

  private static YqlResultsFactory factory = new YqlResultsFactory();

  private YqlResultsTabView tabs;

  @Override
  public boolean isApplicable(@NotNull Project project) {
    return project != null;
  }

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

    this.tabs = new YqlResultsTabView(project);

    ContentManager contentManager = toolWindow.getContentManager();
    Content content = contentManager.getFactory()
        .createContent(tabs, "Query Results", false);
    contentManager.addContent(content);
    contentManager.setSelectedContent(content);
  }

  public static void showResults(Project project) {
    ToolWindow toolWindow = YQL.getVespaToolWindow(project, factory);
    if (toolWindow != null) {
      final ToolWindow win = toolWindow;
      @NotNull ContentManager contentManager = win.getContentManager();

      // factory.createToolWindowContent(project, win);
      getYqlRsultsTabViewContent(contentManager.getContents())
          .ifPresentOrElse(contentManager::setSelectedContent,
              () -> {
                factory.createToolWindowContent(project, win);
              });
      win.activate(() -> {
        win.show(() -> {
          log.debug("show results window");
        });
      });
    }
  }


  private static Optional<Content> getYqlRsultsTabViewContent(Content[] contents) {
    for (Content content : contents) {
      if (content.getComponent() instanceof YqlResultsTabView) {
        return Optional.of(content);
      }
    }
    return Optional.empty();
  }
}

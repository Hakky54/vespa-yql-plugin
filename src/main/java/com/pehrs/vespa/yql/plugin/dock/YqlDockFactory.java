package com.pehrs.vespa.yql.plugin.dock;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsComponent;
import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsState;
import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsStateListener;
import org.jetbrains.annotations.NotNull;

public class YqlDockFactory implements ToolWindowFactory, DumbAware { // }, YqlAppSettingsStateListener {

  Project project;
  private YqlDockPanel settingsComponent;


  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

    this.project = project;

    this.settingsComponent = new YqlDockPanel(project);
    ContentManager contentManager = toolWindow.getContentManager();
    Content content = contentManager.getFactory().createContent(settingsComponent, null, false);
    contentManager.addContent(content);
  }

}

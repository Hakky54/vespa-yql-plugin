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
import com.pehrs.vespa.yql.plugin.settings.YqlSettingsDialog;
import org.jetbrains.annotations.NotNull;

public class YqlLDockFactory implements ToolWindowFactory, DumbAware, YqlAppSettingsStateListener {

  Project project;
  private YqlAppSettingsComponent settingsComponent;

  @Override
  public boolean isApplicable(@NotNull Project project) {
    return project != null;
  }

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

    this.project = project;

    YqlAppSettingsStateListener.addListener(this);

    this.settingsComponent = new YqlAppSettingsComponent(project, false);

    ContentManager contentManager = toolWindow.getContentManager();
    Content content = contentManager.getFactory().createContent(settingsComponent.getPanel(), null, false);
    contentManager.addContent(content);
  }


  @Override
  public void stateChanged(YqlAppSettingsState instance) {
    this.settingsComponent.refresh();
  }
}

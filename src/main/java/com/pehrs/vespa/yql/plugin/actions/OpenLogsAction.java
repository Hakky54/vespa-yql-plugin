package com.pehrs.vespa.yql.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.pehrs.vespa.yql.plugin.logserver.VespaLogsViewFactory;
import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsState;
import org.jetbrains.annotations.NotNull;

public class OpenLogsAction extends AnAction {

  @Override
  public void update(AnActionEvent actionEvent) {
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    actionEvent.getPresentation().setEnabledAndVisible(settings.doMonitorLogs);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    VespaLogsViewFactory.openLogs(event.getProject());
  }
}

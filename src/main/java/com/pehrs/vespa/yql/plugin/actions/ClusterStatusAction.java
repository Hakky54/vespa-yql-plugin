package com.pehrs.vespa.yql.plugin.actions;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vfs.LargeFileWriteRequestor;
import com.pehrs.vespa.yql.plugin.status.ClusterStatusDockFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterStatusAction extends AnAction implements LargeFileWriteRequestor {

  private static final Logger log = LoggerFactory.getLogger(ClusterStatusAction.class);

  @Override
  public void update(AnActionEvent actionEvent) {
    actionEvent.getPresentation().setEnabledAndVisible(true);
  }

  // override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
  @Override
  public ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  private final static XmlMapper xmlMapper = new XmlMapper();

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    ClusterStatusDockFactory.openClusterStatus(event.getProject());
  }

}

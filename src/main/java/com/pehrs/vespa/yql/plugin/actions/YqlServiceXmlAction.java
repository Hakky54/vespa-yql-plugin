package com.pehrs.vespa.yql.plugin.actions;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.LargeFileWriteRequestor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.pehrs.vespa.yql.plugin.graph.VespaServicesDockFactory;
import com.pehrs.vespa.yql.plugin.xml.VespaServicesXml;
import java.io.File;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YqlServiceXmlAction extends AnAction implements LargeFileWriteRequestor {

  private static final Logger log = LoggerFactory.getLogger(YqlServiceXmlAction.class);

  @Override
  public void update(AnActionEvent actionEvent) {
    @Nullable VirtualFile virtualFile = actionEvent.getData(
        CommonDataKeys.VIRTUAL_FILE);
    actionEvent.getPresentation().setEnabledAndVisible(
        virtualFile != null &&
            virtualFile.getName().equals("services.xml")
    );
  }

  // override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
  @Override
  public ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  private final static XmlMapper xmlMapper = new XmlMapper();

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    @Nullable VirtualFile virtualFile = event.getData(
        CommonDataKeys.VIRTUAL_FILE);
    log.info("Services-XML: " + virtualFile);
    if (virtualFile == null) {
      return;
    }

    @Nullable String servicesXmlPath = virtualFile.getCanonicalPath();

    File servicesXmlFile = new File(servicesXmlPath);
    try {
      ToolWindow window = ToolWindowManager.getInstance(
          event.getProject()).getToolWindow("services.xml - Services/Node Overview");
      if (window != null) {
        window.activate(() -> {
          window.show(() -> {
            try {
              VespaServicesXml services = xmlMapper.readValue(servicesXmlFile,
                  VespaServicesXml.class);
              VespaServicesDockFactory.setServicesXml(services);
            } catch (IOException ex) {
              log.error("Could not read/parse " + virtualFile.getCanonicalPath(), ex);
              String errMsg = ex.getMessage();
              NotificationGroupManager.getInstance()
                  .getNotificationGroup("Vespa YQL")
                  .createNotification(errMsg, NotificationType.ERROR)
                  .notify(event.getProject());
              throw new RuntimeException(ex);
            }
          });
        });
      }
    } catch (Exception ex) {
      log.error("Could not read/parse " + virtualFile.getCanonicalPath(), ex);
      String errMsg = ex.getMessage();
      NotificationGroupManager.getInstance()
          .getNotificationGroup("Vespa YQL")
          .createNotification(errMsg, NotificationType.ERROR)
          .notify(event.getProject());
    }
  }

}

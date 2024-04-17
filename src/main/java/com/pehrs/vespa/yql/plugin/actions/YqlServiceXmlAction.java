package com.pehrs.vespa.yql.plugin.actions;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.LargeFileWriteRequestor;
import com.intellij.openapi.vfs.VirtualFile;
import com.pehrs.vespa.yql.plugin.serviceview.VespaServicesDockFactory;
import com.pehrs.vespa.yql.plugin.util.IdeProjectUtils;
import com.pehrs.vespa.yql.plugin.util.NotificationUtils;
import com.pehrs.vespa.yql.plugin.xml.VespaServicesXml;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YqlServiceXmlAction extends AnAction implements LargeFileWriteRequestor {

  private static final Logger log = LoggerFactory.getLogger(YqlServiceXmlAction.class);

  @Override
  public void update(AnActionEvent actionEvent) {
    boolean enabled = false;
    if(actionEvent.getProject() != null) {
      enabled=IdeProjectUtils.isVespaJavaAppProject(actionEvent.getProject());
    }
    @Nullable VirtualFile virtualFile = actionEvent.getData(
        CommonDataKeys.VIRTUAL_FILE);
    if (virtualFile != null) {
      if(virtualFile.getName().equals("services.xml")) {
        enabled = true;
      }
    }
    actionEvent.getPresentation().setEnabledAndVisible(enabled);
  }

  // override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
  @Override
  public ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  private final static XmlMapper xmlMapper = new XmlMapper();

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    @Nullable String servicesXmlPath = null;
    @Nullable VirtualFile virtualFile = event.getData(
        CommonDataKeys.VIRTUAL_FILE);
    if(virtualFile != null && virtualFile.getName().equals("services.xml")) {
      servicesXmlPath = virtualFile.getCanonicalPath();
    } else {
      // Try to get the services.xml from the project instead
      Optional<VirtualFile> serviceXmlVf = IdeProjectUtils.getServicesXmlFile(
          IdeProjectUtils.getProjectRootDir(event.getProject()));
      if(serviceXmlVf.isEmpty()) {
        String msg = "Could not find a Vespa service.xml in project";
        log.warn(msg);
        NotificationUtils.showNotification(event.getProject(), NotificationType.WARNING, msg);
        return;
      }
      servicesXmlPath = serviceXmlVf.get().getCanonicalPath();
    }
    File servicesXmlFile = new File(servicesXmlPath);
    try {
      try {
        VespaServicesXml services = xmlMapper.readValue(servicesXmlFile,
            VespaServicesXml.class);
        VespaServicesDockFactory.setServicesXml(services);
      } catch (IOException ex) {
        log.error("Could not read/parse " + virtualFile.getCanonicalPath(), ex);
        String errMsg = ex.getMessage();
        NotificationUtils.showNotification(event.getProject(), NotificationType.ERROR,
            errMsg);
        throw new RuntimeException(ex);
      }
      VespaServicesDockFactory.openServiceXml(event.getProject());
    } catch (Exception ex) {
      log.error("Could not read/parse " + virtualFile.getCanonicalPath(), ex);
      String errMsg = ex.getMessage();
      NotificationUtils.showNotification(event.getProject(), NotificationType.ERROR, errMsg);
    }
  }

}

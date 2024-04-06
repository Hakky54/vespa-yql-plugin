package com.pehrs.vespa.yql.plugin.actions;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.vfs.VirtualFile;
import com.pehrs.vespa.yql.plugin.deploy.PrepareAndDeployResponse;
import com.pehrs.vespa.yql.plugin.deploy.VespaAppUploader;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrepareAndActivateAction extends AnAction {

  private static final Logger log = LoggerFactory.getLogger(PrepareAndActivateAction.class);

  public void update(AnActionEvent actionEvent) {
//    @Nullable VirtualFile virtualFile = actionEvent.getData(
//        CommonDataKeys.VIRTUAL_FILE);

    @Nullable Object[] selectedObjects = ProjectView.getInstance(actionEvent.getProject())
        .getCurrentProjectViewPane().getSelectedUserObjects();

    boolean enabled = false;
    if (selectedObjects != null && selectedObjects.length > 0) {
      @Nullable Object first = selectedObjects[0];
      if (first instanceof PsiDirectoryNode dirNode) {
        VirtualFile[] children = dirNode.getVirtualFile().getChildren();
        enabled = hasSchemasDir(children) && hasServicesXml(children);
      }
      actionEvent.getPresentation().setEnabledAndVisible(enabled);
    }
  }

  private boolean hasServicesXml(VirtualFile[] children) {
    return Arrays.stream(children)
        .anyMatch(vf -> vf.getName().equals("services.xml") && !vf.isDirectory());
  }

  private boolean hasSchemasDir(VirtualFile[] children) {
    return Arrays.stream(children)
        .anyMatch(vf -> vf.getName().equals("schemas") && vf.isDirectory());
  }

  // override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
  @Override
  public ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.EDT;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    @Nullable VirtualFile virtualFile = event.getData(
        CommonDataKeys.VIRTUAL_FILE);
    log.info("UPLOAD ACTION: " + virtualFile);
    if (virtualFile == null) {
      return;
    }

    ProgressManager.getInstance().run(new Task.Backgroundable(event.getProject(),
        "Package, Prepare and activate", false) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {

        try {
          PrepareAndDeployResponse response = VespaAppUploader.packagePrepareAndActivate(
              virtualFile.getCanonicalPath(), indicator);

          String msg = String.format("Application Prepared and Activated: %s<br/>",
              response.toHtml());

          NotificationGroupManager.getInstance()
              .getNotificationGroup("Vespa YQL")
              .createNotification(msg, response.getNotificationType())
              .notify(event.getProject());

        } catch (Exception ex) {
          log.error("Could not upload " + virtualFile.getCanonicalPath(), ex);
          String errMsg = ex.getMessage();
          NotificationGroupManager.getInstance()
              .getNotificationGroup("Vespa YQL")
              .createNotification(errMsg, NotificationType.ERROR)
              .notify(event.getProject());
        }

      }
    });

//    ApplicationManager.getApplication().executeOnPooledThread(
//        () -> ApplicationManager.getApplication().runReadAction(() -> {
//          // do whatever you need to do
//          try {
//            PrepareAndDeployResponse response = VespaAppUploader.upload(
//                virtualFile.getCanonicalPath());
//
//            String msg = String.format("Application Prepared and Activated: %s<br/>",
//                response.toHtml());
//
//            NotificationGroupManager.getInstance()
//                .getNotificationGroup("Vespa YQL")
//                .createNotification(msg, response.getNotificationType())
//                .notify(event.getProject());
//
//          } catch (Exception ex) {
//            log.error("Could not upload " + virtualFile.getCanonicalPath(), ex);
//            String errMsg = ex.getMessage();
//            NotificationGroupManager.getInstance()
//                .getNotificationGroup("Vespa YQL")
//                .createNotification(errMsg, NotificationType.ERROR)
//                .notify(event.getProject());
//          }
//        }));

  }
}

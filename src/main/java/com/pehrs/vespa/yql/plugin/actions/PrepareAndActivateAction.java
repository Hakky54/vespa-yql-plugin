package com.pehrs.vespa.yql.plugin.actions;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.vfs.VirtualFile;
import com.pehrs.vespa.yql.plugin.VespaClusterChecker;
import com.pehrs.vespa.yql.plugin.deploy.PrepareAndDeployResponse;
import com.pehrs.vespa.yql.plugin.deploy.VespaAppUploader;
import com.pehrs.vespa.yql.plugin.util.IdeProjectUtils;
import com.pehrs.vespa.yql.plugin.util.NotificationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrepareAndActivateAction extends AnAction {

  private static final Logger log = LoggerFactory.getLogger(PrepareAndActivateAction.class);

  public void update(AnActionEvent actionEvent) {
//    @Nullable VirtualFile virtualFile = actionEvent.getData(
//        CommonDataKeys.VIRTUAL_FILE);

    boolean enabled = false;
    VirtualFile projectRoot = actionEvent.getProject().getProjectFile().getParent().getParent();
    if (IdeProjectUtils.isVespaJavaAppProject(projectRoot)) {
      enabled = true;
    } else {
      @Nullable Object[] selectedObjects = ProjectView.getInstance(actionEvent.getProject())
          .getCurrentProjectViewPane().getSelectedUserObjects();
      if (selectedObjects != null && selectedObjects.length > 0) {
        @Nullable Object first = selectedObjects[0];
        if (first instanceof PsiDirectoryNode dirNode) {
          @Nullable VirtualFile vf = dirNode.getVirtualFile();
          enabled = IdeProjectUtils.isApplicationSrcDir(vf);
        }
      }
    }
    actionEvent.getPresentation().setEnabledAndVisible(enabled);
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

    ApplicationManager.getApplication().runReadAction(() -> {
      doRun(event, virtualFile);
    });

    // CommandProcessor.getInstance().executeCommand(event.getProject(), () -> {
    // doRun(event, virtualFile);
    // }, "Package, Prepare and Active", "Vespa YQL");
  }

  private static void packagePrepareAndActivate(@NotNull AnActionEvent event,
      @NotNull VirtualFile virtualFile) {
    final String appDirCanonicalPath = virtualFile.getCanonicalPath();
    ProgressManager.getInstance().run(new Task.Backgroundable(event.getProject(),
        "Package, Prepare and activate", false) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        try {
          PrepareAndDeployResponse response = VespaAppUploader.packagePrepareAndActivate(
              appDirCanonicalPath, indicator);

          String msg = String.format("Application Prepared and Activated: %s<br/>",
              response.toHtml());
          NotificationUtils.showNotification(event.getProject(),
              response.getNotificationType(),
              msg);
        } catch (Exception ex) {
          log.error("Could not upload " + appDirCanonicalPath, ex);
          NotificationUtils.showException(event.getProject(), ex);
        }
        // Update application info
        VespaClusterChecker.checkVespaClusters();
      }
    });
  }

  private static void doRun(@NotNull AnActionEvent event,
      @NotNull final VirtualFile selectedVirtualFile) {

    VirtualFile projectRoot = event.getProject().getProjectFile().getParent().getParent();
    if (IdeProjectUtils.isVespaJavaAppProject(projectRoot)) {

      // Make sure the java build is done here by calling mvn package
      IdeProjectUtils.mavenRunPackage(event.getProject(), () -> {

        VirtualFile appRoot = IdeProjectUtils.getJavaProjectAppDir(projectRoot).orElse(null);
        if (appRoot == null) {
          NotificationUtils.showNotification(event.getProject(), NotificationType.ERROR,
              "Could not find the target/application dir. "
                  + "Please make sure your maven pom.xml is setup correctly to built the Vespa application!");
          return;
        }
        log.info("mvn package is done, lets prepare and active :-)");
        // packagePrepareAndActivate(event, vf);
        try {
          packagePrepareAndActivate(event, appRoot);
        } catch (Exception ex) {
          NotificationUtils.showException(event.getProject(), ex);
        }
      });
    } else {
      packagePrepareAndActivate(event, selectedVirtualFile);
    }
  }


}

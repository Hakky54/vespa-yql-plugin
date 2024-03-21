package com.pehrs.vespa.yql.plugin.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowManager;
import com.pehrs.vespa.yql.plugin.YQL;
import com.pehrs.vespa.yql.plugin.YqlResult;
import com.pehrs.vespa.yql.plugin.YqlResult.YqlQueryError;
import com.pehrs.vespa.yql.plugin.psi.YqlFile;
import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsState;
import java.io.IOException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecuteQueryAction extends AnAction {

  static Logger log = LoggerFactory.getLogger(ExecuteQueryAction.class);

  private static ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void update(AnActionEvent actionEvent) {
    @Nullable VirtualFile virtualFile = actionEvent.getData(
        CommonDataKeys.VIRTUAL_FILE);
    actionEvent.getPresentation().setEnabledAndVisible(
        ApplicationManager.getApplication().isInternal() &&
            virtualFile != null &&
            YqlFile.isYqlFile(virtualFile, actionEvent.getProject())
    );
  }
  // override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
  @Override
  public ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    @Nullable VirtualFile virtualFile = event.getData(
        CommonDataKeys.VIRTUAL_FILE);
    if (virtualFile == null) {
      return;
    }
    Project project = event.getRequiredData(CommonDataKeys.PROJECT);

    try {

      // Get current edited text
      // @NotNull Editor editor = event.getRequiredData(
      //    CommonDataKeys.EDITOR);
      // Document document = editor.getDocument();
      Document document = FileEditorManager.getInstance(project)
          .getSelectedTextEditor().getDocument();

      JsonNode parsed = objectMapper.readTree(document.getText());
      String pretty = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsed);

      YqlAppSettingsState settings = YqlAppSettingsState.getInstance();

      log.debug("Run Query:\n" + pretty);

      // YqlResult result = new YqlResult(YqlResult.YQL_SAMPLE_RESULT);
      YqlResult result = YQL.executeQuery(pretty);
      YqlResult.updateResult(result);

      List<YqlQueryError> errors = result.getErrors();
      if (errors.isEmpty()) {
        ToolWindowManager.getInstance(project)
            .getToolWindow("Vespa YQL Results").show(null);
      } else {
        String errMsg = errors.get(0).message();
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Vespa YQL")
            .createNotification(errMsg, NotificationType.ERROR)
            .notify(project);
      }

    } catch (Exception ex) {
      String errMsg = ex.getMessage();
      NotificationGroupManager.getInstance()
          .getNotificationGroup("Vespa YQL")
          .createNotification(errMsg, NotificationType.ERROR)
          .notify(project);
      // throw new RuntimeException(ex);
    }
  }
}

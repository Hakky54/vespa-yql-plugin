package com.pehrs.vespa.yql.plugin.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.pehrs.vespa.yql.plugin.VespaClusterConnection;
import com.pehrs.vespa.yql.plugin.YqlResult;
import com.pehrs.vespa.yql.plugin.YqlResult.YqlQueryError;
import com.pehrs.vespa.yql.plugin.psi.YqlFile;
import com.pehrs.vespa.yql.plugin.results.YqlResultsFactory;
import com.pehrs.vespa.yql.plugin.util.NotificationUtils;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecuteQueryAction extends AnAction {

  private static final Logger log = LoggerFactory.getLogger(ExecuteQueryAction.class);

  private static ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void update(AnActionEvent actionEvent) {
    @Nullable VirtualFile virtualFile = actionEvent.getData(
        CommonDataKeys.VIRTUAL_FILE);
    actionEvent.getPresentation().setEnabledAndVisible(
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
      String text = new String(virtualFile.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
//      String text = FileEditorManager.getInstance(project)
//          .getSelectedTextEditor().getDocument().getText();

      JsonNode parsed = objectMapper.readTree(text);
      String pretty = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsed);
      log.debug("Run Query:\n" + pretty);

      YqlResult result = VespaClusterConnection.executeQuery(pretty);
      YqlResult.updateResult(result);

      List<YqlQueryError> errors = result.getErrors();
      if (errors.isEmpty()) {
        YqlResultsFactory.showResults(event.getProject());
      } else {
        NotificationUtils.showException(event.getProject(), new RuntimeException(errors.get(0).message()));
      }

    } catch (Exception ex) {
      NotificationUtils.showException(event.getProject(), ex);
    }
  }
}

package com.pehrs.vespa.yql.plugin.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LargeFileWriteRequestor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.pehrs.vespa.yql.plugin.psi.YqlFile;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Not used as we use the YqlFormattingBuilderModel instead
 * @see com.pehrs.vespa.yql.plugin.formatter.YqlFormattingBuilderModel
 */
@Deprecated
public class YqlJacksonReformatAction extends AnAction implements LargeFileWriteRequestor {

  private static ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void update(AnActionEvent actionEvent) {
    @Nullable VirtualFile virtualFile = actionEvent.getData(
        CommonDataKeys.VIRTUAL_FILE);
    actionEvent.getPresentation().setEnabledAndVisible(ApplicationManager.getApplication().isInternal() &&
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
  public void actionPerformed(@NotNull AnActionEvent e) {
    @Nullable VirtualFile virtualFile = e.getData(
        CommonDataKeys.VIRTUAL_FILE);
    if(virtualFile==null) {
      return;
    }
    @Nullable Project project = e.getProject();

    try {
      JsonNode parsed = objectMapper.readTree(
          virtualFile.getInputStream());

      String pretty = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsed);

      @Nullable PsiFile file = PsiManager.getInstance(project)
          .findFile(virtualFile);
      @Nullable Document doc = PsiDocumentManager.getInstance(project)
          .getDocument(file);
      if(doc.isWritable()) {
        ApplicationManager.getApplication().runWriteAction(() -> {
          doc.setText(pretty);
        });
      }
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}

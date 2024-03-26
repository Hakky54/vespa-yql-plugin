package com.pehrs.vespa.yql.plugin.results;

import com.intellij.icons.AllIcons.Actions;
import com.intellij.json.JsonFileType;
import com.intellij.json.JsonLanguage;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI.Borders;
import com.pehrs.vespa.yql.plugin.YqlResult;
import com.pehrs.vespa.yql.plugin.YqlResult.YqlResultListener;
import java.awt.BorderLayout;
import java.io.File;
import java.io.PrintWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class YqlResultsJsonPanel extends JBPanel {

  private final Project project;
  private Editor editor;

  public YqlResultsJsonPanel(Project project) {
    super(new BorderLayout());
    this.project = project;
    super.setBorder(Borders.empty());
    createComponents();
  }

  private void createComponents() {

    EditorFactory factory = EditorFactory.getInstance();
//    @NotNull Document doc = factory.createDocument(
//        "{\"key\": \"value\"}");

    // @NotNull Document doc = factory.createDocument("");
    // FIXME: Trying to get folding to work for editor
    final Document doc = factory.createDocument("");
    LightVirtualFile lvf = new LightVirtualFile(".yql-response.json",
        JsonFileType.INSTANCE,
        "");

    this.editor =
        factory.createEditor(doc, project, lvf, true);

//    this.editor = EditorFactory.getInstance()
//        // .createEditor(doc, project, JsonFileType.INSTANCE, true);
//        .createEditor(doc, project, JsonFileType.INSTANCE, true);

    // FIXME: Trying to get folding to work for editor
    @NotNull EditorSettings editorSettings = editor.getSettings();
    editorSettings.setLineNumbersShown(true);
    editorSettings.setAutoCodeFoldingEnabled(true);
    editorSettings.setFoldingOutlineShown(true);
    editorSettings.setAllowSingleLogicalLineFolding(true);
    editorSettings.setRightMarginShown(true);

    YqlResult.addResultListener(new YqlResultListener() {
      @Override
      public void resultUpdated(YqlResult result) {
        ApplicationManager.getApplication().runWriteAction(() -> {
          doc.setText(result.toString());

//          VirtualFile vfile = null;
//          try {
//            vfile = project.getBaseDir().createChildData(project, ".vespa-response.json");
//          } catch (IOException e) {
//            e.printStackTrace();
//          }
//          Document document = FileDocumentManager.getInstance().getDocument(vfile);
        });
      }
    });

    AnAction exportAction = new DumbAwareAction("Export JSON", "Export JSON result",
        Actions.Download) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e) {
        if (e == null) {
          return;
        }
        FileSaverDescriptor descriptor = new FileSaverDescriptor(
            "Save JSON", "Save Result to json file", "json"
        );
        @NotNull FileSaverDialog dialog = FileChooserFactory.getInstance()
            .createSaveFileDialog(descriptor, project);

        @Nullable VirtualFileWrapper res = dialog.save(project.getWorkspaceFile(), "vespa-yql-result");
        if(res != null) {
          File file = res.getFile();
          try(PrintWriter out = new PrintWriter(file)) {
            out.print(doc.getText());
            out.flush();
          } catch (Exception ex) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Vespa YQL")
                .createNotification(ex.getMessage(), NotificationType.ERROR)
                .notify(project);
          }
        }
      }
    };
    AnAction openInEditorAction = new DumbAwareAction("Open in Editor",
        "Create new file and open JSON response in editor", Actions.AddFile) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e) {
        if (e == null) {
          return;
        }
        PsiFile newDoc = PsiFileFactory.getInstance(project)
            .createFileFromText(JsonLanguage.INSTANCE, doc.getText());
        newDoc.setName("yql-result.json");

        FileEditorManager.getInstance(project)
            .openFile(newDoc.getVirtualFile());
      }
    };

    @NotNull ActionGroup actions = new ActionGroup() {
      @Override
      public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        return new AnAction[]{exportAction, openInEditorAction};
      }
    };

    ActionToolbarImpl toolbar = new ActionToolbarImpl(ActionPlaces.TOOLBAR, actions, true);
    toolbar.setEnabled(false);

    toolbar.setTargetComponent(editor.getComponent());
    super.add(toolbar, BorderLayout.NORTH);
    super.add(editor.getComponent(), BorderLayout.CENTER);
  }

}

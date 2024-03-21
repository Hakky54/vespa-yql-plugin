package com.pehrs.vespa.yql.plugin.dock;

import com.fasterxml.jackson.databind.JsonNode;
import com.intellij.icons.AllIcons.Actions;
import com.intellij.icons.AllIcons.FileTypes;
import com.intellij.icons.AllIcons.Json;
import com.intellij.ide.dnd.aware.DnDAwareTree;
import com.intellij.json.JsonFileType;
import com.intellij.json.JsonLanguage;
import com.intellij.json.json5.Json5FileType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI.Borders;
import com.intellij.util.ui.tree.TreeModelAdapter;
import com.pehrs.vespa.yql.plugin.YqlIcons;
import com.pehrs.vespa.yql.plugin.YqlResult.YqlResultListener;
import com.pehrs.vespa.yql.plugin.YqlResult;
import com.pehrs.vespa.yql.plugin.YqlResultTraceTreeModel;
import com.pehrs.vespa.yql.plugin.YqlResultTraceTreeModel.JsonProperty;
import com.pehrs.vespa.yql.plugin.swing.TableColumnAdjuster;
import java.awt.BorderLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TreeModelEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class YqlResultsFactory implements ToolWindowFactory, DumbAware {


  private JTabbedPane tabs;
  private Editor editor;


  @Override
  public boolean isApplicable(@NotNull Project project) {
    return project != null;
  }

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

    // FIXME: We are using JTabbedPane instead of JBTabbedPane as
    //   the JBTabbedPane does NOT visualize the disabled tabs correctly
    this.tabs = new JTabbedPane(1);

    JPanel panel = new YqlResultsTablePanel(project);
    JPanel jsonPanel = new YqlResultsJsonPanel(project);
    JPanel traceTablePanel = new YqlResultsTraceTablePanel(project, tabs, 2);
    // JPanel traceTreePanel = new YqlResultsTraceTreePanel(project, tabs, 3);

    tabs.insertTab("Results", Json.Object, panel, "Query Results in table format", 0);
    tabs.insertTab("Json", FileTypes.Json, jsonPanel, "Query Results in JSON format", 1);
    tabs.insertTab("Trace", YqlIcons.ZIPKIN, traceTablePanel, "Query Trace Table", 2);
    // tabs.insertTab("Trace Tree", YqlIcons.ZIPKIN, traceTreePanel, "Query Trace Tree", 3);
    tabs.setEnabledAt(2, false);

    ContentManager contentManager = toolWindow.getContentManager();
    Content content = contentManager.getFactory().createContent((JComponent) tabs, null, false);
    contentManager.addContent(content);
  }

//  @NotNull
//  private JPanel createJsonPanel(Project project) {
//    JBPanel jsonPanel = new JBPanel<>(new BorderLayout());
//    jsonPanel.setBorder(Borders.empty());
//
////     PsiFile psiFile = PsiDocumentManager.getInstance(project)
////        .getPsiFile(editor.getDocument());
//
//    EditorFactory factory = EditorFactory.getInstance();
////    @NotNull Document doc = factory.createDocument(
////        "{\"key\": \"value\"}");
//
//    // @NotNull Document doc = factory.createDocument("");
//    // FIXME: Trying to get folding to work for editor
//    final Document doc = factory.createDocument("");
//    LightVirtualFile lvf = new LightVirtualFile(".yql-response.json",
//        JsonFileType.INSTANCE,
//        "");
//
//    this.editor =
//        factory.createEditor(doc, project, lvf, true);
//
////    this.editor = EditorFactory.getInstance()
////        // .createEditor(doc, project, JsonFileType.INSTANCE, true);
////        .createEditor(doc, project, JsonFileType.INSTANCE, true);
//
//    // FIXME: Trying to get folding to work for editor
//    @NotNull EditorSettings editorSettings = editor.getSettings();
//    editorSettings.setLineNumbersShown(true);
//    editorSettings.setAutoCodeFoldingEnabled(true);
//    editorSettings.setFoldingOutlineShown(true);
//    editorSettings.setAllowSingleLogicalLineFolding(true);
//    editorSettings.setRightMarginShown(true);
//
//    YqlResult.addResultListener(new YqlResultListener() {
//      @Override
//      public void resultUpdated(YqlResult result) {
//        ApplicationManager.getApplication().runWriteAction(() -> {
//          doc.setText(result.toString());
//
////          VirtualFile vfile = null;
////          try {
////            vfile = project.getBaseDir().createChildData(project, ".vespa-response.json");
////          } catch (IOException e) {
////            e.printStackTrace();
////          }
////          Document document = FileDocumentManager.getInstance().getDocument(vfile);
//        });
//      }
//    });
//
//    AnAction exportAction = new DumbAwareAction("Export JSON", "Export JSON result",
//        Actions.Download) {
//      @Override
//      public void actionPerformed(@NotNull AnActionEvent e) {
//        if (e == null) {
//          return;
//        }
//        System.out.println("Export JSon Result!!!");
//
//        FileSaverDescriptor descriptor = new FileSaverDescriptor(
//            "Save JSON", "Save Result to json file", "json"
//        );
//        @NotNull FileSaverDialog dialog = FileChooserFactory.getInstance()
//            .createSaveFileDialog(descriptor, project);
//
//        @Nullable VirtualFileWrapper res = dialog.save(project.getWorkspaceFile(), "vespa-yql-result");
//        if(res != null) {
//          File file = res.getFile();
//          try(PrintWriter out = new PrintWriter(file)) {
//            out.print(doc.getText());
//            out.flush();
//          } catch (Exception ex) {
//            NotificationGroupManager.getInstance()
//                .getNotificationGroup("Vespa YQL")
//                .createNotification(ex.getMessage(), NotificationType.ERROR)
//                .notify(project);
//          }
//        }
//      }
//    };
//    AnAction openInEditorAction = new DumbAwareAction("Open in Editor",
//        "Create new file and open JSON response in editor", Actions.AddFile) {
//      @Override
//      public void actionPerformed(@NotNull AnActionEvent e) {
//        if (e == null) {
//          return;
//        }
//        PsiFile newDoc = PsiFileFactory.getInstance(project)
//            .createFileFromText(JsonLanguage.INSTANCE, doc.getText());
//        newDoc.setName("yql-result.json");
//
//        FileEditorManager.getInstance(project)
//            .openFile(newDoc.getVirtualFile());
//      }
//    };
//
//    @NotNull ActionGroup actions = new ActionGroup() {
//      @Override
//      public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
//        return new AnAction[]{exportAction, openInEditorAction};
//      }
//    };
//
//    ActionToolbarImpl toolbar = new ActionToolbarImpl(ActionPlaces.TOOLBAR, actions, true);
//    toolbar.setEnabled(false);
//
//    toolbar.setTargetComponent(editor.getComponent());
//    jsonPanel.add(toolbar, BorderLayout.NORTH);
//    jsonPanel.add(editor.getComponent(), BorderLayout.CENTER);
//
//    return jsonPanel;
//  }

//  @NotNull
//  private JPanel createTablePanel() {
//    JBTable resultsTable = new JBTable();
//    resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//    resultsTable.setBorder(Borders.empty());
//
//    YqlResultTableModel tableModel = new YqlResultTableModel();
//    YqlResult.addResultListener(tableModel);
//    resultsTable.setModel(tableModel);
//
//    tableModel.addTableModelListener(event -> {
//      // Only update columns when data changes as the order of events is HEADER then DATA.
//      if (event.getFirstRow() != TableModelEvent.HEADER_ROW) {
//        TableColumnAdjuster tca = new TableColumnAdjuster(resultsTable, 3);
//        tca.setColumnDataIncluded(true);
//        tca.setColumnMaxWidth(650);
//        tca.adjustColumns();
//      }
//    });
//
//    ToolbarDecorator decorator =
//        ToolbarDecorator.createDecorator(resultsTable)
//            .initPosition()
//            .setToolbarPosition(ActionToolbarPosition.TOP);
//
//    decorator.addExtraAction(
//        AnActionButton.fromAction(
//            new DumbAwareAction("Export CSV", "Export table as CSV", Actions.Download) {
//              public void actionPerformed(@NotNull AnActionEvent e) {
//                if (e == null) {
//                  return;
//                }
//                System.out.println("Export Table Result!!!");
//              }
//            }));
//
//    JPanel panel = decorator.createPanel();
//    panel.setBorder(Borders.empty());
//    return panel;
//  }

//
//  @NotNull
//  private JPanel createTraceTreePanel(Project project) {
//
//    DnDAwareTree traceTree;
//    YqlResultTraceTreeModel traceTreeModel = new YqlResultTraceTreeModel();
//    traceTreeModel.addTreeModelListener(new TreeModelAdapter() {
//      @Override
//      protected void process(@NotNull TreeModelEvent event,
//          @NotNull TreeModelAdapter.EventType type) {
//        super.process(event, type);
//        YqlResult res = YqlResult.getYqlResult();
//        if (res.getErrors().size() > 0) {
//          tabs.setEnabledAt(2, false);
//        } else {
//          res.getTrace()
//              .ifPresentOrElse(trace -> {
//                tabs.setEnabledAt(2, true);
//              }, () -> {
//                tabs.setEnabledAt(2, false);
//              });
//        }
//      }
//    });
//    YqlResult.addResultListener(traceTreeModel);
//    traceTree = new DnDAwareTree(traceTreeModel);
//
//    JBLabel label = new JBLabel();
//    // FIXME: We need some wrapper for the JsonNode and fieldName
//    traceTree.setCellRenderer((tree, value, selected, expanded, leaf, row, hasFocus) -> {
//      JsonProperty property = (JsonProperty) value;
//      String renderedString = property.name();
//      JsonNode node = property.value();
//      switch (node.getNodeType()) {
//        case BOOLEAN -> {
//          renderedString += ": " + node.asBoolean();
//        }
//        case NUMBER -> {
//          if (node.isFloat() || node.isDouble()) {
//            renderedString += ": " + node.asDouble();
//          } else if (node.isInt()) {
//            renderedString += ": " + node.asInt();
//          } else if (node.isLong()) {
//            renderedString += ": " + node.asLong();
//          }
//        }
//        case STRING -> {
//          renderedString += ": \"" + node.asText() + "\"";
//        }
//        case NULL -> {
//          renderedString += ": null";
//        }
//        case MISSING -> {
//          renderedString += ": <missing value>";
//        }
//        case POJO, OBJECT, ARRAY, BINARY -> {
//        }
//      }
//
//      label.setText(renderedString);
//      label.setIcon(null);
//      return label;
//    });
//
//    ToolbarDecorator decorator =
//        ToolbarDecorator.createDecorator(traceTree)
//            .initPosition()
//            .setToolbarPosition(ActionToolbarPosition.TOP);
//
//    decorator.addExtraAction(new DumbAwareAction("Open in Zipkin", "Upload and view in Zipkin",
//        YqlIcons.ZIPKIN) {
//      @Override
//      public void actionPerformed(@NotNull AnActionEvent e) {
//        if (e == null) {
//          return;
//        }
//        System.out.println("Open in Zipkin");
//      }
//    });
//    JPanel panel = decorator.createPanel();
//    panel.setBorder(Borders.empty());
//    return panel;
//  }

//  private JPanel createTraceTablePanel(Project project) {
//    JBTable resultsTable = new JBTable();
//    resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//    resultsTable.setBorder(Borders.empty());
//
//    YqlTraceTableModel tableModel = new YqlTraceTableModel();
//    YqlResult.addResultListener(tableModel);
//    resultsTable.setModel(tableModel);
//
//    tableModel.addTableModelListener(event -> {
//      // Only update columns when data changes as the order of events is HEADER then DATA.
//      if (event.getFirstRow() != TableModelEvent.HEADER_ROW) {
//        TableColumnAdjuster tca = new TableColumnAdjuster(resultsTable, 3);
//        tca.setColumnDataIncluded(true);
//        // tca.setColumnMaxWidth(650);
//        tca.adjustColumns();
//      }
//
//      YqlResult res = YqlResult.getYqlResult();
//      if (res.getErrors().size() > 0) {
//        tabs.setEnabledAt(2, false);
//      } else {
//        res.getTrace()
//            .ifPresentOrElse(trace -> {
//              tabs.setEnabledAt(2, true);
//            }, () -> {
//              tabs.setEnabledAt(2, false);
//            });
//      }
//    });
//
//    ToolbarDecorator decorator =
//        ToolbarDecorator.createDecorator(resultsTable)
//            .initPosition()
//            .setToolbarPosition(ActionToolbarPosition.TOP);
//
//    // FIXME: Add support for parsing response to Zipkin and upload
//    DumbAwareAction zipkinAction = new DumbAwareAction("Open in Zipkin", "Upload and view in Zipkin",
//        YqlIcons.ZIPKIN) {
//      @Override
//      public void actionPerformed(@NotNull AnActionEvent e) {
//        if (e == null) {
//          return;
//        }
//        NotificationGroupManager.getInstance()
//            .getNotificationGroup("Vespa YQL")
//            .createNotification("Zipkin Export is not implemented yet", NotificationType.WARNING)
//            .notify(project);
//      }
//    };
//    decorator.addExtraAction(zipkinAction);
//
//    JPanel panel = decorator.createPanel();
//    panel.setBorder(Borders.empty());
//    return panel;
//  }

}

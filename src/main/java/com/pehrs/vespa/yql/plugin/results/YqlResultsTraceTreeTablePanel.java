package com.pehrs.vespa.yql.plugin.results;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.intellij.icons.AllIcons;
import com.intellij.icons.AllIcons.Debugger;
import com.intellij.icons.AllIcons.Nodes;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.ui.JBUI.Borders;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeModelAdapter;
import com.pehrs.vespa.yql.plugin.YqlIcons;
import com.pehrs.vespa.yql.plugin.YqlResult;
import com.pehrs.vespa.yql.plugin.swing.TableColumnAdjuster;
import com.pehrs.vespa.yql.plugin.trace.TraceUtils;
import com.pehrs.vespa.yql.plugin.trace.YqlTraceMessage;
import com.pehrs.vespa.yql.plugin.trace.YqlTraceNodeBase;
import com.pehrs.vespa.yql.plugin.trace.YqlTraceThread;
import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreeCellRenderer;
import org.jetbrains.annotations.NotNull;

public class YqlResultsTraceTreeTablePanel extends JBPanel {

  private final Project project;
  private final int traceTabIndex;

  private final int zipkinTabIndex;
  private final ZipkinBrowserPanel zipkinPanel;

  private JTabbedPane tabs;

  public YqlResultsTraceTreeTablePanel(Project project, JTabbedPane tabs, int traceTabIndex, int zipkinTabIndex, ZipkinBrowserPanel zipkinPanel) {
    super(new BorderLayout());
    this.project = project;
    this.tabs = tabs;
    this.traceTabIndex = traceTabIndex;
    this.zipkinTabIndex = zipkinTabIndex;
    this.zipkinPanel = zipkinPanel;
    super.setBorder(Borders.empty());
    createComponents();
  }

  private void createComponents() {
    YqlResultsTraceTreeTableModel model = new YqlResultsTraceTreeTableModel();
    YqlResult.addResultListener(model);

    TreeTable treeTable = new TreeTable(model);

    JLabel treeCellLabel = new JLabel("");

    treeTable.setTreeCellRenderer((tree, value, selected, expanded, leaf, row, hasFocus) -> {
      treeCellLabel.setIcon(null);
      if (value instanceof YqlTraceNodeBase traceNode) {
        String name = traceNode.getClass().getSimpleName()
            .replace("YqlTrace", "")
            .replace("Node", "Trace");
        int size = traceNode.getChildren().size();
        if (size > 0) {
          treeCellLabel.setText(String.format("%s[%d]", name, size));
        } else {
          treeCellLabel.setText(name);
        }
        if(value instanceof YqlTraceThread) {
          treeCellLabel.setIcon(Debugger.Threads);
        } else if(value instanceof YqlTraceMessage) {
            treeCellLabel.setIcon(Nodes.Folder);
        } else {
          treeCellLabel.setIcon(Debugger.ThreadAtBreakpoint);
        }
      } else {
        treeCellLabel.setText("" + value);
      }
      return treeCellLabel;
    });

    treeTable.setShowGrid(true);

    TreeTableTree tree = treeTable.getTree();
    UIUtil.setLineStyleAngled(tree);
    tree.setShowsRootHandles(true);
    tree.setRootVisible(true);

    ToolbarDecorator decorator =
        ToolbarDecorator.createDecorator(treeTable)
            .initPosition()
            .setToolbarPosition(ActionToolbarPosition.TOP);

    decorator.addExtraAction(new DumbAwareAction("Upload to Zipkin", "Upload and view in Zipkin Panel",
        YqlIcons.ZIPKIN) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e) {
        if (e == null) {
          return;
        }
        try {
          String traceID = TraceUtils.uploadToZipkin();

          NotificationGroupManager.getInstance()
              .getNotificationGroup("Vespa YQL")
              .createNotification("Zipkin trace " +traceID + " uploaded!", NotificationType.INFORMATION)
              .notify(project);

          TraceUtils.openTrace(project, traceID, zipkinPanel);

          tabs.setEnabledAt(zipkinTabIndex, true);
          tabs.setSelectedIndex(zipkinTabIndex);

        } catch (JsonProcessingException ex) {
          NotificationGroupManager.getInstance()
              .getNotificationGroup("Vespa YQL")
              .createNotification("Zipkin Upload failed", NotificationType.ERROR)
              .notify(project);
        } catch (IOException | URISyntaxException ex) {
          NotificationGroupManager.getInstance()
              .getNotificationGroup("Vespa YQL")
              .createNotification("Could not open default browser", NotificationType.ERROR)
              .notify(project);
        }      }
    });
    JPanel panel = decorator.createPanel();
    panel.setBorder(Borders.empty());

    model.addTreeModelListener(new TreeModelAdapter() {
      public void treeStructureChanged(TreeModelEvent event) {
        TableColumnAdjuster tca = new TableColumnAdjuster(treeTable, 3);
        tca.setColumnDataIncluded(true);
        tca.setColumnMaxWidth(650);
        tca.adjustColumns();

        if(tabs != null) {
          YqlResult res = YqlResult.getYqlResult();
          if (res.getErrors().size() > 0) {
            tabs.setEnabledAt(traceTabIndex, false);
          } else {
            res.getTrace()
                .ifPresentOrElse(trace -> {
                  tabs.setEnabledAt(traceTabIndex, true);
                }, () -> {
                  tabs.setEnabledAt(traceTabIndex, false);
                });
          }
        }
      }
    });

    super.add(panel, BorderLayout.CENTER);
  }


}

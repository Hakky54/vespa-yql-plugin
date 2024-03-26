package com.pehrs.vespa.yql.plugin.results;

import com.fasterxml.jackson.databind.JsonNode;
import com.intellij.ide.dnd.aware.DnDAwareTree;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI.Borders;
import com.intellij.util.ui.tree.TreeModelAdapter;
import com.pehrs.vespa.yql.plugin.YqlIcons;
import com.pehrs.vespa.yql.plugin.YqlResult;
import com.pehrs.vespa.yql.plugin.results.YqlResultTraceTreeModel.JsonProperty;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.TreeModelEvent;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class YqlResultsTraceTreePanel extends JBPanel {
  private final Project project;
  private final int tabIndex;

  private JTabbedPane tabs;
  public YqlResultsTraceTreePanel(Project project, JTabbedPane tabs, int index) {
    super(new BorderLayout());
    this.project = project;
    this.tabs = tabs;
    this.tabIndex = index;
    super.setBorder(Borders.empty());
    createComponents();
  }

  private void createComponents() {
    DnDAwareTree traceTree;
    YqlResultTraceTreeModel traceTreeModel = new YqlResultTraceTreeModel();
    traceTreeModel.addTreeModelListener(new TreeModelAdapter() {
      @Override
      protected void process(@NotNull TreeModelEvent event,
          @NotNull TreeModelAdapter.EventType type) {
        super.process(event, type);
        YqlResult res = YqlResult.getYqlResult();
        if(tabs != null) {
          if (res.getErrors().size() > 0) {
            tabs.setEnabledAt(tabIndex, false);
          } else {
            res.getTrace()
                .ifPresentOrElse(trace -> {
                  tabs.setEnabledAt(tabIndex, true);
                }, () -> {
                  tabs.setEnabledAt(tabIndex, false);
                });
          }
        }
      }
    });
    YqlResult.addResultListener(traceTreeModel);
    traceTree = new DnDAwareTree(traceTreeModel);

    JBLabel label = new JBLabel();
    // FIXME: We need some wrapper for the JsonNode and fieldName
    traceTree.setCellRenderer((tree, value, selected, expanded, leaf, row, hasFocus) -> {
      JsonProperty property = (JsonProperty) value;
      String renderedString = property.name();
      JsonNode node = property.value();
      switch (node.getNodeType()) {
        case BOOLEAN -> {
          renderedString += ": " + node.asBoolean();
        }
        case NUMBER -> {
          if (node.isFloat() || node.isDouble()) {
            renderedString += ": " + node.asDouble();
          } else if (node.isInt()) {
            renderedString += ": " + node.asInt();
          } else if (node.isLong()) {
            renderedString += ": " + node.asLong();
          }
        }
        case STRING -> {
          renderedString += ": \"" + node.asText() + "\"";
        }
        case NULL -> {
          renderedString += ": null";
        }
        case MISSING -> {
          renderedString += ": <missing value>";
        }
        case POJO, OBJECT, ARRAY, BINARY -> {
        }
      }

      label.setText(renderedString);
      label.setIcon(null);
      return label;
    });

    ToolbarDecorator decorator =
        ToolbarDecorator.createDecorator(traceTree)
            .initPosition()
            .setToolbarPosition(ActionToolbarPosition.TOP);

    decorator.addExtraAction(new DumbAwareAction("Open in Zipkin", "Upload and view in Zipkin",
        YqlIcons.ZIPKIN) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e) {
        if (e == null) {
          return;
        }
        System.out.println("Open in Zipkin");
      }
    });
    JPanel panel = decorator.createPanel();
    panel.setBorder(Borders.empty());

    super.add(panel, BorderLayout.CENTER);
  }
}

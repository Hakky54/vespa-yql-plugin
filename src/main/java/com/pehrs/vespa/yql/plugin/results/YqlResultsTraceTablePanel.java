package com.pehrs.vespa.yql.plugin.results;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI.Borders;
import com.pehrs.vespa.yql.plugin.YqlIcons;
import com.pehrs.vespa.yql.plugin.YqlResult;
import com.pehrs.vespa.yql.plugin.swing.TableColumnAdjuster;
import com.pehrs.vespa.yql.plugin.trace.TraceUtils;
import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class YqlResultsTraceTablePanel extends JBPanel {
  private final Project project;
  private final int traceTabIndex;
  private final int zipkinTabIndex;
  private final ZipkinBrowserPanel zipkinPanel;

  private JTabbedPane tabs;

  public YqlResultsTraceTablePanel(Project project, JTabbedPane tabs, int traceTabIndex, int zipkinTabIndex, ZipkinBrowserPanel zipkinPanel) {
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
    JBTable resultsTable = new JBTable();
    resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    resultsTable.setBorder(Borders.empty());

    YqlTraceTableModel tableModel = new YqlTraceTableModel();
    YqlResult.addResultListener(tableModel);
    resultsTable.setModel(tableModel);

    tableModel.addTableModelListener(event -> {
      // Only update columns when data changes as the order of events is HEADER then DATA.
      if (event.getFirstRow() != TableModelEvent.HEADER_ROW) {
        TableColumnAdjuster tca = new TableColumnAdjuster(resultsTable, 3);
        tca.setColumnDataIncluded(true);
        // tca.setColumnMaxWidth(650);
        tca.adjustColumns();
      }

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
    });

    ToolbarDecorator decorator =
        ToolbarDecorator.createDecorator(resultsTable)
            .initPosition()
            .setToolbarPosition(ActionToolbarPosition.TOP);

    // FIXME: Add support for parsing response to Zipkin and upload
    DumbAwareAction zipkinAction = new DumbAwareAction("Upload and open in Zipkin", "Upload and view in Zipkin",
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
        }

      }
    };
    decorator.addExtraAction(zipkinAction);

    JPanel panel = decorator.createPanel();
    panel.setBorder(Borders.empty());

    super.add(panel, BorderLayout.CENTER);
  }

}

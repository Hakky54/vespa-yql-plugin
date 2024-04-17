package com.pehrs.vespa.yql.plugin.results;

import com.intellij.icons.AllIcons.FileTypes;
import com.intellij.icons.AllIcons.Json;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.jcef.JBCefBrowser;
import com.pehrs.vespa.yql.plugin.YqlIcons;
import com.pehrs.vespa.yql.plugin.YqlResult;
import com.pehrs.vespa.yql.plugin.YqlResult.YqlResultListener;
import javax.swing.JTabbedPane;

// FIXME: We are using JTabbedPane instead of JBTabbedPane as
//   the JBTabbedPane does NOT visualize the disabled tabs correctly
public class YqlResultsTabView extends JTabbedPane implements YqlResultListener {

  private final YqlResultsTablePanel tablePanel;
  private final YqlResultsJsonPanel jsonPanel;
  private final ZipkinBrowserPanel zipkinPanel;
  private final JBCefBrowser zipkinBrowser;
  private final YqlResultsTraceTreeTablePanel traceTablePanel;


  private static final int TABLE_PANEL_INDEX = 0;
  private static final int JSON_PANEL_INDEX = 1;
  private static final int TRACE_PANEL_INDEX = 2;
  private static final int ZIPKIN_PANEL_INDEX = 3;

  public YqlResultsTabView(Project project) {
    super( JTabbedPane.TOP);


    this.tablePanel = new YqlResultsTablePanel(project);
    this.jsonPanel = new YqlResultsJsonPanel(project);
    this.zipkinPanel = new ZipkinBrowserPanel(project);
    this.zipkinBrowser = zipkinPanel.getBrowser();
    this.traceTablePanel =
        new YqlResultsTraceTreeTablePanel(project, this,
            TRACE_PANEL_INDEX, ZIPKIN_PANEL_INDEX, zipkinPanel);

    insertTab("Results", Json.Object, tablePanel, "Query Results in table format", TABLE_PANEL_INDEX);
    insertTab("Json", FileTypes.Json, jsonPanel, "Query Results in JSON format", JSON_PANEL_INDEX);
    insertTab("Trace", YqlIcons.TRACE, traceTablePanel, "Query Trace Table", TRACE_PANEL_INDEX);
    insertTab("Zipkin", YqlIcons.ZIPKIN, zipkinPanel, "Zipkin Rendering", ZIPKIN_PANEL_INDEX);
    setEnabledAt(TRACE_PANEL_INDEX, false);
    setEnabledAt(ZIPKIN_PANEL_INDEX, false);

    YqlResult result = YqlResult.getYqlResult();
    if (result != null) {
      ApplicationManager.getApplication().invokeLater(() -> {
        resultUpdated(result);
      });
    }

    YqlResult.addResultListener(this);

  }


  @Override
  public void resultUpdated(YqlResult result) {
    if(!result.getErrors().isEmpty()) {
      setSelectedIndex(TABLE_PANEL_INDEX);
    } else {
      setSelectedIndex(
          result.getColumnNames().isEmpty() ? JSON_PANEL_INDEX : TABLE_PANEL_INDEX);
    }
    setEnabledAt(ZIPKIN_PANEL_INDEX, false);
    if (this.tablePanel != null) {
      this.tablePanel.enableExport(!result.getColumnNames().isEmpty());
      this.tablePanel.notifyModel();
    }
    if (this.jsonPanel != null) {
      this.jsonPanel.resultUpdated(result);
    }
    if (this.tablePanel != null) {
      this.tablePanel.getTableModel().resultUpdated(result);
    }
    if (this.traceTablePanel != null) {
      this.traceTablePanel.getModel().resultUpdated(result);
    }
  }
}

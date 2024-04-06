package com.pehrs.vespa.yql.plugin.results;

import com.intellij.icons.AllIcons.FileTypes;
import com.intellij.icons.AllIcons.Json;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.jcef.JBCefBrowser;
import com.pehrs.vespa.yql.plugin.YqlIcons;
import com.pehrs.vespa.yql.plugin.YqlResult;
import com.pehrs.vespa.yql.plugin.YqlResult.YqlResultListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YqlResultsFactory implements ToolWindowFactory, DumbAware, YqlResultListener {

  private static final Logger log = LoggerFactory.getLogger(YqlResultsFactory.class);

  private JTabbedPane tabs;
  private Editor editor;
  private JBCefBrowser zipkinBrowser;
  private YqlResultsTablePanel tablePanel
      ;
  private YqlResultsJsonPanel jsonPanel;
  private ZipkinBrowserPanel zipkinPanel;


  @Override
  public boolean isApplicable(@NotNull Project project) {
    return project != null;
  }

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

    // FIXME: We are using JTabbedPane instead of JBTabbedPane as
    //   the JBTabbedPane does NOT visualize the disabled tabs correctly
    this.tabs = new JTabbedPane(1);

    this.tablePanel = new YqlResultsTablePanel(project);
    this.jsonPanel = new YqlResultsJsonPanel(project);
    this.zipkinPanel = new ZipkinBrowserPanel(project);
    this.zipkinBrowser = zipkinPanel.getBrowser();
    // JPanel traceTablePanel = new YqlResultsTraceTablePanel(project, tabs, 2, 3, zipkinPanel);
    JPanel traceTablePanel = new YqlResultsTraceTreeTablePanel(project, tabs, 2, 3, zipkinPanel);
    // JPanel traceTreePanel = new YqlResultsTraceTreePanel(project, tabs, 3);


    tabs.insertTab("Results", Json.Object, tablePanel, "Query Results in table format", 0);
    tabs.insertTab("Json", FileTypes.Json, jsonPanel, "Query Results in JSON format", 1);
    tabs.insertTab("Trace", YqlIcons.TRACE, traceTablePanel, "Query Trace Table", 2);
    tabs.insertTab("Zipkin", YqlIcons.ZIPKIN, zipkinPanel, "Zipkin Rendering", 3);
    tabs.setEnabledAt(2, false);
    tabs.setEnabledAt(3, false);

    ContentManager contentManager = toolWindow.getContentManager();
    Content content = contentManager.getFactory().createContent((JComponent) tabs, null, false);
    contentManager.addContent(content);

    YqlResult.addResultListener(this);
  }

  @Override
  public void resultUpdated(YqlResult result) {
    tabs.setSelectedIndex(0);
    tabs.setEnabledAt(3, false);
    this.tablePanel.notifyModel();
  }
}

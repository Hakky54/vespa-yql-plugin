package com.pehrs.vespa.yql.plugin.dock;

import static com.pehrs.vespa.yql.plugin.dock.VespaClusterStatusTableModel.APP_NAME_COLUMN;
import static com.pehrs.vespa.yql.plugin.dock.VespaClusterStatusTableModel.CLUSTER_COLUMN;
import static com.pehrs.vespa.yql.plugin.dock.VespaClusterStatusTableModel.CONFIG_COLUMN;
import static com.pehrs.vespa.yql.plugin.dock.VespaClusterStatusTableModel.QUERY_COLUMN;
import static com.pehrs.vespa.yql.plugin.dock.VespaClusterStatusTableModel.USE_COLUMN;

import com.intellij.icons.AllIcons.Actions;
import com.intellij.icons.AllIcons.General;
import com.intellij.icons.AllIcons.Ide;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.panels.HorizontalBox;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI.Borders;
import com.pehrs.vespa.yql.plugin.VespaClusterChecker;
import com.pehrs.vespa.yql.plugin.VespaClusterChecker.StatusListener;
import com.pehrs.vespa.yql.plugin.YQL;
import com.pehrs.vespa.yql.plugin.YqlIcons;
import com.pehrs.vespa.yql.plugin.logserver.VespaLogsViewFactory;
import com.pehrs.vespa.yql.plugin.settings.VespaClusterConfig;
import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsState;
import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsStateListener;
import com.pehrs.vespa.yql.plugin.status.ClusterStatus;
import com.pehrs.vespa.yql.plugin.swing.TableColumnAdjuster;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YqlDockPanel extends JBPanel implements YqlAppSettingsStateListener {

  private static final Logger log = LoggerFactory.getLogger(YqlDockPanel.class);

  private final Project project;

  private JBTable clusterTable;
  private VespaClusterStatusTableModel clusterTableModel;
  private AnAction configAction;
  private AnAction openLogsConfig;
  private boolean openLogsConfigEnabled = true;


  public YqlDockPanel(Project project) {
    super(new BorderLayout());
    this.project = project;
    super.setBorder(Borders.empty());
    createComponents();
    YqlAppSettingsStateListener.addListener(this);
    refresh();
  }

  private void createComponents() {

    JBLabel label = new JBLabel();

    this.clusterTableModel = new VespaClusterStatusTableModel();
    this.clusterTable = new JBTable(clusterTableModel);
    this.clusterTable.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 1) {
          // Selected
          int selectedRow = clusterTable.getSelectedRow();
          int selectedColumn = clusterTable.getSelectedColumn();
          // log.info("CLICK in row: " + selectedRow +", column: " + selectedColumn);
          if (selectedColumn == USE_COLUMN) {
            YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
            VespaClusterConfig selectedConfig = settings.clusterConfigs.get(selectedRow);
            settings.currentConnection = selectedConfig.name;
            refresh();
          }
          if (selectedColumn == APP_NAME_COLUMN) {
            String appName = clusterTableModel.getValueAt(selectedRow,
                    VespaClusterStatusTableModel.APP_NAME_COLUMN)
                .toString();
            ClusterStatus.openInBrowser(project, appName);
            refresh();
          }
        }
        super.mouseClicked(e);
      }
    });
    this.clusterTable.setDefaultRenderer(String.class,
        (table, value, isSelected, hasFocus, row, column) -> {
          String txt = (String) value;
          switch (column) {
            case CLUSTER_COLUMN: {
              YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
              VespaClusterConfig config = settings.clusterConfigs.get(row);
              label.setText(txt);
              label.setToolTipText(String.format("%s - %s %s", config.name, config.queryEndpoint,
                  config.configEndpoint));
//          if (config.name.equals(settings.currentConnection)) {
//            label.setIcon(Diff.GutterCheckBoxSelected);
//          } else {
//            label.setIcon(Diff.GutterCheckBox);
//          }
              label.setIcon(null);
              break;
            }
            case USE_COLUMN: {
              if (txt.toLowerCase().equals("true")) {
                // label.setIcon(Diff.GutterCheckBoxSelected);
                label.setIcon(YqlIcons.RADIO_SELECTED);
              } else {
                // label.setIcon(Diff.GutterCheckBox);
                label.setIcon(YqlIcons.RADIO);
              }
              label.setText(null);
              break;
            }
            case QUERY_COLUMN, CONFIG_COLUMN: {
              label.setText(txt);
              label.setIcon(YqlIcons.STATUS_FAIL);
              switch (txt.toUpperCase()) {
                case "UP": {
                  label.setIcon(YqlIcons.STATUS_UP);
                  break;
                }
                case "DOWN": {
                  label.setIcon(YqlIcons.STATUS_DOWN);
                  break;
                }
                case "INITIALIZING": {
                  label.setIcon(General.InlineRefresh);
                  break;
                }
                default: {
                }
              }
              label.setToolTipText("" + txt);
              break;
            }
            case APP_NAME_COLUMN: {
              label.setText("" + value);
              label.setIcon(Ide.External_link_arrow);
              label.setToolTipText("Open ClusterController status page in browser");
              break;
            }
            default: {
              label.setText("" + value);
              label.setIcon(null);
              label.setToolTipText(null);
              break;
            }
          }
          return label;
        });

    clusterTableModel.addTableModelListener(event -> {
      // Only update columns when data changes as the order of events is HEADER then DATA.
      if (event.getFirstRow() != TableModelEvent.HEADER_ROW) {
        TableColumnAdjuster tca = new TableColumnAdjuster(clusterTable, 14);
        tca.setColumnDataIncluded(true);
        tca.setColumnMaxWidth(650);
        tca.adjustColumns();
      }
    });

    VespaClusterChecker.addStatusListener(new StatusListener() {
      @Override
      public void vespaClusterStatusUpdated() {
        log.debug("Vespa cluster status updated:\n" +
            "config: " + VespaClusterChecker.getConfigEndpointStatus() + "\n" +
            "query:  " + VespaClusterChecker.getQueryEndpointStatus());
        refresh();
      }
    });

    ToolbarDecorator decorator =
        ToolbarDecorator.createDecorator(clusterTable)
            .initPosition()
            .setToolbarPosition(ActionToolbarPosition.TOP)
            // .disableAddAction()
            .disableRemoveAction()
            .disableDownAction()
            .disableUpAction();

    this.configAction = new AnAction("Config", "Open configuration", General.Settings) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e) {
        if (e == null) {
          return;
        }
        ShowSettingsUtil.getInstance().showSettingsDialog(project, "Vespa YQL");
        // "com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsConfigurable");
      }
    };
    decorator.addExtraAction(configAction);

    AnAction reloadAction = new AnAction("Refresh Vespa Cluster Status", "...", Actions.Refresh) {
      public void actionPerformed(@NotNull AnActionEvent e) {
        if (e == null) {
          return;
        }
        ApplicationManager.getApplication().runReadAction(() -> {
          log.debug("Update Vespa Cluster info...");
          VespaClusterChecker.checkVespaClusters();
        });
      }
    };
    decorator.addExtraAction(reloadAction);
    this.openLogsConfig = new AnAction("Open Vespa Logs", "...", General.Warning) {

      @Override
      public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabledAndVisible(openLogsConfigEnabled);
      }

      public void actionPerformed(@NotNull AnActionEvent e) {
        if (e == null) {
          return;
        }
        ApplicationManager.getApplication().runReadAction(() -> {
          VespaLogsViewFactory.openLogs(project);
        });
      }
    };
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    this.openLogsConfigEnabled = settings.doMonitorLogs;
    decorator.addExtraAction(openLogsConfig);

    JPanel treePanel = decorator.createPanel();
    treePanel.setBorder(Borders.empty());

    PluginId pluginId = PluginId.findId("org.pehrs.vespa-yql-plugin");
    @Nullable IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(
        pluginId);

    JBLabel verLabel = new JBLabel(
        "<html><span style='font-size:10px'>"
            + plugin.getName() + " ver: " + plugin.getVersion()
            + "</span></html>"
    );
    verLabel.setToolTipText(YQL.getBuildTimestamp() + " " + YQL.getBuiltByUser());
    verLabel.setHorizontalAlignment(SwingConstants.RIGHT);

    HorizontalBox title = new HorizontalBox();
    title.add(new JBLabel("Vespa clusters"));
    title.add(verLabel);

    JPanel myMainPanel = FormBuilder.createFormBuilder()
        .addComponent(title)
        .addComponentFillVertically(treePanel, 0)
        .getPanel();

    myMainPanel.setBorder(Borders.empty(8));

    super.add(myMainPanel, BorderLayout.NORTH);
  }

  public void refresh() {
    // YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    // setVespaClusterConfigs(settings.clusterConfigs);
    this.clusterTableModel.vespaClusterStatusUpdated();
  }

  @Override
  public void stateChanged(YqlAppSettingsState instance) {
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    this.openLogsConfigEnabled = settings.doMonitorLogs;
    this.refresh();
  }
}

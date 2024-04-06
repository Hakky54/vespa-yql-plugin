package com.pehrs.vespa.yql.plugin.dock;

import com.intellij.icons.AllIcons;
import com.intellij.icons.AllIcons.Diff;
import com.intellij.icons.AllIcons.General;
import com.intellij.icons.AllIcons.RunConfigurations.TestState;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AnActionButton;
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
import com.pehrs.vespa.yql.plugin.settings.VespaClusterConfig;
import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsState;
import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsStateListener;
import com.pehrs.vespa.yql.plugin.swing.TableColumnAdjuster;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
//  private DefaultMutableTreeNode configRoot;
//  private DefaultTreeModel treeModel;
//  private DnDAwareTree configTree;

  private JBTable clusterTable;
  private VespaClusterStatusTableModel clusterTableModel;


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

//    this.configRoot = new DefaultMutableTreeNode();
//    this.configRoot.setUserObject("Vespa Clusters");
//    this.treeModel = new DefaultTreeModel(configRoot);
//    this.configTree = new DnDAwareTree(treeModel);
//
//    this.configTree.addMouseListener(new MouseAdapter() {
//      @Override
//      public void mouseClicked(MouseEvent e) {
//        if (e.getClickCount() >= 2) {
//          TreePath path = configTree.getLeadSelectionPath();
//          if (path == null) {
//            super.mouseClicked(e);
//            return;
//          }
//          DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
//          Object userObj = node.getUserObject();
//          if (userObj instanceof VespaClusterConfig config) {
//            // This is a double click :-)
//            YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
//            settings.currentConnection = config.name;
//            YqlAppSettingsStateListener.notifyListeners(settings);
//          }
//          super.mouseClicked(e);
//        }
////        if (e.getClickCount() == 1) {
////          // Selected
////          TreePath path = configTree.getLeadSelectionPath();
////          DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
////          Object userObj = node.getUserObject();
////          if (userObj instanceof VespaClusterConfig config) {
////            nameField.setText(config.name);
////            queryEndpointField.setText(config.queryEndpoint);
////            configEndpointField.setText(config.configEndpoint);
////          }
////        }
//      }
//    });
//
//    //
//    // Renderer
//    //
//    this.configTree.setCellRenderer((tree, value, selected, expanded, leaf, row, hasFocus) -> {
//      DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
//      if (node == configRoot) {
//        label.setText("Vespa Clusters");
//        label.setIcon(Nodes.ConfigFolder);
//      } else {
//        Object uo = node.getUserObject();
//        if (uo instanceof VespaClusterConfig config) {
//          label.setText(String.format("%s - %s", config.name, config.queryEndpoint));
//          YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
//          if (config.name.equals(settings.currentConnection)) {
//            // label.setIcon(Actions.Checked);
//            label.setIcon(Diff.GutterCheckBoxSelected);
//          } else {
//            // label.setIcon(YqlIcons.FILE);
//            // label.setIcon(null);
//            label.setIcon(Diff.GutterCheckBox);
//          }
//        } else {
//          label.setText("" + uo);
//          label.setIcon(null);
//        }
//      }
//      return label;
//    });

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
          if(selectedColumn == 1) {
            YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
            VespaClusterConfig selectedConfig = settings.clusterConfigs.get(selectedRow);
            settings.currentConnection = selectedConfig.name;
            refresh();
          }
        }
        super.mouseClicked(e);
      }
    });
    this.clusterTable.setDefaultRenderer(String.class, (table, value, isSelected, hasFocus, row, column) -> {
      String txt = (String) value;
      switch(column) {
        case 0: {
          YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
          VespaClusterConfig config = settings.clusterConfigs.get(row);
          label.setText(txt);
          label.setToolTipText(String.format("%s - %s %s", config.name, config.queryEndpoint, config.configEndpoint));
//          if (config.name.equals(settings.currentConnection)) {
//            label.setIcon(Diff.GutterCheckBoxSelected);
//          } else {
//            label.setIcon(Diff.GutterCheckBox);
//          }
          label.setIcon(null);
          break;
        }
        case 1: {
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
        case 2, 3: {
          label.setText(txt);
          label.setIcon(YqlIcons.STATUS_FAIL);
          switch(txt) {
            case VespaClusterChecker.STATUS_UP: {
              label.setIcon(YqlIcons.STATUS_UP);
              break;
            }
            case VespaClusterChecker.STATUS_DOWN: {
              label.setIcon(YqlIcons.STATUS_DOWN);
              break;
            }
            case VespaClusterChecker.STATUS_INITIALIZING: {
              label.setIcon(General.InlineRefresh);
              break;
            }
            default: {
            }
          }
          label.setToolTipText("" + txt);
          break;
        }
        default: {
          label.setText("?");
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
//    decorator.addExtraAction(
//        AnActionButton.fromAction(new DumbAwareAction("Add Connection", "", General.Add) {
//          public void actionPerformed(@NotNull AnActionEvent e) {
//            if (e == null) {
//              return;
//            }
//            if (project != null) {
//              YqlAddConnectionDialog dialog = new YqlAddConnectionDialog(project);
//              dialog.show();
//            }
//          }
//        }));
//    decorator.addExtraAction(
//        AnActionButton.fromAction(new DumbAwareAction("Delete Connection", "", General.Remove) {
//          public void actionPerformed(@NotNull AnActionEvent e) {
//            if (e == null) {
//              return;
//            }
//
//            DefaultMutableTreeNode selected =
//                (DefaultMutableTreeNode) configTree.getLeadSelectionPath().getLastPathComponent();
//            VespaClusterConfig config = (VespaClusterConfig) selected.getUserObject();
//
//            YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
//            settings.removeClusterConfig(config);
//            YqlAppSettingsStateListener.notifyListeners(settings);
//
//            System.out.println(
//                "Delete connection!!!" + configTree.getLeadSelectionPath().getLastPathComponent());
//          }
//        }));
    AnActionButton configAction =
        AnActionButton.fromAction(new DumbAwareAction("Configuration", "", General.Settings) {
      public void actionPerformed(@NotNull AnActionEvent e) {
        if (e == null) {
          return;
        }
        ShowSettingsUtil.getInstance().showSettingsDialog(project, "Vespa YQL");
        // "com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsConfigurable");
      }
    });
//    configAction.setShortcut(new CustomShortcutSet(
//        KeyStroke.getKeyStroke("control ENTER")
//    ));
    decorator.addExtraAction(configAction);
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

//  public void addClusterConfig(VespaClusterConfig config) {
//    this.configRoot.add(new DefaultMutableTreeNode(config, false));
//  }

//  public void setVespaClusterConfigs(List<VespaClusterConfig> configs) {
//    configRoot.removeAllChildren();
//    configs.stream().forEach(config -> addClusterConfig(config));
//    treeModel.nodeStructureChanged(configRoot);
//  }

  public void refresh() {
    // YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    // setVespaClusterConfigs(settings.clusterConfigs);
    this.clusterTableModel.vespaClusterStatusUpdated();
  }

  @Override
  public void stateChanged(YqlAppSettingsState instance) {
    this.refresh();
  }
}

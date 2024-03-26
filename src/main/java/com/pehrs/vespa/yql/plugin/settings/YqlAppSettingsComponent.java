package com.pehrs.vespa.yql.plugin.settings;

import com.intellij.icons.AllIcons.Actions;
import com.intellij.icons.AllIcons.General;
import com.intellij.ide.dnd.aware.DnDAwareTree;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI.Borders;
import com.pehrs.vespa.yql.plugin.YQL;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.StringReader;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import org.jdesktop.swingx.HorizontalLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class YqlAppSettingsComponent implements YqlAppSettingsStateListener {

  private final JPanel myMainPanel;
  private final JBTextField myZipkinEndpointText;
  private final JBTextField browserScriptText;

  private final JBSplitter splitPane;
  private final JBTextField nameField;
  private final JBTextField queryEndpointField;
  private final JBTextField configEndpointField;
  private DefaultMutableTreeNode configRoot;
  private DnDAwareTree configTree;

  private final boolean editable;
  private DefaultTreeModel treeModel;

  public YqlAppSettingsComponent(Project project, boolean editable) {
    this.editable = editable;

    createTree();

    this.nameField = new JBTextField();
    this.nameField.setEditable(editable);
    this.queryEndpointField = new JBTextField();
    this.queryEndpointField.setEditable(editable);
    this.configEndpointField = new JBTextField();
    this.configEndpointField.setEditable(editable);
    JPanel btnPanel = null;
    JButton setButton = null;
    JButton resetBtn = null;
    if (editable) {
      btnPanel = new JPanel(new HorizontalLayout(2));

      setButton = new JButton("Set Values");
      setButton.addActionListener(e -> {
        TreePath path = configTree.getLeadSelectionPath();
        if (path != null) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
          Object userObj = node.getUserObject();
          if (userObj instanceof VespaClusterConfig config) {
            config.name = nameField.getText();
            config.queryEndpoint = queryEndpointField.getText();
            config.configEndpoint = configEndpointField.getText();
            // Notify about changes...
            YqlAppSettingsStateListener.notifyListeners(YqlAppSettingsState.getInstance());
          }
        }
      });
      resetBtn = new JButton("Reset");
      resetBtn.addActionListener(e -> {
        TreePath path = configTree.getLeadSelectionPath();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObj = node.getUserObject();
        if (userObj instanceof VespaClusterConfig config) {
          nameField.setText(config.name);
          queryEndpointField.setText(config.queryEndpoint);
          configEndpointField.setText(config.configEndpoint);
        }
      });

      btnPanel.add(setButton);
      btnPanel.add(resetBtn);

    }

    FormBuilder builder = FormBuilder.createFormBuilder()
        .addLabeledComponent(new JBLabel("Name: "), nameField)
        .addLabeledComponent(new JBLabel("Query Endpoint: "), queryEndpointField)
        .addLabeledComponent(new JBLabel("Config endpoint: "), configEndpointField);
    // .addComponentFillVertically(new JPanel(), 0)
    if (editable) {
      builder = builder
          .addComponent(btnPanel);
    }
    JPanel configPanel = new JPanel(new BorderLayout());
    JPanel formPanel = builder.getPanel();
    // configPanel.setBorder(Borders.customLine(Color.GREEN));
    configPanel.add(formPanel, BorderLayout.NORTH);

    ToolbarDecorator decorator =
        ToolbarDecorator.createDecorator(configTree)
            .initPosition()
            .setToolbarPosition(ActionToolbarPosition.TOP)
            // .disableAddAction()
            .disableRemoveAction()
            .disableDownAction()
            .disableUpAction();
    decorator.addExtraAction(
        AnActionButton.fromAction(new DumbAwareAction("Add Connection", "", General.Add) {
          public void actionPerformed(@NotNull AnActionEvent e) {
            if (e == null) {
              return;
            }
            if (project != null) {
              YqlAddConnectionDialog dialog = new YqlAddConnectionDialog(project);
              dialog.show();
            }
          }
        }));
    decorator.addExtraAction(
        AnActionButton.fromAction(new DumbAwareAction("Delete Connection", "", General.Remove) {
          public void actionPerformed(@NotNull AnActionEvent e) {
            if (e == null) {
              return;
            }

            DefaultMutableTreeNode selected =
                (DefaultMutableTreeNode) configTree.getLeadSelectionPath().getLastPathComponent();
            VespaClusterConfig config = (VespaClusterConfig) selected.getUserObject();

            YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
            settings.removeClusterConfig(config);
            YqlAppSettingsStateListener.notifyListeners(settings);

            System.out.println(
                "Delete connection!!!" + configTree.getLeadSelectionPath().getLastPathComponent());
          }
        }));
    if (!editable) {
      decorator.addExtraAction(AnActionButton.fromAction(new DumbAwareAction("Configuration", "",
          General.Settings) {
        public void actionPerformed(@NotNull AnActionEvent e) {
          if (e == null) {
            return;
          }
          ShowSettingsUtil.getInstance().showSettingsDialog(project, "Vespa YQL");
          // "com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsConfigurable");
        }
      }));
    }
    JPanel panel = decorator.createPanel();
    panel.setBorder(Borders.empty());

    splitPane = new OnePixelSplitter(false, "test", 0.6F);
    splitPane.setFirstComponent(panel);
    splitPane.setSecondComponent(configPanel);
    splitPane.setProportion(0.3F);

    JBTabbedPane pane = new JBTabbedPane(1);
    pane.insertTab("Connections", null, splitPane, "Vespa Clusters", 0);

    myZipkinEndpointText = new JBTextField();
    myZipkinEndpointText.setEditable(editable);

    browserScriptText = new JBTextField();
    browserScriptText.setEditable(editable);

    refresh();

    PluginId pluginId = PluginId.findId("org.pehrs.vespa-yql-plugin");
    @Nullable IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(
        pluginId);

    JBLabel verLabel = new JBLabel(
        "<html><span style='font-size:10px'>"
            + plugin.getName() + " ver: " + plugin.getVersion() + " " + YQL.getBuildTimestamp()
            + "</span></html>"
    );
    verLabel.setHorizontalAlignment(SwingConstants.RIGHT);

    myMainPanel = FormBuilder.createFormBuilder()
        .addComponent(verLabel)
        .addLabeledComponent(new JBLabel("Zipkin endpoint: "), myZipkinEndpointText, 1, false)
        .addLabeledComponent(new JBLabel("Browser script: "), browserScriptText, 1, false)
        // .addComponentFillVertically(new JPanel(), 0)
        .addComponentFillVertically(pane, 0)
        .getPanel();

    myMainPanel.setBorder(Borders.empty(8));

    YqlAppSettingsStateListener.addListener(this);
  }


  private void createTree() {
    this.configRoot = new DefaultMutableTreeNode();
    configRoot.setUserObject("Vespa Clusters");
    this.treeModel = new DefaultTreeModel(configRoot);
    this.configTree = new DnDAwareTree(treeModel);

    JBLabel label = new JBLabel();

//    this.configTree.addTreeExpansionListener(new TreeExpansionListener() {
//      @Override
//      public void treeExpanded(TreeExpansionEvent event) {
//        TreePath path = event.getPath();
//        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
//        Object userObj = node.getUserObject();
//        if(userObj instanceof VespaClusterConfig config) {
//          // This is a double click :-)
//          YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
//          settings.currentConnection = config.name;
//          YqlAppSettingsStateListener.notifyListeners(settings);
//        }
//      }
//
//      @Override
//      public void treeCollapsed(TreeExpansionEvent event) {
//        // Ignore for now...
//      }
//    });

    this.configTree.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() >= 2) {
          TreePath path = configTree.getLeadSelectionPath();
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
          Object userObj = node.getUserObject();
          if (userObj instanceof VespaClusterConfig config) {
            // This is a double click :-)
            YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
            settings.currentConnection = config.name;
            YqlAppSettingsStateListener.notifyListeners(settings);
          }
          super.mouseClicked(e);
        }
        if (e.getClickCount() == 1) {
          // Selected
          TreePath path = configTree.getLeadSelectionPath();
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
          Object userObj = node.getUserObject();
          if (userObj instanceof VespaClusterConfig config) {
            nameField.setText(config.name);
            queryEndpointField.setText(config.queryEndpoint);
            configEndpointField.setText(config.configEndpoint);
          }
        }
      }
    });

    this.configTree.setCellRenderer(new TreeCellRenderer() {
      @Override
      public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
          boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object uo = node.getUserObject();
        if (uo instanceof VespaClusterConfig config) {
          label.setText(config.name);
          YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
          if (config.name.equals(settings.currentConnection)) {
            label.setIcon(Actions.Checked);
          } else {
            // label.setIcon(YqlIcons.FILE);
            label.setIcon(null);
          }
        } else {
          label.setText("" + uo);
          label.setIcon(null);
        }

        return label;
      }
    });
  }

  public JPanel getPanel() {
    return myMainPanel;
  }

  public JComponent getPreferredFocusedComponent() {
    return myZipkinEndpointText;
  }

  public void setVespaClusterConfigs(List<VespaClusterConfig> configs) {
    configRoot.removeAllChildren();
    configs.stream().forEach(config -> addClusterConfig(config));
    treeModel.nodeStructureChanged(configRoot);
  }

  public List<VespaClusterConfig> getVespaClusterConfigs() {
    return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(this.configRoot.children().asIterator(),
                Spliterator.ORDERED),
            false)
        .map(node -> {
          DefaultMutableTreeNode defNode = (DefaultMutableTreeNode) node;
          return (VespaClusterConfig) defNode.getUserObject();
        }).collect(Collectors.toList());
  }

  @NotNull
  public String getZipkinEndpoint() {
    return myZipkinEndpointText.getText();
  }

  public void setZipkinEndpoint(@NotNull String newText) {
    myZipkinEndpointText.setText(newText);
  }

  public String getBrowserScript() {
    return browserScriptText.getText();
  }

  public void setBrowserScript(String newValue) {
    browserScriptText.setText(newValue);
  }


  public void addClusterConfig(VespaClusterConfig config) {
    this.configRoot.add(new DefaultMutableTreeNode(config, false));
  }

//  public void removeClusterConfig(String queryEndpoint) {
//    Optional<MutableTreeNode> found = StreamSupport.stream(
//            Spliterators.spliteratorUnknownSize(this.configRoot.children().asIterator(),
//                Spliterator.ORDERED),
//            false)
//        .filter(node -> {
//          DefaultMutableTreeNode defNode = (DefaultMutableTreeNode) node;
//          VespaClusterConfig config = (VespaClusterConfig) defNode.getUserObject();
//          return queryEndpoint.equals(config.configEndpoint);
//        })
//        .map(n -> (MutableTreeNode) n)
//        .findFirst();
//    found.ifPresent(node -> this.configRoot.remove(node));
//  }

  public void refresh() {
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    setZipkinEndpoint(settings.zipkinEndpoint);
    setBrowserScript(settings.browserScript);
    setVespaClusterConfigs(settings.clusterConfigs);
  }

  @Override
  public void stateChanged(YqlAppSettingsState instance) {
    refresh();
  }
}

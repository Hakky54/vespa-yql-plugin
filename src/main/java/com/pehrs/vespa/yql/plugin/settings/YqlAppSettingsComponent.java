package com.pehrs.vespa.yql.plugin.settings;

import com.intellij.icons.AllIcons.Actions;
import com.intellij.icons.AllIcons.General;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI.Borders;
import com.pehrs.vespa.yql.plugin.YQL;
import com.pehrs.vespa.yql.plugin.util.NotificationUtils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YqlAppSettingsComponent implements YqlAppSettingsStateListener {

  private static final Logger log = LoggerFactory.getLogger(YqlAppSettingsComponent.class);

  private final Project project;

  private final JPanel myMainPanel;
  private final JBTextField myZipkinEndpointText;
  private final JBTextField mavenParametersText;
  private final JBTextField tenantText;

  private final JBCheckBox logsCheckBox;
  private final JBTextField logsPathText;

  private final JBLabel sslUseClientCertLabel;
  private final JBCheckBox sslUseClientCertCheckBox;
  private final JBLabel sslCaCertLabel;
  private final JBTextField sslCaCertText;
  private final JBLabel sslClientCertLabel;
  private final JBTextField sslClientCertText;
  private final JBLabel sslClientKeyLabel;
  private final JBTextField sslClientKeyText;

  private final JBCheckBox sslAllowAllCheckbox;

  private final JBSplitter splitPane;
  private final JBTextField nameField;
  private final JBTextField queryEndpointField;
  private final JBTextField configEndpointField;
  private String lastPath = null;

//  private DefaultMutableTreeNode configRoot;
//  private DnDAwareTree configTree;
//  private DefaultTreeModel treeModel;

  private List<VespaClusterConfig> clusterConfigs = new ArrayList<>();

  private final JBLabel connectionListLabel = new JBLabel();
  private final JBList connectionList;
  private ConnectionListModel connectionListModel;

  public YqlAppSettingsComponent(Project project) {

    this.project = project;

    // createTree();
    this.connectionList = createConnectionsList();

    sslAllowAllCheckbox = new JBCheckBox();
    sslAllowAllCheckbox.setSelected(false);
    sslAllowAllCheckbox.setToolTipText(
        "[WARNING] This will trust ANY https connection (even self-signed)!!!");
    sslAllowAllCheckbox.addActionListener(event -> {
      log.warn("event: " + event);
      if (this.sslAllowAllCheckbox.isSelected()) {
        log.warn(
            "Security WARNING: Trusting all connections will trust ANY https connection (even self-signed)!!!");

        Messages.showWarningDialog(project,
            "<html><h1>WARNING!</h1><p>Trusting all query connections will trust ANY https connection (even self-signed)!!!</p></html>",
            "Security WARNING");

        NotificationUtils.showNotification(project, NotificationType.WARNING,
            "Security WARNING: Trusting all connections will trust ANY https connection (even self-signed)!!!");
      }
    });

    this.nameField = new JBTextField();
    this.nameField.addKeyListener(new TextFieldAdapter(this.nameField,
        (vespaClusterConfig, value) -> {
          vespaClusterConfig.name = value;
        }));
    this.queryEndpointField = new JBTextField();
    this.queryEndpointField.addKeyListener(new TextFieldAdapter(this.queryEndpointField,
        (vespaClusterConfig, value) -> {
          vespaClusterConfig.queryEndpoint = value;
        }));
    this.configEndpointField = new JBTextField();
    this.configEndpointField.addKeyListener(new TextFieldAdapter(this.configEndpointField,
        (vespaClusterConfig, value) -> {
          vespaClusterConfig.configEndpoint = value;
        }));
    sslUseClientCertLabel = new JBLabel("Use client cert");
    sslUseClientCertLabel.setEnabled(true);
    sslUseClientCertCheckBox = new JBCheckBox();
    sslUseClientCertCheckBox.setEnabled(true);
    sslUseClientCertCheckBox.setSelected(false);
    sslUseClientCertCheckBox.setToolTipText("");
    sslUseClientCertCheckBox.addChangeListener(e -> useClientCertChanged());

    Dimension maxFieldDim = new Dimension(200, 12);

    sslCaCertLabel = new JBLabel("CA certificate");
    sslCaCertLabel.setEnabled(false);
    sslCaCertText = new JBTextField(10);
    sslCaCertText.setMaximumSize(maxFieldDim);
    sslCaCertText.setEnabled(false);
    sslCaCertText.addKeyListener(new TextFieldAdapter(this.sslCaCertText,
        (vespaClusterConfig, value) -> {
          vespaClusterConfig.sslCaCert = value;
        }));

    JBPanel caCertPanel = createFileChooserPanel(this.sslCaCertText,
        JFileChooser.FILES_ONLY,
        (path) -> {
          if (connectionList.getSelectedValue() instanceof VespaClusterConfig config) {
            config.sslCaCert = path;
          }
        });

    sslClientCertLabel = new JBLabel("Client certificate");
    sslClientCertLabel.setEnabled(false);
    sslClientCertText = new JBTextField(10);
    sslClientCertText.setMaximumSize(maxFieldDim);
    sslClientCertText.setEnabled(false);
    sslClientCertText.addKeyListener(new TextFieldAdapter(this.sslClientCertText,
        (vespaClusterConfig, value) -> {
          vespaClusterConfig.sslClientCert = value;
        }));
    JBPanel sslClientCertPanel = createFileChooserPanel(this.sslClientCertText,
        JFileChooser.FILES_ONLY,
        (path) -> {
          if (connectionList.getSelectedValue() instanceof VespaClusterConfig config) {
            config.sslClientCert = path;
          }
        });

    sslClientKeyLabel = new JBLabel("Client key");
    sslClientKeyLabel.setEnabled(false);
    sslClientKeyText = new JBTextField(10);
    sslClientKeyText.setMaximumSize(maxFieldDim);
    sslClientKeyText.setEnabled(false);
    sslClientKeyText.addKeyListener(new TextFieldAdapter(this.sslClientKeyText,
        (vespaClusterConfig, value) -> {
          vespaClusterConfig.sslClientKey = value;
        }));
    JBPanel sslClientKeyPanel = createFileChooserPanel(this.sslClientKeyText,
        JFileChooser.FILES_ONLY,
        (path) -> {
          if (connectionList.getSelectedValue() instanceof VespaClusterConfig config) {
            config.sslClientKey = path;
          }
        });

    FormBuilder builder = FormBuilder.createFormBuilder()
        .addLabeledComponent(new JBLabel("Name: "), nameField)
        .addLabeledComponent(new JBLabel("Query endpoint: "), queryEndpointField)
        .addLabeledComponent(new JBLabel("Config endpoint: "), configEndpointField)
        .addSeparator()
        .addComponent(new JBLabel("SSL/TLS"))
        .addLabeledComponent(this.sslUseClientCertLabel, sslUseClientCertCheckBox, false)
        .addLabeledComponent(this.sslCaCertLabel, caCertPanel, false)
        .addLabeledComponent(this.sslClientCertLabel, sslClientCertPanel, false)
        .addLabeledComponent(this.sslClientKeyLabel, sslClientKeyPanel, false);
    // .addComponentFillVertically(new JPanel(), 0)
    JPanel configPanel = new JPanel(new BorderLayout());
    JPanel formPanel = builder.getPanel();
    // configPanel.setBorder(Borders.customLine(Color.GREEN));
    configPanel.add(formPanel, BorderLayout.NORTH);

    ToolbarDecorator decorator =
        // ToolbarDecorator.createDecorator(configTree)
        ToolbarDecorator.createDecorator(connectionList)
            .initPosition()
            .setToolbarPosition(ActionToolbarPosition.TOP)
            // .disableAddAction()
            .disableRemoveAction()
            .disableDownAction()
            .disableUpAction();

    decorator.addExtraAction(new DumbAwareAction("Add Connection", "", General.Add) {
          public void actionPerformed(@NotNull AnActionEvent e) {
            if (e == null) {
              return;
            }
            if (project != null) {
              YqlAddConnectionDialog dialog = new YqlAddConnectionDialog(project);
              dialog.show();
            }
          }
        });
    decorator.addExtraAction(new DumbAwareAction("Delete Connection", "", General.Remove) {
          public void actionPerformed(@NotNull AnActionEvent e) {
            if (e == null) {
              return;
            }

            VespaClusterConfig config = (VespaClusterConfig) connectionList.getSelectedValue();

            if (config != null) {
              YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
              settings.removeClusterConfig(config);
              YqlAppSettingsStateListener.notifyListeners(settings);

              log.debug("Delete connection " + config.name);
            }
          }
        });

    JPanel panel = decorator.createPanel();
    panel.setBorder(Borders.empty());

    splitPane = new OnePixelSplitter(false, "test", 0.6F);
    splitPane.setFirstComponent(panel);
    splitPane.setSecondComponent(configPanel);
    splitPane.setProportion(0.3F);

    JBTabbedPane pane = new JBTabbedPane(1);
    pane.insertTab("Connections", null, splitPane, "Vespa Clusters", 0);

    myZipkinEndpointText = new JBTextField();

    mavenParametersText = new JBTextField();
    tenantText = new JBTextField();

    logsPathText = new JBTextField(10);
    JBPanel logsPathTextPanel = createFileChooserPanel(this.logsPathText,
        JFileChooser.FILES_AND_DIRECTORIES,
        (path) -> {
          logsPathText.setText(path);
        });

    logsCheckBox = new JBCheckBox(null);
    logsCheckBox.addActionListener((event) -> {
      logsPathText.setEnabled(logsCheckBox.isSelected());
    });

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
        .addLabeledComponent(new JBLabel("Vespa tenant:"), tenantText, 1, false)
        .addLabeledComponent(new JBLabel("Maven build parameters:"), mavenParametersText, 1, false)
        .addLabeledComponent(new JBLabel("Zipkin endpoint:"), myZipkinEndpointText, 1, false)
        .addLabeledComponent(new JBLabel("Monitor Logs:"), logsCheckBox, 1, false)
        // .addLabeledComponent(new JBLabel("Logs Path:"), logsPathText, 1, false)
        .addLabeledComponent(new JBLabel("Logs Path:"), logsPathTextPanel, 1, false)
        .addSeparator()
        .addComponent(new JBLabel("SSL/TLS"))
        .addLabeledComponent(new JBLabel("Trust all query TLS/SSL connections"),
            sslAllowAllCheckbox, false)
        .addSeparator()
        // .addComponentFillVertically(new JPanel(), 0)
        .addComponentFillVertically(pane, 0)
        .getPanel();

    myMainPanel.setBorder(Borders.empty(8));

    YqlAppSettingsStateListener.addListener(this);
  }

  public class TextFieldAdapter extends KeyAdapter {

    final JBTextField textField;
    private final BiConsumer<VespaClusterConfig, String> editFn;

    public TextFieldAdapter(JBTextField textField, BiConsumer<VespaClusterConfig, String> editFn) {
      this.textField = textField;
      this.editFn = editFn;
    }

    @Override
    public void keyReleased(KeyEvent e) {
      String value = textField.getText();
      if (connectionList.getSelectedValue() instanceof VespaClusterConfig config) {
        editFn.accept(config, value);
      }
      super.keyReleased(e);
    }
  }

  public class ConnectionListModel extends AbstractListModel {

    @Override
    public int getSize() {
      return clusterConfigs.size();
    }

    @Override
    public Object getElementAt(int index) {
      return clusterConfigs.get(index);
    }

    public void fireContentsChanged() {
      Object[] listeners = listenerList.getListenerList();
      ListDataEvent e = null;
      for (int i = listeners.length - 2; i >= 0; i -= 2) {
        if (listeners[i] == ListDataListener.class) {
          if (e == null) {
            e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0,
                clusterConfigs.size() - 1);
          }
          ((ListDataListener) listeners[i + 1]).contentsChanged(e);
        }
      }
    }
  }


  private JBList createConnectionsList() {

    JBList connList = new JBList();

    this.connectionListModel = new ConnectionListModel();
    connList.setModel(this.connectionListModel);

    connList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
      YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
      VespaClusterConfig config = (VespaClusterConfig) value;
      connectionListLabel.setEnabled(cellHasFocus);
      connectionListLabel.setBackground(isSelected ? JBColor.BLUE : JBColor.WHITE);
      connectionListLabel.setText(config.name);
      connectionListLabel.setIcon(config.name.equals(settings.currentConnection) ?
          Actions.Checked : null
      );
      return connectionListLabel;
    });

    connList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        if (connectionList.getSelectedValue() instanceof VespaClusterConfig config) {
          nameField.setText(config.name);
          queryEndpointField.setText(config.queryEndpoint);
          configEndpointField.setText(config.configEndpoint);
          sslUseClientCertCheckBox.setSelected(config.sslUseClientCert);
          sslCaCertText.setText(config.sslCaCert);
          sslClientCertText.setText(config.sslClientCert);
          sslClientKeyText.setText(config.sslClientKey);
          useClientCertChanged();
        }
      }
    });

    return connList;
  }

  public JPanel getPanel() {
    return myMainPanel;
  }

  public JComponent getPreferredFocusedComponent() {
    return myZipkinEndpointText;
  }

  public void setVespaClusterConfigs(List<VespaClusterConfig> configs) {
//    configRoot.removeAllChildren();
//    configs.stream().forEach(config -> addClusterConfig(config));
//    treeModel.nodeStructureChanged(configRoot);
    this.clusterConfigs = configs.stream()
        .map(cfg -> cfg.clone())
        .collect(Collectors.toList());
    this.connectionListModel.fireContentsChanged();
  }

  public List<VespaClusterConfig> getVespaClusterConfigs() {

    // Make sure the edited config is updated!
//    TreePath path = configTree.getLeadSelectionPath();
//    if (path != null) {
//      DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
//      Object userObj = treeNode.getUserObject();
//      if (userObj instanceof VespaClusterConfig config) {
//        YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
//        config.name = this.nameField.getText();
//        config.queryEndpoint = this.queryEndpointField.getText();
//        config.configEndpoint = this.configEndpointField.getText();
//        config.sslUseClientCert = this.sslUseClientCertCheckBox.isSelected();
//        config.sslCaCert = this.sslCaCertText.getText();
//        config.sslClientKey = this.sslClientKeyText.getText();
//        config.sslClientKey = this.sslClientKeyLabel.getText();
//      }
//    }
//
//    return StreamSupport.stream(
//            Spliterators.spliteratorUnknownSize(this.configRoot.children().asIterator(),
//                Spliterator.ORDERED),
//            false)
//        .map(node -> {
//          DefaultMutableTreeNode defNode = (DefaultMutableTreeNode) node;
//          return (VespaClusterConfig) defNode.getUserObject();
//        }).collect(Collectors.toList());

    return clusterConfigs;
  }

  @NotNull
  public String getZipkinEndpoint() {
    return myZipkinEndpointText.getText();
  }

  public void setZipkinEndpoint(@NotNull String newText) {
    myZipkinEndpointText.setText(newText);
  }

  @NotNull
  public String getMavenParameters() {
    return mavenParametersText.getText();
  }

  public void setMavenParameters(@NotNull String newText) {
    mavenParametersText.setText(newText);
  }

  @NotNull
  public String getLogsPath() {
    return logsPathText.getText();
  }

  public void setLogsPath(@NotNull String newText) {
    logsPathText.setText(newText);
  }

  @NotNull
  public boolean getMonitorLogs() {
    return this.logsCheckBox.isSelected();
  }

  public void setMonitorLogs(@NotNull Boolean checked) {
    this.logsCheckBox.setSelected(checked);
  }

  @NotNull
  public String getTenant() {
    return tenantText.getText();
  }

  public void setTenant(@NotNull String newText) {
    tenantText.setText(newText);
  }

  public boolean getSslAllowAll() {
    return this.sslAllowAllCheckbox.isSelected();
  }

  public void addClusterConfig(VespaClusterConfig config) {
    // this.configRoot.add(new DefaultMutableTreeNode(config, false));
    this.clusterConfigs.add(config);
    this.connectionListModel.fireContentsChanged();
  }

  public void refresh() {
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    setZipkinEndpoint(settings.zipkinEndpoint);
    setMavenParameters(settings.mavenParameters);
    setLogsPath(settings.logsPath);
    setMonitorLogs(settings.doMonitorLogs);
    this.logsPathText.setEnabled(settings.doMonitorLogs);
    setTenant(settings.tenant);
    sslAllowAllCheckbox.setSelected(settings.sslAllowAll);

    setVespaClusterConfigs(settings.clusterConfigs);
  }



  private void useClientCertChanged() {

    boolean sslUseClientCert = this.sslUseClientCertCheckBox.isSelected();

    if (connectionList.getSelectedValue() instanceof VespaClusterConfig config) {
      config.sslUseClientCert = sslUseClientCert;
      log.info("useClientCertChanged() sslUseClientCert[" + config.name + "]: " + config);
    }

    sslCaCertLabel.setEnabled(sslUseClientCert);
    sslCaCertText.setEnabled(sslUseClientCert);

    sslClientCertLabel.setEnabled(sslUseClientCert);
    sslClientCertText.setEnabled(sslUseClientCert);

    sslClientKeyLabel.setEnabled(sslUseClientCert);
    sslClientKeyText.setEnabled(sslUseClientCert);
  }


  @Override
  public void stateChanged(YqlAppSettingsState instance) {
    refresh();
  }

  private JBPanel createFileChooserPanel(JBTextField textField,
      int mode,
      Consumer<String> postSelectFn) {
    JBPanel panel = new JBPanel(new BorderLayout());
    panel.add(textField, BorderLayout.CENTER);
    JButton selectFileBtn = new JButton("...");
    selectFileBtn.addActionListener(event -> {
      String path = this.lastPath;
      if (path == null) {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects != null && projects.length > 0) {
          path = projects[0].getBasePath();
        }
      }
      JFileChooser fc = new JFileChooser(path);
      fc.setFileSelectionMode(mode);
      if (fc.showDialog(this.myMainPanel, "Select Dir/File")
          == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fc.getSelectedFile();
        this.lastPath = selectedFile.toPath().getParent().toFile().getAbsolutePath();
        String absPath = selectedFile.getAbsolutePath();
        textField.setText(selectedFile.getAbsolutePath());
        postSelectFn.accept(absPath);
      }
    });
    panel.add(selectFileBtn, BorderLayout.EAST);
    return panel;
  }
}

package com.pehrs.vespa.yql.plugin.settings;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.pehrs.vespa.yql.plugin.util.NotificationUtils;
import java.util.Optional;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YqlAddConnectionDialog  extends DialogWrapper {
  
  private static final Logger log = LoggerFactory.getLogger(YqlAddConnectionDialog.class);
  
  JPanel panel;
  private JBTextField nameField;
  private JBTextField queryEndpointField;
  private JBTextField configEndpointField;

  private final JBLabel sslUseClientCertLabel;
  private final JBCheckBox sslUseClientCertCheckBox;
  private final JBLabel sslCaCertLabel;
  private final JBTextField sslCaCertText;
  private final JBLabel sslClientCertLabel;
  private final JBTextField sslClientCertText;
  private final JBLabel sslClientKeyLabel;
  private final JBTextField sslClientKeyText;

  private Project project;

  public YqlAddConnectionDialog(@Nullable Project project) {
    super(project);
    this.project = project;

    sslUseClientCertLabel = new JBLabel("Use client cert");
    sslUseClientCertLabel.setEnabled(true);
    sslUseClientCertCheckBox = new JBCheckBox();
    sslUseClientCertCheckBox.setEnabled(true);
    sslUseClientCertCheckBox.setSelected(false);
    sslUseClientCertCheckBox.setToolTipText("");
    sslUseClientCertCheckBox.addChangeListener(e -> useClientCertChanged());

    sslCaCertLabel = new JBLabel("CA certificate");
    sslCaCertLabel.setEnabled(false);
    sslCaCertText = new JBTextField();
    sslCaCertText.setEnabled(false);

    sslClientCertLabel = new JBLabel("Client certificate");
    sslClientCertLabel.setEnabled(false);
    sslClientCertText = new JBTextField();
    sslClientCertText.setEnabled(false);

    sslClientKeyLabel = new JBLabel("Client key");
    sslClientKeyLabel.setEnabled(false);
    sslClientKeyText = new JBTextField();
    sslClientKeyText.setEnabled(false);

    createCenterPanel();
    this.init();
  }

  private void useClientCertChanged() {

    boolean sslUseClientCert = this.sslUseClientCertCheckBox.isSelected();

    log.info("useClientCertChanged() sslUseClientCert: "
        + sslUseClientCert);

    sslCaCertLabel.setEnabled(sslUseClientCert);
    sslCaCertText.setEnabled(sslUseClientCert);

    sslClientCertLabel.setEnabled(sslUseClientCert);
    sslClientCertText.setEnabled(sslUseClientCert);

    sslClientKeyLabel.setEnabled(sslUseClientCert);
    sslClientKeyText.setEnabled(sslUseClientCert);
  }

  @Override
  protected @Nullable ValidationInfo doValidate() {

    String name = this.nameField.getText();
    Optional<VespaClusterConfig> found = YqlAppSettingsState.getInstance()
        .getClusterConfig(name);
    if(found.isPresent()) {
      return new ValidationInfo("There is a configuration named " + name + " already!", this.nameField);
    }
    return null;
  }

  @Override
  protected void doOKAction() {
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    try {
      settings.addClusterConfig(
          new VespaClusterConfig(
              this.nameField.getText(),
              this.queryEndpointField.getText(),
              this.configEndpointField.getText(),
              this.sslUseClientCertCheckBox.isSelected(),
              this.sslCaCertText.getText(),
              this.sslClientCertText.getText(),
              this.sslClientKeyText.getText()
          )
      );
    } catch (IllegalArgumentException ex) {
      NotificationUtils.showException(project, ex);
    }
    YqlAppSettingsStateListener.notifyListeners(settings);

    super.doOKAction();
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    if(panel==null) {
      nameField = new JBTextField();
      nameField.setText("localhost");
      queryEndpointField = new JBTextField();
      queryEndpointField.setText("http://localhost:8080/search/");
      configEndpointField = new JBTextField();
      configEndpointField.setText("http://localhost:19071");

      panel = FormBuilder.createFormBuilder()
          .addLabeledComponent(new JBLabel("Name: "), nameField, 1, false)
          .addLabeledComponent(new JBLabel("Query Endpoint: "), queryEndpointField, 1, false)
          .addLabeledComponent(new JBLabel("Config endpoint: "), configEndpointField, 1, false)
          .addSeparator()
          .addComponent(new JBLabel("SSL"))
          .addLabeledComponent(this.sslUseClientCertLabel, sslUseClientCertCheckBox, false)
          .addLabeledComponent(this.sslCaCertLabel, sslCaCertText, false)
          .addLabeledComponent(this.sslClientCertLabel, sslClientCertText, false)
          .addLabeledComponent(this.sslClientKeyLabel, sslClientKeyText, false)
          // .addComponentFillVertically(new JPanel(), 0)
          .getPanel();
      // panel.setMinimumSize(new Dimension(800, 600));
    }
    return panel;
  }


}

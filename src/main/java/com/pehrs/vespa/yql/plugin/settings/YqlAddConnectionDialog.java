package com.pehrs.vespa.yql.plugin.settings;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import java.util.Optional;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.Nullable;

public class YqlAddConnectionDialog  extends DialogWrapper {
  JPanel panel;
  private JBTextField nameField;
  private JBTextField queryEndpointField;
  private JBTextField configEndpointField;

  private Project project;

  public YqlAddConnectionDialog(@Nullable Project project) {
    super(project);
    this.project = project;
    createCenterPanel();
    this.init();
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
          new VespaClusterConfig(this.nameField.getText(), this.queryEndpointField.getText(),
              this.configEndpointField.getText()));
    } catch (IllegalArgumentException ex) {
      NotificationGroup group = NotificationGroupManager.getInstance()
          .getNotificationGroup("Vespa YQL");
      group
          .createNotification(ex.getMessage(), NotificationType.ERROR)
          .notify(this.project);
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
          // .addComponentFillVertically(new JPanel(), 0)
          .getPanel();
      // panel.setMinimumSize(new Dimension(800, 600));
    }
    return panel;
  }


}

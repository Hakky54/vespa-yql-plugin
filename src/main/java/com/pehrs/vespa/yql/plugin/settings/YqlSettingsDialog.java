package com.pehrs.vespa.yql.plugin.settings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nullable;

public class YqlSettingsDialog  extends DialogWrapper {

  final YqlSettingsPanel panel;

  public YqlSettingsDialog(@Nullable Project project) {
    super(project);
    panel = new YqlSettingsPanel(project);
    this.init();
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    return panel;
  }
}

package com.pehrs.vespa.yql.plugin.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.ProjectManager;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

public class YqlColorsConfigurable implements Configurable {

  private YqlColorSettingsComponent mySettingsComponent;

  @Nls(capitalization = Nls.Capitalization.Title)
  @Override
  public String getDisplayName() {
    return "Vespa YQL Colors";
  }


  @Override
  public JComponent getPreferredFocusedComponent() {
    return mySettingsComponent.getPreferredFocusedComponent();
  }

  @Override
  public boolean isModified() {
    return false;
  }

  @Override
  public void reset() {
    // Ignore for now...
  }

  @Override
  public void apply() throws ConfigurationException {
    ApplicationManager.getApplication().saveSettings();
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    mySettingsComponent = new YqlColorSettingsComponent(ProjectManager.getInstance().getDefaultProject());
    return mySettingsComponent.getPanel();
  }
}

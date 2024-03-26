package com.pehrs.vespa.yql.plugin.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.ProjectManager;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

public class YqlAppSettingsConfigurable implements Configurable {

  private YqlAppSettingsComponent mySettingsComponent;

  @Nls(capitalization = Nls.Capitalization.Title)
  @Override
  public String getDisplayName() {
    return "Vespa YQL";
  }


  @Override
  public JComponent getPreferredFocusedComponent() {
    return mySettingsComponent.getPreferredFocusedComponent();
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    mySettingsComponent = new YqlAppSettingsComponent(ProjectManager.getInstance().getDefaultProject(), true);
    return mySettingsComponent.getPanel();
  }

  @Override
  public boolean isModified() {
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    boolean modified = !mySettingsComponent.getZipkinEndpoint().equals(settings.zipkinEndpoint);

    List<VespaClusterConfig> configs = mySettingsComponent.getVespaClusterConfigs();
    modified = modified || configs.size() != settings.clusterConfigs.size();
    Set<VespaClusterConfig> configSet = new HashSet<>(configs);
    Set<VespaClusterConfig> settingsConfigSet = new HashSet<>(settings.clusterConfigs);
    modified = modified || !configs.equals(settingsConfigSet);

    return modified;
  }

  @Override
  public void apply() {
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    settings.zipkinEndpoint = mySettingsComponent.getZipkinEndpoint();
    settings.browserScript = mySettingsComponent.getBrowserScript();
    settings.clusterConfigs = mySettingsComponent.getVespaClusterConfigs();
    YqlAppSettingsStateListener.notifyListeners(settings);
  }

  @Override
  public void reset() {
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    mySettingsComponent.setZipkinEndpoint(settings.zipkinEndpoint);
    mySettingsComponent.setBrowserScript(settings.browserScript);
    mySettingsComponent.setVespaClusterConfigs(settings.clusterConfigs);
  }

  @Override
  public void disposeUIResources() {
    mySettingsComponent = null;
  }

}

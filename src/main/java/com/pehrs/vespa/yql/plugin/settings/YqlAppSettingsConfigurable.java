package com.pehrs.vespa.yql.plugin.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.ProjectManager;
import com.pehrs.vespa.yql.plugin.logserver.VespaLogsWatcher;
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
    mySettingsComponent = new YqlAppSettingsComponent(ProjectManager.getInstance().getDefaultProject());
    return mySettingsComponent.getPanel();
  }

  @Override
  public boolean isModified() {
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    boolean modified = !mySettingsComponent.getZipkinEndpoint().equals(settings.zipkinEndpoint);
    modified = modified || !mySettingsComponent.getMavenParameters().equals(settings.mavenParameters);
    modified = modified || !mySettingsComponent.getTenant().equals(settings.tenant);
    modified = modified || !mySettingsComponent.getLogsPath().equals(settings.logsPath);
    modified = modified || (mySettingsComponent.getMonitorLogs()!= settings.doMonitorLogs);

    List<VespaClusterConfig> configs = mySettingsComponent.getVespaClusterConfigs();
    modified = modified || configs.size() != settings.clusterConfigs.size();
    Set<VespaClusterConfig> configSet = new HashSet<>(configs);
    Set<VespaClusterConfig> settingsConfigSet = new HashSet<>(settings.clusterConfigs);
    modified = modified || !configs.equals(settingsConfigSet);

    modified = modified || (mySettingsComponent.getSslAllowAll() != settings.sslAllowAll);

    return modified;
  }

  @Override
  public void apply() {
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    settings.zipkinEndpoint = mySettingsComponent.getZipkinEndpoint();
    settings.mavenParameters = mySettingsComponent.getMavenParameters();
    settings.logsPath = mySettingsComponent.getLogsPath();
    settings.doMonitorLogs = mySettingsComponent.getMonitorLogs();
    settings.tenant = mySettingsComponent.getTenant();
    settings.clusterConfigs = mySettingsComponent.getVespaClusterConfigs();
    settings.sslAllowAll = mySettingsComponent.getSslAllowAll();
    VespaLogsWatcher.forceReload();
    YqlAppSettingsStateListener.notifyListeners(settings);
  }

  @Override
  public void reset() {

    mySettingsComponent.refresh();
  }

  @Override
  public void disposeUIResources() {
    mySettingsComponent = null;
  }

}

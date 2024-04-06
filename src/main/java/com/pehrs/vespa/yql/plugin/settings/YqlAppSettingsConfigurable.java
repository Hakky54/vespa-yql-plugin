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
    mySettingsComponent = new YqlAppSettingsComponent(ProjectManager.getInstance().getDefaultProject());
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

    modified = modified || (mySettingsComponent.getSslAllowAll() != settings.sslAllowAll);
//    modified = modified || (mySettingsComponent.getSslUseClientCert() != settings.sslUseClientCert);
//    modified = modified || !mySettingsComponent.getSslCaCert().equals(settings.sslCaCert);
//    modified = modified || !mySettingsComponent.getSslClientCert().equals(settings.sslClientCert);
//    modified = modified || !mySettingsComponent.getSslClientKey().equals(settings.sslClientKey);

    return modified;
  }

  @Override
  public void apply() {
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    settings.zipkinEndpoint = mySettingsComponent.getZipkinEndpoint();
    settings.browserScript = mySettingsComponent.getBrowserScript();
    settings.clusterConfigs = mySettingsComponent.getVespaClusterConfigs();
    settings.sslAllowAll = mySettingsComponent.getSslAllowAll();
//    settings.sslUseClientCert = mySettingsComponent.getSslUseClientCert();
//    settings.sslCaCert = mySettingsComponent.getSslCaCert();
//    settings.sslClientCert = mySettingsComponent.getSslClientCert();
//    settings.sslClientKey = mySettingsComponent.getSslClientKey();
    YqlAppSettingsStateListener.notifyListeners(settings);
  }

  @Override
  public void reset() {
   //  YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
//    mySettingsComponent.setZipkinEndpoint(settings.zipkinEndpoint);
//    mySettingsComponent.setBrowserScript(settings.browserScript);
//    mySettingsComponent.setVespaClusterConfigs(settings.clusterConfigs);
    mySettingsComponent.refresh();
  }

  @Override
  public void disposeUIResources() {
    mySettingsComponent = null;
  }

}

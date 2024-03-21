package com.pehrs.vespa.yql.plugin.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.XCollection;
import com.intellij.util.xmlb.annotations.XCollection.Style;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = "com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsState",
    storages = @Storage("VespaYqlPlugin.xml")
)
public class YqlAppSettingsState implements PersistentStateComponent<YqlAppSettingsState> {


  @Tag("cluster-config")
  @XCollection(style = Style.v2)
  public List<VespaClusterConfig> clusterConfigs = new ArrayList<>();

  @Tag("zipkin-endpoint")
  public String zipkinEndpoint = "http://localhost:9411";

  @Tag("current-connection")
  public String currentConnection = null;

  public static YqlAppSettingsState getInstance() {
    return ApplicationManager.getApplication().getService(YqlAppSettingsState.class);
  }

  public String getCurrentQueryUrl() {
    return getClusterConfig(currentConnection)
        .map(config -> config.queryEndpoint)
        .orElseThrow(() -> new RuntimeException("Current connection '" + currentConnection + "' does not exist!"));
  }

  @Nullable
  @Override
  public YqlAppSettingsState getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull YqlAppSettingsState state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public Optional<VespaClusterConfig> getClusterConfig(String name) {
   return this.clusterConfigs.stream().filter(config -> config.name.equals(name))
        .findFirst();
  }

  public void addClusterConfig(VespaClusterConfig vespaClusterConfig) {
    Optional<VespaClusterConfig> found = this.getClusterConfig(vespaClusterConfig.name);
    if (found.isPresent()) {
      throw new IllegalArgumentException("Vespa Cluster " + vespaClusterConfig.name + " exists already!");
    } else {
      this.clusterConfigs.add(vespaClusterConfig);
    }
  }

  public void removeClusterConfig(VespaClusterConfig config) {
    getClusterConfig(config.name)
        .ifPresent(found -> {
          this.clusterConfigs.remove(found);
        });
  }
}

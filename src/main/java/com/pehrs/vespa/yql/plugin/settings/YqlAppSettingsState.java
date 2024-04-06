package com.pehrs.vespa.yql.plugin.settings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.ui.JBColor;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.XCollection;
import com.intellij.util.xmlb.annotations.XCollection.Style;
import com.pehrs.vespa.yql.plugin.YQL;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = "com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsState",
    storages = @Storage("VespaYqlPlugin.xml")
)
public class YqlAppSettingsState implements PersistentStateComponent<YqlAppSettingsState> {

  @Tag("cluster-config")
  @XCollection(style = Style.v2)
  public List<VespaClusterConfig> clusterConfigs = new ArrayList<>(
      List.of(
          new VespaClusterConfig(
              "localhost",
              "http://localhost:8080/search/",
              "http://localhost:19071",
              false,
              "",
              "",
              ""
          )
      )
  );

  @Tag("zipkin-endpoint")
  public String zipkinEndpoint = "http://localhost:9411";

  @Tag("browser-script")
  public String browserScript = YQL.getDefaultBrowserScript();

  @Tag("current-connection")
  public String currentConnection = "localhost";

  @Tag("ssl-allow-all")
  public boolean sslAllowAll = false;

  @Tag("overview-node-color-light")
  public String overviewNodeColorLight = "0x008800";
  @Tag("overview-node-color-dark")
  public String overviewNodeColorDark = "0x55ee55";

  @Tag("overview-service-color-light")
  public String overviewServiceColorLight = "0x000088";
  @Tag("overview-service-color-dark")
  public String overviewServiceColorDark = "0x5555ff";

  @Tag("overview-cluster-color-light")
  public String overviewClusterColorLight = "0x000000";
  @Tag("overview-cluster-color-dark")
  public String overviewClusterColorDark = "0x666666";

  @Tag("overview-network-color-light")
  public String overviewNetworkColorLight = "0x333333";
  @Tag("overview-network-color-dark")
  public String overviewNetworkColorDark = "0x888888";

  public static YqlAppSettingsState getInstance() {
    return ApplicationManager.getApplication().getService(YqlAppSettingsState.class);
  }

  public Color getOverviewNodeColorLight() {
    return new Color(Integer.decode(overviewNodeColorLight));
  }

  public Color getOverviewNodeColorDark() {
    return new Color(Integer.decode(overviewNodeColorDark));
  }

  public Color getOverviewServiceColorLight() {
    return new Color(Integer.decode(overviewServiceColorLight));
  }

  public Color getOverviewServiceColorDark() {
    return new Color(Integer.decode(overviewServiceColorDark));
  }

  public Color getOverviewClusterColorLight() {
    return new Color(Integer.decode(overviewClusterColorLight));
  }

  public Color getOverviewClusterColorDark() {
    return new Color(Integer.decode(overviewClusterColorDark));
  }

  public Color getOverviewNetworkColorLight() {
    return new Color(Integer.decode(overviewNetworkColorLight));
  }

  public Color getOverviewNetworkColorDark() {
    return new Color(Integer.decode(overviewNetworkColorDark));
  }

  public JBColor getNodeColor() {
    return new JBColor(Integer.decode(this.overviewNodeColorLight), Integer.decode(this.overviewNodeColorDark));
  }

  public JBColor getServiceColor() {
    return new JBColor(Integer.decode(this.overviewServiceColorLight), Integer.decode(this.overviewServiceColorDark));
  }

  public JBColor getClusterColor() {
    return new JBColor(Integer.decode(this.overviewClusterColorLight), Integer.decode(this.overviewClusterColorDark));
  }

  public JBColor getNetworkColor() {
    return new JBColor(Integer.decode(this.overviewNetworkColorLight), Integer.decode(this.overviewNetworkColorDark));
  }

  public String getCurrentQueryUrl() {
    return getClusterConfig(currentConnection)
        .map(config -> config.queryEndpoint)
        .orElseThrow(() -> new RuntimeException("Current connection '" + currentConnection + "' does not exist!"));
  }

  public String getCurrentConfigUrl() {
    return getClusterConfig(currentConnection)
        .map(config -> config.configEndpoint)
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

  public Optional<VespaClusterConfig> getCurrentClusterConfig() {
    return this.clusterConfigs.stream().filter(config -> config.name.equals(this.currentConnection))
        .findFirst();
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

  public void setOverviewNodeColorLight(Color color) {
    this.overviewNodeColorLight = String.format("0x%06x", color.getRGB() & 0xFFFFFF);
  }

  public void setOverviewNodeColorDark(Color color) {
    this.overviewNodeColorDark = String.format("0x%06x", color.getRGB() & 0xFFFFFF);
  }

  public void setOverviewServiceColorLight(Color color) {
    this.overviewServiceColorLight = String.format("0x%06x", color.getRGB() & 0xFFFFFF);
  }

  public void setOverviewServiceColorDark(Color color) {
    this.overviewServiceColorDark = String.format("0x%06x", color.getRGB() & 0xFFFFFF);
  }

  public void setOverviewClusterColorLight(Color color) {
    this.overviewClusterColorLight = String.format("0x%06x", color.getRGB() & 0xFFFFFF);
  }

  public void setOverviewClusterColorDark(Color color) {
    this.overviewClusterColorDark = String.format("0x%06x", color.getRGB() & 0xFFFFFF);
  }

  public void setOverviewNetworkColorLight(Color color) {
    this.overviewNetworkColorLight = String.format("0x%06x", color.getRGB() & 0xFFFFFF);
  }

  public void setOverviewNetworkColorDark(Color color) {
    this.overviewNetworkColorDark = String.format("0x%06x", color.getRGB() & 0xFFFFFF);
  }
}

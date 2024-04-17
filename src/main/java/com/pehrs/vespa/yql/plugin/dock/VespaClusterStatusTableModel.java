package com.pehrs.vespa.yql.plugin.dock;

import com.pehrs.vespa.yql.plugin.VespaClusterChecker;
import com.pehrs.vespa.yql.plugin.VespaClusterChecker.Status;
import com.pehrs.vespa.yql.plugin.settings.VespaClusterConfig;
import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsState;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.jetbrains.annotations.Nls;

public class VespaClusterStatusTableModel extends AbstractTableModel implements TableModel,
    VespaClusterChecker.StatusListener {

  private final String[] COLUMNS = {
      "Cluster",
      "App Name",
      "Generation",
      "Use",
      "Query",
      "Config"
  };

  public static final int CLUSTER_COLUMN = 0;
  public static final int APP_NAME_COLUMN = 1;
  public static final int GENERATION_COLUMN = 2;
  public static final int USE_COLUMN = 3;
  public static final int QUERY_COLUMN = 4;
  public static final int CONFIG_COLUMN = 5;

  @Override
  public void vespaClusterStatusUpdated() {
    fireTableStructureChanged();
    fireTableDataChanged();
  }

  @Override
  public int getRowCount() {
    List<VespaClusterConfig> clusterConfigs = YqlAppSettingsState.getInstance().clusterConfigs;
    return clusterConfigs.size();
  }

  @Override
  public int getColumnCount() {
    return COLUMNS.length;
  }

  @Nls
  @Override
  public String getColumnName(int columnIndex) {
    return COLUMNS[columnIndex];
  }


  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return String.class;
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    List<VespaClusterConfig> clusterConfigs = settings.clusterConfigs;

    VespaClusterConfig config = clusterConfigs.get(rowIndex);

    Map<VespaClusterConfig, Status> configStatus = VespaClusterChecker.getConfigEndpointStatus();
    Map<VespaClusterConfig, Status> queryStatus = VespaClusterChecker.getQueryEndpointStatus();
    Map<VespaClusterConfig, String> appName = VespaClusterChecker.getAppName();
    switch (columnIndex) {
      // case 0: return String.format("%s - %s", config.name, config.queryEndpoint);
      case CLUSTER_COLUMN: return config.name;
      case APP_NAME_COLUMN: return appName.getOrDefault(config, "-");
      case GENERATION_COLUMN: return VespaClusterChecker.getAppGeneration(config).orElse("-");
      case USE_COLUMN: return "" + config.name.equals(settings.currentConnection);
      case QUERY_COLUMN: return "" + queryStatus.getOrDefault(config,  Status.FAIL);
      case CONFIG_COLUMN: return "" + configStatus.getOrDefault(config,  Status.FAIL);
    }
    return null;
  }
}

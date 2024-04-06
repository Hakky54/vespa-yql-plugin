package com.pehrs.vespa.yql.plugin.dock;

import com.pehrs.vespa.yql.plugin.VespaClusterChecker;
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
      "Use",
      "Query",
      "Config"
  };

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

    Map<VespaClusterConfig, String> configStatus = VespaClusterChecker.getConfigEndpointStatus();
    Map<VespaClusterConfig, String> queryStatus = VespaClusterChecker.getQueryEndpointStatus();
    switch (columnIndex) {
      // case 0: return String.format("%s - %s", config.name, config.queryEndpoint);
      case 0: return config.name;
      case 1: return "" + config.name.equals(settings.currentConnection);
      case 2: return queryStatus.getOrDefault(config,  VespaClusterChecker.STATUS_FAIL);
      case 3: return configStatus.getOrDefault(config,  VespaClusterChecker.STATUS_FAIL);
    }
    return null;
  }
}

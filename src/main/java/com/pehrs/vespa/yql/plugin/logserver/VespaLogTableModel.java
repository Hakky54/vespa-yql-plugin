package com.pehrs.vespa.yql.plugin.logserver;

import com.pehrs.vespa.yql.plugin.logserver.LogRow.Column;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.jetbrains.annotations.Nls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VespaLogTableModel extends AbstractTableModel {

  private static final Logger log = LoggerFactory.getLogger(VespaLogTableModel.class);


  private List<LogRow> rows = new ArrayList<>();

  public LogRow getRow(int rowNum) {
    return rows.get(rowNum);
  }

  @Nls
  @Override
  public String getColumnName(int columnIndex) {
    return LogRow.Column.values()[columnIndex].name();
  }

  public Class<?> getColumnClass(int columnIndex) {
    return String.class;
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }

  @Override
  public int getRowCount() {
    return rows.size();
  }

  @Override
  public int getColumnCount() {
    return LogRow.Column.values().length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    Column col = LogRow.Column.values()[columnIndex];
    LogRow row = rows.get(rowIndex);
    return row.getColumnValue(col);
  }

  public void setRows(List<LogRow> newRows) {
    this.rows = newRows;
    try {
      fireTableStructureChanged();
      fireTableDataChanged();
    } catch (ArrayIndexOutOfBoundsException ex) {
      // FIXME: Ignore these for now
      //  not sure on why we get these. Some main-ui/bg thread issues perhaps?
    }
  }


}

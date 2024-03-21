package com.pehrs.vespa.yql.plugin.dock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pehrs.vespa.yql.plugin.YqlResult.YqlQueryError;
import com.pehrs.vespa.yql.plugin.YqlResult.YqlResultListener;
import com.pehrs.vespa.yql.plugin.YqlResult;
import com.pehrs.vespa.yql.plugin.YqlResult.YqlResultRow;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.jetbrains.annotations.Nls;

public class YqlResultTableModel extends AbstractTableModel implements TableModel,
    YqlResultListener {

  YqlResult result = null;
  List<YqlResultRow> rows = null;
  private List<String> columns = null;

  public YqlResultTableModel() {

  }

//  public YqlResultTableModel(String resultStr) {
//    try {
//      this.result = new YqlResult(resultStr);
//    } catch (JsonProcessingException e) {
//      throw new RuntimeException(e);
//    }
//    this.rows = result.getRows();
//    this.columns = result.getColumnNames();
//  }

  @Override
  public int getRowCount() {
    if (rows == null) {
      return 0;
    }
    return rows.size();
  }

  @Override
  public int getColumnCount() {
    if (columns == null) {
      return 0;
    }
    return columns.size();
  }

  @Nls
  @Override
  public String getColumnName(int columnIndex) {
    if (columns == null) {
      return null;
    }
    return columns.get(columnIndex);
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
    if (rows == null) {
      return null;
    }
    return "" + rows.get(rowIndex).fields().get(columns.get(columnIndex));
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    throw new RuntimeException("setValueAt() Not supported!");
  }

  public String toCsv() {
    return export(",");
  }

  public String toTsv() {
    return export("\t");
  }

  public String export(String delimiter) {
    return
        String.join(delimiter, columns) + "\n" +
        this.rows.stream()
        .map(row ->
            this.columns.stream().map(
                col -> "" + row.fields().get(col)).collect(Collectors.joining(delimiter)).replace("\n", "\\n")
        ).collect(Collectors.joining("\n"));
  }


  @Override
  public void resultUpdated(YqlResult result) {
    this.result = result;
    List<YqlQueryError> errors = result.getErrors();
    if (errors.isEmpty()) {
      this.columns = result.getColumnNames();
      this.rows = result.getRows();
    } else {
      this.columns = List.of("code", "summary", "message");
      this.rows = errors.stream().map(error -> new YqlResultRow(0.0d, Map.of(
          "code", error.code(),
          "summary", error.summary(),
          "message", error.message()
      ))).collect(Collectors.toList());
    }
    fireTableStructureChanged();
    fireTableDataChanged();
  }
}

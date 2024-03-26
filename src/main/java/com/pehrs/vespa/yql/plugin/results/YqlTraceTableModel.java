package com.pehrs.vespa.yql.plugin.results;

import com.pehrs.vespa.yql.plugin.YqlResult;
import com.pehrs.vespa.yql.plugin.YqlResult.YqlQueryError;
import com.pehrs.vespa.yql.plugin.YqlResult.YqlResultListener;
import com.pehrs.vespa.yql.plugin.traceorg.TraceRow;
import com.pehrs.vespa.yql.plugin.traceorg.TraceUtils;
import java.util.Date;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.jetbrains.annotations.Nls;

@Deprecated
public class YqlTraceTableModel extends AbstractTableModel implements TableModel,
    YqlResultListener {

  public static final String TIMESTAMP_COLUMN = "ts";
  public static final String RELATIVE_TIMESTAMP_COLUMN = "relative-ts";
  public static final String DOC_TYPE_COLUMN = "doc-type";
  public static final String DIST_KEY_COLUMN = "dist-key";
  public static final String MESSAGE_COLUMN = "message";

  private static List<String> COLUMNS = List.of(
      DOC_TYPE_COLUMN,
      DIST_KEY_COLUMN,
      TIMESTAMP_COLUMN,
      RELATIVE_TIMESTAMP_COLUMN,
      MESSAGE_COLUMN
  );

  private final static int DOC_TYPE_COLUMN_INDEX = 0;
  private final static int DIST_KEY_COLUMN_INDEX = 1;
  private final static int TIMESTAMP_COLUMN_INDEX = 2;
  private final static int RELATIVE_TIMESTAMP_COLUMN_INDEX = 3;
  private final static int MESSAGE_COLUMN_INDEX = 4;

  private static Object getColumnValue(TraceRow row, int columnIndex) {
    switch (columnIndex) {
      case DOC_TYPE_COLUMN_INDEX:
        return row.docType();
      case DIST_KEY_COLUMN_INDEX:
        return row.distKey();
      case TIMESTAMP_COLUMN_INDEX:
        return TraceUtils.sdf.format(new Date(row.tsNs() / 1_000_000L));
      case RELATIVE_TIMESTAMP_COLUMN_INDEX:
        return String.format("%.3fms", (row.relativeTsNs() / 1_000_000d));
      case MESSAGE_COLUMN_INDEX:
      default:
        return row.message();
    }
  }

  public static final String ERROR_CODE_COLUMN = "code";
  public static final String ERROR_SUMMARY_COLUMN = "summary";
  public static final String ERROR_MESSAGE_COLUMN = "message";

  private static List<String> ERROR_COLUMNS = List.of(
      ERROR_CODE_COLUMN, ERROR_SUMMARY_COLUMN, ERROR_MESSAGE_COLUMN
  );

  private final static int ERROR_CODE_COLUMN_INDEX = 0;
  private final static int ERROR_SUMMARY_COLUMN_INDEX = 1;
  private final static int ERROR_MESSAGE_COLUMN_INDEX = 2;

  private static Object getErrorColumn(YqlQueryError error, int columnIndex) {
    switch (columnIndex) {
      case ERROR_CODE_COLUMN_INDEX:
        return error.code();
      case ERROR_SUMMARY_COLUMN_INDEX:
        return error.message();
      case ERROR_MESSAGE_COLUMN_INDEX:
      default:
        return error.message();
    }
  }

  YqlResult result = null;
  List<TraceRow> rows = null;
  List<YqlQueryError> errorRows = null;
  private List<String> columns = null;


  public YqlTraceTableModel() {
  }

  @Override
  public int getRowCount() {
    if (errorRows != null) {
      return errorRows.size();
    }
    if (rows == null) {
      return 0;
    }
    return rows.size();
  }

  @Override
  public int getColumnCount() {
    if (errorRows != null) {
      return ERROR_COLUMNS.size();
    }
    if (columns == null) {
      return 0;
    }
    return columns.size();
  }

  @Nls
  @Override
  public String getColumnName(int columnIndex) {
    if (errorRows != null) {
      return ERROR_COLUMNS.get(columnIndex);
    }
    if (columns == null) {
      return null;
    }
    return COLUMNS.get(columnIndex);
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
    if (errorRows != null) {
      YqlQueryError row = errorRows.get(rowIndex);
      return getErrorColumn(row, columnIndex);
//      switch (columnIndex) {
//        case ERROR_CODE_COLUMN_INDEX:
//          return row.code();
//        case ERROR_SUMMARY_COLUMN_INDEX:
//          return row.message();
//        case ERROR_MESSAGE_COLUMN_INDEX:
//        default:
//          return row.message();
//      }
    }
    if (rows == null) {
      return null;
    }
    // TraceRow row = rows.get(rowIndex);
    return getColumnValue(rows.get(rowIndex), columnIndex);
//    switch (columnIndex) {
//      case DOC_TYPE_COLUMN_INDEX:
//        return row.docType();
//      case DIST_KEY_COLUMN_INDEX:
//        return row.distKey();
//      case TIMESTAMP_COLUMN_INDEX:
//        return TraceUtils.sdf.format(new Date(row.tsNs() / 1_000_000L));
//      case RELATIVE_TIMESTAMP_COLUMN_INDEX:
//        return String.format("%.3fms", (row.relativeTsNs() / 1_000_000d));
//      case MESSAGE_COLUMN_INDEX:
//      default:
//        return row.message();
//    }
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    throw new RuntimeException("setValueAt() Not supported!");
  }

  @Override
  public void resultUpdated(YqlResult result) {
    this.result = result;
    List<YqlQueryError> errors = result.getErrors();
    if (errors.isEmpty()) {
      this.columns = COLUMNS;
      this.rows = com.pehrs.vespa.yql.plugin.traceorg.TraceUtils.getTraceRows(result);
      this.errorRows = null;
      System.out.println("ROWS: " + this.rows);
    } else {
      this.columns = ERROR_COLUMNS;
      this.rows = null;
      this.errorRows = errors;
    }
    fireTableStructureChanged();
    fireTableDataChanged();
  }


}

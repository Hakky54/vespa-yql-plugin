package com.pehrs.vespa.yql.plugin.dock;

import com.fasterxml.jackson.databind.JsonNode;
import com.pehrs.vespa.yql.plugin.YqlResult;
import com.pehrs.vespa.yql.plugin.YqlResult.YqlQueryError;
import com.pehrs.vespa.yql.plugin.YqlResult.YqlResultListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.jetbrains.annotations.Nls;

public class YqlTraceTableModel extends AbstractTableModel implements TableModel,
    YqlResultListener {

  public static final String TIMESTAMP_COLUMN = "timestamp";
  public static final String MESSAGE_COLUMN = "message";
  public static final String ERROR_CODE_COLUMN = "code";
  public static final String ERROR_SUMMARY_COLUMN = "summary";
  public static final String ERROR_MESSAGE_COLUMN = "message";

  public record TraceRow(Long timestamp, String message) {}

  YqlResult result = null;
  List<TraceRow> rows = null;
  List<YqlQueryError> errorRows = null;
  private List<String> columns = null;

  private static List<String> COLUMNS = List.of(
      TIMESTAMP_COLUMN, MESSAGE_COLUMN
  );

  private final static int TIMESTAMP_COLUMN_INDEX = 0;
  private final static int MESSAGE_COLUMN_INDEX = 1;

  private static List<String> ERROR_COLUMNS = List.of(
      ERROR_CODE_COLUMN, ERROR_SUMMARY_COLUMN, ERROR_MESSAGE_COLUMN
  );

  private final static int ERROR_CODE_COLUMN_INDEX = 0;
  private final static int ERROR_SUMMARY_COLUMN_INDEX = 1;
  private final static int ERROR_MESSAGE_COLUMN_INDEX = 2;


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
      switch (columnIndex) {
        case ERROR_CODE_COLUMN_INDEX:
          return row.code();
        case ERROR_SUMMARY_COLUMN_INDEX:
          return row.message();
        case ERROR_MESSAGE_COLUMN_INDEX:
        default:
          return row.message();
      }
    }
    if (rows == null) {
      return null;
    }
    TraceRow row = rows.get(rowIndex);
    switch (columnIndex) {
      case TIMESTAMP_COLUMN_INDEX:
        return row.timestamp();
      case MESSAGE_COLUMN_INDEX:
      default:
        return row.message();
    }
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
      this.columns = List.of(TIMESTAMP_COLUMN, MESSAGE_COLUMN);
      this.rows = getTraceRows(result);
      this.errorRows = null;
      System.out.println("ROWS: " + this.rows);
    } else {
      this.columns = List.of("code", "summary", "message");
      this.rows = null;
      this.errorRows = errors;
    }
    fireTableStructureChanged();
    fireTableDataChanged();
  }


  private List<TraceRow> getTraceRows(JsonNode node) {

    JsonNode children = node.get("children");
    if (children != null) {
      List<JsonNode> childList = StreamSupport.stream(
              Spliterators.spliteratorUnknownSize(children.iterator(), Spliterator.ORDERED),
              false)
          .collect(Collectors.toList());
      return childList
          .stream()
          .flatMap(child -> {
            Long ts = Optional.ofNullable(child.get("timestamp"))
                .map(tsNode -> tsNode.asLong())
                .orElse(0L);
            JsonNode messageNode = child.get("message");
            String message = "";
            if (messageNode != null && messageNode.isTextual()) {
              message = messageNode.asText();
            } else {
              // FIXME; Handle the TRACE message structure here
              message = "<DOC_TYPE_TRACE>";
            }
            List<TraceRow> result = new ArrayList<>();
            TraceRow row = new TraceRow(ts, message);
            result.add(row);
            result.addAll(getTraceRows(child));
            return result.stream();
          })
          .collect(Collectors.toList());
    } else {
      return List.of();
    }
  }

  private List<TraceRow> getTraceRows(YqlResult result) {

    return result.getTrace()
        .map(trace -> getTraceRows(trace))
        .orElse(List.of());


  }
}

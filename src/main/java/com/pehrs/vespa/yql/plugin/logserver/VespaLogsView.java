package com.pehrs.vespa.yql.plugin.logserver;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.ui.JBColor;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.panels.HorizontalBox;
import com.intellij.ui.table.TableView;
import com.intellij.util.Time;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.JBUI.Borders;
import com.intellij.util.ui.ListTableModel;
import com.pehrs.vespa.yql.plugin.logserver.LogRow.Column;
import com.pehrs.vespa.yql.plugin.logserver.VespaLogsWatcher.VespaLogsWatcherListener;
import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsState;
import com.pehrs.vespa.yql.plugin.util.NotificationUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VespaLogsView extends JPanel implements VespaLogsWatcherListener {

  private static final Logger log = LoggerFactory.getLogger(VespaLogsView.class);

  private final Project project;
  private final ListTableModel<LogRow> tableModel;
  private TableView<LogRow> tableView;

  private final JBLabel viewTitle;

  private static VespaLogRowRenderer logRowRenderer = new VespaLogRowRenderer();


  private static ColumnInfo<LogRow, String> timestampColumn = new ColumnInfo<LogRow, String>(
      "Timestamp") {
    @Override
    public @Nullable String valueOf(LogRow logRow) {
      return logRow.timestamp();
    }
  };
  private static ColumnInfo<LogRow, String> levelColumn = new ColumnInfo<LogRow, String>("Level") {
    @Override
    public @Nullable String valueOf(LogRow logRow) {
      return logRow.level();
    }

    public @Nullable TableCellRenderer getRenderer(LogRow item) {
      return logRowRenderer;
    }
  };
  private static ColumnInfo<LogRow, String> messageColumn = new ColumnInfo<LogRow, String>(
      "Message") {
    @Override
    public @Nullable String valueOf(LogRow logRow) {
      return logRow.message();
    }
  };
  private static ColumnInfo<LogRow, String> serviceColumn = new ColumnInfo<LogRow, String>(
      "Service") {
    @Override
    public @Nullable String valueOf(LogRow logRow) {
      return logRow.service();
    }
  };
  private static ColumnInfo<LogRow, String> componentColumn = new ColumnInfo<LogRow, String>(
      "Component") {
    @Override
    public @Nullable String valueOf(LogRow logRow) {
      return logRow.component();
    }
  };
  private static ColumnInfo<LogRow, String> hostColumn = new ColumnInfo<LogRow, String>("Host") {
    @Override
    public @Nullable String valueOf(LogRow logRow) {
      return logRow.host();
    }
  };
  private static ColumnInfo<LogRow, String> pidColumn = new ColumnInfo<LogRow, String>("Pid") {
    @Override
    public @Nullable String valueOf(LogRow logRow) {
      return logRow.pid();
    }
  };

  public VespaLogsView(Project project) {
    super(new BorderLayout());
    this.project = project;
    this.tableModel = new ListTableModel<>(
        timestampColumn,
        levelColumn,
        messageColumn,
        serviceColumn,
        componentColumn,
        hostColumn,
        pidColumn
    );
    this.tableView = new TableView<>(tableModel);
    this.tableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    this.tableView.setBorder(Borders.empty());
    this.tableView.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() > 1) {
          // Double click
          viewSelectedTableRow();
        }
      }
    });
    JTableHeader logTableHeader = this.tableView.getTableHeader();
    logTableHeader.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent event) {
        Point point = event.getPoint();
        if (point != null) {
          int columnIndex = logTableHeader.columnAtPoint(point);
          if (columnIndex != -1) {
            Column column = LogRow.Column.values()[columnIndex];
            if (event.getClickCount() > 1) {
              columnHeaderDoubleClicked(column);
            }
          }
          super.mouseClicked(event);
        }
      }
    });

    ToolbarDecorator decorator =
        ToolbarDecorator.createDecorator(this.tableView)
            .initPosition()
            .setToolbarPosition(ActionToolbarPosition.TOP);
    decorator.disableAddAction();
    decorator.disableDownAction();
    decorator.disableUpAction();
    decorator.disableRemoveAction();
//    AnAction action = new AnAction(() -> viewTitle, Actions.Refresh) {
//      @Override
//      public void actionPerformed(@NotNull AnActionEvent e) {
//
//      }
//    };
//    decorator.addExtraAction(action);
    VespaLogsWatcher.addListener(this);

    HorizontalBox titleBox = new HorizontalBox();
    this.viewTitle = new JBLabel("Vespa logs...");
    titleBox.add(viewTitle);

    super.add(titleBox, BorderLayout.NORTH);
    super.add(decorator.createPanel(), BorderLayout.CENTER);
    try {
      refresh();
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  }


  private void columnHeaderDoubleClicked(Column column) {
    log.debug("Column " + column + " dbl-clicked!");
    // Not yet :-)
//    switch (column) {
//      case Timestamp -> {
//      }
//      case Message -> {
//        DialogBuilder builder = new DialogBuilder();
//        JBTextField tf = new JBTextField(".*");
//        builder.centerPanel(tf);
//        builder.addOkAction();
//        builder.addCancelAction();
//        // builder.showModal(true);
//        if (builder.showAndGet()) {
//          log.info("new filter:" + tf.getText());
//        }
//      }
//      case Pid, Host, Component, Service, Level -> {
//        JOptionPane.showMessageDialog(this.tableView,
//            "Column header " + column + " is double clicked!");
//      }
//    }
  }

  private void viewSelectedTableRow() {
    int selectedRow = this.tableView.getSelectedRow();
    if (selectedRow != -1) {
      LogRow logRow = this.tableView.getRow(selectedRow);
      String msg = "selected row " + selectedRow + ": " + logRow;
      log.info(msg);
      // NotificationUtils.showNotification(project, NotificationType.INFORMATION, "selected row " + selectedRow + ": " +logRow);

      DialogBuilder builder = new DialogBuilder();
      // FIXME: Use an editor instead of label to enable the user to copy past the log message data
      JBLabel msgLabel = new JBLabel(
          "<html>"
              + "<style>"
              //  + "table {padding: 0px; cellpadding: 0px; cellspacing: 0px; border-collapse: separate;}"
              + "th, td {text-align: left; margin: 0px; border: 1px solid #888; }"
              + "</style>"
              + "<h1>Log Message</h1>"
              + "<div style='width: 450px'>"
              + "<table cellpadding=0 cellspacing=0>"
              + String.format(
              "<tr><th style='padding: 2px;'>Timesamp:</th><td style='padding: 2px;'>%s</td></tr>",
              logRow.timestamp())
              + String.format(
              "<tr><th style='padding: 2px;'>Host:</th><td style='padding: 2px;'>%s</td></tr>",
              logRow.host())
              + String.format(
              "<tr><th style='padding: 2px;'>Pid:</th><td style='padding: 2px;'>%s</td></tr>",
              logRow.pid())
              + String.format(
              "<tr><th style='padding: 2px;'>Level:</th><td  style='padding: 2px;' title='%s'>%s</td></tr>",
              getLevelTooltip(logRow.level()), logRow.level())
              + String.format(
              "<tr><th style='padding: 2px;'>Service:</th><td style='padding: 2px;'>%s</td></tr>",
              logRow.service())
              + String.format(
              "<tr><th style='padding: 2px;'>Component:</th><td style='padding: 2px;'>%s</td></tr>",
              logRow.component())
              + String.format(
              "<tr><th style='padding: 2px;'>Message:</th><td style='padding: 2px;'>%s</td></tr>",
              logRow.message())
              + "</table>"
              + "</div>"
              + "</html>"
      );
      builder.centerPanel(msgLabel);
      builder.addOkAction();
      builder.showModal(true);
    }
  }

  private void scrollToLastLogRow() {
    this.tableView.scrollRectToVisible(
        this.tableView.getCellRect(this.tableView.getRowCount() - 1, 0, true));
  }

  public void refresh() throws IOException {
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    Path logFile = LogRow.getLogToRead(Path.of(settings.logsPath));

    setViewTitle(logFile);

    String content = Files.readString(logFile, Charset.defaultCharset());
    List<LogRow> rows = LogRow.parseLogLines(
        content);
    this.tableModel.setItems(rows);
    resizeColumnWidth(this.tableView);
    scrollToLastLogRow();
  }

  private void setViewTitle(Path logFile) {
    String absPath = logFile.toFile().getAbsolutePath();
    this.viewTitle.setToolTipText(absPath);
    if (absPath.contains(String.format("/logarchive"))) {
      String[] parts = absPath.split("/");
      this.viewTitle.setText(String.format("Log file: %s-%s-%s %s",
          parts[parts.length - 4],
          parts[parts.length - 3],
          parts[parts.length - 2],
          parts[parts.length - 1]
      ));
    } else {
      this.viewTitle.setText("Log file: " + logFile.toFile().getName());
    }
  }

  @Override
  public void logUpdated(Path path) {
    try {
      if (path != null) {
        if (path.toFile().getName().endsWith(".gz")) {
          log.debug("Ignoring changes to the .gz log files for now...");
          return;
        }
        if (path.toFile().isFile()) {
          setViewTitle(path);

          String content = Files.readString(path, Charset.defaultCharset());
          List<LogRow> rows = LogRow.parseLogLines(
              content);
          this.tableModel.setItems(rows);
          resizeColumnWidth(this.tableView);
          scrollToLastLogRow();
        }
      }
    } catch (Exception ex) {
      log.error("Error reading logs from " + path, ex);
      ApplicationManager.getApplication().invokeLaterOnWriteThread(() -> {
        NotificationUtils.showNotification(this.project, NotificationType.ERROR, ex.getMessage());
      });
    }
  }


  public static void resizeColumnWidth(JTable table) {
    final TableColumnModel columnModel = table.getColumnModel();
    int columnMargin = 10;
    for (int column = 0; column < table.getColumnCount(); column++) {
      // Account for header size
      double width = table.getTableHeader().getHeaderRect(column).getWidth();
      for (int row = 0; row < table.getRowCount(); row++) {
        try {
          TableCellRenderer renderer = table.getCellRenderer(row, column);
          Component comp = table.prepareRenderer(renderer, row, column);
          // log.debug("width:" +comp.getPreferredSize().width + " text:"+((JBLabel)(comp)).getText() );
          width = Math.max(comp.getPreferredSize().width + columnMargin, width);
        } catch (Exception ex) {
          // FIXME: We are ignoring these as they will come when
          //   initializing the JTable and model for some reason
          log.warn("resizeColumnWidth(): could not render column " + column + " width");
        }
      }
      // FIXME: This is a workaround for the strange behaviour of JTable vs ColumnModel and TableModel
      switch (LogRow.Column.values()[column]) {
        case Timestamp -> {
          if (width > 340) {
            width = 340;
          }
        }
        case Level -> {
          if (width > 80) {
            width = 80;
          }
        }
        case Service -> {
          if (width > 240) {
            width = 240;
          }
        }
        case Pid -> {
          if (width > 140) {
            width = 140;
          }
        }
        default -> {
          if (width > 640) {
            width = 640;
          }
        }
      }

      try {
        columnModel.getColumn(column).setPreferredWidth((int) width);
      } catch (ArrayIndexOutOfBoundsException ex) {
        // FIXME: Just ignore these for now
        //  we do not know why we get them!
        //  Some main-ui/bg thread issues perhaps?
        log.warn("Exception trying to set preferred with on column", ex);
      }
    }
  }


  private static String getLevelTooltip(String value) {
    switch (value.toLowerCase()) {
      case "fatal":
        return "Fatal error messages. The application must exit immediately, and restarting it will not help";
      case "error":
        return "Error messages. These are serious, the application cannot function correctly";
      case "warning":
        return "Warnings - the application may be able to continue, but the situation should be looked into";
      case "info":
        return "Informational messages that are not reporting error conditions, but should still be useful to the operator";
      case "config":
        return "Configuration settings";
      case "event":
        return "Machine-readable events. May contain information about processes starting and stopping, and various metrics";
      case "debug":
        return "Debug messages - normally suppressed";
      case "spam":
        return "Low-level debug messages, normally suppressed. Generates massive amounts of logs when enabled";
      default:
        return "";
    }
  }

  public static class VespaLogRowRenderer implements TableCellRenderer {

    static JBLabel label = new JBLabel();
    static Color defaultColor = label.getForeground();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column) {
      // label.setText("<html>" + value + "</html>");
      // label.setMaximumSize(new Dimension(650, 200));
      label.setText("" + value);
      label.setIcon(null);

      if (column == LogRow.Column.Level.ordinal()) {
        String levelStr = (String) value;
        label.setToolTipText(((getLevelTooltip(levelStr))));
        label.setForeground(getLevelColor(levelStr));
      } else {
        label.setToolTipText("");
        label.setForeground(defaultColor);
      }
      return label;
    }


    private Color getLevelColor(String value) {
      switch (value.toLowerCase()) {
        case "fatal":
        case "error":
          return JBColor.red;
        case "warning":
          return JBColor.orange;
        case "info":
          return JBColor.green;
        case "debug":
          return JBColor.gray;
        case "event":
        case "config":
        case "spam":
        default:
          return defaultColor;
      }
    }
  }
}

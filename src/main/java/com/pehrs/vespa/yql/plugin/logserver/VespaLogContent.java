package com.pehrs.vespa.yql.plugin.logserver;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.ui.JBColor;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.panels.HorizontalBox;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI.Borders;
import com.pehrs.vespa.yql.plugin.logserver.LogRow.Column;
import com.pehrs.vespa.yql.plugin.logserver.VespaLogsWatcher.VespaLogsWatcherListener;
import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsState;
import com.pehrs.vespa.yql.plugin.util.NotificationUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Please use VespaLogsView instead. This one has bugs...
@Deprecated
public class VespaLogContent implements VespaLogsWatcherListener, TableModelListener {

  private static final Logger log = LoggerFactory.getLogger(VespaLogContent.class);

  private final Project project;
  @Getter
  private final JPanel component;
//  private final JBTextField hostFilterText;
//  private final JBTextField serviceFilterText;
  private final JBLabel filterTitle;
  private VespaLogsWatcher logsWatcher;
  private final JBTable logTable;

  private VespaLogTableModel logTableModel;
  // private final DefaultTableColumnModel columnModel;


  public VespaLogContent(Project project) {
    this.project = project;

    this.logTableModel = new VespaLogTableModel();
    // this.columnModel = new DefaultTableColumnModel();
//    for(Column col :VespaLogTableModel.Column.values()) {
//    }
//    for(int i=0;i< logTableModel.getColumnCount();i++) {
//      TableColumn column = new TableColumn(i);
//      column.setCellRenderer(new VespaLogTableRenderer());
//      columnModel.addColumn(column);
//    }
    // this.logTable = new JBTable(logTableModel, columnModel);
    this.logTable = new JBTable(logTableModel);
    this.logTable.setShowColumns(true);
    JTableHeader logTableHeader = logTable.getTableHeader();
    logTableHeader.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent event) {
        Point point = event.getPoint();
        int columnIndex = logTableHeader.columnAtPoint(point);
        if(columnIndex != -1) {
          Column column = LogRow.Column.values()[columnIndex];
          switch (column) {
            case Timestamp -> {
            }
            case Message -> {
              DialogBuilder builder = new DialogBuilder();
              JBTextField tf = new JBTextField(".*");
              builder.centerPanel(tf);
              builder.addOkAction();
              builder.addCancelAction();
              // builder.showModal(true);
              if(builder.showAndGet()) {
                log.info("new filter:" + tf.getText());
              }
            }
            case Pid, Host, Component, Service, Level -> {
              JOptionPane.showMessageDialog(logTable, "Column header " + column + " is clicked");
            }
          }

        }
        super.mouseClicked(event);
      }
    });
    logTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    logTable.setBorder(Borders.empty());
    //logTable.getColumnModel().getColumn(0).setMinWidth(400);
    //logTable.getColumnModel().getColumn(0).setMaxWidth(650);
//    logTableModel.addTableModelListener(event -> {
//      // Only update columns when data changes as the order of events is HEADER then DATA.
//      if (event.getFirstRow() != TableModelEvent.HEADER_ROW) {
//        TableColumnAdjuster tca = new TableColumnAdjuster(logTable, 3);
//        tca.setColumnDataIncluded(true);
//        tca.setColumnMaxWidth(650);
//        tca.adjustColumns();
//      }
//    });
//    JBLabel label = new JBLabel();
//    this.logTable.setDefaultRenderer(String.class, new TableCellRenderer() {
//      @Override
//      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
//          boolean hasFocus, int row, int column) {
//        label.setText("" + value);
//        return label;
//      }
//    });
    this.logTable.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() > 1) {
          // Double click
          int selectedRow = logTable.getSelectedRow();
          if (selectedRow != -1) {
            LogRow logRow = logTableModel.getRow(selectedRow);
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
        super.mouseClicked(e);
      }
    });
    this.logTableModel.addTableModelListener(this);
    this.logTable.setDefaultRenderer(String.class, new VespaLogTableRenderer());

    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();

    // FIXME: we should monitor the $VESPA_HOME/logs/vespa/logarchive dir if it exist
    // this.dir = Path.of("/home/matti/src/intellij/vespa-yql-plugin/vespa-cluster/docker/logs");
    if (settings.logsPath == null || settings.logsPath.trim().length() == 0) {
      ApplicationManager.getApplication().invokeLaterOnWriteThread(() -> {
        String msg = "Logs dir is not configured. Cannot monitor logs!!! Please set the 'Logs dir' in the Vespa-YQL settings";
        NotificationUtils.showNotification(this.project, NotificationType.ERROR, msg);
      });
    }
    VespaLogsWatcher.setLogsPath(Path.of(settings.logsPath));

    JBPanel filterRow = new JBPanel(new BorderLayout());
    HorizontalBox filterBox = new HorizontalBox();
    this.filterTitle = new JBLabel("Filter  ");
    filterBox.add(filterTitle);
    // columns:
    // time host pid service component level message

    Dimension minSize = new Dimension(220, 12);

//    this.hostFilterText = new JBTextField("   ");
//    this.hostFilterText.setMinimumSize(minSize);
//    this.hostFilterText.addKeyListener(new KeyAdapter() {
//      @Override
//      public void keyReleased(KeyEvent e) {
//        refreshLogs();
//        super.keyPressed(e);
//      }
//    });
//    JBComboBoxLabel hostLabel = new JBComboBoxLabel();
//    hostLabel.setText("Host:");
//    hostLabel.setIcon(null);
//    filterBox.add(hostLabel);
//    filterBox.add(hostFilterText);
//    filterBox.add(new JBLabel("  "));
//
//    this.serviceFilterText = new JBTextField("   ");
//    this.serviceFilterText.setMinimumSize(minSize);
//    this.serviceFilterText.addKeyListener(new KeyAdapter() {
//      @Override
//      public void keyReleased(KeyEvent e) {
//        refreshLogs();
//        super.keyPressed(e);
//      }
//    });
//    JBComboBoxLabel serviceLabel = new JBComboBoxLabel();
//    serviceLabel.setText("Service:");
//    serviceLabel.setIcon(null);
//    filterBox.add(serviceLabel);
//    filterBox.add(serviceFilterText);
//    filterBox.add(new JBLabel("  "));

    filterRow.add(filterBox, BorderLayout.WEST);

    ToolbarDecorator decorator =
        ToolbarDecorator.createDecorator(logTable)
            .initPosition()
            .setToolbarPosition(ActionToolbarPosition.TOP);

    this.component = FormBuilder.createFormBuilder()
        .addComponent(filterRow)
        .addComponentFillVertically(decorator.createPanel(), 0)
        .getPanel();

    try {
      VespaLogsWatcher.addListener(this);
      VespaLogsWatcher.start();
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
    refreshLogs();
  }

  public void refreshLogs() {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      VespaLogsWatcher.refreshAll();
    });
  }

  @Override
  public void logUpdated(Path path) {
    try {
      if(path != null) {
        if(path.toFile().getName().endsWith(".gz")) {
          log.debug("Ignoring changes to the .gz log files for now...");
          return;
        }
        if (path.toFile().isFile()) {

          YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
          if(settings.islogsPathLogarchivePath()) {
            String[] parts = path.toFile().getAbsolutePath().split("/");
            this.filterTitle.setText(String.format("Log file: %s-%s-%s %s",
                parts[parts.length - 4],
                parts[parts.length - 3],
                parts[parts.length - 2],
                parts[parts.length - 1]
            ));
          } else {
            this.filterTitle.setText("Log file: " + path.toFile().getName());
          }
          this.filterTitle.setToolTipText(path.toFile().getAbsolutePath());

          String content = Files.readString(path, Charset.defaultCharset());
          List<LogRow> rows = LogRow.parseLogLines(
              content);
          this.logTableModel.setRows(rows);
        } else {
          log.error("FIXME: We should figure out which file to read here from " + path);
        }
      }
    } catch (Exception ex) {
      log.error("Error reading logs from " + path, ex);
      ApplicationManager.getApplication().invokeLaterOnWriteThread(() -> {
        NotificationUtils.showNotification(this.project, NotificationType.ERROR, ex.getMessage());
      });
    }
  }

  @Override
  public void tableChanged(TableModelEvent e) {
    ApplicationManager.getApplication().invokeLater(() -> {

      resizeColumnWidth(this.logTable);

      // Scroll to last line
      this.logTable.scrollRectToVisible(
          this.logTable.getCellRect(this.logTable.getRowCount() - 1, 0, true));
    });
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


  public static class VespaLogTableRenderer implements TableCellRenderer {

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
}

package com.pehrs.vespa.yql.plugin.dock;

import com.intellij.icons.AllIcons.Actions;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI.Borders;
import com.pehrs.vespa.yql.plugin.YqlResult;
import com.pehrs.vespa.yql.plugin.swing.TableColumnAdjuster;
import java.awt.BorderLayout;
import java.io.File;
import java.io.PrintWriter;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class YqlResultsTablePanel extends JBPanel {

  private final Project project;

  public YqlResultsTablePanel(Project project) {
    super(new BorderLayout());
    this.project = project;
    super.setBorder(Borders.empty());
    createComponents();
  }

  private void createComponents() {
    JBTable resultsTable = new JBTable();
    resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    resultsTable.setBorder(Borders.empty());

    YqlResultTableModel tableModel = new YqlResultTableModel();
    YqlResult.addResultListener(tableModel);
    resultsTable.setModel(tableModel);

    tableModel.addTableModelListener(event -> {
      // Only update columns when data changes as the order of events is HEADER then DATA.
      if (event.getFirstRow() != TableModelEvent.HEADER_ROW) {
        TableColumnAdjuster tca = new TableColumnAdjuster(resultsTable, 3);
        tca.setColumnDataIncluded(true);
        tca.setColumnMaxWidth(650);
        tca.adjustColumns();
      }
    });

    ToolbarDecorator decorator =
        ToolbarDecorator.createDecorator(resultsTable)
            .initPosition()
            .setToolbarPosition(ActionToolbarPosition.TOP);

    decorator.addExtraAction(
        AnActionButton.fromAction(
            new DumbAwareAction("Export as CSV", "Export table as CSV", Actions.Download) {
              public void actionPerformed(@NotNull AnActionEvent e) {
                if (e == null) {
                  return;
                }
                FileSaverDescriptor descriptor = new FileSaverDescriptor(
                    "Save CSV", "Save Result to CSV file", "csv"
                );
                @NotNull FileSaverDialog dialog = FileChooserFactory.getInstance()
                    .createSaveFileDialog(descriptor, project);

                @Nullable VirtualFileWrapper res = dialog.save(project.getWorkspaceFile(), "vespa-yql-result");
                if(res != null) {
                  File file = res.getFile();
                  try(PrintWriter out = new PrintWriter(file)) {
                    out.print(tableModel.toCsv());
                    out.flush();
                  } catch (Exception ex) {
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("Vespa YQL")
                        .createNotification(ex.getMessage(), NotificationType.ERROR)
                        .notify(project);
                  }
                }
              }
            }));

    decorator.addExtraAction(
        AnActionButton.fromAction(
            new DumbAwareAction("Export as TSV", "Export table as TSV", Actions.Download) {
              public void actionPerformed(@NotNull AnActionEvent e) {
                if (e == null) {
                  return;
                }
                FileSaverDescriptor descriptor = new FileSaverDescriptor(
                    "Save CSV", "Save Result to TSV file", "tsv"
                );
                @NotNull FileSaverDialog dialog = FileChooserFactory.getInstance()
                    .createSaveFileDialog(descriptor, project);

                @Nullable VirtualFileWrapper res = dialog.save(project.getWorkspaceFile(), "vespa-yql-result");
                if(res != null) {
                  File file = res.getFile();
                  try(PrintWriter out = new PrintWriter(file)) {
                    out.print(tableModel.toCsv());
                    out.flush();
                  } catch (Exception ex) {
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("Vespa YQL")
                        .createNotification(ex.getMessage(), NotificationType.ERROR)
                        .notify(project);
                  }
                }
              }
            }));

    JPanel panel = decorator.createPanel();
    panel.setBorder(Borders.empty());

    super.add(panel, BorderLayout.CENTER);
  }
}

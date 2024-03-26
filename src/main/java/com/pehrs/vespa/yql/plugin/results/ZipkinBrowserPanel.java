package com.pehrs.vespa.yql.plugin.results;

import com.intellij.icons.AllIcons.Ide;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.util.ui.JBUI.Borders;
import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsState;
import com.pehrs.vespa.yql.plugin.trace.TraceUtils;
import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ZipkinBrowserPanel extends JBPanel {

  private final Project project;
  private final JBCefBrowser zipkinBrowser;
  private String traceId;

  public ZipkinBrowserPanel(Project project) {
    super(new BorderLayout());
    this.project = project;
    super.setBorder(Borders.empty());
    this.zipkinBrowser = new JBCefBrowser();
    // this.zipkinBrowser = null;
    createComponents();
  }

  private void createComponents() {
    this.setBorder(Borders.empty());

    AnAction openInBrowserAction = new DumbAwareAction("Open in Browser",
        "Open Zipkin trace in a browser", Ide.External_link_arrow) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e) {
        // Below code gets Intellij into trouble and makes the Debugger hang in the build process...
//        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
//          try {
//            Desktop.getDesktop().browse(ZipkinUtils.getUri(traceId));
//          } catch (IOException | URISyntaxException ex) {
//            throw new RuntimeException(ex);
//          }
//        }

        // Workaround is to run xdg-open ...
        try {
          YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
          URI uri = TraceUtils.getZipkinUri(traceId);
          String cmd = String.format("%s %s", settings.browserScript, uri.toString());
          Process p = Runtime.getRuntime().exec(cmd);
        } catch (IOException | URISyntaxException ex) {
          NotificationGroupManager.getInstance()
              .getNotificationGroup("Vespa YQL")
              .createNotification("Could not open browser: " + ex.getMessage(), NotificationType.ERROR)
              .notify(project);
        }
      }
    };


    @NotNull ActionGroup actions = new ActionGroup() {
      @Override
      public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        return new AnAction[]{openInBrowserAction};
      }
    };

    ActionToolbarImpl toolbar = new ActionToolbarImpl(ActionPlaces.TOOLBAR, actions, true);
    toolbar.setEnabled(false);

    @NotNull JComponent browserComponent = zipkinBrowser.getComponent();
    toolbar.setTargetComponent(browserComponent);
    super.add(toolbar, BorderLayout.NORTH);
    super.add(browserComponent, BorderLayout.CENTER);
  }

  public JBCefBrowser getBrowser() {
    return this.zipkinBrowser;
  }

  public void loadURL(String traceId) {
    this.traceId = traceId;
    try {
      @NotNull URI uri = TraceUtils.getZipkinUri(traceId);
      this.zipkinBrowser.loadURL(uri.toString());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}

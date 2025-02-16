package com.pehrs.vespa.yql.plugin.status;

import com.intellij.icons.AllIcons.Ide;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbar;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.util.ui.JBUI.Borders;
import java.awt.BorderLayout;
import java.awt.Color;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClusterStatusBrowserPanel extends JBPanel {

  private final Project project;
  private final JBCefBrowser browser;

  Color nodeColor = new JBColor(0x008800, 0x55ee55);

  public ClusterStatusBrowserPanel(Project project) {
    super(new BorderLayout());
    this.project = project;
    // super.setBorder(Borders.empty());
    super.setBorder(Borders.customLine(nodeColor));
    this.browser = new JBCefBrowser();
    createComponents();
  }

  private void createComponents() {
    this.setBorder(Borders.empty());

    AnAction openInBrowserAction = new DumbAwareAction("Open in Browser",
        "Open Cluster Status page in a browser", Ide.External_link_arrow) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e) {
        ClusterStatus.openInBrowser(project);
      }
    };

    @NotNull ActionGroup actions = new ActionGroup() {
      @Override
      public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        return new AnAction[]{openInBrowserAction};
      }
    };

    @NotNull JComponent browserComponent = browser.getComponent();

    FloatingToolbar toolbar = new FloatingToolbar(browserComponent, actions, browser);
    toolbar.setEnabled(true);

    toolbar.setTargetComponent(browserComponent);
    super.add(toolbar, BorderLayout.NORTH);
    super.add(browserComponent, BorderLayout.CENTER);

    setVisible(true);
  }

  public void loadClusterStatus() {
    try {
      URI uri = new URI(ClusterStatus.getClusterStatusUrl(project));
      this.browser.loadURL(uri.toString());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}

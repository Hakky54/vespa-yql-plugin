package com.pehrs.vespa.yql.plugin.graph;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.pehrs.vespa.yql.plugin.YQL;
import com.pehrs.vespa.yql.plugin.graph.VespaServicesPanel.Theme;
import com.pehrs.vespa.yql.plugin.xml.VespaServicesXml;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public class VespaServicesDockFactory implements ToolWindowFactory, DumbAware {

  private Project project;
  private static VespaServicesPanel servicesPanel;


  @Override
  public void init(@NotNull ToolWindow toolWindow) {
    // Hide on start...

  }

  @Override
  public boolean isApplicable(@NotNull Project project) {
    return project != null;
  }

  public static void setServicesXml(VespaServicesXml xml) {
    if(servicesPanel != null) {
      servicesPanel.setServicesXml(xml);
    }
  }

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    this.project = project;

    // FIXME: Remove the resource and read the real config from the server!
    // XmlMapper xmlMapper = new XmlMapper();
    // String xml = YQL.getResource("/services-multiple-groups.xml");
    // String xml = YQL.getResource("/services-single-node.xml");
    //     try {
    // VespaServicesXml services = xmlMapper.readValue(xml, VespaServicesXml.class);
    servicesPanel = new VespaServicesPanel(null);
    ContentManager contentManager = toolWindow.getContentManager();
    Content content = contentManager.getFactory().createContent(servicesPanel, null, false);
    contentManager.addContent(content);
    //    } catch (IOException e) {
    //      throw new RuntimeException(e);
    //    }

    // Hide on startup
    toolWindow.setAutoHide(false);
    toolWindow.hide();
  }
}

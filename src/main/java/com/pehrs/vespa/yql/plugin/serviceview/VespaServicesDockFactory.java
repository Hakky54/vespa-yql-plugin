package com.pehrs.vespa.yql.plugin.serviceview;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.pehrs.vespa.yql.plugin.YQL;
import com.pehrs.vespa.yql.plugin.YqlIcons;
import com.pehrs.vespa.yql.plugin.xml.VespaServicesXml;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VespaServicesDockFactory implements ToolWindowFactory, DumbAware {

  private static final Logger log = LoggerFactory.getLogger(VespaServicesDockFactory.class);

  public static final String TOOL_WINDOW_ID = "Vespa - Services/Node Overview";

  private static VespaServicesXml servicesXml = null;

  private Project project;
  private static VespaServicesPanel servicesPanel;

  private static VespaServicesDockFactory factory = new VespaServicesDockFactory();

  @Override
  public void init(@NotNull ToolWindow toolWindow) {
    // Hide on start...

  }

  @Override
  public boolean isApplicable(@NotNull Project project) {
    return project != null;
  }

  public static void setServicesXml(VespaServicesXml xml) {
    servicesXml = xml;
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
    servicesPanel = new VespaServicesPanel(servicesXml);
    ContentManager contentManager = toolWindow.getContentManager();
    Content content = contentManager.getFactory().createContent(servicesPanel, "Service Overview", false);
    contentManager.addContent(content);
    contentManager.setSelectedContent(content);
    //    } catch (IOException e) {
    //      throw new RuntimeException(e);
    //    }
  }

  public static void openServiceXml(Project project) {

    ToolWindow toolWindow = YQL.getVespaToolWindow(project, factory);
    if (toolWindow != null) {
      final ToolWindow win = toolWindow;
      @NotNull ContentManager contentManager = win.getContentManager();
      // factory.createToolWindowContent(project, win);
      getServiceXmlContent(contentManager.getContents())
          .ifPresentOrElse(contentManager::setSelectedContent,
              () -> {
                factory.createToolWindowContent(project, win);
              });
      win.activate(() -> {
        win.show(() -> {
          log.debug("show window");
        });
      });
    }

//    ToolWindowManager mgr = ToolWindowManager.getInstance(project);
//    ToolWindow window = mgr.getToolWindow(TOOL_WINDOW_ID);
//    if(window == null) {
//      log.warn("No tool window, let's try to create it :-)");
//
//      VespaServicesDockFactory factory = new VespaServicesDockFactory();
//
//      window = mgr.registerToolWindow(new RegisterToolWindowTask(
//              TOOL_WINDOW_ID,
//              ToolWindowAnchor.BOTTOM,
//              null,
//              false,
//              true,
//              false,
//              true,
//              factory,
//              YqlIcons.FILE,
//              () -> TOOL_WINDOW_ID
//          )
//      );
//    }
//    if (window != null) {
//      final ToolWindow win = window;
//      win.activate(() -> {
//        win.show(() -> {
//          log.debug("show window");
//        });
//      });
//    }
  }


  private static Optional<Content> getServiceXmlContent(Content[] contents) {
    for (Content content : contents) {
      if (content.getComponent() == servicesPanel) {
        return Optional.of(content);
      }
    }
    return Optional.empty();
  }
}

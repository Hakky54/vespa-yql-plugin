package com.pehrs.vespa.yql.plugin.graph;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI.Borders;
import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsState;
import com.pehrs.vespa.yql.plugin.xml.ContainerXml;
import com.pehrs.vespa.yql.plugin.xml.ContentXml;
import com.pehrs.vespa.yql.plugin.xml.DisributionXml;
import com.pehrs.vespa.yql.plugin.xml.GroupXml;
import com.pehrs.vespa.yql.plugin.xml.NodeXml;
import com.pehrs.vespa.yql.plugin.xml.VespaServicesXml;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import lombok.Setter;

public class VespaServicesPanel extends JComponent {

  @Setter
  VespaServicesXml servicesXml;
  private JBLabel label;

  int margin = 8;

  @Setter
  Color nodeColor = new JBColor(0x008800, 0x55ee55);

  @Setter
  Color serviceColor = new JBColor(0x000088, 0x5555ff);

  @Setter
  Color clusterColor = new JBColor(0x000000, 0x666666);

  @Setter
  Color networkColor = new JBColor(0x333333, 0x888888);

  public enum Theme {
    SYSTEM, DARK, LIGHT
  }

  Theme theme = Theme.SYSTEM;

  public record NodePlacement(int x, int y, int width, int height) {

  }

  public void setTheme(Theme theme) {
    this.theme = theme;
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    switch(this.theme) {
      case DARK -> {
        this.nodeColor = settings.getOverviewNodeColorDark();
        this.serviceColor = settings.getOverviewServiceColorDark();
        this.clusterColor = settings.getOverviewClusterColorDark();
        this.networkColor = settings.getNetworkColor();
      }
      case LIGHT -> {
        this.nodeColor = settings.getOverviewNodeColorLight();
        this.serviceColor = settings.getOverviewServiceColorLight();
        this.clusterColor = settings.getOverviewClusterColorLight();
        this.networkColor = settings.getOverviewNetworkColorLight();
      }
    }
  }


  private void setServicesXmlColors(YqlAppSettingsState settings) {
    this.setNodeColor(settings.getNodeColor());
    this.setServiceColor(settings.getServiceColor());
    this.setClusterColor(settings.getClusterColor());
    this.setNetworkColor(settings.getNetworkColor());
  }

  Map<String, NodePlacement> containerNodePlacement = new HashMap<>();
  Map<String, NodePlacement> contentGroupPlacement = new HashMap<>();
  Map<String, NodePlacement> contentNodePlacement = new HashMap<>();

  public VespaServicesPanel(VespaServicesXml servicesXml) {
    this.servicesXml = servicesXml;
    super.setBorder(Borders.empty());
    createComponents();
    setServicesXmlColors(YqlAppSettingsState.getInstance());
  }


  private void createComponents() {

    this.label = new JBLabel("content");
    this.label.setLocation(10, 10);
    Dimension dim = new Dimension(100, 20);
    // this.label.setMinimumSize(dim);
    // this.label.setSize(dim);
    this.label.setVisible(true);
    this.label.setForeground(Color.BLACK);
    this.label.setBorder(Borders.customLine(nodeColor));

    // setBackground(Color.white);

    // super.add(contentLabel, BorderLayout.NORTH);
    setVisible(true);
  }

  @Override
  public void paintComponent(Graphics g) {

    if(theme == Theme.SYSTEM) {
      setServicesXmlColors(YqlAppSettingsState.getInstance());
    }

    if (g != null) {
      // Graphics2D g2d = (Graphics2D) g;

      // Background...
      Dimension size = super.getSize();
      // g.setColor(new Color(0xee, 0xee, 0xee));
      switch(theme) {
        case SYSTEM -> g.setColor(getBackground());
        case DARK -> g.setColor(Color.DARK_GRAY);
        case LIGHT -> g.setColor(Color.WHITE);
      }
      g.fillRect(0, 0, size.width, size.height);

      if (this.servicesXml != null) {
        paintContainers(g, 40, 40);
        paintContentGroups(g, 200, 200);
        paintNetworkConnections(g);
      }
    }
    if (ui != null) {
      Graphics scratchGraphics = (g == null) ? null : g.create();
      try {
        ui.update(scratchGraphics, this);
      } finally {
        scratchGraphics.dispose();
      }
    } else {
      super.paintComponent(g);
    }
  }

  private void paintNetworkConnections(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    int fontHeight = g.getFontMetrics().getHeight();

    int topNetworkX = 40;
    int topNetworkY = 160;
    // FIXME: The width needs to be calculated
    int topNetworkWidth = topNetworkX + (this.servicesXml.getContent().getGroupCount() * 400);

    g2d.setColor(networkColor);
//    Stroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
//        0, new float[]{9}, 0);
    Stroke basic = new BasicStroke(2);
    g2d.setStroke(basic);

    // Top network line
    g2d.drawLine(topNetworkX, topNetworkY, topNetworkX + topNetworkWidth, topNetworkY);

    this.servicesXml.getContainers().forEach(containerXml -> {
      containerXml.getNodesOrJvm().getNodes().forEach(node -> {
        NodePlacement placement = containerNodePlacement.get(node.getHostalias());
        int nodex = placement.x + (placement.width / 2);
        g2d.drawLine(nodex,
            placement.y + fontHeight,
            nodex,
            topNetworkY);
      });
    });

    GroupXml topGroup = this.servicesXml.getContent().getGroup();
    if (topGroup != null) {
      List<GroupXml> groups = topGroup.getGroups();

      groups.forEach(groupXml -> {
        NodePlacement placement = contentGroupPlacement.get(groupXml.getDistributionKey());
        int groupx = placement.x - 10;
        g2d.drawLine(groupx,
            topNetworkY,
            groupx,
            placement.y + placement.height + 30);

        groupXml.getNodes().forEach(nodeXml -> {
          NodePlacement nodePlacement = contentNodePlacement.get(nodeXml.getHostalias());
          int nodey = nodePlacement.y + (nodePlacement.height / 2);
          g2d.drawLine(groupx, nodey,
              groupx + 12, nodey);
        });


      });


    } else {
      // Single group/node cluster
      List<NodeXml> nodes = servicesXml.getContent().getNodes();
      GroupXml groupXml = new GroupXml("0", new DisributionXml("0"), "0", nodes, List.of());
      NodePlacement placement = contentGroupPlacement.get(groupXml.getDistributionKey());
      int groupx = placement.x - 10;
      g2d.drawLine(groupx,
          topNetworkY,
          groupx,
          placement.y + placement.height + 15);
      groupXml.getNodes().forEach(nodeXml -> {
        NodePlacement nodePlacement = contentNodePlacement.get(nodeXml.getHostalias());
        int nodey = nodePlacement.y + (nodePlacement.height / 2);
        g2d.drawLine(groupx, nodey,
            groupx + 12, nodey);
      });
    }
  }

  private void paintContentGroups(Graphics g, int xoffset, int yoffset) {
    Graphics2D g2d = (Graphics2D) g;
    int labelMargin = 8;
    int fontHeight = g.getFontMetrics().getHeight();
    int labelX = xoffset + margin;
    int maxHeight = 0;

    // Render title
    label.setForeground(clusterColor);
    label.setText("Content");
    label.setBorder(null);
    label.setSize(labelMargin + g.getFontMetrics().stringWidth("Content"), fontHeight);
    paintComponentAt(g, label, xoffset + margin, yoffset + margin);

    int groupMargin = 30;

    ContentXml content = servicesXml.getContent();
    GroupXml group = content.getGroup();
    if (group != null) {
      List<GroupXml> groups = group.getGroups();
      for (int groupI = 0; groupI < groups.size(); groupI++) {
        GroupXml subGroup = groups.get(groupI);
        Dimension dim = paintContentGroup(g, subGroup,
            labelX + margin,
            yoffset + fontHeight + margin);

        g2d.setColor(serviceColor);
        Stroke plainStroke = new BasicStroke(1);
        g2d.setStroke(plainStroke);
        g2d.drawRect(labelX + margin,
            yoffset + fontHeight + margin,
            dim.width + margin,
            dim.height);

        contentGroupPlacement.put(subGroup.getDistributionKey(), new NodePlacement(labelX + margin,
            yoffset + fontHeight + margin,
            dim.width + margin,
            dim.height));

        labelX += dim.width + groupMargin;
        maxHeight = Math.max(maxHeight, dim.height + margin);
      }
    } else {
      // Single group of nodes.
      List<NodeXml> nodes = content.getNodes();
      GroupXml subGroup = new GroupXml("0", new DisributionXml("0"), "0", nodes, List.of());
      Dimension dim = paintContentGroup(g, subGroup,
          labelX + margin,
          yoffset + fontHeight + margin);

      g2d.setColor(serviceColor);
      Stroke plainStroke = new BasicStroke(1);
      g2d.setStroke(plainStroke);
      g2d.drawRect(labelX + margin,
          yoffset + fontHeight + margin,
          dim.width + margin,
          dim.height + margin);

      contentGroupPlacement.put(subGroup.getDistributionKey(), new NodePlacement(labelX + margin,
          yoffset + fontHeight + margin,
          dim.width + margin,
          dim.height));

      labelX += dim.width + groupMargin;
      maxHeight = Math.max(maxHeight, dim.height + margin);
    }

    g2d.setColor(clusterColor);
    Stroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
        0, new float[]{9}, 0);
    g2d.setStroke(dashed);
    g2d.drawRect(xoffset,
        yoffset,
        labelX - xoffset + margin,
        fontHeight + (2 * margin) + maxHeight);
  }

  private Dimension paintContentGroup(Graphics g, GroupXml group, int xoffset, int yoffset) {
    Graphics2D g2d = (Graphics2D) g;
    int labelMargin = 8;
    int fontHeight = g.getFontMetrics().getHeight();
    int labelY = yoffset;

    label.setForeground(serviceColor);
    String groupText = group.getName() + ", key: " + group.getDistributionKey();
    int textMaxWidth = labelMargin + g.getFontMetrics().stringWidth(groupText);
    label.setText(groupText);
    label.setBorder(null);
    label.setSize(textMaxWidth, fontHeight);
    paintComponentAt(g, label, xoffset + margin, yoffset + margin);

    // We only support one level groups for now...
    // so we assume there are no more group levels
    List<NodeXml> nodes = group.getNodes();
    for (int i = 0; i < nodes.size(); i++) {
      NodeXml node = nodes.get(i);
      String nodeLabel = node.getHostalias() + ", key: " + node.getDistributionKey();
      int nodeLabelWidth = labelMargin + g.getFontMetrics().stringWidth(nodeLabel);
      textMaxWidth = Math.max(textMaxWidth, nodeLabelWidth);
      label.setForeground(nodeColor);
      label.setText(nodeLabel);
      label.setSize(nodeLabelWidth, fontHeight);
      label.setBorder(Borders.customLine(nodeColor));
      labelY = yoffset + ((1 + i) * fontHeight) + margin;
      paintComponentAt(g, label,
          xoffset + margin,
          labelY);

      contentNodePlacement.put(node.getHostalias(),
          new NodePlacement(xoffset + margin, labelY, label.getWidth(), label.getHeight()));
    }

    Dimension dim = new Dimension(textMaxWidth, fontHeight + labelY - yoffset);
    return dim;
  }

  private static void paintComponentAt(Graphics g, JComponent component, int x, int y) {
    Graphics labelG = g.create(x, y, component.getWidth(), component.getHeight());
    component.paint(labelG);
    labelG.dispose();
  }

  private void paintContainers(Graphics g, int xoffset, int yoffset) {
    Graphics2D g2d = (Graphics2D) g;
    int labelMargin = 8;
    int fontHeight = g.getFontMetrics().getHeight();
    int labelX = xoffset;

    // Render title
    label.setForeground(clusterColor);
    label.setText("Containers");
    label.setBorder(null);
    label.setSize(labelMargin + g.getFontMetrics().stringWidth("Containers"), fontHeight);
    paintComponentAt(g, label, labelX + margin, yoffset + margin);

    labelX += margin;

    for (int i = 0; i < this.servicesXml.getContainers().size(); i++) {

      int containerXStart = labelX;
      ContainerXml container = this.servicesXml.getContainers()
          .get(i);

      String containerLabelText = container.getId()
          + " ("
          + (container.hasSearchApi() ? " /search/" : "")
          + (container.hasDocumentApi() ? " /document/v1/" : "")
          + " )";
      int containerLabelWidth = labelMargin + g.getFontMetrics().stringWidth(containerLabelText);

      labelX += margin;

      // Render Container
      label.setForeground(serviceColor);
      label.setText(containerLabelText);
      label.setSize(containerLabelWidth, fontHeight);
      label.setBorder(null);
      paintComponentAt(g, label, labelX, yoffset + fontHeight + margin);

      // Render Nodes
      List<NodeXml> nodes = container.getNodesOrJvm()
          .getNodes();
      for (int n = 0; n < nodes.size(); n++) {
        NodeXml node = nodes.get(n);
        String nodeLabel = node.getHostalias();
        int nodeLabelWidth = labelMargin + g.getFontMetrics().stringWidth(nodeLabel);
        label.setForeground(nodeColor);
        label.setText(nodeLabel);
        label.setSize(nodeLabelWidth, fontHeight);
        label.setBorder(Borders.customLine(nodeColor));
        int nodex = labelX;
        int nodey = yoffset + (2 * fontHeight) + margin;
        paintComponentAt(g, label, nodex, nodey);
        containerNodePlacement.put(node.getHostalias(),
            new NodePlacement(nodex, nodey, label.getWidth(), label.getHeight()));
        labelX += nodeLabelWidth + margin;
      }

      int containerWidth = Math.max(containerLabelWidth + margin, labelX - containerXStart);

      g.setColor(serviceColor);
      g.drawRect(containerXStart,
          yoffset + fontHeight + margin,
          containerWidth,
          (2 * fontHeight) + (1 * margin));

      // Move
      // labelX += containerWidth + margin;
      labelX = containerXStart + containerWidth + margin;
    }

    g2d.setColor(clusterColor);
    Stroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
        0, new float[]{9}, 0);
    g2d.setStroke(dashed);
    g2d.drawRect(xoffset,
        yoffset,
        labelX - xoffset + margin,
        (3 * fontHeight) + (3 * margin));
  }
}

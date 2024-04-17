package com.pehrs.vespa.yql.plugin.serviceview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swing.SwingGraphRenderer;
import org.graphstream.ui.swing_viewer.DefaultView;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.view.Viewer;

public class GraphStreamSample {

  // @Test
  public void testGraph() throws InterruptedException {
    //  System.setProperty("org.graphstream.ui", "swing");
    display();
    while (1 == 1) {
      Thread.sleep(1000);
    }
  }

  private void display() {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    JPanel panel = new JPanel(new GridLayout()) {
      @Override
      public Dimension getPreferredSize() {
        return new Dimension(640, 480);
      }
    };
    panel.setBorder(BorderFactory.createLineBorder(Color.blue, 5));
    Graph graph = new SingleGraph("Tutorial", false, true);
    graph.addEdge("AB", "A", "B");
    Node a = graph.getNode("A");
    a.setAttribute("xy", 1, 1);
    Node b = graph.getNode("B");
    b.setAttribute("xy", -1, -1);
    SwingGraphRenderer renderer = new SwingGraphRenderer();
    SwingViewer viewer = new SwingViewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
    DefaultView viewPanel = new DefaultView(viewer, "id", renderer);
    panel.add(viewPanel);
    frame.add(panel);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

}

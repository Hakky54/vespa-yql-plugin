package com.pehrs.vespa.yql.plugin.settings;

import com.intellij.ide.dnd.aware.DnDAwareTree;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI.Borders;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

@Deprecated
public class YqlSettingsPanel extends SimpleToolWindowPanel implements DataProvider {

  private final Project project;

  public YqlSettingsPanel(Project project) {
    super(true);
    this.project = project;
    createUIComponents();

    DnDAwareTree tree = createTree();

    JBPanel configPanel = new JBPanel();
    configPanel.setBorder(Borders.customLine(Color.GREEN));

    JBSplitter splitPane = new OnePixelSplitter(false, "test", 0.6F);

    splitPane.setFirstComponent(tree);
    splitPane.setSecondComponent(configPanel);
    splitPane.setProportion(0.4F);

    this.setContent(splitPane);
    this.setMinimumSize(new Dimension(800, 600));
  }

  private static DnDAwareTree createTree() {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    root.setUserObject("Vespa Clusters");
    DefaultTreeModel treeModel = new DefaultTreeModel(root);

    root.add(new DefaultMutableTreeNode("http://localhost:19071"));
    root.add(new DefaultMutableTreeNode("http://remote.host.net:19071"));

    JLabel nodeLabel = new JLabel();

    DnDAwareTree tree = new DnDAwareTree(treeModel);
    return tree;
  }

  private void createUIComponents() {

  }


}

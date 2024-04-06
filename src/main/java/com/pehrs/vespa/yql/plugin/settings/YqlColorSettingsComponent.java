package com.pehrs.vespa.yql.plugin.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.panels.HorizontalBox;
import com.intellij.util.Consumer;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI.Borders;
import com.pehrs.vespa.yql.plugin.YQL;
import com.pehrs.vespa.yql.plugin.graph.VespaServicesPanel;
import com.pehrs.vespa.yql.plugin.graph.VespaServicesPanel.Theme;
import com.pehrs.vespa.yql.plugin.xml.VespaServicesXml;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ButtonGroup;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class YqlColorSettingsComponent implements YqlAppSettingsStateListener {


  private final JPanel myMainPanel;

  private final Project project;
  private final VespaServicesPanel servicesPanel;

  private  Theme servicesPanelTheme = Theme.LIGHT;

  static final XmlMapper xmlMapper = new XmlMapper();


  public YqlColorSettingsComponent(Project project) {
    this.project = project;

    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();

//    JLabel lightColor = new JBLabel("Light");
//    lightColor.setOpaque(true);
//    lightColor.setBackground(Color.BLUE);
//    lightColor.setBorder(new EmptyBorder(4, 4, 4, 4));
//    lightColor.addMouseListener(new MouseAdapter() {
//      @Override
//      public void mouseClicked(MouseEvent e) {
//        if (e.getClickCount() == 1) {
//          Color color = ColorChooser.chooseColor(YqlColorSettingsComponent.this.myMainPanel,
//              "Select Color",
//              lightColor.getBackground());
//          lightColor.setBackground(color);
//          settings.overviewNodeColorLight = color.getRGB();
//          stateChanged(settings);
//        }
//        super.mouseClicked(e);
//      }
//    });
//
//    JLabel darkColor = new JBLabel("Dark");
//    darkColor.setBackground(JBColor.RED);
//    darkColor.setOpaque(true);
//    darkColor.addMouseListener(new MouseAdapter() {
//      @Override
//      public void mouseClicked(MouseEvent e) {
//        if (e.getClickCount() == 1) {
//          Color color = ColorChooser.chooseColor(YqlColorSettingsComponent.this.myMainPanel,
//              "Select Color",
//              darkColor.getBackground());
//          darkColor.setBackground(color);
//          settings.overviewNodeColorDark = color.getRGB();
//          stateChanged(settings);
//        }
//        super.mouseClicked(e);
//      }
//    });
//
//    HorizontalBox nodeColors = new HorizontalBox();
//    nodeColors.add(lightColor);
//    nodeColors.add(Box.createRigidArea(new Dimension(24, 0)));
//    nodeColors.add(darkColor);

    int minWidth = 250;


    JComponent nodeColors = createColorChooser(
        minWidth,
        settings.getOverviewNodeColorLight(),
        settings.getOverviewNodeColorDark(),
        (color) -> {
          settings.setOverviewNodeColorLight(color);
          stateChanged(settings);
        },
        (color) -> {
          settings.setOverviewNodeColorDark(color);
          stateChanged(settings);
        }
    );
    JComponent serviceColors = createColorChooser(
        minWidth,
        settings.getOverviewServiceColorLight(),
        settings.getOverviewServiceColorDark(),
        (color) -> {
          settings.setOverviewServiceColorLight(color);
          stateChanged(settings);
        },
        (color) -> {
          settings.setOverviewServiceColorDark(color);
          stateChanged(settings);
        }
    );
    JComponent clusterColors = createColorChooser(
        minWidth,
        settings.getOverviewClusterColorLight(),
        settings.getOverviewClusterColorDark(),
        (color) -> {
          settings.setOverviewClusterColorLight(color);
          stateChanged(settings);
        },
        (color) -> {
          settings.setOverviewClusterColorDark(color);
          stateChanged(settings);
        }
    );
    JComponent networkColors = createColorChooser(
        minWidth,
        settings.getOverviewNetworkColorLight(),
        settings.getOverviewNetworkColorDark(),
        (color) -> {
          settings.setOverviewNetworkColorLight(color);
          stateChanged(settings);
        },
        (color) -> {
          settings.setOverviewNetworkColorDark(color);
          stateChanged(settings);
        }
    );

    String xml = YQL.getResource("/services-multiple-groups.xml");
    try {
      VespaServicesXml servicesXml = xmlMapper.readValue(xml, VespaServicesXml.class);
      this.servicesPanel = new VespaServicesPanel(servicesXml);

      // setServicesXmlColors(settings);
      this.servicesPanel.setTheme(this.servicesPanelTheme);

      JComponent colorTitle = createColorTitle();

      JBRadioButton lightBtn = new JBRadioButton("Light", true);
      lightBtn.addActionListener(event -> {
        if(lightBtn.isSelected()) {
          this.servicesPanelTheme = Theme.LIGHT;
          this.servicesPanel.setTheme(this.servicesPanelTheme);
          this.servicesPanel.repaint();
        }
      });
      JBRadioButton darkBtn = new JBRadioButton("Dark", false);
      darkBtn.addActionListener(event -> {
        if(darkBtn.isSelected()) {
          this.servicesPanelTheme = Theme.DARK;
          this.servicesPanel.setTheme(this.servicesPanelTheme);
          this.servicesPanel.repaint();
        }
      });
      ButtonGroup bg=new ButtonGroup();
      bg.add(lightBtn);
      bg.add(darkBtn);

      HorizontalBox exampleThemeBox = new HorizontalBox();
      exampleThemeBox.add(lightBtn);
      exampleThemeBox.add(darkBtn);

      this.myMainPanel = FormBuilder.createFormBuilder()
          .addComponent(new JBLabel("Overview colors"))
          .addLabeledComponent(new JBLabel(" "), colorTitle, false)
          .addLabeledComponent(new JBLabel("Clusters:"), clusterColors, false)
          .addLabeledComponent(new JBLabel("Services:"), serviceColors, false)
          .addLabeledComponent(new JBLabel("Nodes:"), nodeColors, false)
          .addLabeledComponent(new JBLabel("Network:"), networkColors, false)
          .addSeparator()
          .addLabeledComponent(new JBLabel("Example:"), exampleThemeBox, false)
          .addComponentFillVertically(this.servicesPanel, 0)
          .getPanel();

      myMainPanel.setBorder(Borders.empty(8));

      YqlAppSettingsStateListener.addListener(this);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private static JComponent createColorTitle() {

    JBLabel lightLabel = new JBLabel("Light theme");
    JBLabel darkLabel = new JBLabel("Dark theme");

    GridLayout layout = new GridLayout(0,2);
    JBPanel colorTitle = new JBPanel(layout);
    colorTitle.add(lightLabel);
    colorTitle.add(darkLabel);

    return colorTitle;
  }

  private JComponent createColorChooser(
      int minWidth,
      Color initialLightColor,
      Color initialDarkColor,
      Consumer<Color> listColorConsumer,
      Consumer<Color> darkColorConsumer) {

    JLabel lightColor = new JBLabel(" ");
    lightColor.setOpaque(true);
    lightColor.setBackground(initialLightColor);
    lightColor.setBorder(new EmptyBorder(4, 4, 4, 4));
    lightColor.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 1) {
//          Color color = ColorChooserService.getInstance().showDialog(
//              YqlColorSettingsComponent.this.project,
//              YqlColorSettingsComponent.this.myMainPanel,
//              "Select color",
//              lightColor.getBackground(),
//              true,
//              List.of(),
//              true
//          );
          Color color = JColorChooser.showDialog(YqlColorSettingsComponent.this.myMainPanel,
              "Select light theme color",
              lightColor.getBackground());
          if (color != null) {
            lightColor.setBackground(color);
            listColorConsumer.consume(color);
          }
        }
        super.mouseClicked(e);
      }
    });

    JLabel darkColor = new JBLabel(" ");
    darkColor.setBackground(initialDarkColor);
    darkColor.setOpaque(true);
    darkColor.setBorder(new EmptyBorder(4, 4, 4, 4));
    darkColor.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 1) {
          Color color = JColorChooser.showDialog(YqlColorSettingsComponent.this.myMainPanel,
              "Select dark theme color",
              darkColor.getBackground());
          if (color != null) {
            darkColor.setBackground(color);
            darkColorConsumer.consume(color);
          }
        }
        super.mouseClicked(e);
      }
    });

    GridLayout layout = new GridLayout(0,2);
    JBPanel colorBox = new JBPanel(layout);
    colorBox.add(lightColor);
    colorBox.add(darkColor);

    return colorBox;
  }

  public JComponent getPreferredFocusedComponent() {
    return myMainPanel;
  }

  @Override
  public void stateChanged(YqlAppSettingsState instance) {
    this.servicesPanel.setTheme(this.servicesPanelTheme);
    this.servicesPanel.repaint();
  }


  public JComponent getPanel() {
    return myMainPanel;
  }
}

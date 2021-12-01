//package UGV;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;
import java.util.StringTokenizer;
import java.awt.image.*;
import java.awt.*;
import javax.swing.filechooser.*;
import javax.imageio.ImageIO;


//import javax.swing.plaf.*;

public class UGVViewer extends JFrame implements MouseListener, WindowListener//, KeyListener
{
   public UGVViewer()
   {
      setTitle("Universal Graph Viewer");
      setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      setLayout(new BorderLayout());
      setAlwaysOnTop(false);
      addWindowListener(this);
      //setFocusable(true);
      //addKeyListener(this);


   //Hijack the keyboard manager
      /*KeyboardFocusManager manager =
         KeyboardFocusManager.getCurrentKeyboardFocusManager();
      manager.addKeyEventDispatcher(
            new KeyEventDispatcher(){
               public boolean dispatchKeyEvent(KeyEvent e) {
                  if(e.getID() == KeyEvent.KEY_TYPED)
                     keyTyped(e);
                  else if(e.getID() == KeyEvent.KEY_PRESSED)
                     keyPressed(e);
                  else if(e.getID() == KeyEvent.KEY_RELEASED)
                     keyReleased(e);
                     //System.out.println( "typed" + e.getCharCode() );

               //Allow the event to be redispatched
                  return false;
               }
            });*/


      getContentPane().addHierarchyBoundsListener(
            new HierarchyBoundsListener(){

               @Override
               public void ancestorMoved(HierarchyEvent e) {
                  Component parent = e.getChanged();
                  settings_locationX = parent.getLocation().x;
                  settings_locationY = parent.getLocation().y;
                  saveSettings();
               }
               @Override
               public void ancestorResized(HierarchyEvent e) {
                  //System.out.println(e);
                  Component parent = e.getChanged();
                  settings_locationX = parent.getLocation().x;
                  settings_locationY = parent.getLocation().y;
                  settings_frameWidth = parent.getSize().width;
                  settings_frameHeight = parent.getSize().height;
                  saveSettings();
               }
            });

      defaultColors[0] = defaultBackgroundColor;
      defaultColors[1] = defaultDefaultColor;
      defaultColors[2] = defaultHighlightedVertexFillColor;
      defaultColors[3] = defaultHighlightedVertexColor;
      defaultColors[4] = defaultNewEdgeColor;
      defaultColors[5] = defaultDeleteEdgeColor;

      for(int i=0; i<originalDefaultColors.length; i++)
         originalDefaultColors[i] = defaultColors[i];

      colorStrings[0] = "Background colour";
      colorStrings[1] = "Vertex/arc colour";
      colorStrings[2] = "Highlighted vertex fill colour";
      colorStrings[3] = "Highlighted vertex/arc colour";
      colorStrings[4] = "New edge colour";
      colorStrings[5] = "Edge deletion colour";

      loadSettings();

      setLocation(settings_locationX,settings_locationY);

      createMenuBar();

      //graphPanel = new JPanel();

      tabbedPane = new JTabbedPane();
      tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
      tabbedPane.addChangeListener(
            new ChangeListener(){
               public void stateChanged(ChangeEvent e)
               {
                  if(tabbedPane.getSelectedIndex() == -1)
                  {
                     setTitle("Universal Graph Viewer");
                     vertexSizeSlider.setValue(DEFAULT_VERTEX_SIZE);
                     vertexSizeField.setText(""+DEFAULT_VERTEX_SIZE);
                     labelSizeSlider.setValue(DEFAULT_LABEL_SIZE);
                     labelSizeField.setText(""+DEFAULT_LABEL_SIZE);

                     vertexSizeSlider.setEnabled(false);
                     vertexSizeField.setEnabled(false);
                     labelSizeSlider.setEnabled(false);
                     labelSizeField.setEnabled(false);

                     saveFileItem.setEnabled(false);
                     saveMultipleGraphsFileItem.setEnabled(false);
                     exportAsImageFileItem.setEnabled(false);
                     undoEditItem.setEnabled(false);
                     redoEditItem.setEnabled(false);
                  }
                  else
                  {
                     setTitle("Universal Graph Viewer - " +  tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()));
                     GraphPane gp = (GraphPane)tabbedPane.getSelectedComponent();
                     displayVertexLabelsItem.setState(gp.getDisplayVertexLabels());
                     displayCrossingsItem.setState(gp.getDisplayCrossings());
                     displayDominationItem.setState(gp.getDisplayDomination());

                     vertexSizeSlider.setValue(gp.getRadius());
                     vertexSizeField.setText(""+gp.getRadius());
                     labelSizeSlider.setValue(gp.getTextSize());
                     labelSizeField.setText(""+gp.getTextSize());

                     vertexSizeSlider.setEnabled(true);
                     vertexSizeField.setEnabled(true);
                     labelSizeSlider.setEnabled(true);
                     labelSizeField.setEnabled(true);

                     domTotalItem.setState(gp.getDomTotal());
                     domSecureItem.setState(gp.getDomSecure());
                     domConnectedItem.setState(gp.getDomConnected());
                     domRomanItem.setState(gp.getDomRoman());
                     domWeakRomanItem.setState(gp.getDomWeakRoman());

                     saveFileItem.setEnabled(true);
                     saveMultipleGraphsFileItem.setEnabled(true);
                     exportAsImageFileItem.setEnabled(true);

                  }
               }
            });

      //graphHolderPanel = new JPanel();
      //graphHolderPanel.setLayout(new BorderLayout());
      //graphHolderPanel.setBackground(new Color(255,0,0));
      //graphHolderPanel.setSize(640,480);







      //graphHolderPanel.add(graphPanel, BorderLayout.CENTER);

      //tabbedPane.add(graphHolderPanel, "Graph");
      //tabbedPane.setBackgroundAt(0,null);
     // System.out.println(graphPanel);

      //tabbedPane.add(graphPanel, "Graph");

      add(tabbedPane,BorderLayout.CENTER);



      createGraphEditPane();

      add(graphEditPane, BorderLayout.EAST);




      setSize(settings_frameWidth, settings_frameHeight);
      setVisible(true);
      defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);

      graphPanels = new GraphLinkedList();
      //graphs = new GraphLinkedList();
   }

   public void loadSettings()
   {
      DataInputStream di = null;
      try
      {
         di = new DataInputStream(new FileInputStream(settingsFile));

         settings_frameWidth = di.readInt();
         settings_frameHeight = di.readInt();
         settings_locationX = di.readInt();
         settings_locationY = di.readInt();
         settings_loadFilter = di.readInt();
         settings_saveFilter = di.readInt();
         settings_saveImageFilter = di.readInt();
         settings_displayVertexLabels = di.readBoolean();

         for(int i=0; i<defaultColors.length; i++)
            defaultColors[i] = new Color(di.readInt(),di.readInt(),di.readInt());
      }
      catch(Exception e)
      {

      }
      try
      {
         if(di != null)
            di.close();
      }
      catch(Exception e)
      {

      }
   }

   public void saveSettings()
   {
      try
      {
         DataOutputStream dos = new DataOutputStream(new FileOutputStream(settingsFile));

         dos.writeInt(settings_frameWidth);
         dos.writeInt(settings_frameHeight);
         dos.writeInt(settings_locationX);
         dos.writeInt(settings_locationY);
         dos.writeInt(settings_loadFilter);
         dos.writeInt(settings_saveFilter);
         dos.writeInt(settings_saveImageFilter);
         dos.writeBoolean(settings_displayVertexLabels);

         for(int i=0; i<defaultColors.length; i++)
         {
            dos.writeInt(defaultColors[i].getRed());
            dos.writeInt(defaultColors[i].getGreen());
            dos.writeInt(defaultColors[i].getBlue());
         }

         dos.close();
      }
      catch(Exception e)
      {
      }
   }

   public void createGraphEditPane()
   {
      graphEditPane = new JPanel();
      //graphEditPane.setLayout(new BoxLayout(graphEditPane, BoxLayout.Y_AXIS));
      graphEditPane.setLayout(new VerticalLayout());
      graphEditPane.setBackground(rightColor);

      graphEditPane.addMouseListener(this);

      buttonPane = new JPanel();
      buttonPane.setBackground(rightColor);

      buttonPane.addMouseListener(this);

      buttonPane.setLayout(new GridLayout((int)Math.ceil(choices/2.0),2));

      String[] toolTipsForLabels = new String[choices+bottomChoices+selectChoices];

      toolTipsForLabels[0] = "Insert vertex";
      toolTipsForLabels[1] = "Insert edge";
      toolTipsForLabels[2] = "Delete vertex";
      toolTipsForLabels[3] = "Delete edge";
      toolTipsForLabels[4] = "Relabel vertex";
      toolTipsForLabels[5] = "Toggle dominating set";
      toolTipsForLabels[6] = "Arrange vertices into a circle";
      toolTipsForLabels[7] = "Check graph properties";
      toolTipsForLabels[8] = "Edit edge list";
      toolTipsForLabels[9] = "Spring layout";
      toolTipsForLabels[10] = "Select vertices";
      toolTipsForLabels[11] = "Rotate vertices";


      icons = new ImageIcon[choices+bottomChoices+selectChoices][4];
      icons[0][0] = new ImageIcon("pics/node.png");
      icons[0][1] = new ImageIcon("pics/node_highlight.png");
      icons[0][2] = new ImageIcon("pics/node_select.png");
      icons[0][3] = new ImageIcon("pics/node_chosen.png");


      icons[1][0] = new ImageIcon("pics/edge.png");
      icons[1][1] = new ImageIcon("pics/edge_highlight.png");
      icons[1][2] = new ImageIcon("pics/edge_select.png");
      icons[1][3] = new ImageIcon("pics/edge_chosen.png");


      icons[2][0] = new ImageIcon("pics/eraser.png");
      icons[2][1] = new ImageIcon("pics/eraser_highlight.png");
      icons[2][2] = new ImageIcon("pics/eraser_select.png");
      icons[2][3] = new ImageIcon("pics/eraser_chosen.png");


      icons[3][0] = new ImageIcon("pics/scissors.png");
      icons[3][1] = new ImageIcon("pics/scissors_highlight.png");
      icons[3][2] = new ImageIcon("pics/scissors_select.png");
      icons[3][3] = new ImageIcon("pics/scissors_chosen.png");

      icons[4][0] = new ImageIcon("pics/relabel.png");
      icons[4][1] = new ImageIcon("pics/relabel_highlight.png");
      icons[4][2] = new ImageIcon("pics/relabel_select.png");
      icons[4][3] = new ImageIcon("pics/relabel_chosen.png");

      icons[5][0] = new ImageIcon("pics/dom.png");
      icons[5][1] = new ImageIcon("pics/dom_highlight.png");
      icons[5][2] = new ImageIcon("pics/dom_select.png");
      icons[5][3] = new ImageIcon("pics/dom_chosen.png");

      icons[6][0] = new ImageIcon("pics/arrange.png");
      icons[6][1] = new ImageIcon("pics/arrange_highlight.png");
      icons[6][2] = new ImageIcon("pics/arrange_select.png");
      icons[6][3] = new ImageIcon("pics/arrange_chosen.png");

      icons[7][0] = new ImageIcon("pics/properties.png");
      icons[7][1] = new ImageIcon("pics/properties_highlight.png");
      icons[7][2] = new ImageIcon("pics/properties_select.png");
      icons[7][3] = new ImageIcon("pics/properties_chosen.png");

      icons[8][0] = new ImageIcon("pics/editedges.png");
      icons[8][1] = new ImageIcon("pics/editedges_highlight.png");
      icons[8][2] = new ImageIcon("pics/editedges_select.png");
      icons[8][3] = new ImageIcon("pics/editedges_chosen.png");

      icons[9][0] = new ImageIcon("pics/spring.png");
      icons[9][1] = new ImageIcon("pics/spring_highlight.png");
      icons[9][2] = new ImageIcon("pics/spring_select.png");
      icons[9][3] = new ImageIcon("pics/spring_chosen.png");

      icons[10][0] = new ImageIcon("pics/select.png");
      icons[10][1] = new ImageIcon("pics/select_highlight.png");
      icons[10][2] = new ImageIcon("pics/select_select.png");
      icons[10][3] = new ImageIcon("pics/select_chosen.png");

      icons[11][0] = new ImageIcon("pics/rotate.png");
      icons[11][1] = new ImageIcon("pics/rotate_highlight.png");
      icons[11][2] = new ImageIcon("pics/rotate_select.png");
      icons[11][3] = new ImageIcon("pics/rotate_chosen.png");

      highlighted = new boolean[choices+bottomChoices+selectChoices];
      pressed = new boolean[choices+bottomChoices+selectChoices];
      chosen = new boolean[choices+bottomChoices+selectChoices];

      labels = new JLabel[choices+bottomChoices+selectChoices];

      for(int i=0; i<choices; i++)
      {
         labels[i] = new JLabel(icons[i][0]);
         labels[i].addMouseListener(this);
         labels[i].setMaximumSize(new Dimension(icons[i][0].getIconWidth(),icons[i][0].getIconHeight()));
         labels[i].setToolTipText(toolTipsForLabels[i]);
         buttonPane.add(labels[i]);
      }

      JPanel sliderPanel = new JPanel();
      sliderPanel.setBackground(rightColor);
      sliderPanel.setLayout(new GridLayout(1,3));

      vertexSizeSlider = new JSlider(SwingConstants.VERTICAL,MIN_VERTEX_SIZE,MAX_VERTEX_SIZE,DEFAULT_VERTEX_SIZE);
      vertexSizeSlider.setToolTipText("Resize vertices");
      labelSizeSlider = new JSlider(SwingConstants.VERTICAL,MIN_LABEL_SIZE,MAX_LABEL_SIZE,DEFAULT_LABEL_SIZE);
      labelSizeSlider.setToolTipText("Resize vertex labels");

      vertexSizeSlider.setBackground(rightColor);
      vertexSizeSlider.addChangeListener(
            new ChangeListener(){
               public void stateChanged(ChangeEvent e)
               {
                  if(tabbedPane.getSelectedIndex() != -1)
                  {
                     ((GraphPane)tabbedPane.getSelectedComponent()).setRadius(vertexSizeSlider.getValue());
                     if(vertexSizeField != null)
                        vertexSizeField.setText(""+vertexSizeSlider.getValue());
                     tabbedPane.getSelectedComponent().repaint();
                  }
               }
            });

      labelSizeSlider.setBackground(rightColor);
      labelSizeSlider.addChangeListener(
            new ChangeListener(){
               public void stateChanged(ChangeEvent e)
               {
                  if(tabbedPane.getSelectedIndex() != -1)
                  {
                     //System.out.println("(Slide) Setting text size to " + labelSizeSlider.getValue());
                     ((GraphPane)tabbedPane.getSelectedComponent()).setTextSize(labelSizeSlider.getValue());
                     if(labelSizeField != null)
                        labelSizeField.setText(""+labelSizeSlider.getValue());
                     tabbedPane.getSelectedComponent().repaint();
                  }
               }
            });

      vertexSizeField = new JTextField(""+DEFAULT_VERTEX_SIZE);
      vertexSizeField.setColumns(2);
      vertexSizeField.setToolTipText("Resize vertices");
      vertexSizeField.addActionListener(
            new ActionListener()
            {
               public void actionPerformed(ActionEvent e)
               {
                  try
                  {
                     int size = Integer.parseInt(vertexSizeField.getText());
                     if(size < MIN_VERTEX_SIZE)
                        vertexSizeField.setText(""+MIN_VERTEX_SIZE);
                     if(size > MAX_VERTEX_SIZE)
                        vertexSizeField.setText(""+MAX_VERTEX_SIZE);
                  }
                  catch(Exception ex)
                  {
                     if(tabbedPane.getSelectedIndex() != -1)
                     {
                        vertexSizeField.setText(""+((GraphPane)tabbedPane.getSelectedComponent()).getRadius());
                     }
                     else
                        vertexSizeField.setText(""+DEFAULT_VERTEX_SIZE);
                  }
               }
            });
      vertexSizeField.getDocument().addDocumentListener(
            new DocumentListener() {

               @Override
               public void removeUpdate(DocumentEvent e) {
                  try
                  {
                     int size = Integer.parseInt(vertexSizeField.getText());
                     if(size < MIN_VERTEX_SIZE)
                     {
                        size = MIN_VERTEX_SIZE;
                     }
                     if(size > MAX_VERTEX_SIZE)
                     {
                        size = MAX_VERTEX_SIZE;
                     }

                     if(tabbedPane.getSelectedIndex() != -1)
                     {
                        ((GraphPane)tabbedPane.getSelectedComponent()).setRadius(size);
                        tabbedPane.getSelectedComponent().repaint();
                     }
                     if(vertexSizeSlider != null)
                        vertexSizeSlider.setValue(size);

                  }
                  catch(Exception ex)
                  {
                  }
               }

               @Override
               public void insertUpdate(DocumentEvent e) {
                  try
                  {
                     int size = Integer.parseInt(vertexSizeField.getText());
                     if(size < MIN_VERTEX_SIZE)
                     {
                        size = MIN_VERTEX_SIZE;
                     }
                     if(size > MAX_VERTEX_SIZE)
                     {
                        size = MAX_VERTEX_SIZE;
                     }

                     if(tabbedPane.getSelectedIndex() != -1)
                     {
                        ((GraphPane)tabbedPane.getSelectedComponent()).setRadius(size);
                        tabbedPane.getSelectedComponent().repaint();
                     }
                     if(vertexSizeSlider != null)
                        vertexSizeSlider.setValue(size);

                  }
                  catch(Exception ex)
                  {
                  }
               }

               @Override
               public void changedUpdate(DocumentEvent arg0) {
                  try
                  {
                     int size = Integer.parseInt(vertexSizeField.getText());
                     if(size < MIN_VERTEX_SIZE)
                     {
                        size = MIN_VERTEX_SIZE;
                     }
                     if(size > MAX_VERTEX_SIZE)
                     {
                        size = MAX_VERTEX_SIZE;
                     }

                     if(tabbedPane.getSelectedIndex() != -1)
                     {
                        ((GraphPane)tabbedPane.getSelectedComponent()).setRadius(size);
                        tabbedPane.getSelectedComponent().repaint();
                     }
                     if(vertexSizeSlider != null)
                        vertexSizeSlider.setValue(size);

                  }
                  catch(Exception ex)
                  {
                  }
               }
            });


      labelSizeField = new JTextField(""+DEFAULT_LABEL_SIZE);
      labelSizeField.setColumns(2);
      labelSizeField.setToolTipText("Resize vertex labels");
      labelSizeField.addActionListener(
            new ActionListener()
            {
               public void actionPerformed(ActionEvent e)
               {
                  try
                  {
                     int size = Integer.parseInt(labelSizeField.getText());
                     if(size < MIN_LABEL_SIZE)
                        labelSizeField.setText(""+MIN_LABEL_SIZE);
                     if(size > MAX_LABEL_SIZE)
                        labelSizeField.setText(""+MAX_LABEL_SIZE);
                  }
                  catch(Exception ex)
                  {
                     if(tabbedPane.getSelectedIndex() != -1)
                     {
                        labelSizeField.setText(""+((GraphPane)tabbedPane.getSelectedComponent()).getTextSize());
                     }
                     else
                        labelSizeField.setText(""+DEFAULT_LABEL_SIZE);
                  }
               }
            });

      labelSizeField.getDocument().addDocumentListener(
            new DocumentListener() {

               @Override
               public void removeUpdate(DocumentEvent e) {
                  try
                  {

                     int size = Integer.parseInt(labelSizeField.getText());
                     if(size < MIN_LABEL_SIZE)
                        size = MIN_LABEL_SIZE;
                     if(size > MAX_LABEL_SIZE)
                        size = MAX_LABEL_SIZE;

                     if(tabbedPane.getSelectedIndex() != -1)
                     {
                        ((GraphPane)tabbedPane.getSelectedComponent()).setTextSize(size);
                        tabbedPane.getSelectedComponent().repaint();
                     }
                     if(labelSizeSlider != null)
                        labelSizeSlider.setValue(size);

                  }
                  catch(Exception ex)
                  {
                  }


               }

               @Override
               public void insertUpdate(DocumentEvent e) {
                  try
                  {

                     int size = Integer.parseInt(labelSizeField.getText());
                     if(size < MIN_LABEL_SIZE)
                        size = MIN_LABEL_SIZE;
                     if(size > MAX_LABEL_SIZE)
                        size = MAX_LABEL_SIZE;
                     if(tabbedPane.getSelectedIndex() != -1)
                     {
                        ((GraphPane)tabbedPane.getSelectedComponent()).setTextSize(size);

                        tabbedPane.getSelectedComponent().repaint();
                     }

                     if(labelSizeSlider != null)
                        labelSizeSlider.setValue(size);


                  }
                  catch(Exception ex)
                  {
                        //System.out.println(ex);
                  }
               }

               @Override
               public void changedUpdate(DocumentEvent arg0) {
                  try
                  {

                     int size = Integer.parseInt(labelSizeField.getText());
                     if(size < MIN_LABEL_SIZE)
                        size = MIN_LABEL_SIZE;
                     if(size > MAX_LABEL_SIZE)
                        size = MAX_LABEL_SIZE;

                     if(tabbedPane.getSelectedIndex() != -1)
                     {
                        ((GraphPane)tabbedPane.getSelectedComponent()).setTextSize(size);
                        tabbedPane.getSelectedComponent().repaint();
                     }
                     if(labelSizeSlider != null)
                        labelSizeSlider.setValue(size);

                  }
                  catch(Exception ex)
                  {
                  }
               }
            });


      vertexSizeSlider.setEnabled(false);
      vertexSizeField.setEnabled(false);
      labelSizeSlider.setEnabled(false);
      labelSizeField.setEnabled(false);

   //	System.out.println(vertexSizeSlider.getPreferredSize().width);
   //	System.out.println(vertexSizeSlider.getPreferredSize().height);
      vertexSizeSlider.setPreferredSize(new Dimension(16,150));
      labelSizeSlider.setPreferredSize(new Dimension(16,150));

      JPanel vertexSizeSliderPanel = new JPanel();
      vertexSizeSliderPanel.setBackground(rightColor);
      vertexSizeSliderPanel.setLayout(new VerticalLayout());
      vertexSizeSliderPanel.add(new JLabel(new ImageIcon("pics/vertexResizeBig.png")));
      vertexSizeSliderPanel.add(vertexSizeSlider);
      vertexSizeSliderPanel.add(new JLabel(new ImageIcon("pics/vertexResizeSmall.png")));
      vertexSizeSliderPanel.add(vertexSizeField);

      JPanel labelSizeSliderPanel = new JPanel();
      labelSizeSliderPanel.setBackground(rightColor);
      labelSizeSliderPanel.setLayout(new VerticalLayout());
      /*JLabel topLabelSizeLabel = new JLabel("1");
      topLabelSizeLabel.setFont(topLabelSizeLabel.getFont().deriveFont(27f));
      labelSizeSliderPanel.add(topLabelSizeLabel);*/
      labelSizeSliderPanel.add(new JLabel(new ImageIcon("pics/labelResizeBig.png")));
      labelSizeSliderPanel.add(labelSizeSlider);
      /*JLabel bottomLabelSizeLabel = new JLabel("1");
      bottomLabelSizeLabel.setFont(bottomLabelSizeLabel.getFont().deriveFont(12f));
      labelSizeSliderPanel.add(bottomLabelSizeLabel);*/
      labelSizeSliderPanel.add(new JLabel(new ImageIcon("pics/labelResizeSmall.png")));
      labelSizeSliderPanel.add(labelSizeField);

      sliderPanel.add(vertexSizeSliderPanel);
      sliderPanel.add(new JLabel(""));
      sliderPanel.add(labelSizeSliderPanel);

      bottomButtonPane = new JPanel();
      bottomButtonPane.setBackground(rightColor);

      bottomButtonPane.addMouseListener(this);

      bottomButtonPane.setLayout(new GridLayout((int)Math.ceil(bottomChoices/2.0),2));

      for(int i=choices; i<choices+bottomChoices; i++)
      {
         labels[i] = new JLabel(icons[i][0]);
         labels[i].addMouseListener(this);
         labels[i].setMaximumSize(new Dimension(icons[i][0].getIconWidth(),icons[i][0].getIconHeight()));
         labels[i].setToolTipText(toolTipsForLabels[i]);
         bottomButtonPane.add(labels[i]);
      }



      int blanks = 4;
      JPanel []blankPanels = new JPanel[blanks];
      JLabel []blankLabels = new JLabel[blanks];
      for(int i=0; i<blanks; i++)
      {
         blankPanels[i] = new JPanel();
         blankPanels[i].setBackground(rightColor);
         blankLabels[i] = new JLabel(new ImageIcon("pics/blank.png"));
         blankPanels[i].add(blankLabels[i]);
      }


      selectButtonPane = new JPanel();
      selectButtonPane.setBackground(rightColor);

      selectButtonPane.addMouseListener(this);

      selectButtonPane.setLayout(new GridLayout((int)Math.ceil(selectChoices/2.0),2));

      for(int i=choices+bottomChoices; i<choices+bottomChoices+selectChoices; i++)
      {
         labels[i] = new JLabel(icons[i][0]);
         labels[i].addMouseListener(this);
         labels[i].setMaximumSize(new Dimension(icons[i][0].getIconWidth(),icons[i][0].getIconHeight()));
         labels[i].setToolTipText(toolTipsForLabels[i]);
         selectButtonPane.add(labels[i]);
      }

      int blankCounter = 0;
      graphEditPane.add(blankPanels[blankCounter++]);
      graphEditPane.add(buttonPane);
      graphEditPane.add(blankPanels[blankCounter++]);
      graphEditPane.add(sliderPanel);
      graphEditPane.add(blankPanels[blankCounter++]);
      graphEditPane.add(bottomButtonPane);
      graphEditPane.add(blankPanels[blankCounter++]);
      graphEditPane.add(selectButtonPane);



   }

   public void exit()
   {
      int tabs = tabbedPane.getTabCount();
      int offset = 0;

      for(int i=0; i < tabs; i++)
      {
         if(!((GraphPane)tabbedPane.getComponentAt(i-offset)).getUndoState().getLastSave())
         {
            JOptionPane jop = new JOptionPane();
            String name = tabbedPane.getTitleAt(i-offset);
            name = name.substring(0,name.length()-2);
            int value = jop.showConfirmDialog(parent,"Graph " + name + " has not been saved! Are you sure you want to close?","Graph not saved",JOptionPane.YES_NO_OPTION);
            if(value == JOptionPane.NO_OPTION)
               return;
         }

                     /*windowItem.remove(graphPanels.get(i+1-offset).getMenuItem());
                     graphPanels.delete(i+1-offset);
                     tabbedPane.remove(i-offset);
                     //windowItem.remove(graphPanels.get(tabbedPane.getSelectedIndex()+1).getMenuItem());
                     //graphPanels.delete(tabbedPane.getSelectedIndex()+1);
                     //tabbedPane.remove(tabbedPane.getSelectedIndex());
                     offset++;*/
      }



      System.exit(0);
   }

   public void windowActivated(WindowEvent e)
   {

   }

   public void windowClosed(WindowEvent e)
   {


   }

   public void windowClosing(WindowEvent e)
   {
      exit();
   }

   public void windowDeactivated(WindowEvent e)
   {

   }

   public void windowDeiconified(WindowEvent e)
   {

   }

   public void windowIconified(WindowEvent e)
   {

   }

   public void windowOpened(WindowEvent e)
   {

   }

   public void mouseClicked(MouseEvent e)
   {
   }

   public void mouseEntered(MouseEvent e)
   {
      if(e.getSource() == graphEditPane || e.getSource() == buttonPane)
      {
         setCursor(defaultCursor);
      }
      if(tabbedPane.getSelectedIndex() != -1)
      {
         if(e.getSource() == tabbedPane.getSelectedComponent())
         {
            setCursor(defaultCursor);
         }
      }

      for(int i=0; i<choices+bottomChoices+selectChoices; i++)
         if(e.getSource() == labels[i])
         {
            setCursor(defaultCursor);
            if(chosen[i])
            {
               labels[i].setIcon(icons[i][3]);
            }
            else if(pressed[i])
            {
               labels[i].setIcon(icons[i][2]);
            }
            else
            {
               labels[i].setIcon(icons[i][1]);
            }
            highlighted[i] = true;
         }
   }

   public void mouseExited(MouseEvent e)
   {
      if(e.getSource() == graphEditPane || e.getSource() == buttonPane || e.getSource() == bottomButtonPane)
      {
         setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
      if(tabbedPane.getSelectedIndex() != -1)
      {
         if(e.getSource() == tabbedPane.getSelectedComponent())
         {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
         }
      }
      for(int i=0; i<choices+bottomChoices+selectChoices; i++)
         if(e.getSource() == labels[i])
         {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            if(!pressed[i])
            {
               if(chosen[i])
               {
                  labels[i].setIcon(icons[i][3]);
               }
               else
               {
                  labels[i].setIcon(icons[i][0]);
               }
            }
            highlighted[i] = false;
         }
   }

   public void mousePressed(MouseEvent e)
   {

      if(e.getButton() == MouseEvent.BUTTON1)
      {



         for(int i=0; i<choices+bottomChoices+selectChoices; i++)
            if(e.getSource() == labels[i])
            {
               labels[i].setIcon(icons[i][2]);
               pressed[i] = true;
            }
      }

      //cancel spring timer here
      GraphPane gp = ((GraphPane)tabbedPane.getSelectedComponent());
      gp.cancelSpring();
   }

   public void mouseReleased(MouseEvent e)
   {
      if(e.getButton() == MouseEvent.BUTTON1)
      {
         for(int i=0; i<choices; i++)
            if(e.getSource() == labels[i])
            {
               if(pressed[i] && highlighted[i])
                  if(chosen[i])
                  {
                     chosen[i] = false;
                     changeSelectedOption(-1);
                     changeCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                     labels[i].setIcon(icons[i][1]);
                  }
                  else
                     for(int j=0; j<choices+bottomChoices+selectChoices; j++)
                     {
                        if(j >= choices && j < choices+bottomChoices)
                           continue;
                        if(j == i)
                        {
                           chosen[i] = true;
                           changeSelectedOption(i);
                           changeCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                           labels[i].setIcon(icons[i][3]);
                        }
                        else
                        {
                           chosen[j] = false;
                           labels[j].setIcon(icons[j][0]);
                        }
                     }
            }

         for(int i=choices; i<choices+bottomChoices; i++)
            if(e.getSource() == labels[i])
            {
               if(pressed[i] && highlighted[i])
               {
                  if(tabbedPane.getSelectedIndex() != -1)
                  {
                     chosen[i] = true;
                     //pressed[i] = false;
                     //highlighted[i] = false;
                     labels[i].setIcon(icons[i][3]);
                     repaint();
                     executeBottomFunction(i);
                     chosen[i] = false;
                     repaint();
                  }
                  /*else
                  {
                     pressed[i] = false;
                     labels[i].setIcon(icons[i][2]);
                     repaint();
                  }*/
               }
            }

         for(int i=choices+bottomChoices; i<choices+bottomChoices+selectChoices; i++)
            if(e.getSource() == labels[i])
            {
               if(pressed[i] && highlighted[i])
               {
                  if(chosen[i])
                  {
                     chosen[i] = false;
                     changeSelectedOption(-1);
                     changeCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                     labels[i].setIcon(icons[i][1]);
                  }
                  else
                  {
                     for(int j=0; j<choices+bottomChoices+selectChoices; j++)
                     {
                        if(j >= choices && j < choices+bottomChoices)
                           continue;
                        if(j == i)
                        {
                           chosen[i] = true;
                           changeSelectedOption(i);
                           changeCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                           labels[i].setIcon(icons[i][3]);
                        }
                        else
                        {
                           chosen[j] = false;
                           labels[j].setIcon(icons[j][0]);
                        }
                     }

                  }
               }
            }


         for(int i=0; i<pressed.length; i++)
         {
            pressed[i] = false;
            if(chosen[i])
               labels[i].setIcon(icons[i][3]);
            else if(highlighted[i])
               labels[i].setIcon(icons[i][1]);
            else
               labels[i].setIcon(icons[i][0]);
         }
      }
   }

/*
   public void keyPressed(KeyEvent e)
   {
      //System.out.println(e);
      if(e.getKeyCode() == KeyEvent.VK_CONTROL)
      {
         ctrlPressed = true;
      }
   }

   public void keyReleased(KeyEvent e)
   {
      //System.out.println(e);
      if(e.getKeyCode() == KeyEvent.VK_CONTROL)
      {
         ctrlPressed = false;
      }
      if(ctrlPressed)
      {
         if(e.getKeyCode() == KeyEvent.VK_Y)
         {
            if(redoEditItem.isEnabled())
               ((GraphPane)tabbedPane.getSelectedComponent()).redo();
         }
         else if(e.getKeyCode() == KeyEvent.VK_Z)
         {
            if(undoEditItem.isEnabled())
               ((GraphPane)tabbedPane.getSelectedComponent()).undo();
         }
      }
   }

   public void keyTyped(KeyEvent e)
   {
      //System.out.println(e);
      //keyReleased(e);
   }
   */

   public void changeCursor(Cursor cursor)
   {
      setCursor(cursor);
      defaultCursor = cursor;
      graphPanels.setDefaultCursors(cursor);
      repaint();
      validate();
   }

   public void changeSelectedOption(int so)
   {
      selectedOption = so;
      graphPanels.setSelectedOptions(so);
      for(int i=0; i<choices; i++)
         if(selectedOption == i)
         {
            chosen[i] = true;
            labels[i].setIcon(icons[i][3]);
         }
         else
         {
            chosen[i] = false;
            labels[i].setIcon(icons[i][0]);
         }
   }

   public void executeBottomFunction(int function)
   {
      if(function < choices || function >= choices + bottomChoices)
         return;

      if(function == BOTTOM_ARRANGE)
      {
         Graph gr = ((GraphPane)tabbedPane.getSelectedComponent()).getGraph();
         ArrangeDialog ad = new ArrangeDialog(this,gr.getN(),gr.getContour());

         if(!ad.getCancelled())
         {
            ((GraphPane)tabbedPane.getSelectedComponent()).setUndoState();

            gr.setContour(ad.getContour());
            gr.createCircle();
            //int []jjj = ad.getContour();
            //System.out.println(jjj[4]);
            //((GraphPane)tabbedPane.getSelectedComponent()).repaint();
            fitToScreen();
         }
      }
      if(function == BOTTOM_PROPERTIES)
      {
         GraphPane gp = ((GraphPane)tabbedPane.getSelectedComponent());
         PropertiesDialog pd = new PropertiesDialog(this,gp);
      }
      if(function == BOTTOM_EDITEDGES)
      {
         GraphPane gp = ((GraphPane)tabbedPane.getSelectedComponent());
         Graph gr = gp.getGraph();
         int N = gr.getN();
         EditEdgesDialog eed = new EditEdgesDialog(this,gr.getArcs(),gr.getDegrees(),N);

         if(!eed.getCancelled())
         {
            gp.setUndoState();

            gr.setArcs(eed.getArcs(),eed.getDegrees(),eed.getNodes(),true);
            if(eed.getArrangeContour())
            {
               gr.createCircle();
               fitToScreen();
            }
            else
               gp.repaint();
         }
      }
      if(function == BOTTOM_SPRING){
         GraphPane gp = ((GraphPane)tabbedPane.getSelectedComponent());
         Graph gr = gp.getGraph();
         if(!gr.calculatingSpring) {
            gp.beginSpring();
         }
         /*if(!gr.calculatingSpring) {
            double[] oldX = gr.getXPoses().clone();
            double[] oldY = gr.getYPoses().clone();


            gr.springLayout(gp.radius);
            gp.undoStream.moveVertex(oldX, oldY, gr.getXPoses(), gr.getYPoses());
            gp.validate();
            gp.repaint();
         }*/
      }

      chosen[function] = false;
      labels[function].setIcon(icons[function][0]);
      repaint();

   }

   public GraphPane makeGraphPanel()
   {
      GraphPane graphPanel = new GraphPane(this);
      graphPanel.setBackground(Color.WHITE);
      graphPanel.setSize(5000,5000);

                     //graphPanel.setGraph(graphs.get(graphs.getNumber()));
      graphPanel.setGraph(graph);
      graphPanel.setSelectedOption(selectedOption);
      graphPanel.setDefaultCursor(defaultCursor);

      graphPanel.setDisplayVertexLabels(settings_displayVertexLabels);

      graphPanels.add(graphPanel);

      graphPanel.addMouseListener(this);
      //displayVertexLabelsItem.setState(true);

      return graphPanel;
   }





   public void createWindowItem(GraphPane graphPanel, String name)
   {
      JMenuItem newGraphItem = new JMenuItem(name);
      windowItem.add(newGraphItem);
      graphPanel.setMenuItem(newGraphItem);
      newGraphItem.addActionListener(
            new ActionListener()
            {
               public void actionPerformed(ActionEvent e)
               {
                  JMenuItem menuItem = (JMenuItem)e.getSource();
                  for(int i=1; i<=graphPanels.getNumber(); i++)
                  {
                     if(graphPanels.get(i).getMenuItem().equals(menuItem))
                     {
                        tabbedPane.setSelectedIndex(i-1);
                        break;
                     }
                  }
               }
            });
   }

   public void openGraphASC(File file)
   {

      try
      {
         int graphsToDo = -1;
         long graphsToDoLong = 0;

         BufferedReader br = new BufferedReader(new FileReader(file));
         String line = br.readLine();
         while(line != null)
         {
            if(line.length() > 4 && line.substring(0,5).equals("Graph"))
               graphsToDoLong++;
            line = br.readLine();
         }

         if(graphsToDoLong == 0)
         {
            // Wrong format?
         }

         if(graphsToDoLong == 1)
         {
            graph = new Graph(0,0);
            int node = 0;
            br = new BufferedReader(new FileReader(file));
            line = br.readLine();
            while(line.length() <= 4 || !line.substring(0,5).equals("Graph"))
               line = br.readLine();

            br.readLine();
            line = br.readLine();
            int count = 0;
            while(line.length() < 12 || !line.substring(0,12).equals("Taillenweite"))
            {
               count++;
               if(count > node)
               {
                  node = count;
                  graph.setN(node);
               }
               StringTokenizer tokens = new StringTokenizer(line);
               if(Integer.parseInt(tokens.nextToken()) != count)
               {
                  // Something strange
               }
               tokens.nextToken();
               while(tokens.hasMoreTokens())
               {
                  int newNode = Integer.parseInt(tokens.nextToken());
                  if(newNode > node)
                  {
                     node = newNode;
                     graph.setN(node);
                  }
                  graph.addArc(count,newNode);
               }

               line = br.readLine();
            }

            int []contour = new int[node];
            for(int i=0; i<node; i++)
               contour[i] = i;
            graph.setContour(contour);
            graph.createCircle();


            GraphPane graphPanel = makeGraphPanel();

            tabbedPane.add(graphPanel, file.getName());
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
            createWindowItem(graphPanel,file.getName());
            //graphHolderPanel.revalidate();
                     //graphHolderPanel.repaint();
                     //tabbedPane.repaint();
            validate();
            fitToScreen();
         }
         else
         {
            boolean []openGraphs = new boolean[0];
            boolean useOpenGraphs = true;
            long [][]graphChoices = new long[0][0];
            if(graphsToDoLong > Integer.MAX_VALUE)
            {
               // TOO BIG
               SelectFilesBlind sfb = new SelectFilesBlind(this,graphsToDoLong);

               if(sfb.getCancelled())
                  return;

               graphChoices = sfb.getGraphChoices();
               useOpenGraphs = false;
            }
            else
            {
               graphsToDo = (int)graphsToDoLong;
               SelectFiles sf = new SelectFiles(this,file.getName(),graphsToDo);

               if(sf.getCancelled())
                  return;

               openGraphs = sf.getOpenGraphs();

            }

            int graphIndex = 0;

            br = new BufferedReader(new FileReader(file));
            for(long graphcount=0; graphcount<graphsToDoLong; graphcount++)
            {
               line = br.readLine();
               while(line.length() <= 4 || !line.substring(0,5).equals("Graph"))
                  line = br.readLine();

               if((useOpenGraphs && openGraphs[(int)graphcount]) || (!useOpenGraphs && graphChoices[0][graphIndex]-1 <= graphcount && graphChoices[1][graphIndex]-1 >= graphcount))
               {
                  if(!useOpenGraphs && graphChoices[1][graphIndex]-1 == graphcount)
                     graphIndex++;
                  br.readLine();
                  line = br.readLine();

                  graph = new Graph(0,0);
                  int node = 0;

                  int count = 0;
                  while(line.length() < 12 || !line.substring(0,12).equals("Taillenweite"))
                  {
                     count++;
                     if(count > node)
                     {
                        node = count;
                        graph.setN(node);
                     }
                     StringTokenizer tokens = new StringTokenizer(line);
                     if(Integer.parseInt(tokens.nextToken()) != count)
                     {
                        // Something strange
                     }
                     tokens.nextToken();
                     while(tokens.hasMoreTokens())
                     {
                        int newNode = Integer.parseInt(tokens.nextToken());
                        if(newNode > node)
                        {
                           node = newNode;
                           graph.setN(node);
                        }
                        graph.addArc(count,newNode);
                     }

                     line = br.readLine();
                  }

                  int []contour = new int[node];
                  for(int i=0; i<node; i++)
                     contour[i] = i;
                  graph.setContour(contour);
                  graph.createCircle();

                  GraphPane graphPanel = makeGraphPanel();

                  tabbedPane.add(graphPanel, file.getName() + " #" + (graphcount+1));
                  tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
                  createWindowItem(graphPanel,file.getName() + " #" + (graphcount+1));
                     //graphHolderPanel.revalidate();
                     //graphHolderPanel.repaint();
                     //tabbedPane.repaint();
                  validate();
                  fitToScreen();

                  if(!useOpenGraphs && graphIndex >= graphChoices[0].length)
                     break;

               }
            }
         }


      }
      catch(Exception e)
      {
         System.out.println(e);
      }

   }

   public void openGraphEdgeList(File file)
   {
      int [][]arcs;
      int maxNode;
      int maxDegree;
      int count;

      try
      {
         BufferedReader br = new BufferedReader(new FileReader(file));

         String line = br.readLine();
         count = 0;

         while(line != null)
         {
            count++;
            line = br.readLine();
         }
         br.close();

         br = new BufferedReader(new FileReader(file));

         arcs = new int[count][2];

         line = br.readLine();

         count = 0;
         while(line != null)
         {
            StringTokenizer tokens = new StringTokenizer(line);
            arcs[count][0] = Integer.parseInt(tokens.nextToken());
            arcs[count++][1] = Integer.parseInt(tokens.nextToken());
            line = br.readLine();
         }

         br.close();

         maxNode = 0;

         for(int i=0; i<count; i++)
            for(int j=0; j<2; j++)
               if(arcs[i][j] > maxNode)
                  maxNode = arcs[i][j];


         int[] degrees = new int[maxNode];
         for(int i=0; i<count; i++)
            degrees[arcs[i][0]-1]++;

         maxDegree = 0;
         for(int i=0; i<maxNode; i++)
            if(degrees[i] > maxDegree)
               maxDegree = degrees[i];

         graph = new Graph(maxNode,maxDegree);
         graph.addArcs(arcs);

         for(int i=0; i<count; i++)
         {
            boolean both = false;
            for(int j=0; j<count; j++)
            {
               if(arcs[i][0] == arcs[j][1] && arcs[i][1] == arcs[j][0])
               {
                  both = true;
                  break;
               }
            }
            if(!both)
            {

               graph.addArc(arcs[i][1],arcs[i][0]);
               //System.out.println("Added " + arcs[i][1] + "," + arcs[i][0]);
            }
         }





         GraphPane graphPanel = makeGraphPanel();

         tabbedPane.add(graphPanel, file.getName());
         tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
         createWindowItem(graphPanel,file.getName());

                     //graphHolderPanel.revalidate();
                     //graphHolderPanel.repaint();
                     //tabbedPane.repaint();
         validate();
         fitToScreen();
            //graphs.add(graph);

      }
      catch(Exception e)
      {
         System.out.println(e);
      }
   }

   public void openGraphHCP(File file)
   {
      int [][]arcs;
      int maxNode;
      int maxDegree;
      int count;

      try
      {
         BufferedReader br = new BufferedReader(new FileReader(file));

         String line = br.readLine();
         line = br.readLine();
         line = br.readLine();
         line = br.readLine();

         maxNode = Integer.parseInt(line.substring(line.indexOf(":")+2));

         line = br.readLine();
         line = br.readLine();
         line = br.readLine();

         count = 0;

         while(line.indexOf("-1") == -1)
         {
            count++;
            line = br.readLine();
         }
         br.close();

         br = new BufferedReader(new FileReader(file));

         arcs = new int[count][2];

         line = br.readLine();
         line = br.readLine();
         line = br.readLine();
         line = br.readLine();
         line = br.readLine();
         line = br.readLine();
         line = br.readLine();

         count = 0;
         while(line.indexOf("-1") == -1)
         {
            StringTokenizer tokens = new StringTokenizer(line);
            arcs[count][0] = Integer.parseInt(tokens.nextToken());
            arcs[count++][1] = Integer.parseInt(tokens.nextToken());
            line = br.readLine();
         }

         br.close();

         /*maxNode = 0;

         for(int i=0; i<count; i++)
            for(int j=0; j<2; j++)
               if(arcs[i][j] > maxNode)
                  maxNode = arcs[i][j];*/


         int[] degrees = new int[maxNode];
         for(int i=0; i<count; i++)
            degrees[arcs[i][0]-1]++;

         maxDegree = 0;
         for(int i=0; i<maxNode; i++)
            if(degrees[i] > maxDegree)
               maxDegree = degrees[i];

         graph = new Graph(maxNode,maxDegree);
         graph.addArcs(arcs);

         for(int i=0; i<count; i++)
         {
            boolean both = false;
            for(int j=0; j<count; j++)
            {
               if(arcs[i][0] == arcs[j][1] && arcs[i][1] == arcs[j][0])
               {
                  both = true;
                  break;
               }
            }
            if(!both)
            {
               graph.addArc(arcs[i][1],arcs[i][0]);
               //System.out.println("Added " + arcs[i][1] + "," + arcs[i][0]);
            }
         }

         GraphPane graphPanel = makeGraphPanel();

         tabbedPane.add(graphPanel, file.getName());
         tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
         createWindowItem(graphPanel,file.getName());

                     //graphHolderPanel.revalidate();
                     //graphHolderPanel.repaint();
                     //tabbedPane.repaint();
         validate();
         fitToScreen();
            //graphs.add(graph);

      }
      catch(Exception e)
      {
         System.out.println(e);
      }
   }

   public int readStream(DataInputStream di)
   {
      int read;
      try
      {
         read = Integer.parseInt("" + di.readByte());
      }
      catch(Exception e)
      {
         read = -1;
      }
      return read;
   }

   public long[] getSCDData(File file)
   {
      DataInputStream di = null;
      try
      {
         di = new DataInputStream(new FileInputStream(file));
      }
      catch(Exception e)
      {
         System.out.println(e);
      }
      int degree = 1;
      int N = 0;
      long graphsToDo = 0;

      int[] degrees = new int[257];
      readStream(di);
      int oldRead = readStream(di);
      int maxNode = oldRead;
      degrees[oldRead-1]++;
      int read = readStream(di);
      while(read > oldRead)
      {

         maxNode = read;
         degrees[0]++;
         degrees[read-1]++;
         degree++;
         oldRead = read;
         read = readStream(di);
      }

      degrees[0] = degree;

      //System.out.println("Predicted degree is " + degree);

      boolean graphFinished = false;
      int node = 2;
      boolean needToStart = true;
      while(needToStart)
      {
         needToStart = false;
         while(!graphFinished)
         {
            //System.out.println("Node: " + node + ", Read: " + read);
            degrees[node-1]++;
            degrees[read-1]++;
            if(read > maxNode)
               maxNode = read;

            graphFinished = true;
            for(int i=0; i<maxNode; i++)
               if(degrees[i] != degree)
               {
                  graphFinished = false;
                  break;
               }

            //System.out.println("Degrees are : " + degrees[0] + " " + degrees[1] + " " + degrees[2] + " " + degrees[3] + " " + degrees[4] + " " + degrees[5] + " " + degrees[6] + " " + degrees[7] + " " + degrees[8] + " " + degrees[9] + " " + degrees[10] + " " + degrees[11] + " " + degrees[12] + " " + degrees[13] + " " + degrees[14] + " " + degrees[15]);


            if(!graphFinished)
            {
               oldRead = read;
               read = readStream(di);

               if((degrees[node-1] != degree && read < oldRead) || read <= node || degrees[oldRead-1] > degree)
               {
                  degree--;
                  //System.out.println("Predicted degree is " + degree);
                  for(int i=0; i<maxNode; i++)
                     degrees[i] = 0;


                  maxNode = 1;
                  node = 1;
                  if(degree < 3)
                  {
                     System.out.println("SCD file is invalid");
                     return new long[3];
                  }
                  try
                  {
                     di.close();
                     di = new DataInputStream(new FileInputStream(file));
                  }
                  catch(Exception e)
                  {
                     System.out.println(e);
                  }
                  readStream(di);
                  read = readStream(di);
               }

               while(degrees[node-1] == degree)
                  node++;

            }

         }

         graphsToDo++;

         int numberToSkip = readStream(di);
         while(numberToSkip != -1)
         {
            graphsToDo++;
            for(int i=0; i<maxNode*degree/2 - numberToSkip; i++)
               if(readStream(di) == -1)
               {
                  needToStart = true;
                  break;
               }
            numberToSkip = readStream(di);
         }

         if(needToStart)
         {
            graphsToDo = 0;
            for(int i=0; i<maxNode; i++)
               degrees[i] = 0;

            degree--;
            maxNode = 1;
            node = 1;
            if(degree < 3)
            {
               System.out.println("SCD file is invalid");
               return new long[3];
            }

            try
            {
               di.close();
               di = new DataInputStream(new FileInputStream(file));
            }
            catch(Exception e)
            {
               System.out.println(e);
            }
            readStream(di);
            read = readStream(di);
         }
      }
      long[] scdData = new long[3];
      scdData[0] = maxNode;
      scdData[1] = degree;
      scdData[2] = graphsToDo;
      return scdData;
   }

   public void openGraphSCD(File file)
   {
      DataInputStream di = null;


      long []scdData = getSCDData(file);

      int maxNode = (int)scdData[0];
      int degree = (int)scdData[1];
      int graphsToDo = -1;
      long graphsToDoLong = scdData[2];
      if(maxNode != 0)
      {
         int []degrees = new int[257];
         int read = 0;
         int node = 1;

         try
         {
            di = new DataInputStream(new FileInputStream(file));
         }
         catch(Exception e)
         {
            System.out.println(e);
         }

         if(graphsToDoLong == 1)
         {
            graph = new Graph(maxNode,degree);
            readStream(di);
            for(int i=0; i<maxNode; i++)
               degrees[i] = 0;
            node = 1;
            for(int i=0; i<maxNode*degree/2; i++)
            {
               read = readStream(di);
               degrees[node-1]++;
               degrees[read-1]++;
               graph.addArc(node,read);
               graph.addArc(read,node);
               while(degrees[node-1] >= degree)
                  node++;
            }

            GraphPane graphPanel = makeGraphPanel();

            tabbedPane.add(graphPanel, file.getName());
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
            createWindowItem(graphPanel,file.getName());

                     //graphHolderPanel.revalidate();
                     //graphHolderPanel.repaint();
                     //tabbedPane.repaint();
            validate();
            fitToScreen();

         }
         else
         {
            //if(graphsToDoLong > Integer.MAX_VALUE)
            boolean []openGraphs = new boolean[0];
            boolean useOpenGraphs = true;
            long [][]graphChoices = new long[0][0];
            if(graphsToDoLong > Integer.MAX_VALUE)
            {
               // TOO BIG
               SelectFilesBlind sfb = new SelectFilesBlind(this,graphsToDoLong);

               if(sfb.getCancelled())
                  return;

               graphChoices = sfb.getGraphChoices();
               useOpenGraphs = false;
            }
            else
            {
               graphsToDo = (int)graphsToDoLong;
               SelectFiles sf = new SelectFiles(this,file.getName(),graphsToDo);

               if(sf.getCancelled())
                  return;

               openGraphs = sf.getOpenGraphs();

            }

            int []stream = new int[maxNode*degree/2];
            int graphIndex = 0;

            for(long graphcount=0; graphcount<graphsToDoLong; graphcount++)
            {
               int index = readStream(di);
               for(int i=index; i<maxNode*degree/2; i++)
                  stream[i] = readStream(di);


               if((useOpenGraphs && openGraphs[(int)graphcount]) || (!useOpenGraphs && graphChoices[0][graphIndex]-1 <= graphcount && graphChoices[1][graphIndex]-1 >= graphcount))
               {
                  if(!useOpenGraphs && graphChoices[1][graphIndex]-1 == graphcount)
                     graphIndex++;
                  graph = new Graph(maxNode,degree);
                  for(int i=0; i<maxNode; i++)
                     degrees[i] = 0;
                  node = 1;
                  for(int i=0; i<stream.length; i++)
                  {
                     degrees[node-1]++;
                     degrees[stream[i]-1]++;
                     graph.addArc(node,stream[i]);
                     graph.addArc(stream[i],node);
                     while(degrees[node-1] >= degree)
                        node++;
                  }

                  GraphPane graphPanel = makeGraphPanel();

                  tabbedPane.add(graphPanel, (file.getName() + " #" + (graphcount+1)));
                  tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
                  createWindowItem(graphPanel,file.getName() + " #" + (graphcount+1));

                     //graphHolderPanel.revalidate();
                     //graphHolderPanel.repaint();
                     //tabbedPane.repaint();
                  validate();
                  fitToScreen();
                  if(!useOpenGraphs && graphIndex >= graphChoices[0].length)
                     break;
               }


            }
            try
            {
               di.close();
            }
            catch(Exception e)
            {
               System.out.println(e);
            }

         }
      }
   }


   public void openGraph6(File file)
   {
      int [][]arcs;
      int maxNode = 0;
      int maxDegree = 0;
      int count = 0;

      try
      {
         BufferedReader br = new BufferedReader(new FileReader(file));
         String graphLine = br.readLine();
         int g6count = -1;
         long g6countLong = 0;
         while(graphLine != null)
         {
            g6countLong++;
            graphLine = br.readLine();
         }
         br.close();

         if(g6countLong == 1)
         {
            br = new BufferedReader(new FileReader(file));
            graphLine = br.readLine();


            int lineIndex = 0;

            int headerAscii = graphLine.charAt(lineIndex++);

            if(headerAscii == 126)
            {
               int headerAscii2 = graphLine.charAt(lineIndex++);
               if(headerAscii2 == 126)
               {
                  System.out.println("UGV does not support graphs of this size.");
               }
               else
               {
                  int headerAscii3 = graphLine.charAt(lineIndex++);
                  int headerAscii4 = graphLine.charAt(lineIndex++);
                  String binaryString = intToBinary(headerAscii2-63) + intToBinary(headerAscii3-63) + intToBinary(headerAscii4-63);

                  maxNode = binaryToInt(binaryString);
               }
            }
            else
            {
               maxNode = headerAscii - 63;
               //System.out.println(maxNode);
            }

            String graphString = "";
            for(int i=lineIndex; i<graphLine.length(); i++)
               graphString += intToBinary(((int)graphLine.charAt(i))-63);

            //System.out.println(graphString);

            graph = new Graph(maxNode,1);

            int arcCount = 0;
            for(int i=1; i<maxNode; i++)
               for(int j=0; j<i; j++)
                  if(graphString.charAt(arcCount++) == '1')
                  {
                     graph.addArc(i+1,j+1);
                     graph.addArc(j+1,i+1);
                  }
            GraphPane graphPanel = makeGraphPanel();

            tabbedPane.add(graphPanel, file.getName());
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
            createWindowItem(graphPanel,file.getName());
                     //graphHolderPanel.revalidate();
                     //graphHolderPanel.repaint();
                     //tabbedPane.repaint();
            validate();
            fitToScreen();

            br.close();
         }
         else if(g6countLong > 1)
         {
            boolean []openGraphs = new boolean[0];
            boolean useOpenGraphs = true;
            long [][]graphChoices = new long[0][0];
            if(g6countLong > Integer.MAX_VALUE)
            {
               // TOO BIG
               SelectFilesBlind sfb = new SelectFilesBlind(this,g6countLong);

               if(sfb.getCancelled())
                  return;

               graphChoices = sfb.getGraphChoices();
               useOpenGraphs = false;
            }
            else
            {
               g6count = (int)g6countLong;
               SelectFiles sf = new SelectFiles(this,file.getName(),g6count);

               if(sf.getCancelled())
                  return;

               openGraphs = sf.getOpenGraphs();

            }

            int graphIndex = 0;

            br = new BufferedReader(new FileReader(file));
            for(long graphcount=0; graphcount<g6countLong; graphcount++)
            {
               graphLine = br.readLine();
               if((useOpenGraphs && openGraphs[(int)graphcount]) || (!useOpenGraphs && graphChoices[0][graphIndex]-1 <= graphcount && graphChoices[1][graphIndex]-1 >= graphcount))
               {
                  if(!useOpenGraphs && graphChoices[1][graphIndex]-1 == graphcount)
                     graphIndex++;


                  int lineIndex = 0;

                  int headerAscii = graphLine.charAt(lineIndex++);

                  if(headerAscii == 126)
                  {
                     int headerAscii2 = graphLine.charAt(lineIndex++);
                     if(headerAscii2 == 126)
                     {
                        System.out.println("UGV does not support graphs of this size.");
                     }
                     else
                     {

                        int headerAscii3 = graphLine.charAt(lineIndex++);
                        int headerAscii4 = graphLine.charAt(lineIndex++);
                        String binaryString = intToBinary(headerAscii2-63) + intToBinary(headerAscii3-63) + intToBinary(headerAscii4-63);

                        maxNode = binaryToInt(binaryString);
                     }
                  }
                  else
                  {
                     maxNode = headerAscii - 63;
                     //System.out.println(maxNode);
                  }

                  String graphString = "";
                  for(int i=lineIndex; i<graphLine.length(); i++)
                     graphString += intToBinary(((int)graphLine.charAt(i))-63);

                  //System.out.println(graphString);

                  graph = new Graph(maxNode,1);

                  int arcCount = 0;
                  for(int i=1; i<maxNode; i++)
                     for(int j=0; j<i; j++)
                        if(graphString.charAt(arcCount++) == '1')
                        {
                           graph.addArc(i+1,j+1);
                           graph.addArc(j+1,i+1);
                        }

                  GraphPane graphPanel = makeGraphPanel();

                  tabbedPane.add(graphPanel, (file.getName() + " #" + (graphcount+1)));
                  tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
                  createWindowItem(graphPanel,file.getName() + " #" + (graphcount+1));
                     //graphHolderPanel.revalidate();
                     //graphHolderPanel.repaint();
                     //tabbedPane.repaint();

                  validate();
                  fitToScreen();
                  if(!useOpenGraphs && graphIndex >= graphChoices[0].length)
                     break;

               }
            }
            br.close();



         }

         //graphs.add(graph);

      }
      catch(Exception e)
      {
         System.out.println(e);
      }
   }

   public void openGraphUGV(File file)
   {
      try
      {
         //DataInputStream di = new DataInputStream(new FileInputStream(file));
         BufferedReader br = new BufferedReader(new FileReader(file));

         int graphsToOpen = -1;
         //long graphsToOpenLong = di.readLong();
         long graphsToOpenLong = Long.parseLong(br.readLine());

         if(graphsToOpenLong == 1)
         {
            /*int N = di.readInt();
            double xScale = di.readDouble();
            double yScale = di.readDouble();
            int xTopLeft = di.readInt();
            int yTopLeft = di.readInt();
            int radius = di.readInt();
            int xPos[] = new int[N];
            int yPos[] = new int[N];
            for(int i=0; i<N; i++)
            {
               xPos[i] = di.readInt();
               yPos[i] = di.readInt();
            }
            int []degrees = new int[N];
            int maxDegree = 0;
            for(int i=0; i<N; i++)
            {
               degrees[i] = di.readInt();
               if(degrees[i] > maxDegree)
                  maxDegree = degrees[i];
            }*/

            int N = Integer.parseInt(br.readLine());
            String line = br.readLine();
            StringTokenizer tokens = new StringTokenizer(line);
            double xScale = Double.parseDouble(tokens.nextToken());
            double yScale = Double.parseDouble(tokens.nextToken());
            int xTopLeft = Integer.parseInt(tokens.nextToken());
            int yTopLeft = Integer.parseInt(tokens.nextToken());
            int radius = Integer.parseInt(tokens.nextToken());
            String xPosesString = br.readLine();
            String yPosesString = br.readLine();
            StringTokenizer xTokens = new StringTokenizer(xPosesString);
            StringTokenizer yTokens = new StringTokenizer(yPosesString);
            double[] xPos = new double[N];
            double[] yPos = new double[N];
            for(int i=0; i<N; i++)
            {
               xPos[i] = Double.parseDouble(xTokens.nextToken());
               yPos[i] = Double.parseDouble(yTokens.nextToken());
            }
            int []degrees = new int[N];
            int maxDegree = 0;
            line = br.readLine();
            tokens = new StringTokenizer(line);
            for(int i=0; i<N; i++)
            {
               degrees[i] = Integer.parseInt(tokens.nextToken());
               if(degrees[i] > maxDegree)
                  maxDegree = degrees[i];
            }

            graph = new Graph(N,maxDegree);

            for(int i=0; i<N; i++)
            {
               String arcsString = br.readLine();
               tokens = new StringTokenizer(arcsString);
               for(int j=0; j<degrees[i]; j++)
               {
                  graph.addArc(i+1,Integer.parseInt(tokens.nextToken()));
               }
            }

            br.readLine(); // Should be -1


            for(int i=0; i<N; i++)
            {
               graph.setXPos(i,xPos[i]);
               graph.setYPos(i,yPos[i]);
            }

            GraphPane graphPanel = makeGraphPanel();

            /*GraphPane graphPanel = new GraphPane(this);
            graphPanel.setBackground(Color.WHITE);
            graphPanel.setSize(5000,5000);

                     //graphPanel.setGraph(graphs.get(graphs.getNumber()));
            graphPanel.setGraph(graph); */
            graphPanel.setScale(xScale,yScale);
            graphPanel.setTopLeft(xTopLeft,yTopLeft);
            graphPanel.setRadius(radius);

            /*graphPanels.add(graphPanel);

            graphPanel.addMouseListener(this);*/

            tabbedPane.add(graphPanel, (file.getName()));
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
            createWindowItem(graphPanel,file.getName());

                     //graphHolderPanel.revalidate();
                     //graphHolderPanel.repaint();
                     //tabbedPane.repaint();

            validate();
            repaint();
            //fitToScreen();


         }
         else
         {
            boolean []openGraphs = new boolean[0];
            boolean useOpenGraphs = true;
            long [][]graphChoices = new long[0][0];
            if(graphsToOpenLong > Integer.MAX_VALUE)
            {
               // TOO BIG
               SelectFilesBlind sfb = new SelectFilesBlind(this,graphsToOpenLong);

               if(sfb.getCancelled())
                  return;

               graphChoices = sfb.getGraphChoices();
               useOpenGraphs = false;
            }
            else
            {
               graphsToOpen = (int)graphsToOpenLong;
               SelectFiles sf = new SelectFiles(this,file.getName(),graphsToOpen);

               if(sf.getCancelled())
                  return;

               openGraphs = sf.getOpenGraphs();

            }

            int graphIndex = 0;

            for(long graphcount=0; graphcount<graphsToOpenLong; graphcount++)
            {
               /*int N = di.readInt();
               //System.out.println("N = " +N);
               double xScale = di.readDouble();
               double yScale = di.readDouble();
               int xTopLeft = di.readInt();
               int yTopLeft = di.readInt();
               int radius = di.readInt();
               int xPos[] = new int[N];
               int yPos[] = new int[N];
               for(int i=0; i<N; i++)
               {
                  xPos[i] = di.readInt();
                  yPos[i] = di.readInt();
                  //System.out.println("Position of node " + i + " = " + xPos[i] + "," + yPos[i]);
               }

               int []degrees = new int[N];
               int maxDegree = 0;
               for(int i=0; i<N; i++)
               {
                  degrees[i] = di.readInt();
                  //System.out.println("degree of node " + i + " = " + degrees[i]);
                  if(degrees[i] > maxDegree)
                     maxDegree = degrees[i];
               }
               int [][]arcs = new int[N][maxDegree];
               for(int i=0; i<N; i++)
                  for(int j=0; j<degrees[i]; j++)
                  {
                     arcs[i][j] = di.readInt();
                     //System.out.println("Arc from node " + i + " is " + arcs[i][j]);
                  }
               */

               int N = Integer.parseInt(br.readLine());
               String line = br.readLine();
               StringTokenizer tokens = new StringTokenizer(line);
               double xScale = Double.parseDouble(tokens.nextToken());
               double yScale = Double.parseDouble(tokens.nextToken());
               int xTopLeft = Integer.parseInt(tokens.nextToken());
               int yTopLeft = Integer.parseInt(tokens.nextToken());
               int radius = Integer.parseInt(tokens.nextToken());
               String xPosesString = br.readLine();
               String yPosesString = br.readLine();
               StringTokenizer xTokens = new StringTokenizer(xPosesString);
               StringTokenizer yTokens = new StringTokenizer(yPosesString);
               int[] xPos = new int[N];
               int[] yPos = new int[N];
               for(int i=0; i<N; i++)
               {
                  xPos[i] = Integer.parseInt(xTokens.nextToken());
                  yPos[i] = Integer.parseInt(yTokens.nextToken());
               }
               int []degrees = new int[N];
               int maxDegree = 0;
               line = br.readLine();
               tokens = new StringTokenizer(line);
               for(int i=0; i<N; i++)
               {
                  degrees[i] = Integer.parseInt(tokens.nextToken());
                  if(degrees[i] > maxDegree)
                     maxDegree = degrees[i];
               }

               //graph = new Graph(N,maxDegree);

               int [][]arcs = new int[N][maxDegree];

               for(int i=0; i<N; i++)
               {
                  String arcsString = br.readLine();
                  tokens = new StringTokenizer(arcsString);
                  for(int j=0; j<degrees[i]; j++)
                  {
                     arcs[i][j] = Integer.parseInt(tokens.nextToken());
                     //graph.addArc(i+1,Integer.parseInt(tokens.nextToken()));
                  }
               }


               br.readLine(); // Should be -1

               if((useOpenGraphs && openGraphs[(int)graphcount]) || (!useOpenGraphs && graphChoices[0][graphIndex]-1 <= graphcount && graphChoices[1][graphIndex]-1 >= graphcount))
               {
                  if(!useOpenGraphs && graphChoices[1][graphIndex]-1 == graphcount)
                     graphIndex++;
                  graph = new Graph(N,maxDegree);


                  for(int i=0; i<N; i++)
                     for(int j=0; j<degrees[i]; j++)
                        graph.addArc(i+1,arcs[i][j]);


                  for(int i=0; i<N; i++)
                  {
                     graph.setXPos(i,xPos[i]);
                     graph.setYPos(i,yPos[i]);
                  }

                  GraphPane graphPanel = makeGraphPanel();

                  /*GraphPane graphPanel = new GraphPane(this);
                  graphPanel.setBackground(Color.WHITE);
                  graphPanel.setSize(5000,5000);

                     //graphPanel.setGraph(graphs.get(graphs.getNumber()));
                  graphPanel.setGraph(graph);*/
                  graphPanel.setScale(xScale,yScale);

                  graphPanel.setTopLeft(xTopLeft,yTopLeft);
                  graphPanel.setRadius(radius);
                  /*
                  graphPanels.add(graphPanel);

                  graphPanel.addMouseListener(this);*/

                  tabbedPane.add(graphPanel, (file.getName() + " #" + (graphcount+1)));
                  tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
                  createWindowItem(graphPanel,file.getName() + " #" + (graphcount+1));

                     //graphHolderPanel.revalidate();
                     //graphHolderPanel.repaint();
                     //tabbedPane.repaint();

                  validate();
                  repaint();
                  if(!useOpenGraphs && graphIndex >= graphChoices[0].length)
                     break;



               }


            }
            br.close();

         }

         //graphs.add(graph);

      }
      catch(Exception e)
      {
         System.out.println(e);
      }
   }





   public void buildNewGraph()
   {
      graph = new Graph(0,0);

      GraphPane graphPanel = makeGraphPanel();

      int newGraph = 1;
      String newName = ("New Graph #" + newGraph);
      int tabCount = tabbedPane.getTabCount();
      int index = 0;
      while(index < tabCount)
      {
         String compareName = tabbedPane.getTitleAt(index);
         if(compareName.substring(compareName.length()-2).equals(" *"))
            compareName = compareName.substring(0,compareName.length()-2);
         if(newName.equals(compareName))
         {
            newName = ("New Graph #" + ++newGraph);
            index = 0;
         }
         else
            index++;
      }


      graphPanel.setDisplayVertexLabels(settings_displayVertexLabels);

      tabbedPane.add(graphPanel, newName);
      tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
      createWindowItem(graphPanel,newName);
      //displayVertexLabelsItem.setState(true);

                     //graphHolderPanel.revalidate();
                     //graphHolderPanel.repaint();
                     //tabbedPane.repaint();

      validate();

   }

   public String intToBinary(int number)
   {
   // Assumes number will be less than 64, the following is for testing purposes only!!!
      if(number > 64)
         System.out.println("NUMBER IS WRONG!!");

      String binary = "";

      for(int i=5; i>=0; i--)
         if(number >= Math.pow(2,i))
         {
            binary += "1";
            number -= Math.pow(2,i);
         }
         else
            binary += "0";

      return binary;
   }

   public int binaryToInt(String binary)
   {
      int number = 0;
      for(int i=1; i<=binary.length(); i++)
         number += Math.pow(2,Integer.parseInt(""+binary.charAt(binary.length()-i)));

      return number;
   }





   public void saveGraphASC(String filename, GraphPane graphPane)
   {
      File fileToSave = new File(filename);

      Graph graph = graphPane.getGraph();

      boolean save = true;
      boolean append = false;

      if(fileToSave.exists())
      {
         String []options = new String[3];
         options[0] = "Overwrite";
         options[1] = "Append";
         options[2] = "Cancel";
                              /*JOptionPane jop = new JOptionPane(("File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?"),JOptionPane.WARNING_MESSAGE,JOptionPane.YES_NO_CANCEL_OPTION);
                              jop.setOptions(options);*/

         JOptionPane jop = new JOptionPane("",JOptionPane.WARNING_MESSAGE);
         int option = jop.showOptionDialog(parent,"File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?","WARNING: File already exists!",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE,jop.getIcon(),options,options[2]);
         if(option == JOptionPane.NO_OPTION)
            append = true;
         if(option == JOptionPane.CANCEL_OPTION)
            save = false;
      }
      if(save)
      {
         try
         {
            int latestGraph = 0;
            if(append && fileToSave.exists())
            {
               BufferedReader br = new BufferedReader(new FileReader(fileToSave));
               String line = br.readLine();
               while(line != null)
               {
                  if(line.length() > 5 && line.substring(0,5).equals("Graph"))
                  {
                     int graphNumber = Integer.parseInt(line.substring(6,line.indexOf(":")));
                     if(graphNumber > latestGraph)
                        latestGraph = graphNumber;
                  }
                  line = br.readLine();
               }
               br.close();
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileToSave,append));

            bw.newLine();

            latestGraph++;

            int N = graph.getN();
            int [][]arcs = graph.getArcs();
            int []degrees = graph.getDegrees();


            bw.write(("Graph " +latestGraph+":"));
            bw.newLine();
            bw.newLine();

            for(int i=0; i<N; i++)
            {
               String writeLine = ((i+1)+" :");
               for(int j=0; j<degrees[i]; j++)
                  writeLine += (" "+arcs[i][j]);
               bw.write(writeLine);
               bw.newLine();
            }
            bw.write("Taillenweite: ");
            bw.newLine();
            bw.newLine();
            bw.newLine();

            bw.close();


         }
         catch(Exception e)
         {
            System.out.println(e);
         }
      }

   }

   public void saveMultipleGraphsASC(String filename, GraphPane []graphPanes)
   {
      File fileToSave = new File(filename);

      boolean save = true;
      boolean append = false;

      if(fileToSave.exists())
      {
         String []options = new String[3];
         options[0] = "Overwrite";
         options[1] = "Append";
         options[2] = "Cancel";
                              /*JOptionPane jop = new JOptionPane(("File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?"),JOptionPane.WARNING_MESSAGE,JOptionPane.YES_NO_CANCEL_OPTION);
                              jop.setOptions(options);*/

         JOptionPane jop = new JOptionPane("",JOptionPane.WARNING_MESSAGE);
         int option = jop.showOptionDialog(parent,"File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?","WARNING: File already exists!",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE,jop.getIcon(),options,options[2]);
         if(option == JOptionPane.NO_OPTION)
            append = true;
         if(option == JOptionPane.CANCEL_OPTION)
            save = false;
      }
      if(save)
      {

         try
         {
            int latestGraph = 0;
            if(append && fileToSave.exists())
            {
               BufferedReader br = new BufferedReader(new FileReader(fileToSave));
               String line = br.readLine();
               while(line != null)
               {
                  if(line.length() > 5 && line.substring(0,5).equals("Graph"))
                  {
                     int graphNumber = Integer.parseInt(line.substring(6,line.indexOf(":")));
                     if(graphNumber > latestGraph)
                        latestGraph = graphNumber;
                  }
                  line = br.readLine();
               }
               br.close();
            }

            for(int graphs=0; graphs<graphPanes.length; graphs++)
            {
               Graph graph = graphPanes[graphs].getGraph();


               BufferedWriter bw = new BufferedWriter(new FileWriter(fileToSave,append));

               bw.newLine();

               latestGraph++;

               int N = graph.getN();
               int [][]arcs = graph.getArcs();
               int []degrees = graph.getDegrees();


               bw.write(("Graph " +latestGraph+":"));
               bw.newLine();
               bw.newLine();

               for(int i=0; i<N; i++)
               {
                  String writeLine = ((i+1)+" :");
                  for(int j=0; j<degrees[i]; j++)
                     writeLine += (" "+arcs[i][j]);
                  bw.write(writeLine);
                  bw.newLine();
               }
               bw.write("Taillenweite: ");
               bw.newLine();
               bw.newLine();
               bw.newLine();

               bw.close();

               append = true;
            }

         }
         catch(Exception e)
         {
            System.out.println(e);
         }
      }

   }

   public void saveGraphEdgeList(String filename, GraphPane graphPane)
   {
      File fileToSave = new File(filename);

      Graph graph = graphPane.getGraph();

      boolean save = true;

      if(fileToSave.exists())
      {
         String []options = new String[2];
         options[0] = "Overwrite";
         options[1] = "Cancel";
                              /*JOptionPane jop = new JOptionPane(("File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?"),JOptionPane.WARNING_MESSAGE,JOptionPane.YES_NO_CANCEL_OPTION);
                              jop.setOptions(options);*/

         JOptionPane jop = new JOptionPane("",JOptionPane.WARNING_MESSAGE);
         int option = jop.showOptionDialog(parent,"File " + filename + " already exists. Do you want to overwrite the file?","WARNING: File already exists!",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE,jop.getIcon(),options,options[1]);
         if(option == JOptionPane.NO_OPTION)
            save = false;
      }
      if(save)
      {
         try
         {
            int [][]arcs = graph.getArcs();
            int []degrees = graph.getDegrees();

            if(degrees[degrees.length-1] == 0)
            {
               String []options = new String[2];
               options[0] = "Yes";
               options[1] = "No";
                              /*JOptionPane jop = new JOptionPane(("File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?"),JOptionPane.WARNING_MESSAGE,JOptionPane.YES_NO_CANCEL_OPTION);
                              jop.setOptions(options);*/

               JOptionPane jop = new JOptionPane("",JOptionPane.WARNING_MESSAGE);
               int option = jop.showOptionDialog(parent,"Edge-list format does not retain isolated vertices if they are labelled with the largest number. Are you sure you want to save?","WARNING: Edge-list format will not save full graph!",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE,jop.getIcon(),options,options[1]);
               if(option == JOptionPane.NO_OPTION)
                  return;
            }



            BufferedWriter bw = new BufferedWriter(new FileWriter(filename));

            for(int i=0; i<degrees.length; i++)
               for(int j=0; j<degrees[i]; j++)
                  if(arcs[i][j] > i+1)
                  {
                     bw.write((i+1) + " " + arcs[i][j]);
                     bw.newLine();
                  }

            bw.close();

         }
         catch(Exception e)
         {
            System.out.println(e);
         }
      }


   }

   public void saveGraph6(String filename, GraphPane graphPane)
   {

      Graph graph = graphPane.getGraph();

      File fileToSave = new File(filename);
      boolean save = true;
      boolean append = false;

      if(fileToSave.exists())
      {
         String []options = new String[3];
         options[0] = "Overwrite";
         options[1] = "Append";
         options[2] = "Cancel";
                              /*JOptionPane jop = new JOptionPane(("File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?"),JOptionPane.WARNING_MESSAGE,JOptionPane.YES_NO_CANCEL_OPTION);
                              jop.setOptions(options);*/

         JOptionPane jop = new JOptionPane("",JOptionPane.WARNING_MESSAGE);
         int option = jop.showOptionDialog(parent,"File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?","WARNING: File already exists!",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE,jop.getIcon(),options,options[2]);
         if(option == JOptionPane.NO_OPTION)
            append = true;
         if(option == JOptionPane.CANCEL_OPTION)
            save = false;
      }

      if(save)
      {
         try
         {
                                 //System.out.println("a");
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileToSave,append));

            if(append)
               bw.newLine();

            String line = "";
                                 //System.out.println("b");

            if(graph.getN() < 63)
            {
               line += (char)(graph.getN()+63);
            }
            else if(graph.getN() < 258048)
            {
               line += (char)(126);

               int number = graph.getN();

               String binary = "";
               for(int i=17; i>=0; i--)
                  if(number>Math.pow(2,i))
                  {
                     number -= (int)Math.pow(2,i);
                     binary += "1";
                  }
                  else
                     binary += "0";

               for(int i=0; i<3; i++)
               {
                  String sixBinary = binary.substring(6*i,6*(i+1));
                                       /*int binaryNumber = 0;
                                       for(int j=0; j<6; j++)
                                       {
                                          if(sixBinary.charAt(j) == '1')
                                             binaryNumber += (int)Math.pow(2,(5-j));
                                       }*/
                  int binaryNumber = binaryToInt(sixBinary);
                  line += (char)(binaryNumber+63);
               }
            }
            else
            {
               System.out.println("UGV does not support graphs of this size.");
            }

                                 //System.out.println("c");
            int input = 0;
            int count = 0;
            for(int i=0; i<graph.getN(); i++)
               for(int j=0; j<i; j++)
               {
                  count++;
                  if(graph.isArc(i+1,j+1))
                     input += (int)Math.pow(2,6-count);

                  if(count == 6)
                  {
                     line += (char)(63+input);
                                          //System.out.println("Printing line " +(char)(63+input));
                     count = 0;
                     input = 0;
                  }
               }

            if(count > 0)
               line += (char)(63+input);
            bw.write(line);
                                 //System.out.println(line);
            bw.close();

         }

         catch(Exception ex)
         {
            System.out.println(ex);
         }

      }


   }

   public void saveMultipleGraphs6(String filename, GraphPane []graphPanes)
   {
      File fileToSave = new File(filename);
      boolean save = true;
      boolean append = false;

      if(fileToSave.exists())
      {
         String []options = new String[3];
         options[0] = "Overwrite";
         options[1] = "Append";
         options[2] = "Cancel";
                              /*JOptionPane jop = new JOptionPane(("File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?"),JOptionPane.WARNING_MESSAGE,JOptionPane.YES_NO_CANCEL_OPTION);
                              jop.setOptions(options);*/

         JOptionPane jop = new JOptionPane("",JOptionPane.WARNING_MESSAGE);
         int option = jop.showOptionDialog(parent,"File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?","WARNING: File already exists!",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE,jop.getIcon(),options,options[2]);
         if(option == JOptionPane.NO_OPTION)
            append = true;
         if(option == JOptionPane.CANCEL_OPTION)
            save = false;
      }

      if(save)
      {
         try
         {
                                 //System.out.println("a");
            for(int graphs=0; graphs<graphPanes.length; graphs++)
            {
               BufferedWriter bw = new BufferedWriter(new FileWriter(fileToSave,append));
               Graph graph = graphPanes[graphs].getGraph();

               if(append)
                  bw.newLine();

               String line = "";
                                 //System.out.println("b");

               if(graph.getN() < 63)
               {
                  line += (char)(graph.getN()+63);
               }
               else if(graph.getN() < 258048)
               {
                  line += (char)(126);

                  int number = graph.getN();

                  String binary = "";
                  for(int i=17; i>=0; i--)
                     if(number>Math.pow(2,i))
                     {
                        number -= (int)Math.pow(2,i);
                        binary += "1";
                     }
                     else
                        binary += "0";

                  for(int i=0; i<3; i++)
                  {
                     String sixBinary = binary.substring(6*i,6*(i+1));
                                       /*int binaryNumber = 0;
                                       for(int j=0; j<6; j++)
                                       {
                                          if(sixBinary.charAt(j) == '1')
                                             binaryNumber += (int)Math.pow(2,(5-j));
                                       }*/
                     int binaryNumber = binaryToInt(sixBinary);
                     line += (char)(binaryNumber+63);
                  }
               }
               else
               {
                  System.out.println("UGV does not support graphs of this size.");
               }

                                 //System.out.println("c");
               int input = 0;
               int count = 0;
               for(int i=0; i<graph.getN(); i++)
                  for(int j=0; j<i; j++)
                  {
                     count++;
                     if(graph.isArc(i+1,j+1))
                        input += (int)Math.pow(2,6-count);

                     if(count == 6)
                     {
                        line += (char)(63+input);
                                          //System.out.println("Printing line " +(char)(63+input));
                        count = 0;
                        input = 0;
                     }
                  }

               if(count > 0)
                  line += (char)(63+input);
               bw.write(line);
                                 //System.out.println(line);
               bw.close();

               append = true;
            }
         }
         catch(Exception ex)
         {
            System.out.println(ex);
         }

      }

   }

   public void saveGraphHCP(String filename, GraphPane graphPane)
   {
      File fileToSave = new File(filename);

      Graph graph = graphPane.getGraph();

      boolean save = true;

      if(fileToSave.exists())
      {
         String []options = new String[2];
         options[0] = "Overwrite";
         options[1] = "Cancel";
                              /*JOptionPane jop = new JOptionPane(("File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?"),JOptionPane.WARNING_MESSAGE,JOptionPane.YES_NO_CANCEL_OPTION);
                              jop.setOptions(options);*/

         JOptionPane jop = new JOptionPane("",JOptionPane.WARNING_MESSAGE);
         int option = jop.showOptionDialog(parent,"File " + filename + " already exists. Do you want to overwrite the file?","WARNING: File already exists!",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE,jop.getIcon(),options,options[1]);
         if(option == JOptionPane.NO_OPTION)
            save = false;
      }
      if(save)
      {
         try
         {
            int N = graph.getN();
            int [][]arcs = graph.getArcs();
            int []degrees = graph.getDegrees();

            BufferedWriter bw = new BufferedWriter(new FileWriter(fileToSave));

            bw.write(("NAME : " + filename));
            bw.newLine();
            bw.write("COMMENT : Hamiltonian cycle problem (Erbacci)");
            bw.newLine();
            bw.write("TYPE : HCP");
            bw.newLine();
            bw.write(("DIMENSION : " + N));
            bw.newLine();
            bw.write("EDGE_DATA_FORMAT : EDGE_LIST");
            bw.newLine();
            bw.write("EDGE_DATA_SECTION");
            bw.newLine();

            for(int i=0; i<N; i++)
               for(int j=0; j<degrees[i]; j++)
                  if((i+1) < arcs[i][j])
                  {
                     bw.write((i+1) + " " + arcs[i][j]);
                     bw.newLine();
                  }

            bw.write("-1");
            bw.newLine();
            bw.write("EOF");
            bw.newLine();
            bw.close();
         }
         catch(Exception e)
         {
            System.out.println(e);
         }
      }

   }

   public void saveGraphSCD(String filename, GraphPane graphPane)
   {

      Graph graph = graphPane.getGraph();

      if(!graph.isConnected())
      {
         JOptionPane jop = new JOptionPane("",JOptionPane.ERROR_MESSAGE);
         jop.showMessageDialog(parent,("The graph is disconnected. SCD format requires connected regular graphs."),"Error saving as SCD",JOptionPane.ERROR_MESSAGE);
         return;

      }

      File fileToSave = new File(filename);

      int []degrees = graph.getDegrees();
      for(int i=1; i<degrees.length; i++)
         if(degrees[i] != degrees[0])
         {
            JOptionPane jop = new JOptionPane("",JOptionPane.ERROR_MESSAGE);
            jop.showMessageDialog(parent,"Only regular graphs may be saved in SCD format!","Error saving as SCD",JOptionPane.ERROR_MESSAGE);
            return;
         }

      int degree = degrees[0];
      int maxNode = graph.getN();

      boolean save = true;
      boolean append = false;

      if(fileToSave.exists())
      {

         String []options = new String[3];
         options[0] = "Overwrite";
         options[1] = "Append";
         options[2] = "Cancel";
                              /*JOptionPane jop = new JOptionPane(("File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?"),JOptionPane.WARNING_MESSAGE,JOptionPane.YES_NO_CANCEL_OPTION);
                              jop.setOptions(options);*/

         JOptionPane jop = new JOptionPane("",JOptionPane.WARNING_MESSAGE);
         int option = jop.showOptionDialog(parent,"File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?","WARNING: File already exists!",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE,jop.getIcon(),options,options[2]);
         if(option == JOptionPane.NO_OPTION)
            append = true;
         if(option == JOptionPane.CANCEL_OPTION)
            save = false;



      }

      if(save)
      {
         try
         {
            if(append)
            {
               long []scdData = getSCDData(fileToSave);
               if((int)scdData[0] != maxNode || (int)scdData[1] != degree)
               {
                  JOptionPane jop = new JOptionPane("",JOptionPane.ERROR_MESSAGE);
                  jop.showMessageDialog(parent,(fileToSave.getName() + " contains graphs of size " + (int)scdData[0] + " and degree " + (int)scdData[1] + " which are incompatible with this graph of size " + maxNode + " and degree " + degree + "."),"Error saving as SCD",JOptionPane.ERROR_MESSAGE);
                  return;

               }

               DataInputStream dis = new DataInputStream(new FileInputStream(fileToSave));

               long graphsToDo = scdData[2];

               int[] stream = new int[maxNode*degree/2];

               for(long graphcount=0; graphcount<graphsToDo; graphcount++)
               {
                  int index = readStream(dis);
                  for(int i=index; i<maxNode*degree/2; i++)
                     stream[i] = readStream(dis);
               }

               dis.close();

               int []newStream = new int[maxNode*degree/2];
               int [][]arcs = graph.getArcs();
               int count = 0;
               for(int i=0; i<maxNode; i++)
                  for(int j=0; j<degrees[i]; j++)
                     if(arcs[i][j]>i+1)
                        newStream[count++] = arcs[i][j];

               int repeat = 0;
               int index = 0;
               while(index < maxNode*degree/2 && newStream[index] == stream[index++])
                  repeat++;

               DataOutputStream dos = new DataOutputStream(new FileOutputStream(fileToSave,append));

               dos.writeByte(repeat);
               for(int i=repeat; i<maxNode*degree/2; i++)
                  dos.writeByte(newStream[i]);
               dos.close();

            }
            else
            {

               DataOutputStream dos = new DataOutputStream(new FileOutputStream(fileToSave));
               dos.writeByte(0);
               int [][]arcs = graph.getArcs();
               for(int i=0; i<maxNode; i++)
                  for(int j=0; j<degrees[i]; j++)
                     if(arcs[i][j]>i+1)
                        dos.writeByte(arcs[i][j]);
               dos.close();

            }

         }
         catch(Exception ex)
         {
            System.out.println(ex);
         }
      }


   }

   public void saveMultipleGraphsSCD(String filename, GraphPane []graphPanes)
   {
      Graph graph = graphPanes[0].getGraph();
      int []degrees = graph.getDegrees();
      int degree = degrees[0];
      int maxNode = graph.getN();

      for(int graphs=0; graphs<graphPanes.length; graphs++)
      {
         graph = graphPanes[graphs].getGraph();
         if(!graph.isConnected())
         {
            JOptionPane jop = new JOptionPane("",JOptionPane.ERROR_MESSAGE);
            jop.showMessageDialog(parent,("Graph " + (graphs+1) + " is disconnected. SCD format requires connected regular graphs."),"Error saving as SCD",JOptionPane.ERROR_MESSAGE);
            return;

         }


         degrees = graph.getDegrees();
         for(int i=1; i<degrees.length; i++)
            if(degrees[i] != degrees[0])
            {
               JOptionPane jop = new JOptionPane("",JOptionPane.ERROR_MESSAGE);
               jop.showMessageDialog(parent,"Only regular graphs may be saved in SCD format! Graph " + (graphs+1) + " is irregular!","Error saving as SCD",JOptionPane.ERROR_MESSAGE);
               return;
            }

         if(degrees[0] != degree || graph.getN() != maxNode)
         {
            JOptionPane jop = new JOptionPane("",JOptionPane.ERROR_MESSAGE);
            jop.showMessageDialog(parent,"All graphs must have the same number of vertices, and have the same vertex degree!","Error saving as SCD",JOptionPane.ERROR_MESSAGE);
            return;

         }

      }

      File fileToSave = new File(filename);



      boolean save = true;
      boolean append = false;

      if(fileToSave.exists())
      {

         String []options = new String[3];
         options[0] = "Overwrite";
         options[1] = "Append";
         options[2] = "Cancel";
                              /*JOptionPane jop = new JOptionPane(("File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?"),JOptionPane.WARNING_MESSAGE,JOptionPane.YES_NO_CANCEL_OPTION);
                              jop.setOptions(options);*/

         JOptionPane jop = new JOptionPane("",JOptionPane.WARNING_MESSAGE);
         int option = jop.showOptionDialog(parent,"File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?","WARNING: File already exists!",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE,jop.getIcon(),options,options[2]);
         if(option == JOptionPane.NO_OPTION)
            append = true;
         if(option == JOptionPane.CANCEL_OPTION)
            save = false;



      }

      if(save)
      {
         try
         {
            int []stream = new int[maxNode*degree/2];
            for(int i=0; i<stream.length; i++)
               stream[i] = 0;
            if(append)
            {
               long []scdData = getSCDData(fileToSave);
               if((int)scdData[0] != maxNode || (int)scdData[1] != degree)
               {
                  JOptionPane jop = new JOptionPane("",JOptionPane.ERROR_MESSAGE);
                  jop.showMessageDialog(parent,(fileToSave.getName() + " contains graphs of size " + (int)scdData[0] + " and degree " + (int)scdData[1] + " which are incompatible with graphs of size " + maxNode + " and degree " + degree + "."),"Error saving as SCD",JOptionPane.ERROR_MESSAGE);
                  return;

               }

               DataInputStream dis = new DataInputStream(new FileInputStream(fileToSave));

               long graphsToDo = scdData[2];

               for(long graphcount=0; graphcount<graphsToDo; graphcount++)
               {
                  int index = readStream(dis);
                  for(int i=index; i<maxNode*degree/2; i++)
                     stream[i] = readStream(dis);
               }

               dis.close();
            }
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(fileToSave,append));
            for(int graphs=0; graphs<graphPanes.length; graphs++)
            {
               graph = graphPanes[graphs].getGraph();

               int []newStream = new int[maxNode*degree/2];
               int [][]arcs = graph.getArcs();
               int count = 0;
               for(int i=0; i<maxNode; i++)
                  for(int j=0; j<degrees[i]; j++)
                     if(arcs[i][j]>i+1)
                        newStream[count++] = arcs[i][j];

               int repeat = 0;
               int index = 0;
               while(index < maxNode*degree/2 && newStream[index] == stream[index++])
                  repeat++;

               dos.writeByte(repeat);
               for(int i=repeat; i<maxNode*degree/2; i++)
                  dos.writeByte(newStream[i]);

               stream = newStream;
            }
            dos.close();



         }
         catch(Exception ex)
         {
            System.out.println(ex);
         }
      }

   }

   public void saveGraphUGV(String filename, GraphPane graphPane)
   {
      File fileToSave = new File(filename);

      Graph graph = graphPane.getGraph();

      boolean save = true;
      boolean append = false;

      if(fileToSave.exists())
      {
         String []options = new String[3];
         options[0] = "Overwrite";
         options[1] = "Append";
         options[2] = "Cancel";
                              /*JOptionPane jop = new JOptionPane(("File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?"),JOptionPane.WARNING_MESSAGE,JOptionPane.YES_NO_CANCEL_OPTION);
                              jop.setOptions(options);*/

         JOptionPane jop = new JOptionPane("",JOptionPane.WARNING_MESSAGE);
         int option = jop.showOptionDialog(parent,"File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?","WARNING: File already exists!",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE,jop.getIcon(),options,options[2]);
         if(option == JOptionPane.NO_OPTION)
            append = true;
         if(option == JOptionPane.CANCEL_OPTION)
            save = false;


      }
      if(save)
      {
         try
         {
            int newGraphs = 1;

            BufferedWriter bw;

            //DataOutputStream os;
            if(append)
            {
               BufferedReader br = new BufferedReader(new FileReader(fileToSave));
               bw = new BufferedWriter(new FileWriter(new File(filename + ".temp")));

               String line = br.readLine();
               long graphs = Long.parseLong(line);
               bw.write((graphs+newGraphs)+"");
               bw.newLine();

               for(int i=0; i<graphs; i++)
               {
                  int N = Integer.parseInt(br.readLine());
                  bw.write(N+"");
                  bw.newLine();
                   // xScale, yScale, xTopLeft, yTopLeft, radius
                  bw.write(br.readLine());
                  bw.newLine();
                     // xPoses, yPoses
                  bw.write(br.readLine());
                  bw.newLine();
                  bw.write(br.readLine());
                  bw.newLine();

                // degrees
                  bw.write(br.readLine());
                  bw.newLine();
                  for(int j=0; j<N; j++) // arcs
                  {
                     bw.write(br.readLine());
                     bw.newLine();
                  }
                  bw.write("-1");
                  bw.newLine();
                  br.readLine();
               }
               br.close();

               //DataInputStream is = new DataInputStream(new FileInputStream(fileToSave));
               //os = new DataOutputStream(new FileOutputStream(new File(filename + ".temp")));

               /*long graphs = is.readLong();
               os.writeLong(graphs+newGraphs);

               for(int i=0; i<graphs; i++)
               {
                  int N = is.readInt();
                  os.writeInt(N);
                  os.writeDouble(is.readDouble());
                  os.writeDouble(is.readDouble());
                  os.writeInt(is.readInt());
                  os.writeInt(is.readInt());
                  os.writeInt(is.readInt());
                  for(int j=0; j<N; j++)
                  {
                     os.writeInt(is.readInt());
                     os.writeInt(is.readInt());
                  }
                  int []degrees = new int[N];
                  for(int j=0; j<N; j++)
                  {
                     degrees[j] = is.readInt();
                     os.writeInt(degrees[j]);
                  }
                  for(int j=0; j<N; j++)
                     for(int k=0; k<degrees[j]; k++)
                        os.writeInt(is.readInt());
               }
               is.close();*/
            }
            else
            {
               //os = new DataOutputStream(new FileOutputStream(fileToSave));
               bw = new BufferedWriter(new FileWriter(fileToSave));
            }

            if(!append)
            {
               //os.writeLong(newGraphs);
               bw.write(newGraphs+"");
               bw.newLine();
            }

            bw.write(graph.getN()+"");
            bw.newLine();
            bw.write(graphPane.getXScale()+" "+graphPane.getYScale()+" "+graphPane.getXTopLeft()+" "+graphPane.getYTopLeft()+" "+graphPane.getRadius());
            bw.newLine();
            /*bw.write(graphPane.getYScale()+"");
            bw.newLine();
            bw.write(graphPane.getXTopLeft()+"");
            bw.newLine();
            bw.write(graphPane.getYTopLeft()+"");
            bw.newLine();
            bw.write(graphPane.getRadius()+"");
            bw.newLine();*/
            String xPosesString = "";
            String yPosesString = "";
            for(int i=0; i<graph.getN(); i++)
            {
               if(i == 0)
               {
                  xPosesString = (graph.getXPos(0) + "");
                  yPosesString = (graph.getYPos(0) + "");
               }
               else
               {
                  xPosesString += (" " + graph.getXPos(i));
                  yPosesString += (" " + graph.getYPos(i));
               }
            }

            bw.write(xPosesString);
            bw.newLine();
            bw.write(yPosesString);
            bw.newLine();

            int []degrees = graph.getDegrees();
            String degreesString  = "";
            for(int i=0; i<degrees.length; i++)
               if(i == 0)
                  degreesString = (degrees[0] + "");
               else
                  degreesString += (" " + degrees[i]);

            bw.write(degreesString);
            bw.newLine();

            int [][]arcs = graph.getArcs();
            for(int i=0; i<degrees.length; i++)
            {
               String arcsString = "";
               for(int j=0; j<degrees[i]; j++)
                  if(j == 0)
                     arcsString = (arcs[i][0] + "");
                  else
                     arcsString += (" " + arcs[i][j]);

               bw.write(arcsString);
               bw.newLine();
            }

            bw.write("-1");
            bw.close();

            /*os.writeInt(graph.getN());
            os.writeDouble(graphPane.getXScale());
            os.writeDouble(graphPane.getYScale());
            os.writeInt(graphPane.getXTopLeft());
            os.writeInt(graphPane.getYTopLeft());
            os.writeInt(graphPane.getRadius());
            for(int i=0; i<graph.getN(); i++)
            {
               os.writeInt(graph.getXPos(i));
               os.writeInt(graph.getYPos(i));
            }

            int []degrees = graph.getDegrees();
            for(int i=0; i<degrees.length; i++)
               os.writeInt(degrees[i]);

            int [][]arcs = graph.getArcs();
            for(int i=0; i<degrees.length; i++)
               for(int j=0; j<degrees[i]; j++)
                  os.writeInt(arcs[i][j]);

            os.close();*/


            if(append)
            {
               File newFile = new File(filename+".temp");
               if(newFile.exists())
               {
                  fileToSave.delete();
                  newFile.renameTo(fileToSave);
               }
            }
         }
         catch(Exception ex)
         {
            System.out.println(ex);
         }
      }


   }

   public void saveMultipleGraphsUGV(String filename, GraphPane []graphPanes)
   {
      File fileToSave = new File(filename);

      boolean save = true;
      boolean append = false;

      if(fileToSave.exists())
      {
         String []options = new String[3];
         options[0] = "Overwrite";
         options[1] = "Append";
         options[2] = "Cancel";
                              /*JOptionPane jop = new JOptionPane(("File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?"),JOptionPane.WARNING_MESSAGE,JOptionPane.YES_NO_CANCEL_OPTION);
                              jop.setOptions(options);*/

         JOptionPane jop = new JOptionPane("",JOptionPane.WARNING_MESSAGE);
         int option = jop.showOptionDialog(parent,"File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?","WARNING: File already exists!",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE,jop.getIcon(),options,options[2]);
         if(option == JOptionPane.NO_OPTION)
            append = true;
         if(option == JOptionPane.CANCEL_OPTION)
            save = false;


      }
      if(save)
      {
         try
         {
            int newGraphs = graphPanes.length;

            BufferedWriter bw;

            //DataOutputStream os;
            if(append)
            {
               BufferedReader br = new BufferedReader(new FileReader(fileToSave));
               bw = new BufferedWriter(new FileWriter(new File(filename + ".temp")));

               String line = br.readLine();
               long graphs = Long.parseLong(line);
               bw.write((graphs+newGraphs)+"");
               bw.newLine();

               for(int i=0; i<graphs; i++)
               {
                  int N = Integer.parseInt(br.readLine());
                  bw.write(N+"");
                  bw.newLine();
                 // xScale, yScale, xTopLeft, yTopLeft, radius
                  bw.write(br.readLine());
                  bw.newLine();
                     // xPoses, yPoses
                  bw.write(br.readLine());
                  bw.newLine();
                  bw.write(br.readLine());
                  bw.newLine();

                // degrees
                  bw.write(br.readLine());
                  bw.newLine();
                  for(int j=0; j<N; j++) // arcs
                  {
                     bw.write(br.readLine());
                     bw.newLine();
                  }
                  bw.write("-1");
                  bw.newLine();
                  br.readLine();
               }
               br.close();
            }

            else
            {
               //os = new DataOutputStream(new FileOutputStream(fileToSave));
               bw = new BufferedWriter(new FileWriter(fileToSave));
            }

            if(!append)
            {
               //os.writeLong(newGraphs);
               bw.write(newGraphs+"");
               bw.newLine();
            }

            for(int graphs=0; graphs<graphPanes.length; graphs++)
            {
               Graph graph = graphPanes[graphs].getGraph();


               bw.write(graph.getN()+"");
               bw.newLine();
               bw.write(graphPanes[graphs].getXScale()+" "+graphPanes[graphs].getYScale()+" "+graphPanes[graphs].getXTopLeft()+" "+graphPanes[graphs].getYTopLeft()+" "+graphPanes[graphs].getRadius());
               bw.newLine();
            /*bw.write(graphPane.getYScale()+"");
            bw.newLine();
            bw.write(graphPane.getXTopLeft()+"");
            bw.newLine();
            bw.write(graphPane.getYTopLeft()+"");
            bw.newLine();
            bw.write(graphPane.getRadius()+"");
            bw.newLine();*/
               String xPosesString = "";
               String yPosesString = "";
               for(int i=0; i<graph.getN(); i++)
               {
                  if(i == 0)
                  {
                     xPosesString = (graph.getXPos(0) + "");
                     yPosesString = (graph.getYPos(0) + "");
                  }
                  else
                  {
                     xPosesString += (" " + graph.getXPos(i));
                     yPosesString += (" " + graph.getYPos(i));
                  }
               }

               bw.write(xPosesString);
               bw.newLine();
               bw.write(yPosesString);
               bw.newLine();

               int []degrees = graph.getDegrees();
               String degreesString  = "";
               for(int i=0; i<degrees.length; i++)
                  if(i == 0)
                     degreesString = (degrees[0] + "");
                  else
                     degreesString += (" " + degrees[i]);

               bw.write(degreesString);
               bw.newLine();

               int [][]arcs = graph.getArcs();
               for(int i=0; i<degrees.length; i++)
               {
                  String arcsString = "";
                  for(int j=0; j<degrees[i]; j++)
                     if(j == 0)
                        arcsString = (arcs[i][0] + "");
                     else
                        arcsString += (" " + arcs[i][j]);

                  bw.write(arcsString);
                  bw.newLine();
               }

               bw.write("-1");
               bw.newLine();
            }
            bw.close();



            /*else
            {
               os = new DataOutputStream(new FileOutputStream(fileToSave));
            }

            if(!append)
               os.writeLong(newGraphs);

            for(int graphs=0; graphs<graphPanes.length; graphs++)
            {
               Graph graph = graphPanes[graphs].getGraph();

               os.writeInt(graph.getN());
               os.writeDouble(graphPanes[graphs].getXScale());
               os.writeDouble(graphPanes[graphs].getYScale());
               os.writeInt(graphPanes[graphs].getXTopLeft());
               os.writeInt(graphPanes[graphs].getYTopLeft());
               os.writeInt(graphPanes[graphs].getRadius());
               for(int i=0; i<graph.getN(); i++)
               {
                  os.writeInt(graph.getXPos(i));
                  os.writeInt(graph.getYPos(i));
               }

               int []degrees = graph.getDegrees();
               for(int i=0; i<degrees.length; i++)
                  os.writeInt(degrees[i]);

               int [][]arcs = graph.getArcs();
               for(int i=0; i<degrees.length; i++)
                  for(int j=0; j<degrees[i]; j++)
                     os.writeInt(arcs[i][j]);

            }
            os.close();
            */
            if(append)
            {
               File newFile = new File(filename+".temp");
               if(newFile.exists())
               {
                  fileToSave.delete();
                  newFile.renameTo(fileToSave);
               }
            }
         }
         catch(Exception ex)
         {
            System.out.println(ex);
         }
      }

   }



   public void createMenuBar()
   {
      menuBar = new JMenuBar();

      JMenu fileItem = new JMenu("File");
      JMenuItem newFileItem = new JMenuItem("New");
      JMenuItem openFileItem = new JMenuItem("Open");
      JMenuItem closeFileItem = new JMenuItem("Close");
      saveFileItem = new JMenuItem("Save");
      saveMultipleGraphsFileItem = new JMenuItem("Save multiple graphs...");
      exportAsImageFileItem = new JMenuItem("Export as image...");
      JMenuItem exitFileItem = new JMenuItem("Exit");

      newFileItem.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent e)
               {
                  buildNewGraph();
               }
            });
      newFileItem.setMnemonic(KeyEvent.VK_N);
      newFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,InputEvent.CTRL_DOWN_MASK));

      openFileItem.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent e)
               {
                  JFileChooser jfc = new JFileChooser(".");
                  jfc.setAcceptAllFileFilterUsed(false);

                  String ascName = "GENREG ASC Format (*.asc)";
                  String graph6Name = "Graph6 Format (*.g6)";
                  String hcpName = "HCP Format (*.hcp)";
                  String scdName = "GENREG SCD Format (*.scd)";
                  String edgeListName = "Edge List Format (*.txt)";
                  String ugvName = "UGV Format (*.ugv)";

                  FileNameExtensionFilter []extensions = new FileNameExtensionFilter[6];
                  extensions[0] = new FileNameExtensionFilter(ascName,"asc");
                  extensions[1] = new FileNameExtensionFilter(graph6Name,"g6");
                  extensions[2] = new FileNameExtensionFilter(hcpName,"hcp");
                  extensions[3] = new FileNameExtensionFilter(scdName,"scd");
                  extensions[4] = new FileNameExtensionFilter(edgeListName,"txt");
                  extensions[5] = new FileNameExtensionFilter(ugvName,"ugv");

                  for(int i=0; i<extensions.length; i++)
                  {
                     //jfc.setFileFilter(extensions[i]);
                     jfc.addChoosableFileFilter(extensions[i]);
                  }
                  if(settings_loadFilter >= 0 && settings_loadFilter < extensions.length)
                     jfc.setFileFilter(extensions[settings_loadFilter]);
                  else
                     jfc.setFileFilter(extensions[0]);

                  int choice = jfc.showOpenDialog(null);
                  if(choice == JFileChooser.APPROVE_OPTION)
                  {
                     File file = jfc.getSelectedFile();
                     String extensionName = jfc.getFileFilter().getDescription();

                     if(extensionName.equals(ascName))
                     {
                        settings_loadFilter = 0;
                        openGraphASC(file);
                     }
                     else if(extensionName.equals(graph6Name))
                     {
                        settings_loadFilter = 1;
                        openGraph6(file);
                     }
                     else if(extensionName.equals(hcpName))
                     {
                        settings_loadFilter = 2;
                        openGraphHCP(file);
                     }
                     else if(extensionName.equals(scdName))
                     {
                        settings_loadFilter = 3;
                        openGraphSCD(file);
                     }
                     else if(extensionName.equals(edgeListName))
                     {
                        settings_loadFilter = 4;
                        openGraphEdgeList(file);
                     }
                     else if(extensionName.equals(ugvName))
                     {
                        settings_loadFilter = 5;
                        openGraphUGV(file);
                     }

                     saveSettings();



                     /*GraphPane graphPanel = new GraphPane();
                     graphPanel.setBackground(Color.WHITE);
                     graphPanel.setSize(5000,5000);

                     //graphPanel.setGraph(graphs.get(graphs.getNumber()));
                     graphPanel.setGraph(graph);
                     graphPanel.repaint();

                     graphPanels.add(graphPanel);


                     tabbedPane.add(graphPanel, file.getName());
                     tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);*/

                     //graphHolderPanel.revalidate();
                     //graphHolderPanel.repaint();
                     //tabbedPane.repaint();
                     //repaint();
                  }
                  repaint();
               }
            });
      openFileItem.setMnemonic(KeyEvent.VK_O);
      openFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,InputEvent.CTRL_DOWN_MASK));

      closeFileItem.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent e)
               {
                  if(tabbedPane.getSelectedIndex() != -1)
                  {
                     if(!((GraphPane)tabbedPane.getSelectedComponent()).getUndoState().getLastSave())
                     {
                        JOptionPane jop = new JOptionPane();
                        String name = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
                        name = name.substring(0,name.length()-2);
                        int value = jop.showConfirmDialog(parent,"Graph " + name + " has not been saved! Are you sure you want to close?","Graph not saved",JOptionPane.YES_NO_OPTION);
                        if(value == JOptionPane.NO_OPTION)
                           return;
                     }
                     windowItem.remove(graphPanels.get(tabbedPane.getSelectedIndex()+1).getMenuItem());
                     graphPanels.delete(tabbedPane.getSelectedIndex()+1);
                     tabbedPane.remove(tabbedPane.getSelectedIndex());
                  }
               }
            });
      closeFileItem.setMnemonic(KeyEvent.VK_C);
      closeFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,InputEvent.CTRL_DOWN_MASK));

      saveFileItem.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent e)
               {
                  if(tabbedPane.getSelectedIndex() != -1)
                  {
                     JFileChooser jfc = new JFileChooser(".");
                     jfc.setAcceptAllFileFilterUsed(false);

                     String ascName = "GENREG ASC Format (*.asc)";
                     String graph6Name = "Graph6 Format (*.g6)";
                     String hcpName = "HCP Format (*.hcp)";
                     String scdName = "GENREG SCD Format (*.scd)";
                     String edgeListName = "Edge List Format (*.txt)";
                     String ugvName = "UGV Format (*.ugv)";

                     FileNameExtensionFilter []extensions = new FileNameExtensionFilter[6];
                     extensions[0] = new FileNameExtensionFilter(ascName,"asc");
                     extensions[1] = new FileNameExtensionFilter(graph6Name,"g6");
                     extensions[2] = new FileNameExtensionFilter(hcpName,"hcp");
                     extensions[3] = new FileNameExtensionFilter(scdName,"scd");
                     extensions[4] = new FileNameExtensionFilter(edgeListName,"txt");
                     extensions[5] = new FileNameExtensionFilter(ugvName,"ugv");

                     /*for(int i=0; i<extensions.length; i++)
                        jfc.setFileFilter(extensions[i]);*/

                     for(int i=0; i<extensions.length; i++)
                     {
                     //jfc.setFileFilter(extensions[i]);
                        jfc.addChoosableFileFilter(extensions[i]);
                     }
                     if(settings_saveFilter >= 0 && settings_saveFilter < extensions.length)
                        jfc.setFileFilter(extensions[settings_saveFilter]);
                     else
                        jfc.setFileFilter(extensions[0]);


                     int choice = jfc.showSaveDialog(null);
                     if(choice == JFileChooser.APPROVE_OPTION)
                     {
                        String extensionName = jfc.getFileFilter().getDescription();

                        GraphPane graphPane = (GraphPane)tabbedPane.getSelectedComponent();
                        //Graph graph = graphPane.getGraph();

                        if(extensionName.equals(ascName))
                        {
                           settings_saveFilter = 0;
                           String filename = jfc.getSelectedFile().getName();
                           if(filename.length() < 4 || !filename.substring(filename.length()-4).equals(".asc"))
                              filename = filename + ".asc";

                           saveGraphASC(filename,graphPane);
                        }
                        else if(extensionName.equals(graph6Name))
                        {
                           settings_saveFilter = 1;
                           String filename = jfc.getSelectedFile().getName();
                           if(filename.length() < 3 || !filename.substring(filename.length()-3).equals(".g6"))
                              filename = filename + ".g6";


                           saveGraph6(filename,graphPane);
                        }
                        else if(extensionName.equals(hcpName))
                        {
                           settings_saveFilter = 2;
                           String filename = jfc.getSelectedFile().getName();
                           if(filename.length() < 4 || !filename.substring(filename.length()-4).equals(".hcp"))
                              filename = filename + ".hcp";

                           saveGraphHCP(filename,graphPane);

                        }
                        else if(extensionName.equals(scdName))
                        {
                           settings_saveFilter = 3;
                           String filename = jfc.getSelectedFile().getName();
                           if(filename.length() < 4 || !filename.substring(filename.length()-4).equals(".scd"))
                              filename = filename + ".scd";

                           saveGraphSCD(filename,graphPane);

                        }
                        else if(extensionName.equals(edgeListName))
                        {
                           settings_saveFilter = 4;
                           String filename = jfc.getSelectedFile().getName();
                           if(filename.length() < 4 || !filename.substring(filename.length()-4).equals(".txt"))
                              filename = filename + ".txt";

                           saveGraphEdgeList(filename,graphPane);

                        }
                        else if(extensionName.equals(ugvName))
                        {
                           settings_saveFilter = 5;
                           String filename = jfc.getSelectedFile().getName();
                           if(filename.length() < 4 || !filename.substring(filename.length()-4).equals(".ugv"))
                              filename = filename + ".ugv";
                           saveGraphUGV(filename, graphPane);
                        }

                        graphPane.getUndoState().setLastSave();
                        checkSave(tabbedPane.getSelectedIndex());


                        saveSettings();
                     }
                     repaint();
                  }
               }
            });
      saveFileItem.setMnemonic(KeyEvent.VK_S);
      saveFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK));
      saveFileItem.setEnabled(false);

      saveMultipleGraphsFileItem.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent e)
               {
                  if(tabbedPane.getSelectedIndex() != -1)
                  {
                     String []graphNames = new String[tabbedPane.getTabCount()];
                     for(int i=0; i<graphNames.length; i++)
                        graphNames[i] = tabbedPane.getTitleAt(i);
                     SaveMultipleGraphs smg = new SaveMultipleGraphs(parent,graphNames);

                     if(!smg.getCancelled())
                     {
                        boolean []saveGraphs = smg.getSaveGraphs();
                        int[] graphsToSave = new int[saveGraphs.length];
                        int count = 0;
                        for(int i=0; i<saveGraphs.length; i++)
                           if(saveGraphs[i])
                              graphsToSave[count++] = i;

                        int[] trimmedGraphsToSave = new int[count];
                        for(int i=0; i<count; i++)
                           trimmedGraphsToSave[i] = graphsToSave[i];
                        graphsToSave = trimmedGraphsToSave;

                        if(count > 0)
                        {
                           JFileChooser jfc = new JFileChooser(".");
                           jfc.setAcceptAllFileFilterUsed(false);

                           String ascName = "GENREG ASC Format (*.asc)";
                           String graph6Name = "Graph6 Format (*.g6)";
                           String scdName = "GENREG SCD Format (*.scd)";
                           String ugvName = "UGV Format (*.ugv)";

                           FileNameExtensionFilter []extensions = new FileNameExtensionFilter[4];
                           extensions[0] = new FileNameExtensionFilter(ascName,"asc");
                           extensions[1] = new FileNameExtensionFilter(graph6Name,"g6");
                           extensions[2] = new FileNameExtensionFilter(scdName,"scd");
                           extensions[3] = new FileNameExtensionFilter(ugvName,"ugv");

                        /*for(int i=0; i<extensions.length; i++)
                        jfc.setFileFilter(extensions[i]);*/

                           for(int i=0; i<extensions.length; i++)
                           {
                           //jfc.setFileFilter(extensions[i]);
                              jfc.addChoosableFileFilter(extensions[i]);
                           }
                           if(settings_saveFilter >= 0 && settings_saveFilter < extensions.length)
                              jfc.setFileFilter(extensions[settings_saveFilter]);
                           else
                              jfc.setFileFilter(extensions[0]);


                           int choice = jfc.showSaveDialog(null);
                           if(choice == JFileChooser.APPROVE_OPTION)
                           {
                              String extensionName = jfc.getFileFilter().getDescription();

                              GraphPane[] graphPanes = new GraphPane[count];
                              for(int i=0; i<count; i++)
                                 graphPanes[i] = (GraphPane)tabbedPane.getComponentAt(graphsToSave[i]);
                           //Graph graph = graphPane.getGraph();

                              if(extensionName.equals(ascName))
                              {
                                 settings_saveFilter = 0;
                                 String filename = jfc.getSelectedFile().getName();
                                 if(filename.length() < 4 || !filename.substring(filename.length()-4).equals(".asc"))
                                    filename = filename + ".asc";

                                 saveMultipleGraphsASC(filename,graphPanes);
                              }
                              else if(extensionName.equals(graph6Name))
                              {
                                 settings_saveFilter = 1;
                                 String filename = jfc.getSelectedFile().getName();
                                 if(filename.length() < 3 || !filename.substring(filename.length()-3).equals(".g6"))
                                    filename = filename + ".g6";


                                 saveMultipleGraphs6(filename,graphPanes);
                              }
                              else if(extensionName.equals(scdName))
                              {
                                 settings_saveFilter = 3;
                                 String filename = jfc.getSelectedFile().getName();
                                 if(filename.length() < 4 || !filename.substring(filename.length()-4).equals(".scd"))
                                    filename = filename + ".scd";

                                 saveMultipleGraphsSCD(filename,graphPanes);

                              }
                              else if(extensionName.equals(ugvName))
                              {
                                 settings_saveFilter = 5;
                                 String filename = jfc.getSelectedFile().getName();
                                 if(filename.length() < 4 || !filename.substring(filename.length()-4).equals(".ugv"))
                                    filename = filename + ".ugv";
                                 saveMultipleGraphsUGV(filename, graphPanes);
                              }

                              for(int i=0; i<graphPanes.length; i++)
                              {
                                 graphPanes[i].getUndoState().setLastSave();
                                 checkSave(graphsToSave[i]);
                              }
                              saveSettings();
                           }
                        }
                     }
                     repaint();
                  }
               }
            });
      saveMultipleGraphsFileItem.setMnemonic(KeyEvent.VK_M);
      saveMultipleGraphsFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,InputEvent.CTRL_DOWN_MASK));
      saveMultipleGraphsFileItem.setEnabled(false);

      exportAsImageFileItem.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent e)
               {
                  JFileChooser jfc = new JFileChooser(".");
                  jfc.setAcceptAllFileFilterUsed(false);

                  String bmpName = "Bitmap (*.bmp)";
                  String gifName = "GIF (*.gif)";
                  String gifTransparentName = "GIF with transparent background (*.gif)";
                  String jpgName = "JPEG (*.jpg)";
                  String pngName = "Portable Network Graphic (*.png)";

                  FileNameExtensionFilter []extensions = new FileNameExtensionFilter[5];
                  extensions[0] = new FileNameExtensionFilter(bmpName,"bmp");
                  extensions[1] = new FileNameExtensionFilter(gifName,"gif");
                  extensions[2] = new FileNameExtensionFilter(gifTransparentName,"gif");
                  extensions[3] = new FileNameExtensionFilter(jpgName,"jpg");
                  extensions[4] = new FileNameExtensionFilter(pngName,"png");

                     /*for(int i=0; i<extensions.length; i++)
                        jfc.setFileFilter(extensions[i]);*/

                  for(int i=0; i<extensions.length; i++)
                  {
                     //jfc.setFileFilter(extensions[i]);
                     jfc.addChoosableFileFilter(extensions[i]);
                  }
                  if(settings_saveImageFilter >= 0 && settings_saveImageFilter < extensions.length)
                     jfc.setFileFilter(extensions[settings_saveImageFilter]);
                  else
                     jfc.setFileFilter(extensions[0]);


                  int choice = jfc.showSaveDialog(null);
                  if(choice == JFileChooser.APPROVE_OPTION)
                  {
                     String filename = jfc.getSelectedFile().getName();
                     String extensionName = jfc.getFileFilter().getDescription();
                     String fileFormat = "";

                     GraphPane gp = (GraphPane)tabbedPane.getSelectedComponent();
                     BufferedImage img;
                     if(extensionName.equals(gifTransparentName))
                     {
                        gp.setSavingWithTransparentBackground(true);
                        //img = new BufferedImage(gp.getSize().width, gp.getSize().height, BufferedImage.TYPE_BYTE_INDEXED);
                        /*Graphics g = img.getGraphics();
                        BufferedImage image = new BufferedImage((int)Math.round(getSize().getWidth()), (int)Math.round(getSize().getHeight()), BufferedImage.TYPE_BYTE_INDEXED);
                        Graphics2D g2= (Graphics2D)image.getGraphics();

                        g2.setComposite(AlphaComposite.Clear);
                        g2.fillRect(0, 0, (int)Math.round(getSize().getWidth()), (int)Math.round(getSize().getHeight()));
                        g2.setComposite(AlphaComposite.SrcOver);

                        g.drawImage(image,0,0,null);*/
                        //g.setColor(new Color(203,240,134));
                        //g.fillRect(0,0,gp.getSize().width,gp.getSize().height);
                        //img = gp.makeTransparent(img,0,0);
                     }
                     //else
                        //img = new BufferedImage(gp.getSize().width, gp.getSize().height, BufferedImage.TYPE_3BYTE_BGR);

                     img = new BufferedImage(gp.getSize().width, gp.getSize().height, BufferedImage.TYPE_BYTE_INDEXED);
                     gp.paintComponent(img.getGraphics());
                     img = gp.getImage();

                     //gp.paintComponent(img.getGraphics());

                     if(extensionName.equals(bmpName))
                     {
                        settings_saveImageFilter = 0;
                        fileFormat = "BMP";

                        if(filename.length() < 4 || !filename.substring(filename.length()-4).equals(".bmp"))
                           filename = filename + ".bmp";
                     }
                     else if(extensionName.equals(gifName))
                     {
                        settings_saveImageFilter = 1;
                        fileFormat = "GIF";

                        if(filename.length() < 4 || !filename.substring(filename.length()-4).equals(".gif"))
                           filename = filename + ".gif";
                     }
                     else if(extensionName.equals(gifTransparentName))
                     {
                        settings_saveImageFilter = 2;
                        fileFormat = "GIF";

                        if(filename.length() < 4 || !filename.substring(filename.length()-4).equals(".gif"))
                           filename = filename + ".gif";
                     }
                     else if(extensionName.equals(jpgName))
                     {
                        settings_saveImageFilter = 3;
                        fileFormat = "JPG";

                        if(filename.length() < 4 || !filename.substring(filename.length()-4).equals(".jpg"))
                           filename = filename + ".jpg";
                     }
                     else if(extensionName.equals(pngName))
                     {
                        settings_saveImageFilter = 4;
                        fileFormat = "PNG";

                        if(filename.length() < 4 || !filename.substring(filename.length()-4).equals(".png"))
                           filename = filename + ".png";
                     }
                     try
                     {
                        File file = new File(filename);
                        boolean save = true;
                        if(file.exists())
                        {
                           JOptionPane jop = new JOptionPane();
                           int value = jop.showConfirmDialog(parent,"File " + filename + " already exists! Do you want to overwrite this file?","File already exists!",JOptionPane.YES_NO_OPTION);
                           if(value == JOptionPane.NO_OPTION)
                              save = false;
                        }
                        if(save)
                           ImageIO.write(img,fileFormat,file);
                     }
                     catch(Exception ex)
                     {
                        System.out.println(ex);
                     }
                     gp.setSavingWithTransparentBackground(false);
                     saveSettings();
                  }
                  repaint();
               }
            });
      exportAsImageFileItem.setMnemonic(KeyEvent.VK_E);
      exportAsImageFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,InputEvent.CTRL_DOWN_MASK));
      exportAsImageFileItem.setEnabled(false);

      exitFileItem.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent e)
               {
                  exit();
               }
            });
      exitFileItem.setMnemonic(KeyEvent.VK_X);
      exitFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,InputEvent.CTRL_DOWN_MASK));


      fileItem.add(newFileItem);
      fileItem.add(openFileItem);
      fileItem.add(closeFileItem);
      fileItem.addSeparator();
      fileItem.add(saveFileItem);
      fileItem.add(saveMultipleGraphsFileItem);
      fileItem.addSeparator();
      fileItem.add(exportAsImageFileItem);
      fileItem.addSeparator();
      fileItem.add(exitFileItem);
      fileItem.setMnemonic(KeyEvent.VK_F);

      JMenu editItem = menuBar.add(new JMenu("Edit"));

      undoEditItem = new JMenuItem("Undo");
      undoEditItem.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent e)
               {
                  if(tabbedPane.getSelectedIndex() != -1)
                  {
                     ((GraphPane)tabbedPane.getSelectedComponent()).undo();
                  }
               }
            });
      undoEditItem.setMnemonic(KeyEvent.VK_U);
      undoEditItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,InputEvent.CTRL_DOWN_MASK));

      redoEditItem = new JMenuItem("Redo");
      redoEditItem.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent e)
               {
                  if(tabbedPane.getSelectedIndex() != -1)
                  {
                     ((GraphPane)tabbedPane.getSelectedComponent()).redo();
                  }
               }
            });
      redoEditItem.setMnemonic(KeyEvent.VK_R);
      redoEditItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,InputEvent.CTRL_DOWN_MASK));

      checkCrossingsItem = new JMenuItem("Check Crossings");
      checkCrossingsItem.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent e)
               {
                  if(tabbedPane.getSelectedIndex() != -1)
                  {
                     ((GraphPane)tabbedPane.getSelectedComponent()).checkCrossings();
                  }
               }
            });
      checkCrossingsItem.setMnemonic(KeyEvent.VK_C);
      checkCrossingsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K,InputEvent.CTRL_DOWN_MASK));

      copyItem = new JMenuItem("Copy Selected");
      copyItem.addActionListener(
              new ActionListener(){
                 public void actionPerformed(ActionEvent e)
                 {
                    copySelected();

                 }
              });
      copyItem.setMnemonic(KeyEvent.VK_K);
      copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.CTRL_DOWN_MASK));

      pasteItem = new JMenuItem("Paste Selected");
      pasteItem.addActionListener(
              new ActionListener(){
                 public void actionPerformed(ActionEvent e)
                 {
                    pasteGraph();
                    //copySelected();
                    validate();
                    repaint();
                 }
              });
      pasteItem.setMnemonic(KeyEvent.VK_C);
      pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,InputEvent.CTRL_DOWN_MASK));

      growItem = new JMenuItem("Enlarge Selected");
      growItem.addActionListener(
              new ActionListener(){
                 public void actionPerformed(ActionEvent e)
                 {
                    if(tabbedPane.getSelectedIndex() != -1){
                       GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();
                       gp.setUndoState();
                       Graph g = gp.getGraph();
                       g.rescaleSelected(1.0/1.1);
                       validate();
                       repaint();


                       runMILP(MILPRunner.DOMINATION);
                    }

                 }
              });
      growItem.setMnemonic(KeyEvent.VK_K);
      growItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,InputEvent.CTRL_DOWN_MASK));


      shrinkItem = new JMenuItem("Shrink Selected");
      shrinkItem.addActionListener(
              new ActionListener(){
                 public void actionPerformed(ActionEvent e)
                 {
                    if(tabbedPane.getSelectedIndex() != -1){
                       GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();
                       gp.setUndoState();
                       Graph g = gp.getGraph();
                       g.rescaleSelected(1.1);
                       //g.alignToGrid(10);
                       validate();
                       repaint();

                       //runMILP(MILPRunner.SECURE_DOMINATION);

                    }

                 }
              });
      shrinkItem.setMnemonic(KeyEvent.VK_K);
      shrinkItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,InputEvent.CTRL_DOWN_MASK));

      gridItem = new JMenuItem("Arrange Grid");
      gridItem.addActionListener(
              new ActionListener(){
                 public void actionPerformed(ActionEvent e)
                 {
                    if(tabbedPane.getSelectedIndex() != -1){
                       GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();

                       Graph g = gp.getGraph();

                       ArrangeGridDialog ad = new ArrangeGridDialog(parent);

                       if(!ad.getCancelled())
                       {
                          gp.setUndoState();

                          g.createGrid(ad.getGridSize(),ad.getVertical(),ad.getSpacing());
                          fitToScreen();
                       }

                    }

                 }
              });
      gridItem.setMnemonic(KeyEvent.VK_K);
      gridItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,InputEvent.CTRL_DOWN_MASK));

      editItem.add(copyItem);
      editItem.add(pasteItem);
      editItem.add(undoEditItem);
      editItem.add(redoEditItem);
      editItem.add(checkCrossingsItem);

      editItem.add(growItem);
      editItem.add(shrinkItem);

      editItem.add(gridItem);

      editItem.setMnemonic(KeyEvent.VK_E);
      undoEditItem.setEnabled(false);
      redoEditItem.setEnabled(false);
      pasteItem.setEnabled(false);



      JMenu viewItem = new JMenu("View");

      JMenuItem fitToScreenItem = new JMenuItem("Fit to screen");
      fitToScreenItem.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent e)
               {
                  fitToScreen();
               }
            });
      fitToScreenItem.setMnemonic(KeyEvent.VK_F);
      fitToScreenItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,InputEvent.CTRL_DOWN_MASK));

      displayVertexLabelsItem = new JCheckBoxMenuItem("Display vertex labels",settings_displayVertexLabels);
      displayVertexLabelsItem.addActionListener(
            new ActionListener()
            {
               public void actionPerformed(ActionEvent e)
               {
                  settings_displayVertexLabels = displayVertexLabelsItem.getState();
                  saveSettings();
                  if(tabbedPane.getSelectedIndex() != -1)
                  {
                     ((GraphPane)tabbedPane.getSelectedComponent()).setDisplayVertexLabels(displayVertexLabelsItem.getState());
                     ((GraphPane)tabbedPane.getSelectedComponent()).repaint();
                  }
               }
            });
      displayVertexLabelsItem.setMnemonic(KeyEvent.VK_D);
      displayVertexLabelsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,InputEvent.CTRL_DOWN_MASK));

      JMenuItem colourSettingsItem = new JMenuItem("Colour settings...");
      colourSettingsItem.addActionListener(
            new ActionListener()
            {
               public void actionPerformed(ActionEvent e)
               {
                  ColorSettingsDialog csd;
                  if(tabbedPane.getSelectedIndex() != -1)
                     csd = new ColorSettingsDialog(parent,((GraphPane)tabbedPane.getSelectedComponent()).getDefaultColors(),colorStrings,true);
                  else
                  {
                     Color []tempColors = new Color[defaultColors.length];
                     System.arraycopy(defaultColors,0,tempColors,0,defaultColors.length);
                     csd = new ColorSettingsDialog(parent,tempColors,colorStrings,false);
                  }
                  if(!csd.getCancelled() && csd.whichGraphs() != -1)
                  {
                     if(csd.whichGraphs() == 1)
                     {
                        graphPanels.setDefaultColors(csd.getDefaultColors());
                     }
                     else
                        ((GraphPane)tabbedPane.getSelectedComponent()).setDefaultColors(csd.getDefaultColors());

                     ((GraphPane)tabbedPane.getSelectedComponent()).repaint();

                  }
               }

            });
      colourSettingsItem.setMnemonic(KeyEvent.VK_L);
      colourSettingsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,InputEvent.CTRL_DOWN_MASK));

      displayCrossingsItem = new JCheckBoxMenuItem("Display crossings");
      displayCrossingsItem.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent e)
               {
                  if(tabbedPane.getSelectedIndex() != -1)
                  {
                     ((GraphPane)tabbedPane.getSelectedComponent()).setDisplayCrossings(displayCrossingsItem.getState());
                     ((GraphPane)tabbedPane.getSelectedComponent()).repaint();
                  }
                  //displayCrossings();
               }
            });
      displayCrossingsItem.setMnemonic(KeyEvent.VK_R);
      displayCrossingsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,InputEvent.CTRL_DOWN_MASK));

      displayDominationItem = new JCheckBoxMenuItem("Display domination");
      displayDominationItem.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent e)
               {
                  if(tabbedPane.getSelectedIndex() != -1)
                  {
                     ((GraphPane)tabbedPane.getSelectedComponent()).setDisplayDomination(displayDominationItem.getState());
                     ((GraphPane)tabbedPane.getSelectedComponent()).repaint();
                  }
               }
            });
      displayDominationItem.setMnemonic(KeyEvent.VK_I);
      displayDominationItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,InputEvent.CTRL_DOWN_MASK));

      displayDominationTypeMenu = new JMenu("Domination type");
      domTotalItem = new JCheckBoxMenuItem("Total domination");
      domTotalItem.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent e)
               {
                  if(tabbedPane.getSelectedIndex() != -1)
                  {
                     ((GraphPane)tabbedPane.getSelectedComponent()).setDomTotal(domTotalItem.getState());
                     ((GraphPane)tabbedPane.getSelectedComponent()).repaint();
                  }
               }
            });
      domTotalItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,InputEvent.CTRL_DOWN_MASK));

      domConnectedItem = new JCheckBoxMenuItem("Connected domination");
      domConnectedItem.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent e)
               {
                  if(tabbedPane.getSelectedIndex() != -1)
                  {
                     ((GraphPane)tabbedPane.getSelectedComponent()).setDomConnected(domConnectedItem.getState());
                     ((GraphPane)tabbedPane.getSelectedComponent()).repaint();
                  }
               }
            });
      domConnectedItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2,InputEvent.CTRL_DOWN_MASK));

      domSecureItem = new JCheckBoxMenuItem("Secure domination");
      domSecureItem.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent e)
               {
                  if(tabbedPane.getSelectedIndex() != -1)
                  {
                     ((GraphPane)tabbedPane.getSelectedComponent()).setDomSecure(domSecureItem.getState());
                     ((GraphPane)tabbedPane.getSelectedComponent()).repaint();
                  }
               }
            });
      domSecureItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3,InputEvent.CTRL_DOWN_MASK));

      domWeakRomanItem = new JCheckBoxMenuItem("Weak Roman domination");
      domWeakRomanItem.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent e)
               {
                  if(tabbedPane.getSelectedIndex() != -1)
                  {
                     ((GraphPane)tabbedPane.getSelectedComponent()).setDomWeakRoman(domWeakRomanItem.getState());
                     ((GraphPane)tabbedPane.getSelectedComponent()).repaint();
                  }
               }
            });
      domWeakRomanItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4,InputEvent.CTRL_DOWN_MASK));

      domRomanItem = new JCheckBoxMenuItem("Roman domination");
      domRomanItem.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent e)
               {
                  if(tabbedPane.getSelectedIndex() != -1)
                  {
                     ((GraphPane)tabbedPane.getSelectedComponent()).setDomRoman(domRomanItem.getState());
                     ((GraphPane)tabbedPane.getSelectedComponent()).repaint();
                  }
               }
            });
      domRomanItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5,InputEvent.CTRL_DOWN_MASK));


      displayDominationTypeMenu.add(domTotalItem);
      displayDominationTypeMenu.add(domConnectedItem);
      displayDominationTypeMenu.add(domSecureItem);
      displayDominationTypeMenu.add(domWeakRomanItem);
      displayDominationTypeMenu.add(domRomanItem);

      viewItem.add(fitToScreenItem);
      viewItem.add(displayVertexLabelsItem);
      viewItem.add(colourSettingsItem);
      viewItem.add(displayCrossingsItem);
      viewItem.add(displayDominationItem);
      viewItem.add(displayDominationTypeMenu);
      viewItem.setMnemonic(KeyEvent.VK_V);

      JMenuItem closeAllWindowItem = new JMenuItem("Close All");
      closeAllWindowItem.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent e)
               {
                  int tabs = tabbedPane.getTabCount();
                  int offset = 0;
                  for(int i=0; i < tabs; i++)
                  {
                     if(!((GraphPane)tabbedPane.getComponentAt(i-offset)).getUndoState().getLastSave())
                     {
                        JOptionPane jop = new JOptionPane();
                        String name = tabbedPane.getTitleAt(i-offset);
                        name = name.substring(0,name.length()-2);
                        int value = jop.showConfirmDialog(parent,"Graph " + name + " has not been saved! Are you sure you want to close?","Graph not saved",JOptionPane.YES_NO_OPTION);
                        if(value == JOptionPane.NO_OPTION)
                           continue;
                     }

                     windowItem.remove(graphPanels.get(i+1-offset).getMenuItem());
                     graphPanels.delete(i+1-offset);
                     tabbedPane.remove(i-offset);
                     //windowItem.remove(graphPanels.get(tabbedPane.getSelectedIndex()+1).getMenuItem());
                     //graphPanels.delete(tabbedPane.getSelectedIndex()+1);
                     //tabbedPane.remove(tabbedPane.getSelectedIndex());
                     offset++;
                  }
               }
            });
      closeAllWindowItem.setMnemonic(KeyEvent.VK_A);
      closeAllWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,InputEvent.CTRL_DOWN_MASK));

      windowItem = new JMenu("Window");
      windowItem.add(closeAllWindowItem);
      windowItem.addSeparator();
      windowItem.setMnemonic(KeyEvent.VK_W);


      menuBar.add(fileItem);
      menuBar.add(editItem);
      menuBar.add(viewItem);
      menuBar.add(windowItem);

      setJMenuBar(menuBar);
   }

   public void setUndoAvailable(boolean available)
   {
      undoEditItem.setEnabled(available);
   }

   public void setRedoAvailable(boolean available)
   {
      redoEditItem.setEnabled(available);
   }

   public void fitToScreen()
   {
      int index = tabbedPane.getSelectedIndex();
      if(index == -1)
         return;
      GraphPane graphPane = graphPanels.get(index+1);
      int radius = graphPane.getRadius();
      Graph thisGraph = graphPane.getGraph();

      if(thisGraph.getN() <= 0) return;

      double minX = thisGraph.getXPos(0)-radius;
      double maxX = thisGraph.getXPos(0)+radius;
      double minY = thisGraph.getYPos(0)-radius;
      double maxY = thisGraph.getYPos(0)+radius;
      for(int i=0; i<thisGraph.getN(); i++)
      {
         if(minX > thisGraph.getXPos(i)-radius)
            minX = thisGraph.getXPos(i)-radius;
         if(maxX < thisGraph.getXPos(i)+radius)
            maxX = thisGraph.getXPos(i)+radius;
         if(minY > thisGraph.getYPos(i)-radius)
            minY = thisGraph.getYPos(i)-radius;
         if(maxY < thisGraph.getYPos(i)+radius)
            maxY = thisGraph.getYPos(i)+radius;
      }
      //double scale = Math.min(0.9*graphPane.getSize().getWidth()*1.0/(maxX+graphPane.getRadius()*2-minX),0.9*graphPane.getSize().getHeight()*1.0/(maxY+graphPane.getRadius()*2-minY));
      double scale = Math.min(0.9*graphPane.getSize().getWidth()*1.0/(maxX-minX),0.9*graphPane.getSize().getHeight()*1.0/(maxY-minY));
      graphPane.setScale(scale,scale);
      //graphPane.setTopLeft((int)Math.round(minX + (scale*(maxX + graphPane.getRadius()*2 - minX) - graphPane.getSize().getWidth())/2.0/scale),(int)Math.round(minY + (scale*(maxY + graphPane.getRadius()*2 - minY) - graphPane.getSize().getHeight())/2.0/scale));
      //graphPane.setTopLeft((int)Math.round(minX + (scale*(maxX-minX) - graphPane.getSize().getWidth())/2.0/scale),(int)Math.round(minY + (scale*(maxY-minY) - graphPane.getSize().getHeight())/2.0/scale));
      graphPane.setTopLeft((int)Math.round(minX - ((graphPane.getSize().getWidth())/scale - (maxX-minX))/2.0),(int)Math.round(minY - ((graphPane.getSize().getHeight())/scale - (maxY-minY))/2.0));

               	/*
                  double xScale = 0.9*graphPane.getSize().getWidth()*1.0/(maxX+graphPane.getRadius()*2-minX);
                  double yScale = 0.9*graphPane.getSize().getHeight()*1.0/(maxY+graphPane.getRadius()*2-minY);
                  graphPane.setScale(xScale,yScale);
                  graphPane.setTopLeft((int)Math.round(minX + (xScale*(maxX + graphPane.getRadius()*2 - minX) - graphPane.getSize().getWidth())/2.0/xScale),(int)Math.round(minY + (yScale*(maxY + graphPane.getRadius()*2 - minY) - graphPane.getSize().getHeight())/2.0/yScale));
               	*/

      graphPane.repaint();

   }

  /*
   public void displayCrossings()
   {
      int index = tabbedPane.getSelectedIndex();
      if(index == -1)
         return;
      GraphPane graphPane = graphPanels.get(index+1);
      Graph thisGraph = graphPane.getGraph();
      int [][]arcs = thisGraph.getArcs();
      int []degrees = thisGraph.getDegrees();
      int N = thisGraph.getN();
      int crossings = 0;
      int crossingsX[] = new int[100000];
      int crossingsY[] = new int[100000];
      for(int i=0; i<N; i++)
         for(int j=0; j<degrees[i]; j++)
            for(int k=i; k<N; k++)
               for(int l=0; l<degrees[k]; l++)
               {
                  int v1 = i;
                  int v2 = arcs[i][j];
                  int v3 = k;
                  int v4 = arcs[k][l];
                  if(v1 == v3 || v1 == v4 || v2 == v3 || v2 == v4)
                     continue;

                  int p1x = thisGraph.getXPos(v1);
                  int p1y = thisGraph.getYPos(v1);
                  int p2x = thisGraph.getXPos(v2);
                  int p2y = thisGraph.getYPos(v2);
                  int p3x = thisGraph.getXPos(v3);
                  int p3y = thisGraph.getYPos(v3);
                  int p4x = thisGraph.getXPos(v4);
                  int p4y = thisGraph.getYPos(v4);

                  double s1x = p2x - p1x;
                  double s1y = p2y - p1y;
                  double s2x = p4x - p3x;
                  double s2y = p4y - p3y;

                  double s = (-s1y*(p1x - p3x) + s1x*(p1y - p3y))/(-s2x*s1y + s1x*s2y);
                  double t = (s2x*(p1y - p3y) - s2y*(p1x - p3x))/(-s2x*s1y + s1x*s2y);

                  if(s >= 0 && s <= 1 && y >= 0 && t <= 1)
                  {
                     crossings++;
                     crossingsX[crossings] = Integer.parseInt(p1x + (t*s1x));
                     crossingsY[crossings] = Integer.parseInt(p1y + (t*s1y));
                  }
               }




   }*/

   public Color[] getDefaultColors()
   {
      Color []tempColors = new Color[defaultColors.length];
      System.arraycopy(defaultColors,0,tempColors,0,defaultColors.length);
      return tempColors;
   }

   public void saveDefaultColors(Color []dc)
   {
      System.arraycopy(dc,0,defaultColors,0,dc.length);
      saveSettings();
   }

   public Color[] getOriginalDefaultColors()
   {
      Color []tempColors = new Color[originalDefaultColors.length];
      System.arraycopy(originalDefaultColors,0,tempColors,0,originalDefaultColors.length);
      return tempColors;
   }

   public void checkSave()
   {
      if(tabbedPane.getSelectedIndex() != -1)
      {
         GraphPane gp = (GraphPane)tabbedPane.getSelectedComponent();
         if(gp.getUndoState().getLastSave())
         {
         	//System.out.println("First");
            String title = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
            if(title.substring(title.length()-2).equals(" *"))
               title = title.substring(0,title.length()-2);
            tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(),title);
            setTitle("Universal Graph Viewer - " +  title);
            gp.getMenuItem().setText(title);
         }
         else
         {
         						//System.out.println("Second");
            String title = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
            if(!title.substring(title.length()-2).equals(" *"))
               title = (title + " *");
            tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(),title);
            setTitle("Universal Graph Viewer - " +  title);
            gp.getMenuItem().setText(title);

         }
      }
   }

   public void checkSave(int index)
   {
      if(tabbedPane.getSelectedIndex() != -1)
      {
         GraphPane gp = (GraphPane)tabbedPane.getComponentAt(index);
         if(gp.getUndoState().getLastSave())
         {
         	//System.out.println("First");
            String title = tabbedPane.getTitleAt(index);
            if(title.substring(title.length()-2).equals(" *"))
               title = title.substring(0,title.length()-2);
            tabbedPane.setTitleAt(index,title);
            //setTitle("Universal Graph Viewer - " +  title);
            gp.getMenuItem().setText(title);
         }
         else
         {
         						//System.out.println("Second");
            String title = tabbedPane.getTitleAt(index);
            if(!title.substring(title.length()-2).equals(" *"))
               title = (title + " *");
            tabbedPane.setTitleAt(index,title);
            //setTitle("Universal Graph Viewer - " +  title);
            gp.getMenuItem().setText(title);
         }
         setTitle("Universal Graph Viewer - " + tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()));
      }
   }


   public void copySelected(){
      if(tabbedPane.getSelectedIndex() != -1){
         GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();
         Graph g = gp.getGraph();
         copiedGraph = g.getSubgraph(g.getSelected(), true);
         pasteItem.setEnabled(true);
      }
   }

   public void pasteGraph(){
      if(copiedGraph != null && tabbedPane.getSelectedIndex() != -1){


         GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();
         gp.pasteGraph(copiedGraph);
      }
   }

   public void runMILP(int domType){
      if(tabbedPane.getSelectedIndex() != -1) {
         GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();
         Graph g = gp.getGraph();

         MILPRunner runner = new MILPRunner(domType, g);
         try {
            double[] solution = runner.run();

            int[] domset = new int[g.getN()];

            for (int i = 0; i < g.getN(); i++) {
               domset[i] = (int) Math.round(solution[i]);

               //System.out.println("solution["+i+"] = " + solution[i]);
               
               if(domType == MILPRunner.WEAK_ROMAN_DOMINATION || domType == MILPRunner.ROMAN_DOMINATION){
                  domset[i] = (int) Math.round(solution[i]+2*solution[i+g.getN()]);
                  //System.out.println("solution["+i+g.getN()+"] = " + solution[i+g.getN()]);
               }

            }
            g.setDomset(domset);
            validate();
            repaint();

         } catch (Exception e1) {

         }
      }

   }

   Graph graph;
   JMenuBar menuBar;

   Graph copiedGraph;

   JTabbedPane tabbedPane;
   JPanel graphHolderPanel;
   //GraphPane []graphPanel = new GraphPane[1000];
   GraphLinkedList graphPanels;

   JSlider vertexSizeSlider, labelSizeSlider;
   JTextField vertexSizeField, labelSizeField;

   JCheckBoxMenuItem displayVertexLabelsItem;
   JCheckBoxMenuItem displayCrossingsItem;
   JCheckBoxMenuItem displayDominationItem;
   JMenu displayDominationTypeMenu;
   JCheckBoxMenuItem domTotalItem;
   JCheckBoxMenuItem domSecureItem;
   JCheckBoxMenuItem domConnectedItem;
   JCheckBoxMenuItem domRomanItem;
   JCheckBoxMenuItem domWeakRomanItem;
   JMenu windowItem;
   JMenuItem saveFileItem, saveMultipleGraphsFileItem, exportAsImageFileItem, undoEditItem, redoEditItem, checkCrossingsItem, copyItem, pasteItem;
   JMenuItem growItem, shrinkItem;
   JMenuItem gridItem;

   JPanel graphEditPane, buttonPane, bottomButtonPane, selectButtonPane;
   JLabel []labels;
   ImageIcon [][]icons;
   boolean []highlighted;
   boolean []pressed;
   boolean []chosen;
   int choices = 6;
   int bottomChoices = 4;
   int selectChoices = 2;
   int selectedOption = -1;

   final Color rightColor = new Color(0.7529f,0.7529f,0.7529f);

   final int MIN_VERTEX_SIZE = 1;
   final int MAX_VERTEX_SIZE = 50;
   final int DEFAULT_VERTEX_SIZE = 12;
   final int MIN_LABEL_SIZE = 5;
   final int MAX_LABEL_SIZE = 50;
   final int DEFAULT_LABEL_SIZE = 12;

   final int BOTTOM_ARRANGE = choices;
   final int BOTTOM_PROPERTIES = choices+1;
   final int BOTTOM_EDITEDGES = choices+2;
   final int BOTTOM_SPRING = choices+3;

   Cursor defaultCursor;

   //JPanel graphGraphPanel;
   JFrame parent = this;


	// Settings data

   File settingsFile = new File("settings.cfg");

   int settings_frameWidth = 640;
   int settings_frameHeight = 640;
   int settings_locationX = 0;
   int settings_locationY = 0;
   int settings_loadFilter = 0;
   int settings_saveFilter = 5;
   int settings_saveImageFilter = 2;
   boolean settings_displayVertexLabels = true;

   Color defaultBackgroundColor = new Color(1f,1f,1f);
   Color defaultDefaultColor = new Color(0f,0f,0f);
   Color defaultHighlightedVertexFillColor = new Color(0.8f,0.9f,0.9f);
   Color defaultHighlightedVertexColor = new Color(0f,0.5f,0.5f);
   Color defaultNewEdgeColor = new Color(0.9f,0.35f,0.25f);
   Color defaultDeleteEdgeColor = new Color(1f,0f,0f);

   final int colorTypes = 6;
   Color []originalDefaultColors = new Color[colorTypes];
   Color []defaultColors = new Color[colorTypes];
   String []colorStrings = new String[colorTypes];

   boolean ctrlPressed = false;
}
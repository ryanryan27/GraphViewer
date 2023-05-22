//package UGV;

   import javax.swing.*;
   import javax.swing.event.*;
   import java.awt.*;
   import java.awt.event.*;

   public class ColorSettingsDialog extends JDialog implements ActionListener
   {
      JFrame parent;
      Color []defaultColors;
      String []colorStrings;
      JButton loadDefaultButton, saveDefaultButton, resetDefaultButton, okButton, cancelButton, changeColorButtons[];
      JRadioButton thisGraphOnlyRadioButton, allGraphsRadioButton;
      ButtonGroup whichGraphsGroup;
      boolean cancelled = true;
      Canvas []colorCanvases;
      ColorSquare []colorSquares;
      JTextField redFields[], greenFields[], blueFields[];
      JColorChooser []colorChoosers;
      ColorChoose []colorChoose;
      boolean updatingFromColorChoose = false;
   
   
      public ColorSettingsDialog(JFrame frame, Color []dc, String []cs, boolean graphOpen)
      {
         super(frame, true);
         parent = frame;
         defaultColors = new Color[dc.length];
         System.arraycopy(dc,0,defaultColors,0,dc.length);
         colorStrings = cs;
      
         setSize(700,620);
         setResizable(false);
         setLocationRelativeTo(parent);
      
         int numberOfColors = defaultColors.length;
      
         JPanel colorChangePanel = new JPanel();
         colorChangePanel.setLayout(new GridLayout(numberOfColors/2,2,10,40));
      
         JPanel []colorChangePanels = new JPanel[numberOfColors];
         //colorCanvases = new Canvas[numberOfColors];
         colorSquares = new ColorSquare[numberOfColors];
         colorChoosers = new JColorChooser[numberOfColors];
         changeColorButtons = new JButton[numberOfColors];
         colorChoose = new ColorChoose[numberOfColors];
         redFields = new JTextField[numberOfColors];
         greenFields = new JTextField[numberOfColors];
         blueFields = new JTextField[numberOfColors];
      
         for(int i=0; i<numberOfColors; i++)
         {
            
         
            JPanel upperColorChangePanel = new JPanel();
            //upperColorChangePanel.setLayout(new BoxLayout(upperColorChangePanel,BoxLayout.X_AXIS));
            upperColorChangePanel.setLayout(new BorderLayout());
            //upperColorChangePanel.add(new JLabel(colorStrings[i]));
            upperColorChangePanel.add(new JLabel("            "+colorStrings[i]),BorderLayout.WEST);
           
         
            colorSquares[i] = new ColorSquare(defaultColors[i]);
         
            //upperColorChangePanel.add(Box.createHorizontalGlue());
            //colorSquares[i].setColor(defaultColors[i]);
            
            //upperColorChangePanel.add(colorSquares[i]);
            upperColorChangePanel.add(colorSquares[i],BorderLayout.EAST);
         
            JPanel redPanel = new JPanel();
            redPanel.add(new JLabel("R  "));
            redFields[i] = new JTextField(""+defaultColors[i].getRed(),3);
            redPanel.add(redFields[i]);
         
            JPanel greenPanel = new JPanel();
            greenPanel.add(new JLabel("G  "));
            greenFields[i] = new JTextField(""+defaultColors[i].getGreen(),3);
            greenPanel.add(greenFields[i]);
         
            JPanel bluePanel = new JPanel();
            bluePanel.add(new JLabel("B  "));
            blueFields[i] = new JTextField(""+defaultColors[i].getBlue(),3);
            bluePanel.add(blueFields[i]);
         
            JPanel rightLowerColorChangePanel = new JPanel();
            rightLowerColorChangePanel.setLayout(new BoxLayout(rightLowerColorChangePanel, BoxLayout.Y_AXIS));
            rightLowerColorChangePanel.add(redPanel);
            rightLowerColorChangePanel.add(greenPanel);
            rightLowerColorChangePanel.add(bluePanel);
                     
            //colorChoosers[i] = new JColorChooser(defaultColors[i]);				
            //changeColorButtons[i] = new JButton("Change colour");				
            colorChoose[i] = new ColorChoose(this,defaultColors[i],i);
         				
         				
            JPanel lowerColorChangePanel = new JPanel();
            lowerColorChangePanel.add(colorChoose[i]);
            //lowerColorChangePanel.add(changeColorButtons[i]);
            //lowerColorChangePanel.add(colorChoosers[i]);
            lowerColorChangePanel.add(rightLowerColorChangePanel);
            
            colorChangePanels[i] = new JPanel();
            colorChangePanels[i].setLayout(new BoxLayout(colorChangePanels[i],BoxLayout.Y_AXIS));
            //colorChangePanels[i].setLayout(new VerticalLayout());
            colorChangePanels[i].add(upperColorChangePanel);
            colorChangePanels[i].add(lowerColorChangePanel);
         
         
         
         	
            colorChangePanel.add(colorChangePanels[i]);
         }
         
      	
         for(int i=0; i<numberOfColors; i++)
         {
            final int index = i;
            redFields[i].addActionListener(
                  new ActionListener()
                  {
                     public void actionPerformed(ActionEvent e)
                     {
                        try
                        {
                           int red = Integer.parseInt(redFields[index].getText());
                           if(red < 0)
                              redFields[index].setText(""+0);
                           if(red > 255)
                              redFields[index].setText(""+255);
                        }
                           catch(Exception ex)
                           {
                              redFields[index].setText(""+colorSquares[index].getColor().getRed());
                           }
                     }
                  });
            redFields[i].getDocument().addDocumentListener(
                  new DocumentListener() {
                  
                     @Override
                     public void removeUpdate(DocumentEvent e) {
                        try
                        {
                           if(updatingFromColorChoose)
                              return;
                           int red = Integer.parseInt(redFields[index].getText());
                           if(red < 0)
                           {
                              red = 0;  
                           }
                           if(red > 255)
                           {
                              red = 255;
                           }
                        
                           colorChoose[index].setColor(new Color(red,Integer.parseInt(greenFields[index].getText()),Integer.parseInt(blueFields[index].getText())));
                        }
                           catch(Exception ex)
                           {
                           }
                     }
                  
                     @Override
                     public void insertUpdate(DocumentEvent e) {
                        try
                        {
                           if(updatingFromColorChoose)
                              return;
                           int red = Integer.parseInt(redFields[index].getText());
                           if(red < 0)
                           {
                              red = 0;  
                           }
                           if(red > 255)
                           {
                              red = 255;
                           }
                        
                           colorChoose[index].setColor(new Color(red,Integer.parseInt(greenFields[index].getText()),Integer.parseInt(blueFields[index].getText())));
                        }
                           catch(Exception ex)
                           {
                           }          
                     }
                  
                     @Override
                     public void changedUpdate(DocumentEvent arg0) {
                        try
                        {
                           if(updatingFromColorChoose)
                              return;
                           int red = Integer.parseInt(redFields[index].getText());
                           if(red < 0)
                           {
                              red = 0;  
                           }
                           if(red > 255)
                           {
                              red = 255;
                           }
                        
                           colorChoose[index].setColor(new Color(red,Integer.parseInt(greenFields[index].getText()),Integer.parseInt(blueFields[index].getText())));
                        }
                           catch(Exception ex)
                           {
                           }            
                     }
                  });
            greenFields[i].addActionListener(
                  new ActionListener()
                  {
                     public void actionPerformed(ActionEvent e)
                     {
                        try
                        {
                           int green = Integer.parseInt(greenFields[index].getText());
                           if(green < 0)
                              greenFields[index].setText(""+0);
                           if(green > 255)
                              greenFields[index].setText(""+255);
                        }
                           catch(Exception ex)
                           {
                              greenFields[index].setText(""+colorSquares[index].getColor().getGreen());
                           }
                     }
                  });
            greenFields[i].getDocument().addDocumentListener(
                  new DocumentListener() {
                  
                     @Override
                     public void removeUpdate(DocumentEvent e) {
                        try
                        {
                           if(updatingFromColorChoose)
                              return;
                           int green = Integer.parseInt(greenFields[index].getText());
                           if(green < 0)
                           {
                              green = 0;  
                           }
                           if(green > 255)
                           {
                              green = 255;
                           }
                        
                           colorChoose[index].setColor(new Color(Integer.parseInt(redFields[index].getText()),green,Integer.parseInt(blueFields[index].getText())));
                        }
                           catch(Exception ex)
                           {
                           }
                     }
                  
                     @Override
                     public void insertUpdate(DocumentEvent e) {
                        try
                        {
                           if(updatingFromColorChoose)
                              return;
                           int green = Integer.parseInt(greenFields[index].getText());
                           if(green < 0)
                           {
                              green = 0;  
                           }
                           if(green > 255)
                           {
                              green = 255;
                           }
                        
                           colorChoose[index].setColor(new Color(Integer.parseInt(redFields[index].getText()),green,Integer.parseInt(blueFields[index].getText())));
                        }
                           catch(Exception ex)
                           {
                           }          
                     }
                  
                     @Override
                     public void changedUpdate(DocumentEvent arg0) {
                        try
                        {
                           if(updatingFromColorChoose)
                              return;
                           int green = Integer.parseInt(greenFields[index].getText());
                           if(green < 0)
                           {
                              green = 0;  
                           }
                           if(green > 255)
                           {
                              green = 255;
                           }
                        
                           colorChoose[index].setColor(new Color(Integer.parseInt(redFields[index].getText()),green,Integer.parseInt(blueFields[index].getText())));
                        }
                           catch(Exception ex)
                           {
                           }            
                     }
                  });
            blueFields[i].addActionListener(
                  new ActionListener()
                  {
                     public void actionPerformed(ActionEvent e)
                     {
                        try
                        {
                           int blue = Integer.parseInt(blueFields[index].getText());
                           if(blue < 0)
                              blueFields[index].setText(""+0);
                           if(blue > 255)
                              blueFields[index].setText(""+255);
                        }
                           catch(Exception ex)
                           {
                              blueFields[index].setText(""+colorSquares[index].getColor().getBlue());
                           }
                     }
                  });
            blueFields[i].getDocument().addDocumentListener(
                  new DocumentListener() {
                  
                     @Override
                     public void removeUpdate(DocumentEvent e) {
                        try
                        {
                           if(updatingFromColorChoose)
                              return;
                           int blue = Integer.parseInt(blueFields[index].getText());
                           if(blue < 0)
                           {
                              blue = 0;  
                           }
                           if(blue > 255)
                           {
                              blue = 255;
                           }
                        
                           colorChoose[index].setColor(new Color(Integer.parseInt(redFields[index].getText()),Integer.parseInt(greenFields[index].getText()),blue));
                        }
                           catch(Exception ex)
                           {
                           }
                     }
                  
                     @Override
                     public void insertUpdate(DocumentEvent e) {
                        try
                        {
                           if(updatingFromColorChoose)
                              return;
                           int blue = Integer.parseInt(blueFields[index].getText());
                           if(blue < 0)
                           {
                              blue = 0;  
                           }
                           if(blue > 255)
                           {
                              blue = 255;
                           }
                        
                           colorChoose[index].setColor(new Color(Integer.parseInt(redFields[index].getText()),Integer.parseInt(greenFields[index].getText()),blue));
                        }
                           catch(Exception ex)
                           {
                           }          
                     }
                  
                     @Override
                     public void changedUpdate(DocumentEvent arg0) {
                        try
                        {
                           if(updatingFromColorChoose)
                              return;
                           int blue = Integer.parseInt(blueFields[index].getText());
                           if(blue < 0)
                           {
                              blue = 0;  
                           }
                           if(blue > 255)
                           {
                              blue = 255;
                           }
                        
                           colorChoose[index].setColor(new Color(Integer.parseInt(redFields[index].getText()),Integer.parseInt(greenFields[index].getText()),blue));
                        }
                           catch(Exception ex)
                           {
                           }            
                     }
                  });
         
         }
      
         loadDefaultButton = new JButton("Load default");
         loadDefaultButton.addActionListener(this);
         saveDefaultButton = new JButton("Save default");
         saveDefaultButton.addActionListener(this);
         resetDefaultButton = new JButton("Reset default");
         resetDefaultButton.addActionListener(this);
      
         JPanel defaultButtonPane = new JPanel();
         defaultButtonPane.add(loadDefaultButton);
         defaultButtonPane.add(saveDefaultButton);
         defaultButtonPane.add(resetDefaultButton);  
      	
      	
      	
         thisGraphOnlyRadioButton = new JRadioButton("Apply to this graph only ",graphOpen);
         allGraphsRadioButton = new JRadioButton("Apply to all graphs ",false);
         if(!graphOpen)
         {
            thisGraphOnlyRadioButton.setEnabled(false);
            allGraphsRadioButton.setEnabled(false);
         }
         whichGraphsGroup = new ButtonGroup();
         whichGraphsGroup.add(thisGraphOnlyRadioButton);
         whichGraphsGroup.add(allGraphsRadioButton);
         
         JPanel radioButtonPane = new JPanel();
         radioButtonPane.add(thisGraphOnlyRadioButton);
         radioButtonPane.add(new JLabel("       "));
         radioButtonPane.add(allGraphsRadioButton);
      	
         
      
         okButton = new JButton("OK");
         okButton.addActionListener(this);
         cancelButton = new JButton("Cancel");
         cancelButton.addActionListener(this);
      
         JPanel buttonPane = new JPanel();
         buttonPane.add(okButton);
         buttonPane.add(cancelButton);
      
         JPanel colorSettingsPanel = new JPanel();
         colorSettingsPanel.setLayout(new BoxLayout(colorSettingsPanel,BoxLayout.Y_AXIS));
         
         colorSettingsPanel.add(Box.createVerticalStrut(10));
         colorSettingsPanel.add(colorChangePanel);
         colorSettingsPanel.add(Box.createVerticalStrut(20));
         colorSettingsPanel.add(defaultButtonPane);
         colorSettingsPanel.add(Box.createVerticalStrut(10));
         colorSettingsPanel.add(radioButtonPane);
         colorSettingsPanel.add(buttonPane);
      
         getContentPane().add(colorSettingsPanel);
      
      
         setVisible(true);
      
      
      }
   
      public void actionPerformed(ActionEvent e)
      {
         if(e.getSource() == loadDefaultButton)
         {
            defaultColors = ((UGVViewer)parent).getDefaultColors();
            for(int i=0; i<defaultColors.length; i++)
            {
               updatingFromColorChoose = true;
               redFields[i].setText(""+defaultColors[i].getRed());
               greenFields[i].setText(""+defaultColors[i].getGreen());
               updatingFromColorChoose = false;
               blueFields[i].setText(""+defaultColors[i].getBlue());
            }
            repaint();
         }
         if(e.getSource() == saveDefaultButton)
         {
            ((UGVViewer)parent).saveDefaultColors(defaultColors);
         }
         if(e.getSource() == resetDefaultButton)
         {
            defaultColors = ((UGVViewer)parent).getOriginalDefaultColors();
            for(int i=0; i<defaultColors.length; i++)
            {
               updatingFromColorChoose = true;
               redFields[i].setText(""+defaultColors[i].getRed());
               greenFields[i].setText(""+defaultColors[i].getGreen());
               updatingFromColorChoose = false;
               blueFields[i].setText(""+defaultColors[i].getBlue());
            }
            repaint();
         }
         if(e.getSource() == okButton)
         {
            cancelled = false;
            setVisible(false);
            dispose();
         }
         if(e.getSource() == cancelButton)
         {
            setVisible(false);
            dispose();
         }
      }
      
      public Color[] getDefaultColors()
      {
         return defaultColors;
      }
      
      public int whichGraphs()
      {
         if(thisGraphOnlyRadioButton.isSelected())
            return 0;
         else if(allGraphsRadioButton.isSelected())
            return 1;
         else
            return -1;
      }
   	
      public boolean getCancelled()
      {
         return cancelled;
      }
      
      public void changeColor(Color newColor, int index)
      {
         updatingFromColorChoose = true;
         changeColorSquare(newColor, index);
         redFields[index].setText(""+newColor.getRed());
         greenFields[index].setText(""+newColor.getGreen());
         blueFields[index].setText(""+newColor.getBlue());
         updatingFromColorChoose = false;
         }
      
      public void changeColorSquare(Color newColor, int index)
      {
         //System.out.println("Changing color " + (index+1) + " to " + newColor);
         defaultColors[index] = newColor;
         colorSquares[index].setColor(newColor);
      }
   }
   
	
   class ColorSquare extends JComponent {
   
      Color color = new Color(0.5f,0.5f,0.5f);
   
      public ColorSquare(Color c)
      {
         color = c;
         setPreferredSize(new Dimension(86,16));
      }
   
      /*public void setColor(Color c)
      {
         color = c;
      }*/
   
      public void paintComponent(Graphics g)
      {
         g.setColor(color);
         g.fillRect(35,0,15,15);
         g.setColor(new Color(0f,0f,0f));
         g.drawRect(35,0,15,15);
         //System.out.println(color);
         //System.out.println(getLocationOnScreen());
      }
      
      public void setColor(Color newColor)
      {
         color = newColor;
         repaint();
      }
      
      public Color getColor()
      {
         return color;
      }
   }
   
   class ColorChoose extends JComponent implements MouseListener, MouseMotionListener {
   
      ColorSettingsDialog parent;
      Color chosenColor;
      int panelIndex;
      int xCoord;
      int yCoord;
      int zCoord;
      boolean initialisedCoordinates = false;
    //boolean selecting = false;  
   	
      public ColorChoose(ColorSettingsDialog p, Color c, int pi)
      {
         parent = p;
         chosenColor = c;
         panelIndex = pi;
         setPreferredSize(new Dimension(239,81));
         addMouseListener(this);
         addMouseMotionListener(this);
      }
   
      public void paintComponent(Graphics gr)
      {
         //System.out.println("Painting " + chosenColor.getRed() + " , " + chosenColor.getGreen() + " , " + chosenColor.getBlue());
         Graphics2D g = (Graphics2D)gr;
       
         for(int i=0; i<192; i++)
            for(int j=0; j<65; j++)
            {
               int red = 0;
               int green = 0;
               int blue = 0;
               if(i < 32)
               {
                  red = 255;
                  green = i*8;
                  blue = 0;
               }	
               else if(i < 64)
               {
                  red = 255 - (i%32)*8;
                  green = 255;
                  blue = 0;
               }
               else if(i < 96)
               {
                  red = 0;
                  green = 255;
                  blue = (i%32)*8;
               }
               else if(i < 128)
               {
                  red = 0;
                  green = 255 - (i%32)*8;
                  blue = 255;
               }
               else if(i < 160)
               {
                  red = (i%32)*8;
                  green = 0;
                  blue = 255;
               }
               else
               {
                  red = 255;
                  green = 0;
                  blue = 255 - (i%32)*8;
               }
               
               red = (int)Math.round(127*j/64.0 + red*(64-j)/64.0);
               green = (int)Math.round(127*j/64.0 + green*(64-j)/64.0);
               blue = (int)Math.round(127*j/64.0 + blue*(64-j)/64.0);
               
               g.setColor(new Color(red,green,blue));
               g.fillRect(8+i,12+j,1,1);
            }
         
         /// Now to identify where the colour is
         
         double red = chosenColor.getRed();
         double green = chosenColor.getGreen();
         double blue = chosenColor.getBlue();
         
         double smallest = red;
         double largest = red;
         if(green < smallest)
            smallest = green;
         if(green > largest)
            largest = green;
         if(blue < smallest)
            smallest = blue;
         if(blue > largest)
            largest = blue;
            
         double z = 32*(largest+smallest)/255.0;
         
         if(smallest == 255 || largest == 0)
         {
            red = 255;
            green = 0;
            blue = 0;
         }
            //else if(largest == 255)
         else if(z >= 32)
         {
            red = (red - 255)*32/(64-z) + 255;
            green = (green - 255)*32/(64-z) + 255;
            blue = (blue - 255)*32/(64-z) + 255;
         }
         else
         {
            red = 32*red/z;
            green = 32*green/z;
            blue = 32*blue/z;
         }
         
         
         smallest = red;
         largest = red;
         if(green < smallest)
            smallest = green;
         if(green > largest)
            largest = green;
         if(blue < smallest)
            smallest = blue;
         if(blue > largest)
            largest = blue;
         
         //System.out.println(smallest);
         
         double y = 64*smallest/127;
         
         if(red == largest)
            red = 255;
         else	
            red = 127*(red-smallest)/(127-smallest);
         if(green == largest)
            green = 255;
         else
            green = 127*(green-smallest)/(127-smallest);
         if(blue == largest)
            blue = 255;
         else
            blue = 127*(blue-smallest)/(127-smallest);
         
         double x = 0;			
         if(red == 255 && blue == 0)
            x = green/8;
         else if(green == 255 && blue == 0)
            x = 64 - red/8;
         else if(green == 255 && red == 0)
            x = 64 + blue/8;
         else if(blue == 255 && red == 0)
            x = 128 - green/8;
         else if(blue == 255 && green == 0)
            x = 128 + red/8;
         else
            x = 192 - blue/8;
         
         /*int xCoordPredict = 8+(int)Math.round(x);
         int yCoordPredict = 12+(int)Math.round(y);
         int zCoordPRedict = 12+64-(int)Math.round(z);*/
         if(!initialisedCoordinates)
         {
            xCoord = 8+(int)Math.round(x);
            yCoord = 12+(int)Math.round(y);
            zCoord = 12+64-(int)Math.round(z);
            initialisedCoordinates = true;	
         }
      
      	
         
         g.setColor(new Color(0,0,0));
         g.setStroke(new BasicStroke(2.0f));
         g.drawLine(xCoord-7,yCoord,xCoord-3,yCoord);
         g.drawLine(xCoord+3,yCoord,xCoord+7,yCoord);
         g.drawLine(xCoord,yCoord-7,xCoord,yCoord-3);
         g.drawLine(xCoord,yCoord+3,xCoord,yCoord+7);
      
      // Now to draw contrastBar
      
         /*red = chosenColor.getRed();
         green = chosenColor.getGreen();
         blue = chosenColor.getBlue();*/
         
         red = 0;
         green = 0;
         blue = 0;
         if((xCoord-8) < 32)
         {
            red = 255;
            green = (xCoord-8)*8;
            blue = 0;
         }	
         else if((xCoord-8) < 64)
         {
            red = 255 - ((xCoord-8)%32)*8;
            green = 255;
            blue = 0;
         }
         else if((xCoord-8) < 96)
         {
            red = 0;
            green = 255;
            blue = ((xCoord-8)%32)*8;
         }
         else if((xCoord-8) < 128)
         {
            red = 0;
            green = 255 - ((xCoord-8)%32)*8;
            blue = 255;
         }
         else if((xCoord-8) < 160)
         {
            red = ((xCoord-8)%32)*8;
            green = 0;
            blue = 255;
         }
         else
         {
            red = 255;
            green = 0;
            blue = 255 - ((xCoord-8)%32)*8;
         }
            //System.out.println("Clicked1 " + red + " " + green + " " + blue + " " + zCoord + " " + yCoord);
         red = (int)Math.round(127*(yCoord-12)/64.0 + red*(64-(yCoord-12))/64.0);
         green = (int)Math.round(127*(yCoord-12)/64.0 + green*(64-(yCoord-12))/64.0);
         blue = (int)Math.round(127*(yCoord-12)/64.0 + blue*(64-(yCoord-12))/64.0);
      
      
         for(int i=0; i<10; i++)
            for(int j=0; j<65; j++)
            {
               if(j == 64)
                  g.setColor(new Color(0,0,0));
               else if(j == 0)
                  g.setColor(new Color(255,255,255));
               else
               {
                  if(j <= 32)
                     g.setColor(new Color((int)Math.round(j*(red-255)/32 + 255),(int)Math.round(j*(green-255)/32 + 255),(int)Math.round(j*(blue-255)/32 + 255)));
                  else
                     g.setColor(new Color((int)Math.round((64-j)*red/32),(int)Math.round((64-j)*green/32),(int)Math.round((64-j)*blue/32)));
               }
               g.fillRect(210+i,12+j,1,1);
            }
      
         g.setStroke(new BasicStroke(1.0f));
         g.setColor(new Color(0,0,0));
         int []xPoints = new int[3];
         xPoints[0] = 219; xPoints[1] = 226; xPoints[2] = 226;
         int []yPoints = new int[3];
         yPoints[0] = zCoord; yPoints[1] = zCoord-5; yPoints[2] = zCoord+5;
         g.fillPolygon(xPoints, yPoints,3);
      
      }
   
      public void mouseClicked(MouseEvent e)
      {
         //System.out.println(e.getX() + " , " + e.getY());
         int x = e.getX();
         int y = e.getY();
      
         if(y >= 12 && y <= 76 && x >= 8)
         {
            if(x <= 199)
            {
               xCoord = x;
               yCoord = y;
             // System.out.println("xCoord = " + xCoord + " and yCoord = " + yCoord);
            // Inside main rectangle
            }
            else if(x >= 210 && x <= 226)
            {
            // Inside right rectangle
               zCoord = y;  
            }
         
            int red = 0;
            int green = 0;
            int blue = 0;
            if((xCoord-8) < 32)
            {
               red = 255;
               green = (xCoord-8)*8;
               blue = 0;
            }	
            else if((xCoord-8) < 64)
            {
               red = 255 - ((xCoord-8)%32)*8;
               green = 255;
               blue = 0;
            }
            else if((xCoord-8) < 96)
            {
               red = 0;
               green = 255;
               blue = ((xCoord-8)%32)*8;
            }
            else if((xCoord-8) < 128)
            {
               red = 0;
               green = 255 - ((xCoord-8)%32)*8;
               blue = 255;
            }
            else if((xCoord-8) < 160)
            {
               red = ((xCoord-8)%32)*8;
               green = 0;
               blue = 255;
            }
            else
            {
               red = 255;
               green = 0;
               blue = 255 - ((xCoord-8)%32)*8;
            }
            //System.out.println("Clicked1 " + red + " " + green + " " + blue + " " + zCoord + " " + yCoord);
            red = (int)Math.round(127*(yCoord-12)/64.0 + red*(64-(yCoord-12))/64.0);
            green = (int)Math.round(127*(yCoord-12)/64.0 + green*(64-(yCoord-12))/64.0);
            blue = (int)Math.round(127*(yCoord-12)/64.0 + blue*(64-(yCoord-12))/64.0);
         
                     
            if((zCoord-12) <= 32)
            {
            
               red = (int)Math.round((zCoord-12)*(red-255)/32.0+255);
               green = (int)Math.round((zCoord-12)*(green-255)/32.0+255);
               blue = (int)Math.round((zCoord-12)*(blue-255)/32.0+255);
               //g.setColor(new Color((int)Math.round(j*(redRemoveZ-255)/32 + 255),(int)Math.round(j*(greenRemoveZ-255)/32 + 255),(int)Math.round(j*(blueRemoveZ-255)/32 + 255)));
            }
            else
            {
               red = (int)Math.round((64-(zCoord-12))*red/32.0);
               green = (int)Math.round((64-(zCoord-12))*green/32.0);
               blue = (int)Math.round((64-(zCoord-12))*blue/32.0);
               //g.setColor(new Color((int)Math.round((64-j)*redRemoveZ/32),(int)Math.round((64-j)*greenRemoveZ/32),(int)Math.round((64-j)*blueRemoveZ/32)));
            }
            //System.out.println("xCoord = " + xCoord + " and yCoord = " + yCoord + " and zCoord = " + zCoord);
            //System.out.println("Clicked " + red + " " + green + " " + blue + " " + zCoord + " " + yCoord);
            chosenColor = new Color(red,green,blue);
            repaint();
            parent.changeColor(chosenColor,panelIndex);
            
         }
      }
   
      public void mousePressed(MouseEvent e)
      {
            //System.out.println("Pressed");
         //mouseClicked(e);
      }
   
      public void mouseReleased(MouseEvent e)
      {
      
      }
   
      public void mouseEntered(MouseEvent e)
      {
      }
   
      public void mouseExited(MouseEvent e)
      {
      }
   
      public void mouseMoved(MouseEvent e)
      {
      }
   
      public void mouseDragged(MouseEvent e)
      {
         //System.out.println("Dragged");
         mouseClicked(e);
      }
      
      public void setColor(Color newColor)
      {
         chosenColor = newColor;
         initialisedCoordinates = false;
         parent.changeColorSquare(newColor,panelIndex);
         //parent.changeColor(newColor,panelIndex);
         repaint();
      }
   
   }
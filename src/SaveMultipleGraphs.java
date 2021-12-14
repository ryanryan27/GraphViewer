//package UGV;

   import javax.swing.*;
   import java.awt.*;
   import java.awt.event.*;
   import javax.swing.border.LineBorder;
   import java.util.StringTokenizer;

   public class SaveMultipleGraphs extends JDialog implements ActionListener
   {
      String []graphNames;
      int number;
      JFrame parent;
      boolean cancelled = true;
      JButton selectAllButton, selectAllPageButton, selectNonePageButton, selectNoneButton, firstButton, leftButton, rightButton, lastButton, saveButton, cancelButton;
      JTextField pageField, smartGraphChooserField;
      String mostRecentSmartGraphText = "";
      JPanel panel, topPanel, checkBoxPane, selectAllPane, smartGraphChooserPane, buttonPane;
      JCheckBox []checkBoxes;
      boolean []saveGraphs;
      int currentPage = 1;
      int checkBoxesPerPage = 60;
   
      public SaveMultipleGraphs(JFrame frame, String []gn)
      {
         super(frame,true);
         parent = frame;
         graphNames = gn;
         number = graphNames.length;
         saveGraphs = new boolean[number];
      
         setSize(600,760);
         setResizable(false);
      
      
         panel = new JPanel();
         panel.setLayout(new VerticalLayout(5));
         
         topPanel = new JPanel();
         topPanel.add(new JLabel("Select which graphs you wish to save:"));
      	
         panel.add(topPanel);
         panel.add(Box.createVerticalGlue());
      	
         checkBoxPane = new JPanel();
         checkBoxPane.setLayout(new GridBagLayout());
         GridBagConstraints cst = new GridBagConstraints();
         cst.fill = GridBagConstraints.HORIZONTAL;
      	
         int checkBoxNumber = checkBoxesPerPage;
         if(number < checkBoxesPerPage)
            checkBoxNumber = (int)number;
      	
         checkBoxes = new JCheckBox[checkBoxNumber];
         //for(int i=0; i<checkBoxNumber; i++)
         for(int i=0; i<checkBoxesPerPage; i++)
         {
            if(i >= checkBoxNumber)
            {
               cst.gridy = i%20;
               cst.gridx = (i)/20;
            
               checkBoxPane.add(new JLabel(" "), cst);
            }
            else
            {
               String checkboxName = ((i+1) + ": " + graphNames[i]);
            
               for(int j=0; j<14-checkboxName.length(); j++)
                  checkboxName += "  ";
               checkBoxes[i] = new JCheckBox(checkboxName,false);
               checkBoxes[i].addActionListener(this);
            
               cst.gridy = i%20;
               cst.gridx = (i)/20;
            
               checkBoxPane.add(checkBoxes[i], cst);
            }
         	
            
         }
         //checkBoxes[0].setSelected(true);
         
         //JScrollPane scrollPane = new JScrollPane(checkBoxPane);
         panel.add(checkBoxPane);
         //panel.add(scrollPane);
      	
         panel.add(Box.createVerticalGlue());
      	
         selectAllButton = new JButton("Select all");
         selectAllButton.addActionListener(this);
      	
         selectAllPageButton = new JButton("Select all on this page");
         selectAllPageButton.addActionListener(this);
      	
         selectNonePageButton = new JButton("Deselect all on this page");
         selectNonePageButton.addActionListener(this);
      	
         selectNoneButton = new JButton("Deselect all");
         selectNoneButton.addActionListener(this);
         
         selectAllPane = new JPanel();
         selectAllPane.add(selectAllButton);
         selectAllPane.add(selectAllPageButton);
         selectAllPane.add(selectNonePageButton);
         selectAllPane.add(selectNoneButton);
      	
         panel.add(selectAllPane);
         panel.add(Box.createVerticalGlue());
      	
         //firstButton = new JButton("�");
         firstButton = new JButton("<<");
         firstButton.setFont(firstButton.getFont().deriveFont(20f));
         firstButton.setText("<<");
         firstButton.setPreferredSize(new Dimension(29,22));
         firstButton.setBorder(new LineBorder(new Color(0f,0f,0f)));
         firstButton.setVerticalTextPosition(JButton.TOP);
         firstButton.addActionListener(this);
      
         leftButton = new JButton("<");
         leftButton.setFont(leftButton.getFont().deriveFont(20f));
         leftButton.setText("<");
         leftButton.setPreferredSize(new Dimension(29,22));
         leftButton.setBorder(new LineBorder(new Color(0f,0f,0f)));
         leftButton.setVerticalTextPosition(JButton.BOTTOM);
         leftButton.addActionListener(this);
      	
         rightButton = new JButton(">");
         rightButton.setFont(rightButton.getFont().deriveFont(20f));
         rightButton.setText(">");
         rightButton.setPreferredSize(new Dimension(29,22));
         rightButton.setBorder(new LineBorder(new Color(0f,0f,0f)));
         rightButton.setVerticalTextPosition(JButton.BOTTOM);
         rightButton.addActionListener(this);
      	
         //lastButton = new JButton("�");
         lastButton = new JButton(">>");
         lastButton.setFont(lastButton.getFont().deriveFont(20f));
         lastButton.setText(">>");
         lastButton.setPreferredSize(new Dimension(29,22));
         lastButton.setBorder(new LineBorder(new Color(0f,0f,0f)));
         lastButton.setVerticalTextPosition(JButton.TOP);
         lastButton.addActionListener(this);
      	
         pageField = new JTextField("1",5);
         pageField.setHorizontalAlignment(JTextField.CENTER);
         pageField.setFont(pageField.getFont().deriveFont(13f));
         pageField.addActionListener(this);
      	
         JPanel pageButtonPane = new JPanel();
         pageButtonPane.add(Box.createHorizontalGlue());
         pageButtonPane.add(firstButton);
         pageButtonPane.add(leftButton);
         pageButtonPane.add(pageField);
         pageButtonPane.add(rightButton);
         pageButtonPane.add(lastButton);
         pageButtonPane.add(Box.createHorizontalGlue());
      	
         panel.add(pageButtonPane);
         panel.add(Box.createVerticalGlue());
         
         smartGraphChooserField = new JTextField("",50);
         mostRecentSmartGraphText = "";
         smartGraphChooserField.addActionListener(this);
      	
         smartGraphChooserPane = new JPanel();
         smartGraphChooserPane.setLayout(new VerticalLayout(3));
         smartGraphChooserPane.add(new JLabel(" "));
         smartGraphChooserPane.add(smartGraphChooserField);
         smartGraphChooserPane.add(new JLabel("List graphs to open (Example: 3,6,19-25,48)"));
      	
         panel.add(smartGraphChooserPane);
         panel.add(Box.createVerticalGlue());
      
         saveButton = new JButton("Save these graphs");
         saveButton.addActionListener(this);
         cancelButton = new JButton("Cancel");
         cancelButton.addActionListener(this);
      
         buttonPane = new JPanel();
         buttonPane.add(saveButton);
         buttonPane.add(cancelButton);
      
         panel.add(buttonPane);
      
         getContentPane().add(panel);
         pack();
         setVisible(true);
      }
   
      public boolean []getSaveGraphs()
      {
         
         return saveGraphs;
      }
   
      public void actionPerformed(ActionEvent e)
      {
         if(e.getSource() == saveButton)
         {
            if(!smartGraphChooserField.getText().equals(mostRecentSmartGraphText))
               processSmartGraphChooserField();
            cancelled = false;
            setVisible(false);
            dispose();
         }
         else if(e.getSource() == cancelButton)
         {
            setVisible(false);
            dispose();
         }
         else if(e.getSource() == selectAllButton)
         {
            for(int i=0; i<number; i++)
               saveGraphs[i] = true;
            updateCheckBoxes();
            mostRecentSmartGraphText = ("1-"+number);
            smartGraphChooserField.setText(mostRecentSmartGraphText);
         }
         else if(e.getSource() == selectAllPageButton)
         {
            for(int i=(currentPage-1)*checkBoxesPerPage; i<currentPage*checkBoxesPerPage; i++)
            {
               if(i == number)
                  break;
               saveGraphs[i] = true;
            }
            updateSmartGraphChooserField();
            updateCheckBoxes();
         }
         else if(e.getSource() == selectNonePageButton)
         {
            for(int i=(currentPage-1)*checkBoxesPerPage; i<currentPage*checkBoxesPerPage; i++)
            {
               if(i == number)
                  break;
               saveGraphs[i] = false;
            }
            updateSmartGraphChooserField();
            updateCheckBoxes();
         }
         else if(e.getSource() == selectNoneButton)
         {
            for(int i=0; i<number; i++)
               saveGraphs[i] = false;
            updateCheckBoxes();
            mostRecentSmartGraphText = "";
            smartGraphChooserField.setText("");
         }
         else if(e.getSource() == firstButton)
         {
            if(currentPage >= 2)
            {
               currentPage = 1;
               pageField.setText(""+currentPage);
               updateCheckBoxes();
            }
         }
         else if(e.getSource() == leftButton)
         {
            if(currentPage >= 2)
            {
               currentPage--;
               pageField.setText(""+currentPage);
               updateCheckBoxes();
            }
         }
         else if(e.getSource() == rightButton)
         {
            if(currentPage <= (int)Math.ceil(1.0*number/checkBoxesPerPage)-1)
            {
               currentPage++;
               pageField.setText(""+currentPage);
               updateCheckBoxes();
            }
         }
         else if(e.getSource() == lastButton)
         {
            if(currentPage <= (int)Math.ceil(1.0*number/checkBoxesPerPage*1.0)-1)
            {
               currentPage = (int)Math.ceil(1.0*number/checkBoxesPerPage);
               pageField.setText(""+currentPage);
               updateCheckBoxes();
            }
         }
         else if(e.getSource() == pageField)
         {
            try
            {
               int newPage = Integer.parseInt(pageField.getText());
               if(newPage > (int)Math.ceil(1.0*number/checkBoxesPerPage))
               {
                  newPage = (int)Math.ceil(1.0*number/checkBoxesPerPage);
                  pageField.setText(""+newPage);   
               }
               if(newPage < 1)
               {
                  pageField.setText(""+currentPage);
                  return;
               }
               currentPage = newPage;
               updateCheckBoxes();
            }
               catch(Exception ex)
               {
                  pageField.setText(""+currentPage);
               }
         }
         else if(e.getSource() == smartGraphChooserField)
         {
            processSmartGraphChooserField();
         }
         else
            for(int i=0; i<checkBoxes.length; i++)
               if(e.getSource() == checkBoxes[i])
               {
                  saveGraphs[checkBoxesPerPage*(currentPage-1)+i] = checkBoxes[i].isSelected();	
                  updateSmartGraphChooserField();
               }
      }
   
      public boolean getCancelled()
      {
         return cancelled;
      }
      
      public void processSmartGraphChooserField()
      {
         boolean []newSaveGraphs = new boolean[number];
         
         StringTokenizer tokens = new StringTokenizer(smartGraphChooserField.getText(),",");
         try 
         {
            while(tokens.hasMoreTokens())
            {
               String nextGraphs = tokens.nextToken();
               int hyphen = nextGraphs.indexOf("-");
               if(hyphen == -1)
               {
                  newSaveGraphs[Integer.parseInt(nextGraphs)-1] = true;
               }
               else
               {
                  int start = Integer.parseInt(nextGraphs.substring(0,hyphen));
                  int finish = Integer.parseInt(nextGraphs.substring(hyphen+1));
                  if(finish <= start)
                  {
                     smartGraphChooserField.setText(mostRecentSmartGraphText);
                     return;
                  }
                  for(int i=start; i<=finish; i++)
                     newSaveGraphs[i-1] = true;
               }
            }
               
            saveGraphs = newSaveGraphs;  
            	
            mostRecentSmartGraphText = smartGraphChooserField.getText();
            updateCheckBoxes();
         }
            catch(Exception ex)
            {
               smartGraphChooserField.setText(mostRecentSmartGraphText);
            }
      
      }
   	
      public void updateSmartGraphChooserField()
      {
         String fieldText = "";
         int streak = 0;
         boolean first = true;
         for(int i=0; i<number; i++)
         {
            if(saveGraphs[i])
            {
               if(first)
               {
                  first = false;
                  fieldText = (""+(i+1));
                  streak = 1;
               }
               else
               {
                  if(streak > 0)
                     streak++;
                  else
                  {
                     fieldText += (","+(i+1));
                     streak = 1;
                  }
               }
            }
            else
            {
               if(streak > 1)
               {
                  fieldText += ("-"+i);
               }
               streak = 0;
            }
         }
         if(streak > 1)
            fieldText += ("-"+number);
      
         mostRecentSmartGraphText = fieldText;      
         smartGraphChooserField.setText(fieldText);
      }
   
      public void updateCheckBoxes()
      {
      //panel.remove(checkBoxPane);
         checkBoxPane = new JPanel();
      	
         checkBoxPane.setLayout(new GridBagLayout());
         GridBagConstraints cst = new GridBagConstraints();
         cst.fill = GridBagConstraints.HORIZONTAL;
      	
              
         if(currentPage == number/checkBoxesPerPage + 1)
            checkBoxes = new JCheckBox[number - (currentPage-1)*checkBoxesPerPage];
         else
            checkBoxes = new JCheckBox[checkBoxesPerPage];
            
         //System.out.println("Number is " + number + " and currentPages is " + currentPage + " and number of checkboxes is " + checkBoxes.length);
      
      
      
         /*for(int i=0; i<checkBoxes.length; i++)
         {
            String checkboxName = ("Graph #" + (checkBoxesPerPage*(currentPage-1)+(i+1)));
            for(int j=0; j<14-checkboxName.length(); j++)
               checkboxName += "  ";
            checkBoxes[i] = new JCheckBox(checkboxName,openGraphs[checkBoxesPerPage*(currentPage-1)+i]);
            checkBoxes[i].addActionListener(this);
            cst.gridy = i%20;
            cst.gridx = (i)/20;
         	
            checkBoxPane.add(checkBoxes[i], cst);
         }*/
         
         for(int i=0; i<checkBoxesPerPage; i++)
         {
            if(i >= checkBoxes.length)
            {
               cst.gridy = i%20;
               cst.gridx = (i)/20;
            
               checkBoxPane.add(new JLabel(" "), cst);
            }
            else
            {
               String checkboxName = ((currentPage-1)*checkBoxesPerPage+(i+1) + ": " + graphNames[(currentPage-1)*checkBoxesPerPage+i]);
            
               for(int j=0; j<14-checkboxName.length(); j++)
                  checkboxName += "  ";
               checkBoxes[i] = new JCheckBox(checkboxName,saveGraphs[checkBoxesPerPage*(currentPage-1)+i]);
               checkBoxes[i].addActionListener(this);
            
               cst.gridy = i%20;
               cst.gridx = (i)/20;
            
               checkBoxPane.add(checkBoxes[i], cst);
            }
         	
            
         }
      
         getContentPane().removeAll();
         
         panel.removeAll();
      	
         //JLabel topLabel1 = new JLabel("The file " + name + " contains " + number + " graphs.");
         //topLabel1.setHorizontalAlignment(SwingConstants.CENTER);
         //JLabel topLabel2 = new JLabel("Which graphs would you like to open?");
         //topLabel2.setHorizontalAlignment(SwingConstants.CENTER);
         //panel.add(topLabel1);
         //panel.add(topLabel2);
         panel.add(topPanel);
         panel.add(Box.createVerticalGlue());
      
      
         //JScrollPane scrollPane = new JScrollPane(checkBoxPane);
         panel.add(checkBoxPane);
         panel.add(Box.createVerticalGlue());
         //panel.add(scrollPane);
      	
         panel.add(selectAllPane);
         panel.add(Box.createVerticalGlue());
      	
         JPanel pageButtonPane = new JPanel();
         pageButtonPane.add(Box.createHorizontalGlue());
         pageButtonPane.add(firstButton);
         pageButtonPane.add(leftButton);
         pageButtonPane.add(pageField);
         pageButtonPane.add(rightButton);
         pageButtonPane.add(lastButton);
         pageButtonPane.add(Box.createHorizontalGlue());
      	
         panel.add(pageButtonPane);
         panel.add(Box.createVerticalGlue());
         
         panel.add(smartGraphChooserPane);
         panel.add(Box.createVerticalGlue());
      	
         JPanel buttonPane = new JPanel();
         buttonPane.add(Box.createHorizontalGlue());
         buttonPane.add(saveButton);
         buttonPane.add(cancelButton);
         buttonPane.add(Box.createHorizontalGlue());
      	
         panel.add(buttonPane);
      	
         getContentPane().add(panel);
      
         validate();
         repaint();
         
      }   
      
   
   }
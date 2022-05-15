//package UGV;

   import javax.swing.*;
   import java.awt.*;
   import java.awt.event.*;

   public class RelabelDialog extends JDialog implements ActionListener
   {
      JFrame parent;
      JTextField newLabelField;
      JRadioButton incrementButton, swapButton;
      JButton okButton, cancelButton;
      int original;
      boolean cancelled = true;
      int nodes;
   
      public RelabelDialog(JFrame frame, int current, int N)
      {
         super(frame, true);
         parent = frame;
         original = current;
         nodes = N;
         setSize(200,150);
         setResizable(false);
         setLocationRelativeTo(parent);
         //Math.max(0,mouseX-220),mouseY+20);
      
         
      
         newLabelField = new JTextField(""+current);
         newLabelField.setColumns(3);
         JPanel topPanel = new JPanel();
         topPanel.add(new JLabel("Relabel vertex " + current + " to:"));
         topPanel.add(newLabelField);
      	
         incrementButton = new JRadioButton("Increment necessary labels",true);
         swapButton = new JRadioButton("Swap existing label",false);
         ButtonGroup buttons = new ButtonGroup();
         buttons.add(incrementButton);
         buttons.add(swapButton);
         JPanel middlePanel = new JPanel();
         middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
         middlePanel.add(incrementButton);
         middlePanel.add(swapButton);
      	
         okButton = new JButton("OK");
         okButton.addActionListener(this);
         cancelButton = new JButton("Cancel");
         cancelButton.addActionListener(this);
         JPanel bottomPanel = new JPanel();
         bottomPanel.add(okButton);
         bottomPanel.add(cancelButton);
      	
         JPanel relabelPanel = new JPanel();
         relabelPanel.setLayout(new BoxLayout(relabelPanel, BoxLayout.Y_AXIS));
         relabelPanel.add(topPanel);
         relabelPanel.add(middlePanel);
         relabelPanel.add(bottomPanel);
      	
         getContentPane().add(relabelPanel);
         pack();
         setVisible(true);
      	
      }
      
      public void actionPerformed(ActionEvent e)
      {
         if(e.getSource() == okButton)
         {
            JOptionPane jop = new JOptionPane();
            boolean processed = false;
            int newVertex = -1;
            try
            {
               newVertex = Integer.parseInt(newLabelField.getText());
               processed = true;
            }
               catch(Exception ex)
               {
               }
            
            if(!processed)
               jop.showMessageDialog(this,"Vertex label must be an integer!","Incorrect format",JOptionPane.ERROR_MESSAGE);
            else if(newVertex < 0)
               jop.showMessageDialog(this,"Vertex label must be positive!","Incorrect format",JOptionPane.ERROR_MESSAGE);
            else{
               //boolean relabel = true;
               if(newVertex == original)
               {
                  int choice = jop.showConfirmDialog(this,"You have not changed the label, are you sure?","Same label chosen",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
                  if(choice == JOptionPane.NO_OPTION)
                     return; 
               }
               if(newVertex > nodes)
               {
                  jop.showMessageDialog(this,("You have chosen " + newVertex + " but the graph only contains " + nodes + " vertices!"),"Label is bigger than graph",JOptionPane.ERROR_MESSAGE);
                  return;
               }
               cancelled = false;
               setVisible(false);
               dispose();
               
            }
               
               
               
            
            
         	
         }
         if(e.getSource() == cancelButton)
         {
            setVisible(false);
            dispose();
         }
      }
      
      public boolean getCancelled()
      {
         return cancelled;
      }
   	
      public int getNewLabel()
      {
         try
         {
            if(newLabelField != null)
            {
               int newLabel = Integer.parseInt(newLabelField.getText());
               return newLabel;
            }
         }
            catch(Exception e)
            {
            }
         return -1;
      }
      
      public boolean getIncrement()
      {
         return incrementButton.isSelected();
      }
   }
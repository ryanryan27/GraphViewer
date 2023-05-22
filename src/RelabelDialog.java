//package UGV;

   import javax.swing.*;
   import java.awt.event.*;

   public class RelabelDialog extends JDialog implements ActionListener {
      JFrame parent;
      JTextField newLabelField;
      JRadioButton incrementButton, swapButton;
      JButton okButton, cancelButton;
      int original;
      boolean cancelled = true;
      int nodes;

      /**
       * Creates a dialog box to update the label of the provided vertex.
       * @param frame the frame from which this dialog is created.
       * @param current the vertex selected to be changed.
       * @param N the total number of vertices.
       */
      public RelabelDialog(JFrame frame, int current, int N) {
         super(frame, true);
         parent = frame;
         original = current;
         nodes = N;
         setSize(200,150);
         setResizable(false);
         setLocationRelativeTo(parent);
      
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
      
      public void actionPerformed(ActionEvent e) {
         if(e.getSource() == okButton) {
            boolean processed = false;
            int newVertex = -1;
            try {
               newVertex = Integer.parseInt(newLabelField.getText());
               processed = true;
            } catch(Exception ex) {
               System.err.println(ex.getMessage());
            }
            
            if(!processed) {
               JOptionPane.showMessageDialog(this,"Vertex label must be an integer!","Incorrect format",JOptionPane.ERROR_MESSAGE);
            } else if(newVertex < 0) {
               JOptionPane.showMessageDialog(this,"Vertex label must be positive!","Incorrect format",JOptionPane.ERROR_MESSAGE);
            } else{
               if(newVertex == original) {
                  int choice = JOptionPane.showConfirmDialog(this,"You have not changed the label, are you sure?","Same label chosen",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
                  if(choice == JOptionPane.NO_OPTION)
                     return; 
               }
               if(newVertex > nodes) {
                  JOptionPane.showMessageDialog(this,("You have chosen " + newVertex + " but the graph only contains " + nodes + " vertices!"),"Label is bigger than graph",JOptionPane.ERROR_MESSAGE);
                  return;
               }
               cancelled = false;
               setVisible(false);
               dispose();
               
            }
         	
         }
         if(e.getSource() == cancelButton) {
            setVisible(false);
            dispose();
         }
      }

      /**
       * Returns whether the dialog was closed before being completed.
       * @return whether the dialog was cancelled.
       */
      public boolean getCancelled()
      {
         return cancelled;
      }

      /**
       * Gets the new label to be assigned to the specified vertex.
       * @return the new label for the vertex.
       */
      public int getNewLabel() {
         try {
            if(newLabelField != null) {
               return Integer.parseInt(newLabelField.getText());
            }
         }
         catch(Exception e) {
            System.err.println(e.getMessage());
         }
         return -1;
      }

      /**
       * Gets whether the vertices with labels higher than the selected one should be incremented.
       * @return true if the labels should be incremented, false otherwise.
       */
      public boolean getIncrement()
      {
         return incrementButton.isSelected();
      }
   }
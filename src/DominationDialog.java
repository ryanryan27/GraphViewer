//package UGV;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.StringJoiner;

public class DominationDialog extends JDialog implements ActionListener
{
   JFrame parent;
   int[] domsetorig;
   int[] domset;


   JScrollPane scrollPane;
   JTextArea domsetArea;
   JButton okButton, cancelButton;

   boolean cancelled = true;


   /**
    * Creates and shows a new dialog to edit the current graph's dominating set.
    * @param frame parent frame for the dialog.
    * @param domset current dominating function values for each vertex.
    */
   public DominationDialog(JFrame frame, int[] domset)
   {
      super(frame, true);
      parent = frame;

      this.domsetorig = domset.clone();

      setSize(550,200);
      setResizable(false);
      setLocationRelativeTo(parent);

      StringJoiner domstring = new StringJoiner(" ");

      for (int i = 0; i < domsetorig.length; i++) {
         if(domset[i] == 1) {
            domstring.add(Integer.toString(i+1));
         } else if(domset[i] == 2){
            domstring.add(Integer.toString(-1*(i+1)));
         }
      }


      domsetArea = new JTextArea(domstring.toString(), 5, 40);
      scrollPane = new JScrollPane(domsetArea);

      okButton = new JButton("OK");
      okButton.addActionListener(this);
      cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(this);


      JPanel buttonPane = new JPanel();
      buttonPane.add(okButton);
      buttonPane.add(cancelButton);

      JPanel arrangePanel = new JPanel();
      arrangePanel.setLayout(new BoxLayout(arrangePanel, BoxLayout.Y_AXIS));

      arrangePanel.add(new JLabel("Edit dominating set below. A negative index indicates two guards at a vertex."));
      arrangePanel.add(scrollPane);
      arrangePanel.add(buttonPane);

      getContentPane().add(arrangePanel);
      pack();
      setVisible(true);
   }

   public void actionPerformed(ActionEvent e)
   {
      if(e.getSource() == okButton)
      {
         try{

            domset = new int[domsetorig.length];

            String[] verts = domsetArea.getText().split("\\s+");

            for (int i = 0; i < verts.length; i++) {

               if(Objects.equals(verts[i], "")) {
                  continue;
               }
               int v = Integer.parseInt(verts[i]);

               if(v < 0){
                  domset[v*-1 -1] = 2;
               } else {
                  domset[v - 1] = 1;
               }
            }

            cancelled = false;
            setVisible(false);
            dispose();
         }catch (Exception ex){
            JOptionPane.showMessageDialog(this,"Ensure input is in correct format","Incorrect format",JOptionPane.ERROR_MESSAGE);
            return;
         }


      }
      if(e.getSource() == cancelButton)
      {
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
    * Returns an array containing the dominating function values for each vertex.
    * @return array containing values of 0,1 or 2 for each vertex.
    */
   public int[] getDomset(){
      return domset;
   }


}
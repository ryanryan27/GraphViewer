//package UGV;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DominationDialog extends JDialog implements ActionListener
{
   JFrame parent;
   int[] domsetorig;
   int[] domset;
   boolean overwrite;


   JScrollPane scrollPane;
   JTextArea domsetArea;
   JCheckBoxMenuItem overwriteBox;
   JButton okButton, cancelButton;

   boolean cancelled = true;



   public DominationDialog(JFrame frame, int[] domset)
   {
      super(frame, true);
      parent = frame;

      this.domsetorig = domset.clone();

      setSize(550,220);
      setResizable(false);
      setLocationRelativeTo(parent);

      StringBuilder domstring = new StringBuilder();

      for (int i = 0; i < domsetorig.length; i++) {
         if(domset[i] > 0) {
            domstring.append(i + 1);
            domstring.append(" ");
         }
      }



      domsetArea = new JTextArea(domstring.toString(), 5, 40);
      scrollPane = new JScrollPane(domsetArea);
      overwriteBox = new JCheckBoxMenuItem("Clear first?", false);

      okButton = new JButton("OK");
      okButton.addActionListener(this);
      cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(this);



      JPanel buttonPane = new JPanel();
      buttonPane.add(okButton);
      buttonPane.add(cancelButton);

      JPanel arrangePanel = new JPanel();
      arrangePanel.setLayout(new VerticalLayout(0));

      arrangePanel.add(new JLabel("Edit dominating set below"));
      arrangePanel.add(scrollPane);
      arrangePanel.add(overwriteBox);
      arrangePanel.add(buttonPane);

      getContentPane().add(arrangePanel);
      setVisible(true);
   }

   public void actionPerformed(ActionEvent e)
   {
      if(e.getSource() == okButton)
      {



         try{

            overwrite = overwriteBox.getState();
            int N = domsetorig.length;
            if(overwrite){
               domset = new int[N];
            } else {
               domset = domsetorig.clone();
            }

            String[] verts = domsetArea.getText().split("\s");

            for (int i = 0; i < verts.length; i++) {
               int v = Integer.parseInt(verts[i]);

               domset[v-1] = 1;
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

   public boolean getCancelled()
   {
      return cancelled;
   }

   public int[] getDomset(){
      return domset;
   }


}
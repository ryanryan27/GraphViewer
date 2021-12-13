//package UGV;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SolverDialog extends JDialog implements ActionListener
{
   JFrame parent;

   int domination_type = MILPRunner.DOMINATION;
   boolean preserve;

   ButtonGroup domtype;
   JRadioButton secure_button, dom_button, total_button, roman_button, weak_button;
   JCheckBox preserve_box;
   JButton okButton, cancelButton;

   boolean cancelled = true;



   public SolverDialog(JFrame frame)
   {
      super(frame, true);
      parent = frame;


      setSize(250,250);
      setResizable(false);
      setLocationRelativeTo(parent);



      domtype = new ButtonGroup();
      dom_button = new JRadioButton("Domination");
      dom_button.setSelected(true);

      secure_button = new JRadioButton("Secure Domination");
      total_button = new JRadioButton("Total Domination");
      roman_button = new JRadioButton("Roman Domination");
      weak_button = new JRadioButton("Weak Roman Domination");

      domtype.add(dom_button);
      domtype.add(secure_button);
      domtype.add(total_button);
      domtype.add(roman_button);
      domtype.add(weak_button);

      preserve_box = new JCheckBox("Preserve layout?");
      preserve_box.setSelected(false);


      okButton = new JButton("OK");
      okButton.addActionListener(this);
      cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(this);

      JPanel optionPane = new JPanel();
      optionPane.setLayout(new BoxLayout(optionPane,BoxLayout.Y_AXIS));
      optionPane.add(dom_button);
      optionPane.add(secure_button);
      optionPane.add(total_button);
      optionPane.add(roman_button);
      optionPane.add(weak_button);
      optionPane.add(preserve_box);

      JPanel buttonPane = new JPanel();
      buttonPane.add(okButton);
      buttonPane.add(cancelButton);

      JPanel arrangePanel = new JPanel();
      arrangePanel.setLayout(new VerticalLayout(0));

      arrangePanel.add(new JLabel("Choose a domination type below."));
      arrangePanel.add(optionPane);
      arrangePanel.add(buttonPane);

      getContentPane().add(arrangePanel);
      setVisible(true);
   }

   public void actionPerformed(ActionEvent e)
   {
      if(e.getSource() == okButton)
      {



         try{
            preserve = preserve_box.isSelected();

            if(dom_button.isSelected()){
               domination_type = MILPRunner.DOMINATION;
            } else if(secure_button.isSelected()){
               domination_type = MILPRunner.SECURE_DOMINATION;
            } else if(total_button.isSelected()){
               domination_type = MILPRunner.TOTAL_DOMINATION;
            } else if(roman_button.isSelected()){
               domination_type = MILPRunner.ROMAN_DOMINATION;
            } else if(weak_button.isSelected()){
               domination_type = MILPRunner.WEAK_ROMAN_DOMINATION;
            }


            cancelled = false;
            setVisible(false);
            dispose();
         }catch (Exception ex){
            JOptionPane.showMessageDialog(this,"I don't know what you did but it broke...","Incorrect format",JOptionPane.ERROR_MESSAGE);
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

   public int getDomtype(){return domination_type;}

   public boolean getPreserve(){return preserve;};


}
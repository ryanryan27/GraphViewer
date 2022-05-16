//package UGV;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.StringJoiner;

public class SolverDialog extends JDialog implements ActionListener
{
   JFrame parent;

   int domination_type = MILPRunner.DOMINATION;


   ButtonGroup domtype;
   JRadioButton secure_button, dom_button, total_button, roman_button, weak_button;

   JScrollPane zeros_pane, ones_pane, twos_pane;
   JTextArea zeros_area,  ones_area, twos_area;

   JButton all_button, zeros_button, ones_button, twos_button;

   JButton okButton, cancelButton;

   boolean cancelled = true;

   int[] domset;

   int[] fixed;


   public SolverDialog(JFrame frame, Graph graph)
   {
      super(frame, true);
      parent = frame;


      setSize(250,250);
      setResizable(false);
      setLocationRelativeTo(parent);

      domset = graph.getDomset();


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



      ones_area = new JTextArea("", 2, 40);
      ones_pane = new JScrollPane(ones_area);


      twos_area = new JTextArea("", 2, 40);
      twos_pane = new JScrollPane(twos_area);


      zeros_area = new JTextArea("", 2, 40);
      zeros_pane = new JScrollPane(zeros_area);


      all_button = new JButton("Fill All");
      all_button.addActionListener(this);
      zeros_button = new JButton("Fill Zeros");
      zeros_button.addActionListener(this);
      ones_button = new JButton("Fill Ones");
      ones_button.addActionListener(this);
      twos_button = new JButton("Fill Twos");
      twos_button.addActionListener(this);

      okButton = new JButton("OK");
      okButton.addActionListener(this);
      cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(this);

      JPanel optionPane = new JPanel();
      optionPane.setLayout(new BoxLayout(optionPane,BoxLayout.Y_AXIS));
      optionPane.add(label("Choose Domination Type"));
      optionPane.add(dom_button);
      optionPane.add(secure_button);
      optionPane.add(total_button);
      optionPane.add(roman_button);
      optionPane.add(weak_button);



      JPanel fillButtons = new JPanel();
      fillButtons.setLayout(new BoxLayout(fillButtons, BoxLayout.X_AXIS));
      fillButtons.add(all_button);
      fillButtons.add(zeros_button);
      fillButtons.add(ones_button);
      fillButtons.add(twos_button);

      JPanel inputPane = new JPanel();
      inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.Y_AXIS));
      inputPane.add(label("Choose vertices fixed to 0"));
      inputPane.add(zeros_pane);
      inputPane.add(label("Choose vertices fixed to 1"));
      inputPane.add(ones_pane);
      inputPane.add(label("Choose vertices fixed to 2"));
      inputPane.add(twos_pane);
      inputPane.add(fillButtons);


      JPanel biggerPane = new JPanel();
      biggerPane.setLayout(new BoxLayout(biggerPane, BoxLayout.X_AXIS));
      biggerPane.add(optionPane);
      biggerPane.add(inputPane);

      JPanel buttonPane = new JPanel();
      buttonPane.add(okButton);
      buttonPane.add(cancelButton);

      JPanel arrangePanel = new JPanel();
      arrangePanel.setLayout(new BoxLayout(arrangePanel, BoxLayout.Y_AXIS));

      arrangePanel.add(biggerPane);
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

            int N = domset.length;

            fixed = new int[N];

            String[] fixed_verts = zeros_area.getText().trim().split("\\s+");

            for (int i = 0; i < fixed_verts.length; i++) {
               if(Objects.equals(fixed_verts[i], "")) {
                  continue;
               }
               int v = Integer.parseInt(fixed_verts[i]);

               fixed[v-1] = -1;

            }

            fixed_verts = ones_area.getText().trim().split("\\s+");

            for (int i = 0; i < fixed_verts.length; i++) {
               if(Objects.equals(fixed_verts[i], "")) {
                  continue;
               }
               int v = Integer.parseInt(fixed_verts[i]);

               fixed[v-1] = 1;

            }

            fixed_verts = twos_area.getText().trim().split("\\s+");

            for (int i = 0; i < fixed_verts.length; i++) {
               if(Objects.equals(fixed_verts[i], "")) {
                  continue;
               }
               int v = Integer.parseInt(fixed_verts[i]);

               fixed[v-1] = 2;

            }



            cancelled = false;
            setVisible(false);
            dispose();
         }catch (Exception ex){
            JOptionPane.showMessageDialog(this,"bad formatting in some box","Incorrect format",JOptionPane.ERROR_MESSAGE);
            return;
         }




      }
      if(e.getSource() == cancelButton)
      {
         setVisible(false);
         dispose();
      }

      if(e.getSource() == all_button){

         StringJoiner sj_z = new StringJoiner(" ");
         StringJoiner sj_o = new StringJoiner(" ");
         StringJoiner sj_t = new StringJoiner(" ");

         for (int i = 0; i < domset.length; i++) {
            if(domset[i] == 0){
               sj_z.add("" + (i+1));
            } else if(domset[i] == 1){
               sj_o.add("" + (i+1));
            } else if(domset[i] == 2){
               sj_t.add("" + (i+1));
            }

         }

         zeros_area.setText(sj_z.toString());
         ones_area.setText(sj_o.toString());
         twos_area.setText(sj_t.toString());


      }

      if(e.getSource() == zeros_button){

         StringJoiner sj_z = new StringJoiner(" ");

         for (int i = 0; i < domset.length; i++) {
            if(domset[i] == 0){
               sj_z.add("" + (i+1));
            }

         }

         zeros_area.setText(sj_z.toString());
      }

      if(e.getSource() == ones_button){

         StringJoiner sj_o = new StringJoiner(" ");

         for (int i = 0; i < domset.length; i++) {
            if(domset[i] == 1){
               sj_o.add("" + (i+1));
            }

         }

         ones_area.setText(sj_o.toString());


      }

      if(e.getSource() == twos_button){

         StringJoiner sj_t = new StringJoiner(" ");

         for (int i = 0; i < domset.length; i++) {
            if(domset[i] == 2){
               sj_t.add("" + (i+1));
            }

         }

         twos_area.setText(sj_t.toString());


      }



   }

   public boolean getCancelled()
   {
      return cancelled;
   }

   public int getDomtype(){return domination_type;}

   public int[] getFixed(){return fixed;}

   private JLabel label(String s){
      JLabel l = new JLabel(s);
      l.setHorizontalAlignment(SwingConstants.LEFT);
      return l;
   }



}
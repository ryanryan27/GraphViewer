//package UGV;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;

public class ArrangeGridDialog extends JDialog implements ActionListener
{
   JFrame parent;
   int width;
   double spacing;
   boolean vertical;


   JTextField widthBox;
   JLabel widthLabel;
   JTextField spacingBox;
   JLabel spacingLabel;
   JCheckBoxMenuItem verticalBox;
   JButton okButton, cancelButton;

   boolean cancelled = true;



   public ArrangeGridDialog(JFrame frame)
   {
      super(frame, true);
      parent = frame;


      setSize(200,180);
      setResizable(false);
      setLocationRelativeTo(parent);

      widthBox = new JTextField(4);
      widthBox.setText("2");
      widthLabel = new JLabel("Number of Columns");
      spacingBox = new JTextField(4);
      spacingBox.setText("100.0");
      spacingLabel = new JLabel("Spacing");
      verticalBox = new JCheckBoxMenuItem("Vertical Numbering", false);

      okButton = new JButton("OK");
      okButton.addActionListener(this);
      cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(this);

      JPanel widthPane = new JPanel();
      widthPane.add(widthLabel);
      widthPane.add(widthBox);

      JPanel spacingPane = new JPanel();
      spacingPane.add(spacingLabel);
      spacingPane.add(spacingBox);

      JPanel buttonPane = new JPanel();
      buttonPane.add(okButton);
      buttonPane.add(cancelButton);

      JPanel arrangePanel = new JPanel();
      arrangePanel.setLayout(new VerticalLayout(0));

      arrangePanel.add(new JLabel("Choose grid options"));
      arrangePanel.add(widthPane);
      arrangePanel.add(spacingPane);
      arrangePanel.add(verticalBox);
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

            width = Integer.parseInt(widthBox.getText());
            spacing = Double.parseDouble(spacingBox.getText());
            vertical = verticalBox.getState();

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

   public int getGridSize(){
      return width;
   }

   public boolean getVertical(){
      return vertical;
   }

   public double getSpacing(){
      return spacing;
   }
}
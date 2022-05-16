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


   /**
    * Creates a dialog box where a number of columns, a spacing, and which direction vertices are labelled can be specified.
    * Used to rearrange a graph onto a grid.
    * @param frame parent frame for the dialog box.
    */
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
      arrangePanel.setLayout(new BoxLayout(arrangePanel, BoxLayout.Y_AXIS));

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

   /**
    * Returns whether the dialog was closed before being completed.
    * @return whether the dialog was cancelled.
    */
   public boolean getCancelled()
   {
      return cancelled;
   }

   /**
    * Returns the number of columns specified for the grid.
    * @return number of columns.
    */
   public int getGridSize(){
      return width;
   }

   /**
    * Returns whether the vertex labelling is done horizontally (first row is 1 2 3 ...) or vertically (first column is 1 2 3 ...).
    * @return true if ordering is vertical, false if ordering is horizontal.
    */
   public boolean getVertical(){
      return vertical;
   }

   /**
    * Returns the spacing between the centres of vertices for the grid layout.
    * @return grid spacing in pixels (when no zoom applied).
    */
   public double getSpacing(){
      return spacing;
   }
}
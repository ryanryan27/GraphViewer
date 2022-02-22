//package UGV;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

public class TexDialog extends JDialog implements ActionListener
{
   JFrame parent;

   Graph graph;


   JScrollPane scrollPane;
   JTextArea texArea;
   JButton generateButton, saveButton, closeButton;

   JCheckBoxMenuItem labels;

   boolean doLabel = true;



   public TexDialog(JFrame frame, Graph graph)
   {
      super(frame, true);
      parent = frame;

      this.graph = graph;

      setSize(550,200);
      setResizable(true);
      setLocationRelativeTo(parent);


      texArea = new JTextArea("", 10, 80);
      scrollPane = new JScrollPane(texArea);

      generateButton = new JButton("Generate Tex");
      generateButton.addActionListener(this);
      saveButton = new JButton("Save To File");
      saveButton.addActionListener(this);
      closeButton = new JButton("Cancel");
      closeButton.addActionListener(this);

      labels = new JCheckBoxMenuItem("Show Labels?");
      labels.setSelected(false);


      JPanel buttonPane = new JPanel();
      buttonPane.add(labels);
      buttonPane.add(generateButton);
      buttonPane.add(saveButton);
      buttonPane.add(closeButton);

      JPanel arrangePanel = new JPanel();
      arrangePanel.setLayout(new VerticalLayout(0));


      arrangePanel.add(scrollPane);
      arrangePanel.add(buttonPane);

      getContentPane().add(arrangePanel);
      pack();
      setVisible(true);
   }

   public void actionPerformed(ActionEvent e)
   {

      doLabel = labels.getState();

      if(e.getSource() == generateButton)
      {

         //TODO print the tex to the dialog

         texArea.setText(generateTex());


      }
      else if(e.getSource() == saveButton){
         //TODO save the tex to a file
      }
      else if(e.getSource() == closeButton)
      {
         setVisible(false);
         dispose();
      }
   }

   private String generateTex() {

      double[] topleft = graph.getTopLeft();
      double[] bottomRight = graph.getBottomRight();

      double w = bottomRight[0] - topleft[0];
      double h = bottomRight[1] - topleft[1];

      double scale = 10/Math.max(w,h);


      int nodeSize = 6;
      String colour = "black";

      if(doLabel){
         nodeSize = 12;
         colour = "white";
      }


      StringBuilder texSB = new StringBuilder();

      texSB.append("\\begin{tikzpicture}[scale=0.5, thick]\n");
      texSB.append(" \\tikzstyle{every node}=[circle, draw, fill=").append(colour).append(", inner sep=0pt, minimum width=").append(nodeSize).append("pt]\n ");
      texSB.append(" \\tikzstyle{dom}=[fill=white]\n ");
      for (int i = 0; i < graph.getN(); i++) {
         String p1 = String.format("%.3f",((graph.getXPos(i) -topleft[0])*scale));
         String p2 = String.format("%.3f", 10-(graph.getYPos(i)-topleft[1])*scale);
         texSB.append("\\coordinate (V").append(i + 1).append(") at (").append(p1).append(",").append(p2).append("); ");
      }

      texSB.append("\n ");

      int[][] arcs = graph.getArcs();
      int[] degrees = graph.getDegrees();

      for (int i = 0; i < graph.getN(); i++){
         for (int j = 0; j < degrees[i]; j++) {
            int v1 = i + 1;
            int v2 = arcs[i][j];

            texSB.append("\\draw (V").append(v1).append(") -- (V").append(v2).append("); ");


         }
      }

      texSB.append("\n \\draw");

      for (int i = 1; i <= graph.getN(); i++) {
         String lab = "";
          if(doLabel){
             lab = Integer.toString(i);
          }

          String dom = "";

          if(graph.inDomset(i) > 0){
             dom = "[dom]";
          }

         texSB.append(" (V").append(i).append(") node").append(dom).append("{").append(lab).append("}");
      }

      texSB.append(";\n\\end{tikzpicture}");

      return texSB.toString();

   }


}
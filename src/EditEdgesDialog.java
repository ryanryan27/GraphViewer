//package UGV;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;

public class EditEdgesDialog extends JDialog implements ActionListener
{
   JFrame parent;
   int [][]arcs;
   int []degrees;
   int nodes;
   JScrollPane scrollPane;
   JTextArea edgeArea;
   JTextField numberOfVerticesField;
   JCheckBox contourCheckBox;
   JButton okButton, cancelButton;
   int [][]newArcs;
   int []newDegrees;
   int newNodes;
   boolean cancelled = true;

   public EditEdgesDialog(JFrame frame, int[][] a, int[] d, int N)
   {
      super(frame, true);
      parent = frame;
      arcs = a;
      degrees = d;
      nodes = N;
   
      setSize(350,520);
      setResizable(false);
      setLocationRelativeTo(parent);


   
      String edgeString = "";
      for(int i=0; i<nodes; i++)
         for(int j=0; j<degrees[i]; j++)
            if((i+1) < arcs[i][j])
               edgeString += ((i+1) + " " + arcs[i][j] + "\n");
   
      edgeArea = new JTextArea(edgeString,20,15);
      scrollPane = new JScrollPane(edgeArea);
      
      JPanel numberOfVerticesPane = new JPanel();
      numberOfVerticesPane.add(new JLabel("Number of vertices"));
      numberOfVerticesField = new JTextField(""+N,5);
      numberOfVerticesPane.add(numberOfVerticesField);
   
      contourCheckBox = new JCheckBox("Arrange vertices in a circle:   ",false);
   
      //JPanel contourPane = new JPanel();
   
   //contourPane.add(new JLabel("Arrange vertices in a circle: "));
     // contourPane.add(
   
   
      okButton = new JButton("OK");
      okButton.addActionListener(this);
      cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(this);
   
      JPanel buttonPane = new JPanel();
      buttonPane.add(okButton);
      buttonPane.add(cancelButton);
   
      JPanel editEdgesPanel = new JPanel();
      editEdgesPanel.setLayout(new BoxLayout(editEdgesPanel, BoxLayout.Y_AXIS));
   
      editEdgesPanel.add(new JLabel("Edit the list of edges below. Note that edges may"));
      editEdgesPanel.add(new JLabel("be entered for vertices not yet created."));
      editEdgesPanel.add(new JLabel(" "));
      editEdgesPanel.add(scrollPane);
      editEdgesPanel.add(new JLabel(" "));
      editEdgesPanel.add(numberOfVerticesPane);
      editEdgesPanel.add(contourCheckBox);
      editEdgesPanel.add(buttonPane);
   
      getContentPane().add(editEdgesPanel);
      pack();
      setVisible(true);
   }
   
   public void actionPerformed(ActionEvent e)
   {
      if(e.getSource() == okButton)
      {
         JOptionPane jop = new JOptionPane();
         int numberOfVertices;
         try
         {
            numberOfVertices = Integer.parseInt(numberOfVerticesField.getText());
            if(numberOfVertices < 0)
            {
               jop.showMessageDialog(this,("There must be a positive number of vertices!"),"Incorrect format",JOptionPane.ERROR_MESSAGE);
               return;
            }
         }
         catch(Exception ex)
         {
            jop.showMessageDialog(this,("There must be an integer number of vertices!"),"Incorrect format",JOptionPane.ERROR_MESSAGE);
            return;
         }
         String newEdgesString = edgeArea.getText();
         StringTokenizer tokens = new StringTokenizer(newEdgesString);
         StringTokenizer tokensEndLine = new StringTokenizer(newEdgesString,"\n");
         int entries = tokens.countTokens();
         if(entries % 2 == 1)
         {
            jop.showMessageDialog(this,("Exactly two entries per row must be entered in the edge list!"),"Incorrect format",JOptionPane.ERROR_MESSAGE);
            return;
         }
         int [][]readInArcs = new int[entries/2][2];
         int arcCount = 0;
         int maximum = 0;
         try
         {
            while(tokensEndLine.hasMoreTokens())
            {
               tokens = new StringTokenizer(tokensEndLine.nextToken());
               if(tokens.countTokens() == 0)
                  continue;
               if(tokens.countTokens() != 2)
               {
                  jop.showMessageDialog(this,("Exactly two entries per row must be entered in the edge list!"),"Incorrect format",JOptionPane.ERROR_MESSAGE);
                  return;
               }
               int v1 = Integer.parseInt(tokens.nextToken());
               int v2 = Integer.parseInt(tokens.nextToken());
               if(v1 < 1 || v2 < 1)
               {
                  jop.showMessageDialog(this,("All entries in the edge list must be positive!"),"Incorrect format",JOptionPane.ERROR_MESSAGE);
                  return;
               }
               if(v1 == v2)
               {
                  jop.showMessageDialog(this,("Self-loops are not permitted in UGV!"),"Incorrect format",JOptionPane.ERROR_MESSAGE);
                  return;
               }
               if(v1 > maximum)
                  maximum = v1;
               if(v2 > maximum)
                  maximum = v2;
               readInArcs[arcCount][0] = v1;
               readInArcs[arcCount][1] = v2;
               arcCount++;   
            }
         }
                     
         catch(Exception ex)
         {
            jop.showMessageDialog(this,("Only integer values may be entered in the edge list!"),"Incorrect format",JOptionPane.ERROR_MESSAGE);
            return;
         }
        
         if(readInArcs[entries/2-1][0] == 0 || readInArcs[entries/2-1][1] == 0)
            System.out.println("Error: This should never happen");
            
         if(numberOfVertices < maximum)
         {
            //jop.showMessageDialog(this,("This edge list requires at least " + maximum + " vertices!"),"Not enough vertices",JOptionPane.ERROR_MESSAGE);
            //return;
            numberOfVertices = maximum;
         }
      		
         newNodes = numberOfVertices;
         newDegrees = new int[numberOfVertices];
      		
         for(int i=0; i<entries/2; i++)
            for(int j=0; j<2; j++)
               newDegrees[readInArcs[i][j]-1]++;
      	
         int maxDegree = 0;
         for(int i=0; i<newNodes; i++)
            if(newDegrees[i] > maxDegree)
               maxDegree = newDegrees[i];
      
      	
         newArcs = new int[newNodes][maxDegree];
      	
      	
         int []count = new int[newNodes];
      	
         for(int i=0; i<entries/2; i++)
         {
            int v1 = readInArcs[i][0];
            int v2 = readInArcs[i][1];
            boolean enter = true;
            for(int j=0; j<count[v1-1]; j++)
               if(newArcs[v1-1][j] == v2)
               {
                  enter = false;
               }
            for(int j=0; j<count[v2-1]; j++)
               if(newArcs[v2-1][j] == v1)
               {
                  enter = false;
               }
               
         
            if(enter)
            {
               newArcs[v1-1][count[v1-1]++] = v2;
               newArcs[v2-1][count[v2-1]++] = v1;
            }
         }
      	
         int oldMaxDegree = maxDegree;
         maxDegree = 0;
         for(int i=0; i<newNodes; i++)
            for(int j=0; j<oldMaxDegree+1; j++)
            {
               if(j == oldMaxDegree)
               {
                  newDegrees[i] = oldMaxDegree;
                  if(oldMaxDegree > maxDegree)
                     maxDegree = oldMaxDegree;
                  break;
               }
               if(newArcs[i][j] == 0)
               {
                  newDegrees[i] = j;
                  if(j > maxDegree)
                     maxDegree = j;
                  break;
               }
            
            }
               
               
         if(maxDegree != oldMaxDegree)
         {
            int updatedNewArcs[][] = new int[newNodes][maxDegree];
            for(int i=0; i<newNodes; i++)
               for(int j=0; j<maxDegree; j++)
                  updatedNewArcs[i][j] = newArcs[i][j];
         	
            newArcs = updatedNewArcs;	
         }
      
         cancelled = false;
         setVisible(false);
         dispose();
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

   public boolean getArrangeContour()
   {
      return contourCheckBox.isSelected();
   }
   
   public int[][] getArcs()
   {
      return newArcs;
   }
	
   public int[] getDegrees()
   {
      return newDegrees;
   }
	
   public int getNodes()
   {
      return newNodes;
   }
}
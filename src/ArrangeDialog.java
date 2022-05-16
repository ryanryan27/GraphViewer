//package UGV;

   import javax.swing.*;
   import java.awt.event.*;
   import java.util.StringJoiner;
   import java.util.StringTokenizer;

   public class ArrangeDialog extends JDialog implements ActionListener
   {
      JFrame parent;
      int nodes;
      int []contour;
      JScrollPane scrollPane;
      JTextArea contourArea;
      JButton okButton, cancelButton;
      boolean cancelled = true;
      int []newContour;


      /**
       * Creates and displays a new dialog box to specify a new ordering for the vertices.
       * @param frame parent frame for this dialog to be created from.
       * @param N number of vertices in the graph.
       * @param co current vertex ordering.
       */
      public ArrangeDialog(JFrame frame, int N, int[] co)
      {
         super(frame, true);
         parent = frame;
         nodes = N;
         contour = co;
      
         setSize(400,180);
         setResizable(false);
         setLocationRelativeTo(parent);

         StringJoiner contourSJ = new StringJoiner(" ");

         for (int j : contour) {
            contourSJ.add(Integer.toString(j + 1));
         }
      
         contourArea = new JTextArea(contourSJ.toString(),6,30);
         contourArea.setLineWrap(true);
         contourArea.setWrapStyleWord(true);
      
         scrollPane = new JScrollPane(contourArea);
      
         okButton = new JButton("OK");
         okButton.addActionListener(this);
         cancelButton = new JButton("Cancel");
         cancelButton.addActionListener(this);
      
         JPanel buttonPane = new JPanel();
         buttonPane.add(okButton);
         buttonPane.add(cancelButton);
      
         JPanel arrangePanel = new JPanel();
         arrangePanel.setLayout(new BoxLayout(arrangePanel, BoxLayout.Y_AXIS));
      
         arrangePanel.add(new JLabel("Enter vertex order"));
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
            String contourText = contourArea.getText();
            StringTokenizer tokens = new StringTokenizer(contourText);
            boolean[] check = new boolean[nodes];

            for(int i=0; i<nodes; i++) {
               check[i] = false;
            }

            newContour = new int[nodes];

            try
            {
               for(int i=0; i<nodes; i++)
               {
                  if(!tokens.hasMoreTokens())
                  {
                  // ERROR: Not enough numbers
                     JOptionPane.showMessageDialog(this,("Only " + i + " vertices entered, but the graph contains " + nodes + " vertices!"),"Not enough vertices",JOptionPane.ERROR_MESSAGE);
                     return;
                  }
                  newContour[i] = Integer.parseInt(tokens.nextToken());
                  if(newContour[i] <= 0 || newContour[i] > nodes)
                  {
                  // ERROR: Input out of range
                     if(newContour[i] <= 0)
                        JOptionPane.showMessageDialog(this,"All inputs must be positive integers!","Incorrect format",JOptionPane.ERROR_MESSAGE);
                     else
                        JOptionPane.showMessageDialog(this,"Vertex " + newContour[i] + " entered, but the graph only contains " + nodes + " vertices!","Vertex entry too large",JOptionPane.ERROR_MESSAGE);
                     return;
                  }
                  if(check[newContour[i]-1])
                  {
                  // ERROR: Repeated number 
                     JOptionPane.showMessageDialog(this,("Vertex " + newContour[i] + " entered multiple times!"),"Multiple vertex entry",JOptionPane.ERROR_MESSAGE);
                     return;
                  }
                  check[newContour[i]-1] = true;
               }
               if(tokens.hasMoreTokens())
               {
                  int count = 0;
                  while(tokens.hasMoreTokens())
                  {
                     count++;
                     tokens.nextToken();
                  }
               // ERROR: Too many numbers
                  JOptionPane.showMessageDialog(this,("" + (nodes+count) + " vertices entered, but the graph only contains " + nodes + " vertices!"),"Too many vertices",JOptionPane.ERROR_MESSAGE);
                  return;
               }
               
            	// SUCCESS: Retrieve new contour
               cancelled = false;
               setVisible(false);
               dispose();
            
            	
            }
               catch(Exception ex)
               {
                  JOptionPane.showMessageDialog(this,"All inputs must be positive integers!","Incorrect format",JOptionPane.ERROR_MESSAGE);
                  return;
                  // ERROR : Non-integer
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
       * Gets an array indicating some ordering of the vertices, which may not be the numerical order.
       * @return array in order of intended vertex order.
       */
      public int[] getContour()
      {
         int []returnedContour = new int[nodes];
         for(int i=0; i<nodes; i++)
            returnedContour[i] = newContour[i]-1;
         return returnedContour;
      }
   }
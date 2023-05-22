//package UGV;

   import javax.swing.*;
   import java.awt.*;
   import java.awt.event.*;

   public class PropertiesDialog extends JDialog implements ActionListener {
      JFrame parent;
      GraphPane graphPane;
      Graph graph;
      JLabel verticesLabel, edgesLabel, connectedLabel, regularLabel, maxDegreeLabel, minDegreeLabel, averageDegreeLabel, bipartiteLabel, planarLabel;
      JButton connectivityButton, girthButton, diameterButton;
      JLabel connectivityLabel, girthLabel, diameterLabel;
      JButton okButton;
      JProgressBar progressBar;
   	
      SlowPropertiesRunner spr;
      Thread thread = null;
   	
      final int ACTIONS_CONNECTIVITY = 0;
      final int ACTIONS_GIRTH = 1;
      final int ACTIONS_DIAMETER = 2;

      /**
       * Creates a dialog box to view and calculate different properties of the graph within the specified GraphPane.
       * @param frame the parent frame from which to create this dialog.
       * @param gp the GraphPane whose graph's properties are to be viewed or calculated.
       */
      public PropertiesDialog(JFrame frame, GraphPane gp) {
         super(frame,true);
         parent = frame;
         graphPane = gp;
         graph = graphPane.getGraph();
      
         setSize(480,370);
         setResizable(false);
         setLocationRelativeTo(parent);
         
         prepareStats();
         
         spr = new SlowPropertiesRunner(graph, this);
         
         JPanel titlePanel = new JPanel();
         titlePanel.add(Box.createHorizontalGlue());
         JLabel titleLabel = new JLabel("                                              Properties                                              ");
         titleLabel.setFont(titleLabel.getFont().deriveFont(24f));
         titlePanel.add(titleLabel);
         titlePanel.add(Box.createHorizontalGlue());
      
         JPanel statsPanel = new JPanel();
         statsPanel.setLayout(new GridLayout(3,3,10,8));
         statsPanel.add(verticesLabel);
         statsPanel.add(edgesLabel);
         statsPanel.add(connectedLabel);
         statsPanel.add(minDegreeLabel);
         statsPanel.add(maxDegreeLabel);
         statsPanel.add(averageDegreeLabel);
         statsPanel.add(regularLabel);
         statsPanel.add(bipartiteLabel);
         statsPanel.add(planarLabel);

         connectivityButton = new JButton("Check Connectivity");
         connectivityButton.addActionListener(this);
      	
         girthButton = new JButton("Check Girth");
         girthButton.addActionListener(this);

         diameterButton = new JButton("Check Diameter");
         diameterButton.addActionListener(this);
         
         JPanel combinedButtonsPanel = new JPanel();
         JPanel buttonsPanel = new JPanel();
         buttonsPanel.setLayout(new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints();
      	
         c.gridx = 0;
         c.gridy = 0;
         c.fill = 1;
         c.ipadx = 10;
         c.ipady = 10;
         buttonsPanel.add(connectivityButton,c);
         c.gridx = 1;	
         c.gridy = 0;
         buttonsPanel.add(new JLabel("        "));
         c.gridx = 2;
         c.gridy = 0;
         buttonsPanel.add(connectivityLabel,c);
         c.gridx = 3;
         c.gridy = 0;
         buttonsPanel.add(new JLabel(" "));
         c.gridx = 0;
         c.gridy = 1;
         buttonsPanel.add(girthButton,c);
         c.gridx = 1;
         c.gridy = 1;
         buttonsPanel.add(new JLabel("        "));
         c.gridx = 2;
         c.gridy = 1;
         buttonsPanel.add(girthLabel,c);
         c.gridx = 3;
         c.gridy = 1;
         buttonsPanel.add(new JLabel(" "));
         c.gridx = 0;
         c.gridy = 2;
         buttonsPanel.add(diameterButton,c);
         c.gridx = 1;
         c.gridy = 2;
         buttonsPanel.add(new JLabel("        "));
         c.gridx = 2;
         c.gridy = 2;
         buttonsPanel.add(diameterLabel,c);
         c.gridx = 3;
         c.gridy = 2;
         buttonsPanel.add(new JLabel(" "));
      	
         progressBar = new JProgressBar(SwingConstants.VERTICAL);
         progressBar.setPreferredSize(new Dimension(14,120));
      	      	
         combinedButtonsPanel.add(buttonsPanel);
         combinedButtonsPanel.add(new JLabel("  "));
         combinedButtonsPanel.add(progressBar);

      	
         okButton = new JButton("OK");
         okButton.addActionListener(this);
         JPanel bottomButtonPanel = new JPanel();
         bottomButtonPanel.add(Box.createHorizontalGlue());
         bottomButtonPanel.add(okButton);
         bottomButtonPanel.add(Box.createHorizontalGlue());
      
      
         JPanel propertiesPanel = new JPanel();
         propertiesPanel.setLayout(new BoxLayout(propertiesPanel, BoxLayout.Y_AXIS));
      
      
         propertiesPanel.add(titlePanel);
         propertiesPanel.add(new JLabel(" "));
         propertiesPanel.add(statsPanel);
         propertiesPanel.add(new JLabel(" "));
         propertiesPanel.add(new JLabel(" "));
         propertiesPanel.add(combinedButtonsPanel);
         propertiesPanel.add(new JLabel(" "));
         propertiesPanel.add(bottomButtonPanel);
      
         getContentPane().add(propertiesPanel);
         pack();
         setVisible(true);
      
      }

      /**
       * Closes the dialog and kills the calculation threads.
       */
      public void exit() {
         if(thread != null){
            spr.stop();
            try {
               thread.join();
            } catch (InterruptedException e1) {
               System.out.println("oops");
            }
         }
         setVisible(false);
         dispose();
         
      }


      /**
       * Calculates the easy to process graph properties.
       */
      public void prepareStats() {
         int nodes = graph.getN();
         String nodesString = (""+nodes);
      	
         verticesLabel = new JLabel("Vertices        :   " + nodesString);
      
         int [][]arcs = graph.getArcs();
         int []degrees = graph.getDegrees();
      
         int edges = 0;
         for(int i=0; i<nodes; i++) {
            for(int j=0; j<degrees[i]; j++) {
               if(arcs[i][j] > (i+1)) {
                  edges++;
               }
            }
         }
      
         String edgesString = (""+edges);
      
         edgesLabel = new JLabel("Edges              :   " + edgesString);
      
         if(nodes > 0 && edges > 0 && graph.isConnected()) {
            connectedLabel = new JLabel("Connected       :   Yes");
         } else {
            connectedLabel = new JLabel("Connected       :   No ");
         }
      
         int minDegree, maxDegree, totalDegrees;
      
         if(nodes > 0) {
         
            minDegree = degrees[0];
            maxDegree = degrees[0];
            totalDegrees = degrees[0];
            for(int i=1; i<nodes; i++) {
               if(degrees[i] < minDegree) {
                  minDegree = degrees[i];
               }
               if(degrees[i] > maxDegree) {
                  maxDegree = degrees[i];
               }
               totalDegrees += degrees[i];
            }
         } else {
            minDegree = 0;
            maxDegree = 0;
            totalDegrees = 0;
         }
         String minDegreeString = (""+minDegree);

      
         String maxDegreeString = (""+maxDegree);

      
         String averageDegreeString = "";
      
         if(nodes > 0) {
            averageDegreeString = (""+Math.round(totalDegrees*1.0/nodes*10000)/10000.0);
         } else {
            averageDegreeString = ("0.0");
         }
      
         minDegreeLabel = new JLabel("Min Degree   :   " + minDegreeString);
         maxDegreeLabel = new JLabel("Max Degree   :   " + maxDegreeString);
         averageDegreeLabel = new JLabel("Mean Degree   :   " +averageDegreeString);

         if(minDegree != maxDegree) {
            regularLabel = new JLabel("Regular         :   No ");
         } else {
            regularLabel = new JLabel("Regular         :   Yes");
         }
      
         if(graph.isBipartite()) {
            bipartiteLabel = new JLabel("Bipartite          :   Yes");
         } else {
            bipartiteLabel = new JLabel("Bipartite          :   No ");
         }
      
         planarLabel = new JLabel("             ");

         connectivityLabel = new JLabel("Not yet checked                         ");
         connectivityLabel.setPreferredSize(new Dimension(160,25));
         girthLabel = new JLabel("Not yet checked                         ");
         girthLabel.setPreferredSize(new Dimension(160,25));
         diameterLabel = new JLabel("Not yet checked                         ");
         diameterLabel.setPreferredSize(new Dimension(160,25));
      
      
      }
      
      public void actionPerformed(ActionEvent e)
      {
         if(e.getSource() == okButton){
            exit();
         }
         if(e.getSource() == connectivityButton)
         {
            spr.setAction(ACTIONS_CONNECTIVITY);
            if(thread != null){
               spr.stop();
               try {
                  thread.join();
               } catch (InterruptedException e1) {
                  System.out.println("oops");
               }
            }
            thread = new Thread(spr);
            thread.start();
         }
         if(e.getSource() == girthButton)
         {
            spr.setAction(ACTIONS_GIRTH);
            if(thread != null){
               spr.stop();
               try {
                  thread.join();
               } catch (InterruptedException e1) {
                  System.out.println("oops");
               }
            }
            thread = new Thread(spr);
            thread.start();
         }
         if(e.getSource() == diameterButton)
         {
            spr.setAction(ACTIONS_DIAMETER);
            if(thread != null){
               spr.stop();
               try {
                  thread.join();
               } catch (InterruptedException e1) {
                  System.out.println("oops");
               }
            }
            thread = new Thread(spr);
            
            thread.start();
         }
      }

      /**
       * Updates text labels based on the results of property calculations.
       * @param action which property has been calculated.
       * @param result the result of the calculation.
       */
      public void setLabel(int action, int result)
      {
         if(action == ACTIONS_CONNECTIVITY)
         {
            if(result == 0) {
               connectivityLabel.setText("The graph is disconnected ");
            } else if(result == -1){
               connectivityLabel.setText("Connectivity check cancelled");
            } else {
               connectivityLabel.setText("The graph is " + result + "-connected ");
            }
         }
         if(action == ACTIONS_GIRTH)
         {
            if(result == -1) {
               girthLabel.setText("The graph contains no cycles ");
            } else if(result == -2){
               girthLabel.setText("Girth check cancelled");
            }  else {
               girthLabel.setText("The graph has girth " + result + " ");
            }
         }
         if(action == ACTIONS_DIAMETER)
         { 
             if(result == -1){
               diameterLabel.setText("Diameter check cancelled");
            } else {
               diameterLabel.setText("The graph has diameter " + result + " ");
            }
         }
         validate();   
         repaint();
      }

      /**
       * The progress bar associated with this dialog.
       * @return the dialog progress bar.
       */
      public JProgressBar getProgressBar()
      {
         return progressBar;
      }
   
   }
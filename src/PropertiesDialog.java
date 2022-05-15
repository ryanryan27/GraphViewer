//package UGV;

   import javax.swing.*;
   import java.awt.*;
   import java.awt.event.*;

   public class PropertiesDialog extends JDialog implements ActionListener, WindowListener
   {
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
   
      public PropertiesDialog(JFrame frame, GraphPane gp)
      {
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
      
         /*JPanel topStatsPanel = new JPanel();
         topStatsPanel.add(Box.createHorizontalGlue());
         topStatsPanel.add(new JLabel("Vertices:"));
         topStatsPanel.add(verticesLabel);
         topStatsPanel.add(Box.createHorizontalGlue());
         topStatsPanel.add(new JLabel("Edges:"));
         topStatsPanel.add(edgesLabel);
         topStatsPanel.add(Box.createHorizontalGlue());
         topStatsPanel.add(connectedLabel);
         topStatsPanel.add(Box.createHorizontalGlue());
            
         JPanel middleStatsPanel = new JPanel();
         middleStatsPanel.add(Box.createHorizontalGlue());
         middleStatsPanel.add(new JLabel("Min Degree:"));
         middleStatsPanel.add(minDegreeLabel);
         middleStatsPanel.add(Box.createHorizontalGlue());
         middleStatsPanel.add(new JLabel("Max Degree:"));
         middleStatsPanel.add(maxDegreeLabel);
         middleStatsPanel.add(Box.createHorizontalGlue());
         middleStatsPanel.add(new JLabel("Average Degree:"));
         middleStatsPanel.add(averageDegreeLabel);
         middleStatsPanel.add(Box.createHorizontalGlue());
                                    
         JPanel bottomStatsPanel = new JPanel();
         bottomStatsPanel.add(Box.createHorizontalGlue());
         bottomStatsPanel.add(regularLabel);
         bottomStatsPanel.add(Box.createHorizontalGlue());
         bottomStatsPanel.add(bipartiteLabel);
         bottomStatsPanel.add(Box.createHorizontalGlue());
         bottomStatsPanel.add(planarLabel);
         bottomStatsPanel.add(Box.createHorizontalGlue());*/
         
         //JPanel connectivityPanel = new JPanel();
         connectivityButton = new JButton("Check Connectivity");
         connectivityButton.addActionListener(this);
         //connectivityPanel.add(connectivityButton);
         //connectivityPanel.add(connectivityLabel);
      	
         //JPanel girthPanel = new JPanel();
         girthButton = new JButton("Check Girth");
         girthButton.addActionListener(this);
         //girthPanel.add(girthButton);
         //girthPanel.add(girthLabel);
      	
         //JPanel diameterPanel = new JPanel();
         diameterButton = new JButton("Check Diameter");
         diameterButton.addActionListener(this);
         
         JPanel combinedButtonsPanel = new JPanel();
         JPanel buttonsPanel = new JPanel();
         //buttonsPanel.setLayout(new GridLayout(3,2,30,15));
         buttonsPanel.setLayout(new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints();
      	
         c.gridx = 0;
         c.gridy = 0;
         c.fill = 1;
         c.ipadx = 10;
         c.ipady = 10;
         //c.weightx = 0;
         //c.weightx = 0.25;	
         buttonsPanel.add(connectivityButton,c);
         c.gridx = 1;	
         c.gridy = 0;
         buttonsPanel.add(new JLabel("        "));
         c.gridx = 2;
         c.gridy = 0;
         //c.weightx = 0.75;
         buttonsPanel.add(connectivityLabel,c);
         c.gridx = 3;
         c.gridy = 0;
         //c.weightx = 1.0;
         buttonsPanel.add(new JLabel(" "));
         c.gridx = 0;
         c.gridy = 1;
         //c.weightx = 0.25;
         buttonsPanel.add(girthButton,c);
         c.gridx = 1;
         c.gridy = 1;
         buttonsPanel.add(new JLabel("        "));
         c.gridx = 2;
         c.gridy = 1;
         //c.weightx = 0.75;
         buttonsPanel.add(girthLabel,c);
         c.gridx = 3;
         c.gridy = 1;
         //c.weightx = 1.0;
         buttonsPanel.add(new JLabel(" "));
         c.gridx = 0;
         c.gridy = 2;
         //c.weightx = 0.25;
         buttonsPanel.add(diameterButton,c);
         c.gridx = 1;
         c.gridy = 2;
         buttonsPanel.add(new JLabel("        "));
         c.gridx = 2;
         c.gridy = 2;
         //c.weightx = 0.75;
         buttonsPanel.add(diameterLabel,c);
         c.gridx = 3;
         c.gridy = 2;
         //c.weightx = 1.0;
         buttonsPanel.add(new JLabel(" "));
         /*c.gridx = 0;
         c.gridy = 3;
         buttonsPanel.add(new JLabel("            "));
         c.gridx = 1;
         c.gridy = 3;
         buttonsPanel.add(new JLabel("        "));
         c.gridx = 2;
         c.gridy = 3;
         buttonsPanel.add(new JLabel("       "));   
         c.gridx = 3;
         c.gridy = 3;
         buttonsPanel.add(new JLabel(" "));*/
      	
         progressBar = new JProgressBar(SwingConstants.VERTICAL);
         progressBar.setPreferredSize(new Dimension(14,120));
      	      	
         combinedButtonsPanel.add(buttonsPanel);
         combinedButtonsPanel.add(new JLabel("  "));
         combinedButtonsPanel.add(progressBar);
      	
      	
         //diameterPanel.add(diameterButton);
         //diameterPanel.add(diameterLabel);
      	
         okButton = new JButton("OK");
         okButton.addActionListener(this);
         JPanel bottomButtonPanel = new JPanel();
         bottomButtonPanel.add(Box.createHorizontalGlue());
         bottomButtonPanel.add(okButton);
         bottomButtonPanel.add(Box.createHorizontalGlue());
      
      
         JPanel propertiesPanel = new JPanel();
         propertiesPanel.setLayout(new BoxLayout(propertiesPanel, BoxLayout.Y_AXIS));
      
      
         propertiesPanel.add(titlePanel);
         //propertiesPanel.add(topStatsPanel);
         //propertiesPanel.add(middleStatsPanel);
         //propertiesPanel.add(bottomStatsPanel);
         propertiesPanel.add(new JLabel(" "));
         propertiesPanel.add(statsPanel);
         propertiesPanel.add(new JLabel(" "));
         propertiesPanel.add(new JLabel(" "));
         propertiesPanel.add(combinedButtonsPanel);
         /*propertiesPanel.add(connectivityPanel);
         propertiesPanel.add(girthPanel);
         propertiesPanel.add(diameterPanel);*/
         propertiesPanel.add(new JLabel(" "));
         //propertiesPanel.add(new JLabel(" "));
         propertiesPanel.add(bottomButtonPanel);
      
         getContentPane().add(propertiesPanel);
         pack();
         setVisible(true);
      
      }
      
      public void windowActivated(WindowEvent e)
      {
      
      }
   	
      public void windowClosed(WindowEvent e)
      {
       
      
      }
   	
      public void windowClosing(WindowEvent e)
      {
         exit();
      }
   	
      public void windowDeactivated(WindowEvent e)
      {
      
      }
   	
      public void windowDeiconified(WindowEvent e)
      {
      
      }
   	
      public void windowIconified(WindowEvent e)
      {
      
      }
   	
      public void windowOpened(WindowEvent e)
      {
      
      }
      
      public void exit()
      {
         if(thread != null)
            thread.stop();
         setVisible(false);
         dispose();
         
      }
   
    
      public void prepareStats()
      {
         int nodes = graph.getN();
         String nodesString = (""+nodes);
         String addition = "";
         while(nodesString.length() + addition.length() < 5)
            addition += " ";  
      	
         verticesLabel = new JLabel("Vertices        :   " + nodesString);
      
         int [][]arcs = graph.getArcs();
         int []degrees = graph.getDegrees();
      
         int edges = 0;
         for(int i=0; i<nodes; i++)
            for(int j=0; j<degrees[i]; j++)
               if(arcs[i][j] > (i+1))
                  edges++;
      
         String edgesString = (""+edges);
         addition = "";
         while(edgesString.length() + addition.length() < 6)
            addition += " ";
      
         edgesLabel = new JLabel("Edges              :   " + edgesString);
      
         if(nodes > 0 && edges > 0 && graph.isConnected())
            connectedLabel = new JLabel("Connected       :   Yes");
         else
            connectedLabel = new JLabel("Connected       :   No ");
      
         int minDegree, maxDegree, totalDegrees;
      
         if(nodes > 0)
         {
         
            minDegree = degrees[0];
            maxDegree = degrees[0];
            totalDegrees = degrees[0];
            for(int i=1; i<nodes; i++)
            {
               if(degrees[i] < minDegree)
                  minDegree = degrees[i];
               if(degrees[i] > maxDegree)
                  maxDegree = degrees[i];
               totalDegrees += degrees[i];
            }
         }
         else
         {
            minDegree = 0;
            maxDegree = 0;
            totalDegrees = 0;
         }
         String minDegreeString = (""+minDegree);
         addition = "";
         while(minDegreeString.length() + addition.length() < 3)
            addition += " ";
      
         String maxDegreeString = (""+maxDegree);
         addition = "";
         while(maxDegreeString.length() + addition.length() < 3)
            addition += " ";
      
         String averageDegreeString = "";
      
         if(nodes > 0)
         {
            averageDegreeString = (""+Math.round(totalDegrees*1.0/nodes*10000)/10000.0);
         }
         else
         {
            averageDegreeString = ("0.0");
         }
         addition = "";
         while(averageDegreeString.length() + addition.length() < 8)
            addition += " ";
      
         minDegreeLabel = new JLabel("Min Degree   :   " + minDegreeString);
         maxDegreeLabel = new JLabel("Max Degree   :   " + maxDegreeString);
         averageDegreeLabel = new JLabel("Mean Degree   :   " +averageDegreeString);
      
         //int regular = graph.checkRegular();
         if(minDegree != maxDegree)
            regularLabel = new JLabel("Regular         :   No ");
         else
         {
            /*String regularString = (""+regular);
            addition = "";
            while(regularString.length() + addition.length() < 3)
               addition += " ";
            regularLabel = new JLabel(regularString+"-regular"+addition);*/
            regularLabel = new JLabel("Regular         :   Yes");
         }
      
         if(graph.isBipartite())
            bipartiteLabel = new JLabel("Bipartite          :   Yes");
         else
            bipartiteLabel = new JLabel("Bipartite          :   No ");
      
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
            if(thread != null)
               thread.stop();
            thread = new Thread(spr);
            thread.start();
           /* int connectivity = graph.connectivity();
            if(connectivity == 0)
               connectivityLabel.setText("The graph is disconnected ");
            else
               connectivityLabel.setText("The graph is " + connectivity + "-connected ");
            validate();   
            repaint();*/
         
         }
         if(e.getSource() == girthButton)
         {
            spr.setAction(ACTIONS_GIRTH);
            if(thread != null)
               thread.stop();
            thread = new Thread(spr);
            thread.start();
            /*int girth = graph.girth();
            if(girth == -1)
               girthLabel.setText("The graph contains no cycles ");
            else
               girthLabel.setText("The graph has girth " + girth + " ");
            validate();   
            repaint();*/
         	
            //spr.setAction(ACTIONS_GIRTH);
            //spr.start();
         }
         if(e.getSource() == diameterButton)
         {
            spr.setAction(ACTIONS_DIAMETER);
            if(thread != null)
               thread.stop();
            thread = new Thread(spr);
            thread.start();
            //spr.setAction(ACTIONS_DIAMETER);
            //spr.start();
            
         //diameterLabel.setText("The graph has diameter " + graph.diameter() + " ");
         }
      }
   
      public void setLabel(int action, int result)
      {
         if(action == ACTIONS_CONNECTIVITY)
         {
            if(result == 0)
               connectivityLabel.setText("The graph is disconnected ");
            else
               connectivityLabel.setText("The graph is " + result + "-connected ");
         }
         if(action == ACTIONS_GIRTH)
         {
            if(result == -1)
               girthLabel.setText("The graph contains no cycles ");
            else
               girthLabel.setText("The graph has girth " + result + " ");
         }
         if(action == ACTIONS_DIAMETER)
         {
            diameterLabel.setText("The graph has diameter " + result + " ");
         }
         validate();   
         repaint();
      }
   
      public JProgressBar getProgressBar()
      {
         return progressBar;
      }
   
   }
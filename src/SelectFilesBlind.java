//package UGV;

   import javax.swing.*;
   import java.awt.*;
   import java.awt.event.*;
   import java.util.StringTokenizer;
   import java.util.Arrays;

   public class SelectFilesBlind extends JDialog implements ActionListener
   {
      long number;
      JFrame parent;
      JTextField smartGraphChooserField;
      JButton openButton, cancelButton;
      boolean cancelled = true;
   
      public SelectFilesBlind(JFrame frame, long nu)
      {
         super(frame,true);
         parent = frame;
         number = nu;
      
         setSize(600,160);
         setResizable(false);
         
         JPanel panel = new JPanel();
         panel.setLayout(new VerticalLayout(8));
      
         JPanel topPanel = new JPanel();
      
         if(number == -1)
         {
            topPanel.add(new JLabel("Which graphs would you like to open?"));
         }
         else
         {
            topPanel.add(new JLabel("This file contains " + number + " graphs. Which graphs would you like to open?"));
         }
         
         panel.add(topPanel);
      	
         smartGraphChooserField = new JTextField("",50);
      
         JPanel smartGraphChooserPane = new JPanel();
         smartGraphChooserPane.setLayout(new VerticalLayout(3));
         smartGraphChooserPane.add(smartGraphChooserField);
         smartGraphChooserPane.add(new JLabel("List graphs to open (Example: 3,6,19-25,48)"));
         panel.add(smartGraphChooserPane);
                  
      	
         openButton = new JButton("Open graphs");
         openButton.addActionListener(this);
         cancelButton = new JButton("Cancel");
         cancelButton.addActionListener(this);
      	
         JPanel buttonPane = new JPanel();
         buttonPane.add(openButton);
         buttonPane.add(cancelButton);
      	
         panel.add(buttonPane);
      
      	
      	
         getContentPane().add(panel);
      	
         setVisible(true);
         setLocationRelativeTo(frame); 
      }
   
      public void actionPerformed(ActionEvent e)
      {
         if(e.getSource() == openButton)
         {
            StringTokenizer tokens = new StringTokenizer(smartGraphChooserField.getText(),",");
            JOptionPane jop = new JOptionPane();
            String nextGraphs = "";
            try
            {
               while(tokens.hasMoreTokens())
               {
                  nextGraphs = tokens.nextToken();
                  if(nextGraphs.indexOf("-") == -1)
                  {
                     long graph = Long.parseLong(nextGraphs);
                     if(number != -1 && graph > number)
                     {
                     // Too big
                        jop.showMessageDialog(this,"Graph " + graph + " requested, but file only contains " + number + " graphs!","Graph too large",JOptionPane.ERROR_MESSAGE);
                        return;
                     }
                  }
                  else
                  {
                     StringTokenizer breakdown = new StringTokenizer(nextGraphs,"-");
                     long graph = Long.parseLong(breakdown.nextToken());
                     if(number != -1 && graph > number)
                     {
                     // Too big
                        jop.showMessageDialog(this,"Graph " + graph + " requested, but file only contains " + number + " graphs!","Graph too large",JOptionPane.ERROR_MESSAGE);
                        return;
                     }
                     long graph2 = Long.parseLong(breakdown.nextToken());
                     if(number != -1 && graph2 > number)
                     {
                     // Too big
                        jop.showMessageDialog(this,"Graph " + graph2 + " requested, but file only contains " + number + " graphs!","Graph too large",JOptionPane.ERROR_MESSAGE);
                        return;
                     }
                     if(graph2 <= graph)
                     {
                        jop.showMessageDialog(this,"Incorrect format: " + nextGraphs,"Incorrect format",JOptionPane.ERROR_MESSAGE);
                        return;
                     }
                     if(breakdown.hasMoreTokens())
                     {
                     // Incorrect format
                        jop.showMessageDialog(this,"Incorrect format: " + nextGraphs,"Incorrect format",JOptionPane.ERROR_MESSAGE);
                        return;
                     }
                  }
               }
               
               cancelled = false;
               setVisible(false);
               dispose();
            }
               catch(Exception ex)
               {
                  jop.showMessageDialog(this,"Incorrect format: " + nextGraphs,"Incorrect format",JOptionPane.ERROR_MESSAGE);
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
      
      public long[][] getGraphChoices()
      {
         StringTokenizer tokens = new StringTokenizer(smartGraphChooserField.getText(),",");
         
         String []graphChoices = new String[tokens.countTokens()];
         int count = 0;
         while(tokens.hasMoreTokens())
            graphChoices[count++] = tokens.nextToken();
         
         long numbers[] = new long[graphChoices.length];
         long numbersEnd[] = new long[graphChoices.length];
         long ranges[] = new long[2*graphChoices.length];
         int rangesIndices[] = new int[graphChoices.length];
         int rangeCount = 0;
         try
         {
            for(int i=0; i<graphChoices.length; i++)
            {
               int hyphen = graphChoices[i].indexOf("-");
               if(hyphen == -1)
               {
                  numbers[i] = Long.parseLong(graphChoices[i]);
                  numbersEnd[i] = numbers[i];
               }
               else
               {
                  numbers[i] = Long.parseLong(graphChoices[i].substring(0,hyphen));
                  numbersEnd[i] = Long.parseLong(graphChoices[i].substring(hyphen+1));
                  ranges[2*rangeCount] = numbers[i];
                  ranges[2*rangeCount+1] = numbersEnd[i];
                  rangesIndices[rangeCount] = i;
                  rangeCount++;
               }
            }
         }
            catch(Exception ex)
            {
               System.out.println("This should never happen");
            }
            
      		// First make sure ranges don't overlap
      		
         boolean startAgain = true;
         while(startAgain)
         {
            startAgain = false;
            for(int i=0; i<rangeCount; i++)
            {
               if(ranges[2*i] != -1)
               {
                  for(int j=i+1; j<rangeCount; j++)
                  {
                     if(ranges[2*j] != -1)
                     {
                        if((ranges[2*j] > ranges[2*i] && ranges[2*j] < ranges[2*i+1]) || ((ranges[2*j+1] > ranges[2*i] && ranges[2*j+1] < ranges[2*i+1])))
                        {
                           numbers[rangesIndices[i]] = Math.min(ranges[2*i],ranges[2*j]);
                           numbersEnd[rangesIndices[i]] = Math.max(ranges[2*i+1],ranges[2*j+1]);
                           numbers[rangesIndices[j]] = -1;
                           numbersEnd[rangesIndices[j]] = -1;
                           ranges[2*i] = numbers[rangesIndices[i]];
                           ranges[2*i+1] = numbersEnd[rangesIndices[i]];
                           ranges[2*j] = -1;
                           ranges[2*j+1] = -1;
                           startAgain = true;
                           break;
                        }
                     }
                  }
                  if(startAgain)
                  {
                     break;
                  }
               }
            }      
         }
      		
      		// Next make sure ranges don't include individual graphs
      		      
         for(int i=0; i<rangeCount; i++)
         {
            for(int j=0; j<numbers.length; j++)
            {
               if(rangesIndices[i] != j && ((numbers[j] >= ranges[2*i] && numbers[j] <= ranges[2*i+1]) || (numbersEnd[j] >= ranges[2*i] && numbersEnd[j] <= ranges[2*i+1])))
               {
                  numbers[j] = -1;
                  numbersEnd[j] = -1;
               }
            }
         }
                  
         Arrays.sort(numbers);
         Arrays.sort(numbersEnd);
         
         int index = 0;
         while(index < numbers.length && numbers[index] == -1)
         {
            index++;
         }
         if(index == numbers.length)
         {
            long returnNumber[][] = new long[0][0];
            return returnNumber;
         }
      				
         long returnNumbers[][] = new long[2][numbers.length-index];         
         for(int i=index; i<numbers.length; i++)
         {
            returnNumbers[0][i-index] = numbers[i];
            returnNumbers[1][i-index] = numbersEnd[i];
         }
            
         return returnNumbers;        
      }
   }
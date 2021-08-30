import java.io.*;
import java.util.StringTokenizer;

public class CountGraphs
{
   public static void main(String [] args)
   {
   
      File dir = new File("./checkby/");
      File []files = dir.listFiles();
      
      int countingDone[] = new int[19];
      int countingGraphs[] = new int[19];
   
      for(int i=0; i<files.length; i++)
      {
         if(files[i].getName().indexOf(".cross") != -1)
         {
            String name = files[i].getName();
            name = name.substring(6);
            StringTokenizer tokens = new StringTokenizer(name,".");
            int number = -1;
            try
            {
               number = Integer.parseInt(tokens.nextToken());
            }
            catch(Exception e)
            {
            }
           
            for(int j=0; j<19; j++)
            {
               if(number <= 10000*(j+1) && number > 10000*j)
                  countingDone[j]++;
            }
         }
         else
         {
            if(files[i].getName().indexOf("26_g6") != -1 && files[i].getName().indexOf(".cross") == -1)
            {
               String name = files[i].getName();
               name = name.substring(6);
               StringTokenizer tokens = new StringTokenizer(name,".");
               int number = Integer.parseInt(tokens.nextToken());
               for(int j=0; j<19; j++)
               {
                  if(number <= 10000*(j+1) && number > 10000*j)
                     countingGraphs[j]++;
               }
            
            }
         }
      }
      
      for(int i=0; i<19; i++)
         System.out.println("Graphs " + (10000*i+1) + " to " + (10000*(i+1)) + "  :  " + countingDone[i] + " / " + countingGraphs[i] + "  (" + (countingDone[i]+0.0)/(countingGraphs[i])*100 + "%)");
   
      System.out.println();
      int totaldone = 0;
      int totalgraphs = 0;
      for(int i=0; i<19; i++)
      {
         totaldone += countingDone[i];
         totalgraphs += countingGraphs[i];
      }
      System.out.println("Total : " + totaldone + " / " + totalgraphs + " (" + (totaldone + 0.0)/(totalgraphs)*100 + "%)");
   }
}
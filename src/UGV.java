//package UGV;

import javax.swing.*;

public class UGV
   {
      public static void main(String [] args)
      {

         try {
            // Set System L&F
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         }
         catch (Exception e) {
            // handle exception
         }



      
      UGVViewer viewer = new UGVViewer();
      
      
      
      }
   }
   
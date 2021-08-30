   public class SlowPropertiesRunner implements Runnable
   {
      Graph graph;
      PropertiesDialog propertiesDialog;
      int action;
   
      final int ACTIONS_CONNECTIVITY = 0;
      final int ACTIONS_GIRTH = 1;
      final int ACTIONS_DIAMETER = 2;
   
      public SlowPropertiesRunner(Graph g, PropertiesDialog p)
      {
         graph = g;
         propertiesDialog = p;
         action = ACTIONS_CONNECTIVITY;
      }
      
      public void setAction(int a)
      {
         action = a;
      }
   
      public void run()
      {
         if(action == ACTIONS_CONNECTIVITY)
         {
            int connectivity = graph.connectivity(propertiesDialog);
            propertiesDialog.setLabel(action,connectivity);
         }
         if(action == ACTIONS_GIRTH)
         {
            int girth = graph.girth(propertiesDialog);
            propertiesDialog.setLabel(action,girth);
         }
         if(action == ACTIONS_DIAMETER)
         {
            int diameter = graph.diameter(propertiesDialog);
            propertiesDialog.setLabel(action,diameter);
         }
      }
   }
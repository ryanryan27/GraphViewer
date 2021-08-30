//package UGV;

   public class GraphLLNode
   {
   
      GraphPane graphPane;
      GraphLLNode next = null;
   
      public GraphLLNode(GraphPane gr)
      {
         graphPane = gr;
      }
   
      public GraphLLNode getNext()
      {
         return next;
      }
   
      public void setNext(GraphLLNode ne)
      {
         next = ne;
      }
      
      public GraphPane getGraphPane()
      {
         return graphPane;
      }
   }
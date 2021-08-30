//package UGV;

   import java.awt.*;

   public class GraphLinkedList
   {
   
      GraphLLNode head = null;
      int number = 0;
   
      public GraphLinkedList()
      {
      
      }
   
      public void add(GraphPane graphPane)
      {
         if(head == null)
            head = new GraphLLNode(graphPane);
         else
         {
            GraphLLNode node = head;
            while(node.getNext() != null)
               node = node.getNext();
            node.setNext(new GraphLLNode(graphPane));
         }
         number++;
      }
      
      public GraphPane get(int nu)
      {
         if(nu > number || nu <= 0)
            return null;
         GraphLLNode node = head;
         for(int i=1; i<nu; i++)
            node = node.getNext();
         return node.getGraphPane();
      }
   	
      public void delete(int nu)
      {
         if(nu > number)
            return;
         if(nu == 1)
            head = head.getNext();
         else
         {
            GraphLLNode node = head;
            for(int i=2; i<nu; i++)
               node = node.getNext();
            node.setNext(node.getNext().getNext());
         
         }
         number--;
      }
      
      public int getNumber()
      {
         return number;
      }
   	
      public void setDefaultCursors(Cursor cursor)
      {
         GraphLLNode current = head;
         while(current != null)
         {
            current.getGraphPane().setDefaultCursor(cursor);
            current.getGraphPane().setCursor(cursor);
            current = current.getNext();
         }
      }
      
      public void setSelectedOptions(int so)
      {
         GraphLLNode current = head;
         while(current != null)
         {
            current.getGraphPane().setSelectedOption(so);
            current = current.getNext();
         }
      
      }
      
      public void setDefaultColors(Color []colors)
      {
         GraphLLNode current = head;
         while(current != null)
         {
            current.getGraphPane().setDefaultColors(colors);
            current = current.getNext();
         }
      }
   
   }
import java.util.Arrays;

public class UndoStream
{

   GraphPane graphPane;

   final int START_STREAM = -1;
   final int ADD_VERTEX = 0;
   final int ADD_EDGE = 1;
   final int REMOVE_VERTEX = 2;
   final int REMOVE_EDGE = 3;
   final int RELABEL_VERTICES = 4;
   final int ARRANGE_VERTICES = 5;
   final int EDIT_EDGE_LIST = 6;
   final int MOVE_VERTEX = 7;
   final int TOGGLE_DOM = 8;
   
   UndoNode head;
   UndoNode current;
   int number = 0;
   int maxNumber = 5;

   public UndoStream(GraphPane gp)
   {
      graphPane = gp;
      head = new UndoNode(null,graphPane,-1);
      current = head;
   }

   public void addVertex(int v, int xp, int yp)
   {
      current.setNext(new UndoNode(current,graphPane,ADD_VERTEX));
      current = current.getNext();
      current.addVertex(v, xp, yp);
      graphPane.getParent().checkSave();   
   }
   
   public void addEdge(int e1, int e2)
   {
      current.setNext(new UndoNode(current,graphPane,ADD_EDGE));
      current = current.getNext();
      current.addEdge(e1,e2);
      graphPane.getParent().checkSave();
      
   }
   	
   public void removeVertex(int v, int [][]oa, int []d, int []ods, int []xp, int []yp, int []c)
   {
      current.setNext(new UndoNode(current,graphPane,REMOVE_VERTEX));
      current = current.getNext();
      current.removeVertex(v, copy2D(oa), copy(d), copy(ods), copy(xp), copy(yp), copy(c));
      graphPane.getParent().checkSave();
   }
   
   public void removeEdge(int e1, int e2)
   {
      current.setNext(new UndoNode(current,graphPane,REMOVE_EDGE));
      current = current.getNext();
      current.removeEdge(e1,e2);
      graphPane.getParent().checkSave();
   }
   
   public void relabelVertices(int [][]oa, int []d, int []xp, int []yp, int v, int nv, boolean i)
   {
      current.setNext(new UndoNode(current,graphPane,RELABEL_VERTICES));
      current = current.getNext();
      current.relabelVertices(copy2D(oa), copy(d), copy(xp), copy(yp), v, nv, i);
      graphPane.getParent().checkSave();
   }
   
   public void arrangeVertices(int []xp, int []yp, int []c, int []nc)
   {
      current.setNext(new UndoNode(current,graphPane,ARRANGE_VERTICES));
      current = current.getNext();
      current.arrangeVertices(copy(xp), copy(yp), copy(c), copy(nc));
      graphPane.getParent().checkSave();
   }
   
   public void editEdgeList(int [][]oa, int []d, int []xp, int []yp, int []c, int [][]na, int []nd, boolean a)
   {
      current.setNext(new UndoNode(current,graphPane,EDIT_EDGE_LIST));
      current = current.getNext();
      current.editEdgeList(copy2D(oa), copy(d), copy(xp), copy(yp), copy(c), copy2D(na), copy(nd), a);
      graphPane.getParent().checkSave();
   }
   
   public void moveVertex(int []xp, int []yp, int []nx, int []ny)
   {
      current.setNext(new UndoNode(current,graphPane,MOVE_VERTEX));
      current = current.getNext();
      current.moveVertex(copy(xp), copy(yp), copy(nx), copy(ny));
      graphPane.getParent().checkSave();
   }
   
   public void toggleDom(int []ods, int[] nds)
   {
      current.setNext(new UndoNode(current,graphPane,TOGGLE_DOM));
      current = current.getNext();
      current.toggleDom(copy(ods), copy(nds));
      graphPane.getParent().checkSave();
   }

   public boolean undoAvailable()
   {
      if(current.getEventType() == START_STREAM)
         return false;
      return true;
   }
   
   public boolean redoAvailable()
   {
      if(current.getNext() == null)
         return false;
      return true;
   }
   
   public void undo()
   {
      if(undoAvailable())
      {
         current.undo();
         current = current.getPrevious();
         graphPane.getParent().checkSave();
      }
   }
	
   public void redo()
   {
      if(redoAvailable())
      {
         current = current.getNext();
         current.redo(); 
         graphPane.getParent().checkSave();
      }
   }
   
   public int[] copy(int []a)
   {
      int []copy = new int[a.length];
      for(int i=0; i<copy.length; i++)
         copy[i] = a[i];
      return copy;
   }
	
   public int[][] copy2D(int [][]a)
   {
      int[][] copy = new int[a.length][];
      for (int i = 0; i < a.length; i++) 
         //for(int j=0; j<a[0].length; j++) 
         copy[i] = Arrays.copyOf(a[i],a[i].length);
      return copy;
   }
   
   public void saved(int index)
   {
      UndoNode temp = head.getNext();
      while(temp != null)
      {
         temp.setLastSave(false);
         temp = temp.getNext();
      }
      current.setLastSave(true);
   	
      graphPane.getParent().checkSave(index);
   }
   
   public boolean getLastSave()
   {
      return current.getLastSave();
   }
}
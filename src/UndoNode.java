import java.util.Arrays;

public class UndoNode
{

   UndoNode next = null;
   UndoNode prev = null;
   
   int eventType;
   
   GraphPane graphPane;
	
   int vertex;
   int newVertex;
   int edge1;
   int edge2;
   int [][]oldArcs;
   int [][]newArcs;
   int []degrees;
   int []newDegrees;
   int singleXPos;
   int singleYPos;
   int []xPos;
   int []yPos;
   int []newXPos;
   int []newYPos;
   int []contour;
   int []newContour;
   int []oldDomset;
   int []newDomset;
   boolean increment;
   boolean arrange;
   
   boolean lastSave = false;
	
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
	

   public UndoNode(UndoNode p, GraphPane gp, int et)
   {
      prev = p;
      graphPane = gp;
      eventType = et;
      if(prev != null)
      {
         gp.setUndoAvailable(true);
         
      }
      else
         lastSave = true;   
      gp.setRedoAvailable(false);
   }
  
   public UndoNode getNext()
   {
      return next;
   }
   
   public void setNext(UndoNode n)
   {
      next = n;
   }
   
   public UndoNode getPrevious()
   {
      return prev;
   }
	
   public void setPrevious(UndoNode p)
   {
      prev = p;
   }
   
   public int getEventType()
   {
      return eventType;
   }
   
   public void addVertex(int v, int xp, int yp)
   {
      vertex = v;
      singleXPos = xp;
      singleYPos = yp;
   }
   
   public void addEdge(int e1, int e2)
   {
      edge1 = e1;
      edge2 = e2;
   }
   
   public void removeVertex(int v, int [][]oa, int []d, int []ods, int []xp, int []yp, int []c)
   {
      vertex = v;
      oldArcs = oa;
      degrees = d;
      oldDomset = ods;
      xPos = xp;
      yPos = yp;
      contour = c;
   }
   
   public void removeEdge(int e1, int e2)
   {
      edge1 = e1;
      edge2 = e2;
   }
   
   public void relabelVertices(int [][]oa, int []d, int []xp, int []yp, int v, int nv, boolean i)
   {
      oldArcs = oa;
      degrees = d;
      xPos = xp;
      yPos = yp;
      vertex = v;
      newVertex = nv;
      increment = i;
   }
   
   public void arrangeVertices(int []xp, int []yp, int []c, int []nc)
   {
      xPos = xp;
      yPos = yp;
      contour = c;
      newContour = nc;
   }
   
   public void editEdgeList(int [][]oa, int []d, int []xp, int []yp, int []c, int [][]na, int []nd, boolean a)
   {
      oldArcs = oa;
      degrees = d;
      xPos = xp;
      yPos = yp;
      contour = c;
      newArcs = na;
      newDegrees = nd;
      arrange = a;
   }
   
   public void moveVertex(int []xp, int []yp, int []nx, int []ny)
   {
      xPos = xp;
      yPos = yp;
      newXPos = nx;
      newYPos = ny;
   }
   
   public void toggleDom(int []ods, int []nds)
   {
      oldDomset = ods;
      newDomset = nds;
   }
   
   public void undo()
   {
      System.out.println(eventType);
      Graph graph = graphPane.getGraph();
      if(eventType == ADD_VERTEX)
      {
         graph.deleteVertex(vertex);
      }
      else if(eventType == ADD_EDGE)
      {
         graph.deleteEdge(edge1,edge2);
      }
      else if(eventType == REMOVE_VERTEX)
      {
         graph.setArcs(copy2D(oldArcs),copy(degrees),graph.getN()+1,false);
         /*for(int i=0; i<graph.getN(); i++)
         {
            graph.setXPos(i,xPos[i]);
            graph.setYPos(i,yPos[i]);
         }*/
         graph.setAllPos(copy(xPos),copy(yPos));
         graph.setContour(copy(contour));
         graph.setDomset(copy(oldDomset));
      }
      else if(eventType == REMOVE_EDGE)
      {
         graph.addEdge(edge1,edge2);
      }
      else if(eventType == RELABEL_VERTICES)
      {
         graph.setArcs(copy2D(oldArcs), copy(degrees), degrees.length, false);
         graph.setAllPos(copy(xPos),copy(yPos));
      }
      else if(eventType == ARRANGE_VERTICES)
      {
         graph.setAllPos(copy(xPos),copy(yPos));
         graph.setContour(copy(contour));
      }
      else if(eventType == EDIT_EDGE_LIST)
      {
         graph.setArcs(copy2D(oldArcs), copy(degrees), degrees.length, false);
         graph.setAllPos(copy(xPos),copy(yPos));
         graph.setContour(copy(contour));
      }
      else if(eventType == MOVE_VERTEX)
      {
         graph.setAllPos(copy(xPos),copy(yPos));
      }
      else if(eventType == TOGGLE_DOM)
      {
         graph.setDomset(copy(oldDomset));
      }
   }
   
	
   public void redo()
   {
      Graph graph = graphPane.getGraph();
      if(eventType == ADD_VERTEX)
      {
         int N = graph.getN()+1;
         graph.setN(N);
         graph.setXPos(N-1,singleXPos);
         graph.setYPos(N-1,singleYPos);
      }
      else if(eventType == ADD_EDGE)
      {
         graph.addEdge(edge1,edge2);
      }
      else if(eventType == REMOVE_VERTEX)
      {
         graph.deleteVertex(vertex);
      }
      else if(eventType == REMOVE_EDGE)
      {
         graph.deleteEdge(edge1,edge2);
      }
      else if(eventType == RELABEL_VERTICES)
      {
         if(increment)
         {
           // Increment rest
            if(vertex < newVertex)
               for(int i=vertex; i<newVertex; i++)
                  graph.swapVertices(i,i+1);
            else
               for(int i=vertex; i>newVertex; i--)
                  graph.swapVertices(i,i-1);
         }
         else
         {
           // Swap labels
            graph.swapVertices(vertex,newVertex);
         }
      
      }
      else if(eventType == ARRANGE_VERTICES)
      {
         graph.setContour(copy(newContour));
         graph.createCircle();
      
      }
      else if(eventType == EDIT_EDGE_LIST)
      {
         graph.setArcs(copy2D(newArcs), copy(newDegrees), newDegrees.length, true);
         if(arrange)
         {
            graph.createCircle();
         }
      }
      else if(eventType == MOVE_VERTEX)
      {
         graph.setAllPos(copy(newXPos), copy(newYPos));
      }
      else if(eventType == TOGGLE_DOM)
      {
         graph.setDomset(newDomset);
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

   public void setLastSave(boolean ls)
   {
      lastSave = ls;
      graphPane.getParent().checkSave();
   }  

   public boolean getLastSave()
   {
      return lastSave;
   }  
}
//package UGV;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Graph
{

   int N;
   int maxDegree;
   int [][]arcs;
   int []degrees;

   int[][] dists;
   double springStrength = .8;
   boolean calculatingSpring = false;

   double []nodePosX;
   double []nodePosY;

   boolean []selected;

   int []contour;

   int []domset;
   final int DOM_TYPE_STANDARD = 1;
   final int DOM_TYPE_TOTAL = 2;
   final int DOM_TYPE_SECURE = 3;
   final int DOM_TYPE_CONNECTED = 4;

   /**
    * Create a new Graph with a specified number of vertices.
    * @param inN number of vertices in the graph.
    * @param inMaxDegree greatest number of incident edges on any vertex.
    */
   public Graph(int inN, int inMaxDegree) {
      N = inN;
      maxDegree = inMaxDegree;
      arcs = new int[N][maxDegree];
      degrees = new int[N];
      nodePosX = new double[N];
      nodePosY = new double[N];
      contour = new int[N];
      domset = new int[N];
      selected = new boolean[N];

      for(int i=0; i<N; i++)
         contour[i] = i;

      createCircle();
   }

   /**
    * Gets the number of vertices in the graph.
    * @return the number of vertices in the graph.
    */
   public int getN()
   {
      return N;
   }

   /**
    * Get the total number of (undirected) edges in the graph.
    * @return the total number of edges in the graph.
    */
   public int getEdgeCount(){
      int count = 0;
      for (int i = 0; i < N; i++) {
         count += degrees[i];
      }
      return count/2;
   }

   /**
    * Get the greatest number of edges incident on any vertex.
    * @return the greatest number of edges incident on any vertex.
    */
   public int getMaxDegree()
   {
      return maxDegree;
   }

   /**
    * Set the number of vertices of the graph. If this is greater than the original number, new vertices are added.
    * If this is less than the original number of vertices, the highest indexed vertices are deleted.
    * @param inN
    */
   public void setN(int inN) {
      if(N != inN)
      {
         int oldN = N;
         N = inN;

         int [][]newArcs = new int[N][maxDegree];
         int []newDegrees = new int[N];
         double []newNodePosX = new double[N];
         double []newNodePosY = new double[N];
         int []newContour = new int[N];
         boolean []newSelected = new boolean[N];


         if(inN > oldN)
         {
            int []newDomset = new int[inN];
            for(int i=0; i<oldN; i++)
               newDomset[i] = domset[i];
            for(int i=oldN; i<inN; i++)
               newDomset[i] = 0;

            domset = newDomset;


            for(int i=0; i<oldN; i++)
               for(int j=0; j<maxDegree; j++)
                  newArcs[i][j] = arcs[i][j];

            arcs = newArcs;

            for(int i=0; i<oldN; i++)
               newDegrees[i] = degrees[i];

            degrees = newDegrees;

            int totalX = 0;
            for(int i=0; i<oldN; i++)
            {
               newNodePosX[i] = nodePosX[i];
               totalX += nodePosX[i];
            }

            for(int i=oldN; i<inN; i++)
               newNodePosX[i] = (int)Math.round(totalX*1.0/oldN)-(inN-oldN)/2*2 + (i-oldN)*2;

            nodePosX = newNodePosX;

            int totalY = 0;
            for(int i=0; i<oldN; i++)
            {
               newNodePosY[i] = nodePosY[i];
               totalY += nodePosY[i];
            }

            for(int i=oldN; i<inN; i++)
               newNodePosY[i] = (int)Math.round(totalY*1.0/oldN)-(inN-oldN)/2*2 + (i-oldN)*2;

            nodePosY = newNodePosY;

            for(int i=0; i<oldN; i++)
               newContour[i] = contour[i];

            for(int i=oldN; i<inN; i++)
               newContour[i] = i;

            contour = newContour;

            for(int i=0; i<oldN; i++)
               newSelected[i] = selected[i];

            selected = newSelected;

         }
         else
         {
            int []newDomset = new int[inN];
            for(int i=0; i<inN; i++)
               newDomset[i] = domset[i];

            domset = newDomset;

            for(int i=0; i<inN; i++)
               for(int j=0; j<maxDegree; j++)
                  newArcs[i][j] = arcs[i][j];

            arcs = newArcs;

            for(int i=0; i<inN; i++)
               newDegrees[i] = degrees[i];

            degrees = newDegrees;

            for(int i=0; i<inN; i++)
               newNodePosX[i] = nodePosX[i];

            nodePosX = newNodePosX;

            for(int i=0; i<inN; i++)
               newNodePosY[i] = nodePosY[i];

            nodePosY = newNodePosY;

            for(int i=0; i<inN; i++)
               newContour[i] = contour[i];

            contour = newContour;

            for(int i=0; i<inN; i++)
               newSelected[i] = selected[i];

            selected = newSelected;
         }
      }
   }

   /**
    * Deletes the vertex with the specified label, and removes associated edges.
    * @param vertex the label of the vertex to be removed.
    */
   public void deleteVertex(int vertex) {
      if(vertex >= 1 && vertex <= N)
      {
         int []newDomset = new int[N-1];
         for(int i=0; i<vertex-1; i++)
            newDomset[i] = domset[i];
         for(int i=vertex; i<N; i++)
            newDomset[i-1] = domset[i];
         domset = newDomset;


         int [][]newArcs = new int[N-1][maxDegree];
         for(int i=0; i<N; i++)
            for(int j=0; j<degrees[i]; j++)
               if((i+1) < vertex)
                  newArcs[i][j] = arcs[i][j];
               else if((i+1) > vertex)
                  newArcs[i-1][j] = arcs[i][j];

         int []newDegrees = new int[N-1];
         for(int i=0; i<N; i++)
            if((i+1) < vertex)
               newDegrees[i] = degrees[i];
            else if((i+1) > vertex)
               newDegrees[i-1] = degrees[i];

         arcs = newArcs;
         degrees = newDegrees;
         N = N-1;

         for(int i=0; i<N; i++)
         {
            int moveBack = -1;
            for(int j=0; j<degrees[i]; j++)
               if(arcs[i][j] > vertex)
                  arcs[i][j]--;
               else if(arcs[i][j] == vertex)
                  moveBack = j;

            if(moveBack > -1)
            {
               for(int j=moveBack+1;j<degrees[i]; j++)
                  arcs[i][j-1] = arcs[i][j];

               arcs[i][degrees[i]-1] = 0;
               degrees[i]--;
            }
         }

         int newMaxDegree = 0;
         for(int i=0; i<N; i++)
            if(degrees[i] > newMaxDegree)
               newMaxDegree = degrees[i];

         maxDegree = newMaxDegree;

         double []newNodePosX = new double[N];
         double []newNodePosY = new double[N];

         for(int i=0; i<N+1; i++)
            if((i+1) < vertex)
            {
               newNodePosX[i] = nodePosX[i];
               newNodePosY[i] = nodePosY[i];
            }
            else if((i+1) > vertex)
            {
               newNodePosX[i-1] = nodePosX[i];
               newNodePosY[i-1] = nodePosY[i];
            }

         nodePosX = newNodePosX;
         nodePosY = newNodePosY;

         int []newContour = new int[N];
         if(N != 0)
         {
            int offset = 0;
            for(int i=0; i<N+1; i++)
               if((contour[i] + 1) < vertex)
                  newContour[i-offset] = contour[i];
               else if((contour[i] + 1) > vertex)
                  newContour[i-offset] = contour[i]-1;
               else
                  offset = 1;

         }
         contour = newContour;

         boolean []newSelected = new boolean[N];

         for(int i=0; i<N+1; i++)
            if((i+1) < vertex)
               newSelected[i] = selected[i];
            else if((i+1) > vertex)
               newSelected[i-1] = selected[i];
         selected = newSelected;


      }

   }

   /**
    * Deletes the arc (single direction) between vertices with the specified labels.
    * @param v1 label of the first vertex.
    * @param v2 label of the second vertex.
    */
   public void deleteArc(int v1, int v2) {
      for(int i=0; i<degrees[v1-1]; i++)
      {
         if(arcs[v1-1][i] == v2)
         {
            for(int j=i+1; j<degrees[v1-1]; j++)
               arcs[v1-1][j-1] = arcs[v1-1][j];
            arcs[v1-1][degrees[v1-1]-1] = 0;
            degrees[v1-1]--;

            if(degrees[v1-1]+1 == maxDegree)
            {
               int maxDegree = 0;
               for(int j=0; j<N; j++)
                  if(degrees[j] > maxDegree)
                     maxDegree = degrees[j];
            }
            return;
         }
      }
   }

   /**
    * Deletes the edge (both directions) between vertices with the specified labels.
    * @param v1 label of the first vertex.
    * @param v2 label of the second vertex.
    */
   public void deleteEdge(int v1, int v2)
   {
      deleteArc(v1,v2);
      deleteArc(v2,v1);
   }

   /**
    * Sets the new maximum degree of any vertex within the graph.
    * @param inMaxDegree new max degree.
    */
   public void setMaxDegree(int inMaxDegree) {
      if(maxDegree != inMaxDegree)
      {
         int oldMaxDegree = maxDegree;
         maxDegree = inMaxDegree;

         int [][]newArcs = new int[N][maxDegree];

         if(inMaxDegree > oldMaxDegree)
         {
            for(int i=0; i<N; i++)
               for(int j=0; j<oldMaxDegree; j++)
                  newArcs[i][j] = arcs[i][j];

            arcs = newArcs;
         }
         else
         {
            for(int i=0; i<N; i++)
               for(int j=0; j<maxDegree; j++)
                  newArcs[i][j] = arcs[i][j];

            arcs = newArcs;

            for(int i=0; i<N; i++)
               if(degrees[i] > maxDegree)
                  degrees[i] = maxDegree;
         }
      }
   }

   /**
    * Checks if there is an arc (single direction) between the vertices with the specified labels.
    * @param v1 label of the first vertex.
    * @param v2 label of the second vertex.
    * @return true if the arc exists, false otherwise.
    */
   public boolean isArc(int v1, int v2)
   {
      for(int i=0; i<degrees[v1-1]; i++)
         if(arcs[v1-1][i] == v2)
            return true;

      return false;
   }

   /**
    * Adds a new arc (single direction) between the vertices with the given labels.
    * @param v1 label of the first vertex.
    * @param v2 label of the second vertex.
    */
   public void addArc(int v1, int v2)
   {
      if(degrees[v1-1] == maxDegree)
         setMaxDegree(maxDegree+1);
      arcs[v1-1][degrees[v1-1]++] = v2;
      orderArcs(v1);
      orderArcs(v2);
   }

   /**
    * Adds a set of new arcs (single direction) to the graph.
    * @param newArcs a k x 2 array (int[k][2]) where k is the number of arcs, [i][0] is the start vertex label
    *                and [i][1] is the goal vertex label for each arc.
    */
   public void addArcs(int [][]newArcs) {
      for(int i=0; i<newArcs.length; i++)
         addArc(newArcs[i][0],newArcs[i][1]);
   }

   /**
    * Sorts the list of arcs associated with a given vertex.
    * Does not remove duplicates.
    * @param row label of the vertex whose arcs are to be sorted.
    */
   public void orderArcs(int row) {
      int []newRow = new int[degrees[row-1]];
      for(int i=0; i<degrees[row-1]; i++)
         newRow[i] = arcs[row-1][i];

      Arrays.sort(newRow);

      for(int i=0; i<degrees[row-1]; i++)
         arcs[row-1][i] = newRow[i];
   }

   /**
    * A method for updating the degree of every vertex in bulk.
    * @param newDegrees an array containing the new degree of each vertex, in index order.
    */
   public void setDegrees(int []newDegrees) {
      degrees = newDegrees;
      maxDegree = 0;
      for(int i=0; i<N; i++)
         if(degrees[i] > maxDegree)
            maxDegree = degrees[i];
   }

   /**
    * Checks if an edge (two directions) exists between the specified vertices.
    * @param v1 label of the first vertex.
    * @param v2 label of the second vertex.
    * @return true if an edge (two directions) exists between the vertices, false otherwise.
    */
   public boolean isEdge(int v1, int v2) {
      return (isArc(v1, v2) && isArc(v2, v1));
   }

   /**
    * Adds an edge (two directions) between the specified vertices.
    * @param v1 label of the first vertex.
    * @param v2 label of the second vertex.
    */
   public void addEdge(int v1, int v2)
   {
      addArc(v1, v2);
      addArc(v2, v1);
   }

   /**
    * Adds a set of new edges (two directions) to the graph.
    * @param newEdges a k x 2 array (int[k][2]) where k is the number of edges, [i][0] is the start vertex label
    *                and [i][1] is the goal vertex label for each edge.
    */
   public void addEdges(int [][]newEdges)
   {
      for(int i=0; i<newEdges.length; i++)
         addEdge(newEdges[i][0],newEdges[i][1]);
   }

   /**
    * Gets an array containing all arcs (single direction) of the graph.
    * The number of elements in each array[i] is recorded in the array given by getDegrees.
    * Each element in array[i] is either the label of a target vertex, or 0. 0 is never the label of a graph, hence it
    * corresponds to an empty slot.
    * @return an N x maxDegree array (int[N][maxDegree]) where N is the number of vertices, and maxDegree is the greatest
    * degree of any vertex. If a given vertex has degree fewer than maxDegree, then array[i] will be filled out with 0s.
    */
   public int[][] getArcs()
   {
      return arcs;
   }

   /**
    * Deletes all existing arcs and overwrites them with an array of new arcs.
    * @param a an N x maxDegree array (int[N][maxDegree]) where N is the number of vertices, and maxDegree is the greatest
    *          degree of any vertex. Non-zero entries of a[i] indicate labels of vertices adjacent to vertex with index i.
    *          If a given vertex has degree fewer than maxDegree, then the remainder of array[i] will be filled out with 0s.
    * @param d an array of length N, where the value of d[i] is equal to the number of non-zero entries in a[i].
    * @param nodes the number of vertices described in a and d. (N).
    * @param requiresSorting whether or not the arcs should be sorted after being added.
    */
   public void setArcs(int[][] a, int[] d, int nodes, boolean requiresSorting) {
      // THIS FUNCTION ASSUMES arcs AND degrees HAVE BEEN PROCESSED AND ARE ACCURATE!
      if(N != nodes)
         setN(nodes);

      arcs = a;
      setDegrees(d);

      if(requiresSorting)
      {
         for(int i=0; i<N; i++)
            orderArcs(i+1);
      }
   }

   /**
    * Swaps all vertex information between the specified vertices.
    * @param v1 the label of the first vertex.
    * @param v2 the label of the second vertex.
    */
   public void swapVertices(int v1, int v2)
   {

      if(v1 == v2 || v1 <= 0 || v2 <= 0 || v1 > N || v2 > N) {
         return;
      }

      for(int i=0; i<N; i++) {
         boolean changed = false;
         for(int j=0; j<degrees[i]; j++) {

            if(arcs[i][j] == v1) {
               arcs[i][j] = v2;
               changed = true;
            } else if(arcs[i][j] == v2) {
               arcs[i][j] = v1;
               changed = true;
            }
         }
         if(changed){
            orderArcs(i+1);
         }
      }


      for(int i=0; i<maxDegree; i++) {
         int temp = arcs[v1-1][i];
         arcs[v1-1][i] = arcs[v2-1][i];
         arcs[v2-1][i] = temp;
      }

      int temp = degrees[v1-1];
      degrees[v1-1] = degrees[v2-1];
      degrees[v2-1] = temp;

      double tempX = nodePosX[v1-1];
      nodePosX[v1-1] = nodePosX[v2-1];
      nodePosX[v2-1] = tempX;

      double tempY = nodePosY[v1-1];
      nodePosY[v1-1] = nodePosY[v2-1];
      nodePosY[v2-1] = tempY;

      int tempDom = domset[v1-1];
      domset[v1-1] = domset[v2-1];
      domset[v2-1] = tempDom;

      boolean tempSelected = selected[v1-1];
      selected[v1-1] = selected[v2-1];
      selected[v2-1] = tempSelected;
   }

   /**
    * Gets an array of length N, where the entry at index i is the degree of the vertex with index i.
    * @return an array of degrees of each vertex.
    */
   public int[] getDegrees() {
      return degrees;
   }

   /**
    * Gets the x coordinate of the specified vertex.
    * @param node the index of the vertex.
    * @return the x coordinate of the vertex.
    */
   public double getXPos(int node)
   {
      return nodePosX[node];
   }

   /**
    * Gets the y coordinate of the specified vertex.
    * @param node the index of the vertex.
    * @return the y coordinate of the vertex.
    */
   public double getYPos(int node)
   {
      return nodePosY[node];
   }

   /**
    * Gets the x coordinate of the specified vertex.
    * @param node the index of the vertex.
    * @param posX the x coordinate of the vertex.
    */
   public void setXPos(int node, double posX)
   {
      nodePosX[node] = posX;
   }

   /**
    * Gets the y coordinate of the specified vertex.
    * @param node the index of the vertex.
    * @param posY the y coordinate of the vertex.
    */
   public void setYPos(int node, double posY)
   {
      nodePosY[node] = posY;
   }

   /**
    * Sets the positions of all vertices in bulk.
    * @param nx an array of length N where nx[i] is the x position of the vertex with index i;
    * @param ny an array of length N where ny[i] is the y position of the vertex with index i;
    */
   public void setAllPos(double[] nx, double[] ny)
   {
      nodePosX = nx;
      nodePosY = ny;
   }

   /**
    * Gets an array containing the x positions of all vertices.
    * @return an array of length N where the ith element is the x position of the vertex with index i;
    */
   public double[] getXPoses()
   {
      return nodePosX;
   }

   /**
    * Gets an array containing the y positions of all vertices.
    * @return an array of length N where the ith element is the y position of the vertex with index i;
    */
   public double[] getYPoses()
   {
      return nodePosY;
   }

   /**
    * Gets an array containing the current ordering of vertices.
    * @return an array of length N where each element is the index of a vertex.
    */
   public int[] getContour()
   {
      return contour;
   }

   /**
    * Sets the ordering of vertices. Used for aligning vertices in a circle.
    * @param co an array of length N where each element is the index of a vertex, and each element is unique.
    */
   public void setContour(int[] co)
   {
      contour = co;
   }

   /**
    * Gets an array indicating which vertices are currently selected.
    * TODO should selected be a property of GraphPane?
    * @return an array of length N, where the element at position i is:
    * true if the vertex with index i is selected,
    * false if the vertex with index i is not selected.
    */
   public boolean[] getSelected()
   {
      return selected;
   }

   /**
    * Sets which vertices are currently selected.
    * @param se an array of length N, where se[i] is:
    * true if the vertex with index i is selected,
    * false if the vertex with index i is not selected.
    */
   public void setSelected(boolean[] se)
   {
      selected = se;
   }

   /**
    * Gives the specified vertex the selected status.
    * @param v the index of the vertex.
    */
   public void select(int v)
   {
      selected[v] = true;
   }

   /**
    * Makes the specified vertex unselected.
    * @param v the index of the vertex.
    */
   public void deselect(int v)
   {
      selected[v] = false;
   }

   /**
    * Removes the selected status of every vertex.
    */
   public void deselectAll(){
      selected = new boolean[N];
   }

   /**
    * Checks if the specified vertex is selected.
    * @param v the index of the vertex.
    * @return true if the vertex is selected, false otherwise.
    */
   public boolean isSelected(int v)
   {
      return selected[v];
   }

   /**
    * Arranges the vertex in a circle, where the clockwise order of the vertices is specified by setContour().
    */
   public void createCircle()
   {
      int radius = 15*N;

      for(int i=0; i<N; i++)
      {
         int node = contour[i];
         setXPos(node, (int)(Math.round(radius*(1 + Math.sin(i*2*Math.PI/N)))));
         setYPos(node, (int)(Math.round(radius*(1 - Math.cos(i*2*Math.PI/N)))));
      }
   }

   /**
    * Arranges the vertices of the graph in a grid.
    * @param copies number of elements in the first column (if vertical is true) or row (if vertical is false)
    * @param vertical true if the first column should contain vertices (1 2 3 ...),
    *                 false if the first row should contain vertices (1 2 3 ...)
    * @param spacing vertical and horizontal distance between vertices.
    */
   public void createGrid(int copies, boolean vertical, double spacing){

      int width = copies;

      if(vertical){
         width = (int) Math.ceil((N*1.0)/copies);
      }


      for (int i = 0; i < N; i++) {
         double x = (i % width)*spacing;
         //noinspection IntegerDivisionInFloatingPointContext
         double y = (i/width)*spacing;

         if(vertical){
            nodePosX[i] = y;
            nodePosY[i] = x;
         } else {
            nodePosY[i] = y;
            nodePosX[i] = x;
         }

      }

   }

   /**
    * Checks if the graph is a connected graph.
    * @return true if the graph is connected, false if the graph is disconnected.
    */
   public boolean isConnected() {
      if(N == 0)
         return false;
      if(N == 1)
         return true;
      int []reached = new int[N];
      int []checked = new int[N];

      reached[0] = 1;

      int difference = 0;
      for(int i=0; i<N; i++)
         difference += reached[i]-checked[i];

      while(difference > 0)
      {
         int index = 0;
         while(index < N && (reached[index] == 0 || checked[index] == 1))
            index++;
         if(index == N)
            return false;

         for(int i=0; i<degrees[index]; i++)
            reached[arcs[index][i]-1] = 1;

         checked[index] = 1;

         difference = 0;
         for(int i=0; i<N; i++)
            difference += reached[i]-checked[i];
      }

      for(int i=0; i<N; i++)
         if(reached[i] == 0)
            return false;
      return true;
   }

   /**
    * Checks if the graph is regular (every vertex has the same degree).
    * @return the degree of every vertex if the graph is regular, or -1 if it is not.
    */
   public int checkRegular()
   {
      int regular = degrees[0];
      for(int i=1; i<N; i++)
         if(degrees[i] != regular)
            return -1;

      return regular;
   }

   /**
    * Checks if the graph is bipartite.
    * (Vertices can be put into two separate groups, where no vertex is adjacent to any other in the same group).
    * @return true if the graph is bipartite, false otherwise
    */
   public boolean isBipartite()
   {


      if(N == 0)
         return false;
      if(N == 1)
         return true;
      int []colour = new int[N];
      boolean []toCheck = new boolean[N];
      for(int i=0; i<N; i++)
         toCheck[i] = false;

      int firstNode = 0;

      while(firstNode != -1)
      {
         colour[firstNode] = 1;
         for(int i=0; i<degrees[firstNode]; i++)
         {
            colour[arcs[firstNode][i]-1] = -1;
            toCheck[arcs[firstNode][i]-1] = true;
         }

         int nodeToCheck = -1;
         for(int i=0; i<N; i++)
            if(toCheck[i])
            {
               nodeToCheck = i;
               break;
            }

         while(nodeToCheck != -1)
         {
            toCheck[nodeToCheck] = false;
            int thisColour = colour[nodeToCheck];
            for(int i=0; i<degrees[nodeToCheck]; i++)
            {
               if(colour[arcs[nodeToCheck][i]-1] == thisColour)
                  return false;
               else if(colour[arcs[nodeToCheck][i]-1] == 0)
               {
                  colour[arcs[nodeToCheck][i]-1] = -thisColour;
                  toCheck[arcs[nodeToCheck][i]-1] = true;
               }
            }

            nodeToCheck = -1;
            for(int i=0; i<N; i++)
               if(toCheck[i])
               {
                  nodeToCheck = i;
                  break;
               }
         }


         firstNode = -1;
         for(int i=0; i<N; i++)
            if(colour[i] == 0)
            {
               firstNode = i;
               break;
            }


      }

      for(int i=0; i<N; i++)
         for(int j=0; j<degrees[i]; j++)
            if(colour[i] == colour[arcs[i][j]-1])
               return false;


      return true;
   }

   /**
    * Checks if the specified vertex is in the dominating set.
    * @param node label of the vertex.
    * @return the number of guards at the vertex.
    */
   public int inDomset(int node) {
      return domset[node-1];
   }

   /**
    * Sets the number of guards at the specified vertex. Between 0 and 2 inclusive.
    * @param node the index of the vertex.
    * @param domValue the number of guards to be put at the vertex.
    */
   public void setDomValue(int node, int domValue){

      if(domValue > 2) domValue = 0;
      if(domValue < 0) domValue = 0;

      domset[node] = domValue;
   }

   /**
    * Checks whether or not vertices meet the specified domination conditions.
    * @param domTotal if true checks which vertices are total dominated.
    * @param domSecure if true checks which vertices are secure dominated.
    * @param domConnected if true checks which vertices are connected dominated.
    * @param domRoman if true checks which vertices are roman dominated.
    * @param domWeakRoman if true checks which vertices are weak roman dominated.
    * @return an array of length N where an element i is true if the vertex with index i meets all of the
    * specified domination conditions, false otherwise.
    */
   public boolean[] dominatedVertices(boolean domTotal, boolean domSecure, boolean domConnected, boolean domRoman, boolean domWeakRoman) {
      boolean []dv = new boolean[N];

      if(domConnected)
      {
         Graph newGraph = new Graph(N,maxDegree);

         for(int i=0; i<N; i++)
            for(int j=0; j<maxDegree; j++)
               if(arcs[i][j] > i+1)
                  newGraph.addEdge(i+1,arcs[i][j]);

         newGraph.setDomset(domset);

         for(int i=N-1; i>=0; i--)
            if(domset[i] == 0)
               newGraph.deleteVertex(i+1);

         if(!newGraph.isConnected())
            return dv;
      }


      for(int i=0; i<N; i++)
      {
         if(domset[i] > 0 && !domTotal)
            dv[i] = true;
         else
         {
            for(int j=0; j<N; j++)
            {
               if(domset[j]>0 && isEdge(i+1,j+1))
               {
                  if(domset[j] == 2 && (domRoman || domWeakRoman))
                  {
                     dv[i] = true;
                     break;
                  }

                  if(domSecure || domWeakRoman)
                  {
                     boolean canmove = true;
                     for(int k=0; k<degrees[j]; k++)
                     {
                        int v1 = arcs[j][k]-1;
                        if(v1 != i && (domset[v1] == 0 || domTotal))
                        {
                           boolean stillcovered = false;
                           for(int l=0; l<degrees[v1]; l++)
                           {
                              int v2 = arcs[v1][l]-1;
                              if(v2 == j)
                                 continue;
                              if(domset[v2]>0 || v2 == i)
                              {
                                 stillcovered = true;
                                 break;
                              }
                           }
                           if(!stillcovered)
                           {
                              canmove = false;
                              break;
                           }
                        }
                     }
                     if(canmove)
                     {
                        dv[i] = true;
                        break;
                     }
                  }
                  else
                  {
                     if(!domRoman)
                     {
                        dv[i] = true;
                        break;
                     }
                  }
               }
            }
         }
      }


      return dv;
   }

   /**
    * Gets the number of guards at each vertex of the graph.
    * @return an array of length N, where element i is the number of guards at the vertex with index i.
    */
   public int[] getDomset() {
      return domset;
   }

   /**
    * Gets the sum of all guards in graph.
    * @return the sum of all domination values of all vertices in this graph.
    */
   public int getDomSize(){
      int count = 0;
      for (int i = 0; i < N; i++) {
         count += domset[i];
      }
      return count;

   }

   /**
    * Sets the number of guards at each vertex of the graph.
    * @param ds an array of length N, where ds[i] is the number of guards at the vertex with index i.
    */
   public void setDomset(int[] ds) {
      domset = ds;
   }

   /**
    * Progresses the number of guards at the specified vertex along the cyclic order (0 1 2).
    * @param node the index of the vertex.
    */
   public void toggleDom(int node) {
      if(domset[node] == 0)
         domset[node] = 1;
      else if(domset[node] == 1)
         domset[node] = 2;
      else if(domset[node] == 2)
         domset[node] = 0;
   }

   /**
    * Updates the positions of every vertex using a Kamada-Kawai spring algorithm.
    * @param radius the current visual radius of the vertices. Affects the desired length of each edge in the calculations.
    */
   public void springLayout(int radius){
      calculateShortestPaths();

      double tolerance = 0.001/N;

      int[][] l = new int[N][N];
      double[][] k = new double[N][N];
      double[] parX = new double[N];
      double[] parY = new double[N];
      double[] delta = new double[N];

      for (int i = 0; i < N; i++) {
         for (int j = 0; j < N; j++) {
            if(dist(i,j) < N+1) {
               l[i][j] = radius * 10 * dist(i,j);
               k[i][j] = springStrength/dist(i,j);
            } else {
               l[i][j] = 0;
               k[i][j] = 0;
            }
         }
      }

      int m = 0;

      for (int i = 0; i < N; i++) {
         for (int j = 0; j < N; j++) {
            if(i != j){
               parX[i] += k[i][j]*((getXPos(i)-getXPos(j)) - l[i][j]*(getXPos(i)-getXPos(j))/Math.sqrt(distL2(i,j)));
               parY[i] += k[i][j]*((getYPos(i)-getYPos(j)) - l[i][j]*(getYPos(i)-getYPos(j))/Math.sqrt(distL2(i,j)));
            }
         }
         delta[i] = Math.sqrt(Math.pow(parX[i],2) + Math.pow(parY[i],2));
         if(delta[i] >= delta[m]){
            m = i;
         }
      }


      double prevD = 0;
      int count = 0;
      while(count < 1000 && (delta[m] > 0.001 && Math.abs(delta[m] - prevD)/prevD >= tolerance)){
         count++;
         prevD = delta[m];

         double[] contX = new double[N];
         double[] contY = new double[N];

         for (int i = 0; i < N; i++) {
            if (i == m){
               continue;
            }
            contX[i] = k[i][m]*((getXPos(i)-getXPos(m)) - l[i][m]*(getXPos(i) - getXPos(m))/Math.sqrt(distL2(i,m)));
            contY[i] = k[i][m]*((getYPos(i)-getYPos(m)) - l[i][m]*(getYPos(i) - getYPos(m))/Math.sqrt(distL2(i,m)));
         }

         double pimd;

         int countInner = 0;
         do {
            countInner++;
            pimd = delta[m];

            double C = -1*parX[m];
            double E = -1*parY[m];
            double F = 0;
            double B = 0;
            double D = 0;

            for (int i = 0; i < N; i++) {
               if(i==m)
                  continue;

               F += k[m][i]*(1-l[m][i]*Math.pow(getYPos(m)-getYPos(i),2)/Math.pow(distL2(m,i),1.5));
               D += k[m][i]*(1-l[m][i]*Math.pow(getXPos(m)-getXPos(i),2)/Math.pow(distL2(m,i),1.5));
               B += k[m][i]*(l[m][i]*(getXPos(m)-getXPos(i))*(getYPos(m)-getYPos(i))/Math.pow(distL2(m,i),1.5));
            }

            double dX = (C*D - E*B)/(F*D - B*B);
            double dY = (E*F - B*C)/(F*D - B*B);


            setXPos(m, getXPos(m)+dX);
            setYPos(m, getYPos(m)+dY);

            parX[m] = 0;
            parY[m] = 0;

            for (int i = 0; i < N; i++) {
               if(i==m)
                  continue;

               parX[m] += k[m][i]*((getXPos(m)-getXPos(i)) - l[m][i]*(getXPos(m)-getXPos(i))/Math.sqrt(distL2(m,i))) - contX[m];
               parY[m] += k[m][i]*((getYPos(m)-getYPos(i)) - l[m][i]*(getYPos(m)-getYPos(i))/Math.sqrt(distL2(m,i))) - contY[m];
            }
            delta[m] = Math.sqrt(parX[m]*parX[m] + parY[m]*parY[m]);

         } while (countInner < 1000 && delta[m] > 0.000001 && (pimd - delta[m])/pimd > tolerance);



         for (int i = 0; i < N; i++) {
            if(delta[i] >= delta[m]) {
               m = i;
            }
         }



      }
   }

   /**
    * Gets the mean x and y coordinates of the selected vertices.
    * @param vertices an array of length N, where vertices[i] is true if the vertex with index i should be included in the calculation.
    * @return an array of two elements, {mean x coordinate, mean y coordinate}
    */
   public double[] getMiddle(boolean[] vertices){

      double x = 0;
      double y = 0;

      int count = 0;

      for (int i = 0; i < N; i++) {
         if(vertices[i]) {
            count++;
            x += nodePosX[i];
            y += nodePosY[i];
         }
      }

      x = x/count;
      y = y/count;

      return new double[]{x, y};
   }

   /**
    * Gets the mean x and y coordinates of all vertices of the graph.
    * @return an array of two elements, {mean x coordinate, mean y coordinate}
    */
   public double[] getMiddle(){
      double x = 0;
      double y = 0;

      for (int i = 0; i < N; i++) {
         x += nodePosX[i];
         y += nodePosY[i];
      }

      x = x/N;
      y = y/N;

      return new double[]{x, y};
   }

   /**
    * Gets the number of arcs in the shortest path between the specified vertices.
    * @param v1 the index of the first vertex.
    * @param v2 the index of the second vertex.
    * @return the number of arcs in shortest path between the v1 and v2.
    */
   public int dist(int v1, int v2){
      if (dists == null || dists.length != N) {
         //TODO need to update shortest paths when verts/edges/labels changed
         calculateShortestPaths();
      }
      return dists[v1][v2];
   }

   /**
    * Gets the square of euclidean distance between the two specified vertices.
    * @param v1 the index of the first vertex.
    * @param v2 the index of the second vertex.
    * @return the square of the Euclidean distance between v1 and v2.
    */
   public double distL2(int v1, int v2){
      return Math.pow(getXPos(v1)-getXPos(v2),2) + Math.pow(getYPos(v1)-getYPos(v2),2);
   }

   /**
    * Gets the list of vertices which have a shortest path of 2 from the specified vertex.
    * @param v1 the index of the vertex.
    * @return an array where each element is the index of a vertex two away from the specified vertex.
    */
   public int[] twoApartList(int v1){
      Set<Integer> verts = new HashSet<>();

      //get 1 away
      int[] neighbours = getArcs()[v1];
      for (int jj = 0; jj < degrees[v1];  jj++) {
         int j = neighbours[jj]-1;
         //get 2 away
         int[] n2 = getArcs()[j];
         for (int vv2 = 0; vv2 < degrees[j];vv2++) {
            int v2 = n2[vv2]-1;
            //don't add self
            if (v2 == v1) continue;

            for (int nn = 0; nn < degrees[j]; nn++) {
               int neighbour = neighbours[nn]-1;
               //don't add 1 away
               if (v2 == neighbour) continue;
               verts.add(v2);
               break;
            }
         }
      }

      return verts.stream().mapToInt(Number::intValue).toArray();
   }

   /**
    * Calculates the shortest paths between every pair of vertices and stores the result in dists[][].
    */
   private void calculateShortestPaths() {


      dists = new int[N][N];

      for (int i = 0; i < N; i++) {
         for (int j = 0; j < N; j++) {
            if (i != j){
               dists[i][j] = N*2;
            }
         }
      }


      for(int i=0; i<N; i++)
      {
         int[] next = new int[N];
         next[0] = i;
         int index = 0;
         int count = 0;

         boolean[] reached = new boolean[N];

         int v = next[index];
         reached[v] = true;

         while(true)
         {
            for(int j=0; j<degrees[v]; j++)
            {
               int v2 = arcs[v][j]-1;
               if(!reached[v2])
               {
                  reached[v2] = true;
                  dists[i][v2] = dists[i][v] + 1;
                  next[++count] = v2;
               }
            }

            index++;
            if(index > count)
               break;
            v = next[index];
         }
      }

   }


   /**
    * Creates a new graph using the currently selected vertices, copying position, domination value and edges.
    * Optionally aligns the new graph so its top left boundary is at (0,0).
    * @param align whether the graph should be aligned to (0,0).
    * @return a new graph object created from the currently selected vertices.
    */
   public Graph getSelectedSubgraph(boolean align){
      return getSubgraph(selected, align);
   }

   /**
    * Creates a new graph using the indicated vertices. Copies position, domination values and edges.
    * @param vertices an array of length N, where vertex with index i will be in the new graph if vertices[i] is true.
    * @return a new graph created from the indicated vertices.
    */
   public Graph getSubgraph(boolean[] vertices){
      return getSubgraph(vertices, false);
   }

   /**
    * Creates a new graph using the indicated vertices. Copies position, domination values and edges.
    * Optionally aligns the new graph so its top left boundary is at (0,0).
    * @param vertices an array of length N, where vertex with index i will be in the new graph if vertices[i] is true.
    * @param align whether the graph should be aligned to (0,0).
    * @return a new graph created from the indicated vertices, optionally aligned to (0,0).
    */
   public Graph getSubgraph(boolean[] vertices, boolean align){
      int subN = 0;
      for (boolean v : vertices) {
         if(v) subN++;
      }

      if(subN == 0){
         subN = N;
      }

      int[] verts = new int[subN];
      int count = 0;
      for (int i = 0; i < N; i++) {
         if(vertices[i]){
            verts[count] = i;
            count++;
         }
      }

      return getSubgraph(verts, align);
   }

   /**
    * Creates a new graph using the indicated vertices. Copies position, domination values and edges.
    * @param vertices an array where each element is the index of a vertex to be included in the new graph.
    * @return a new graph created from the indicated vertices.
    */
   public Graph getSubgraph(int[] vertices){
      return getSubgraph(vertices, false);
   }

   /**
    * Creates a new graph using the indicated vertices. Copies position, domination values and edges.
    * Optionally aligns the new graph so its top left boundary is at (0,0).
    * @param vertices an array where each element is the index of a vertex to be included in the new graph.
    * @param align whether the graph should be aligned to (0,0).
    * @return a new graph created from the indicated vertices, optionally aligned to (0,0).
    */
   public Graph getSubgraph(int[] vertices, boolean align){
      int subN = vertices.length;
      int mDegree = 0;
      for (int v : vertices) {
         if(degrees[v] > mDegree)
            mDegree = degrees[v];
      }

      Graph g = new Graph(subN, mDegree);

      for (int i = 0; i < subN; i++) {
         for (int j = 0; j < subN; j++) {
            if(isArc(vertices[i]+1,vertices[j]+1)){
               g.addArc(i+1,j+1);
            }

         }
         g.nodePosX[i] = nodePosX[vertices[i]];
         g.nodePosY[i] = nodePosY[vertices[i]];
         g.domset[i] = domset[vertices[i]];
      }

      if(align) {
         g.alignTopLeft();
      }

      return g;
   }

   /**
    * Adds a new vertex to the graph, with the given x and y coordinates.
    * @param x the x coordinate of the new vertex.
    * @param y the y coordinate of the new vertex.
    */
   public void addVertex(double x, double y){
      this.setN(N+1);

      setXPos(N-1, x);
      setYPos(N-1, y);
   }

   /**
    * Adds all vertices and edges of the graph g to this graph. Domination values and coordinates are kept.
    * @param g the graph to add to this one.
    */
   public void addSubgraph(Graph g){
      addSubgraph(g, 0,0, 1);
   }

   /**
    * Adds all vertices and edges of the graph g to this graph. Domination values are kept and coordinates are scaled by
    * the specified value.
    * @param g the graph to add to this one.
    * @param scale the factor by which the vertex coordinates will be divided.
    */
   public void addSubgraph(Graph g, double scale){
      addSubgraph(g, 0, 0, scale);
   }

   /**
    * Adds all vertices and edges of the graph g to this graph. Domination values are kept, and coordinates are shifted
    * by the given offsets.
    * @param g the graph to add to this one.
    * @param xOffset how much the x coordinates of the vertices are offset by.
    * @param yOffset how much the y coordinates of the vertices are offset by.
    */
   public void addSubgraph(Graph g, double xOffset, double yOffset){
      addSubgraph(g, xOffset, yOffset, 1);
   }

   /**
    * Adds all vertices and edges of the graph g to this graph. Domination values are kept, and coordinates are scaled
    * by the given amount then shifted by the given offsets.
    * @param g the graph to add to this one.
    * @param xOffset how much the x coordinates of the vertices are offset by.
    * @param yOffset how much the y coordinates of the vertices are offset by.
    * @param scale inverse of how much the original coordinates are scaled by.
    */
   public void addSubgraph(Graph g, double xOffset, double yOffset, double scale){
      int oldN = N;


      setN(oldN + g.getN());

      for (int i = 0; i < g.N; i++) {
         setXPos(oldN + i, g.getXPos(i)/scale+xOffset);
         setYPos(oldN + i,g.getYPos(i)/scale+yOffset);
         domset[oldN + i] = g.domset[i];

      }


      boolean[] select = new boolean[N];

      for (int i = 0; i < g.N; i++) {
         for (int j = 0; j < g.N; j++) {
            if(g.isArc(i+1,j+1)){
               addArc(oldN+i+1,oldN+j+1);
            }
         }

         select[oldN+i] = true;
      }


      setSelected(select);

   }

   /**
    * Gets the coordinates of the closest vertex to the vertex with the given index.
    * @param vertex the index of the vertex to find a neighbour of.
    * @return a two element array containing the x and y coordinates (respectively) of the closest vertex.
    * Has a default value of (15,15).
    */
   public double[] distToClosestNeighbour(int vertex){
      double[] closestXY = {15,15};

      if(vertex >= degrees.length){
         return closestXY;
      }

      int degree = degrees[vertex];

      double x = getXPos(vertex);
      double y = getYPos(vertex);

      double closestDist = Integer.MAX_VALUE;


      for (int i = 0; i < degree; i++) {
         int v2 = arcs[vertex][i]-1;
         double x2 = getXPos(v2);
         double y2 = getYPos(v2);

         double dist = Math.sqrt(Math.pow(x-x2,2)+Math.pow(y-y2,2));

         if(dist <= closestDist){
            closestXY[0] = Math.abs(x-x2)/2;
            closestXY[1] = Math.abs(y-y2)/2;
         }
      }

      return closestXY;
   }

   /**
    * Aligns this graph such that the top left boundary of the graph is at (0,0).
    */
   public void alignTopLeft(){
      alignTopLeft(0,0, 1);
   }


   /**
    * Rescales all coordinates using the given scale, then aligns this graph such that the top left boundary
    * of the graph is at (xOffset, yOffset).
    * @param xOffset x coordinate that the top left should be aligned to.
    * @param yOffset y coordinate that the top left should be aligned to.
    * @param scale inverse how much the coordinates should be scaled by before aligning them.
    */
   public void alignTopLeft(double xOffset, double yOffset, double scale){

      if(N == 0) return;

      if(scale != 1) {
         rescale(scale);
      }

      double[] topleft = getTopLeft();


      for (int i = 0; i < N; i++) {
         nodePosX[i] = nodePosX[i] - topleft[0] + xOffset;
         nodePosY[i] = nodePosY[i] - topleft[1] + yOffset;
      }



   }

   /**
    * Gets the coordinates of the top left boundary of the given vertices. This is given by the smallest x coordinate
    * and smallest y coordinate among all vertices (not necessarily the same vertex).
    * @return a two element array containing the x coordinate and y coordinate (respectively) of the top left boundary
    * of this graph.
    */
   public double[] getTopLeft(){

      if(N == 0) return new double[2];

      double[] topleft = {Integer.MAX_VALUE, Integer.MAX_VALUE};

      for (int i = 0; i < N; i++) {
         if(nodePosX[i] < topleft[0]){
            topleft[0] = nodePosX[i];

         }
         if(nodePosY[i] < topleft[1]){
            topleft[1] = nodePosY[i];
         }

      }


      return topleft;

   }

   /**
    * Gets the coordinates of the bottom right boundary of the given vertices. This is given by the largest x coordinate
    * and largest y coordinate among all vertices (not necessarily the same vertex).
    * @return a two element array containing the x coordinate and y coordinate (respectively) of the bottom right boundary
    * of this graph.
    */
   public double[] getBottomRight(){
      if(N == 0) return new double[2];

      double[] bottomRight = {-1*Integer.MAX_VALUE, -1*Integer.MAX_VALUE};

      for (int i = 0; i < N; i++) {
         if(nodePosX[i] > bottomRight[0]){
            bottomRight[0] = nodePosX[i];

         }
         if(nodePosY[i] > bottomRight[1]){
            bottomRight[1] = nodePosY[i];
         }

      }

      return bottomRight;

   }

   /**
    * Divides the coordinates of every vertex in this graph by the given scale.
    * @param scale factor by which the coordinates are reduced.
    */
   public void rescale(double scale){

      for (int i = 0; i < N; i++) {
         nodePosX[i] = nodePosX[i]/scale;
         nodePosY[i] = nodePosY[i]/scale;
      }

   }

   /**
    * Divides the coordinates of every selected vertex in this graph by the given scale.
    * @param scale factor by which the coordinates are reduced.
    */
   public void rescaleSelected(double scale){
      rescaleList(scale, selected);
   }

   /**
    * Divides the coordinates of every indicated vertex in this graph by the given scale.
    * @param scale factor by which the coordinates are reduced.
    * @param toRescale an array of length N, where vertex with index i will be rescaled if toRescale[i] is true.
    */
   public void rescaleList(double scale, boolean[] toRescale){

      if(N == 0) return;

      double[] corner = {Integer.MAX_VALUE, Integer.MAX_VALUE};

      for (int i = 0; i < N; i++) {
         if(toRescale[i]) {
            if (nodePosX[i] < corner[0]) {
               corner[0] = nodePosX[i];
            }
            if (nodePosY[i] < corner[1]) {
               corner[1] = nodePosY[i];
            }
         }
      }

      for (int i = 0; i < N; i++) {
         if(toRescale[i]){
            nodePosX[i] = (nodePosX[i]-corner[0])/scale + corner[0];
            nodePosY[i] = (nodePosY[i]-corner[1])/scale + corner[1];
         }
      }

   }

   /**
    * Creates a new graph with the same vertices, edges, domination values and selected vertices as this one.
    * @return a copy of this graph.
    */
   public Graph getCopy(){
      boolean[] all = new boolean[N];
      Arrays.fill(all, true);

      Graph g =  getSubgraph(all);
      g.selected = selected.clone();
      return g;

   }

   /**
    * Adjusts all x and y coordinates of vertices of this graph to the nearest multiple of the given spacing.
    * @param spacing distance between points where vertices are aligned.
    */
   public void alignToGrid(double spacing){
      boolean[] all = new boolean[N];
      Arrays.fill(all, true);

      alignToGrid(spacing, all, 0, 0);
   }

   /**
    * Adjusts x and y coordinates of vertices of this graph to the nearest multiple of the given spacing, then offset
    * by the given amount. If any vertex is selected, only the selected vertices will be adjusted, otherwise all vertices
    * are adjusted.
    * @param spacing distance between points where vertices are aligned.
    * @param offsetX how much the x coordinate is offset by before and after rounding to the nearest multiple of spacing.
    * @param offsetY how much the y coordinate is offset by before and after rounding to the nearest multiple of spacing.
    */
   public void alignToGrid(double spacing, double offsetX, double offsetY){

      for (int i = 0; i < N; i++) {
         if(selected[i]){
            alignToGrid(spacing, selected, offsetX, offsetY);
            return;
         }
      }

      boolean[] all = new boolean[N];
      Arrays.fill(all, true);

      alignToGrid(spacing, all, offsetX, offsetY);
   }

   /**
    * Adjusts x and y coordinates of the selected vertices of this graph to the nearest multiple of the given spacing, then offset
    * by the given amount.
    * @param spacing distance between points where vertices are aligned.
    * @param toAlign an array of length N, where vertex with index i will be realigned if toAlign[i] is true.
    * @param offsetX how much the x coordinate is offset by before and after rounding to the nearest multiple of spacing.
    * @param offsetY how much the y coordinate is offset by before and after rounding to the nearest multiple of spacing.
    */
   public void alignToGrid(double spacing, boolean[] toAlign, double offsetX, double offsetY){

      for (int i = 0; i < N; i++) {
         if(toAlign[i]){
            nodePosX[i] = Math.round((nodePosX[i]-offsetX)/spacing)*spacing + offsetX;
            nodePosY[i] = Math.round((nodePosY[i]-offsetY)/spacing)*spacing + offsetY;
         }
      }

   }


}

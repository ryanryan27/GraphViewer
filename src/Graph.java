//package UGV;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;

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

   public Graph(int inN, int inMaxDegree)
   {
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

   public int getN()
   {
      return N;
   }

   public int getEdgeCount(){
      int count = 0;
      for (int i = 0; i < N; i++) {
         count += degrees[i];
      }
      return count/2;
   }

   public int getMaxDegree()
   {
      return maxDegree;
   }

   public void setN(int inN)
   {
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

   public void deleteVertex(int vertex)
   {
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

   public void deleteArc(int v1, int v2)
   {
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

   public void deleteEdge(int v1, int v2)
   {
      deleteArc(v1,v2);
      deleteArc(v2,v1);
   }

   public void setMaxDegree(int inMaxDegree)
   {
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

   public boolean isArc(int v1, int v2)
   {
      for(int i=0; i<degrees[v1-1]; i++)
         if(arcs[v1-1][i] == v2)
            return true;

      return false;
   }

   public void addArc(int v1, int v2)
   {
      if(degrees[v1-1] == maxDegree)
         setMaxDegree(maxDegree+1);
      arcs[v1-1][degrees[v1-1]++] = v2;
      orderArcs(v1);
      orderArcs(v2);
   }

   public void addArcs(int [][]newArcs)
   {
      for(int i=0; i<newArcs.length; i++)
         addArc(newArcs[i][0],newArcs[i][1]);
   }

   public void orderArcs(int row)
   {
      int []newRow = new int[degrees[row-1]];
      for(int i=0; i<degrees[row-1]; i++)
         newRow[i] = arcs[row-1][i];

      Arrays.sort(newRow);

      for(int i=0; i<degrees[row-1]; i++)
         arcs[row-1][i] = newRow[i];
   }

   public void setDegrees(int []newDegrees)
   {
      degrees = newDegrees;
      maxDegree = 0;
      for(int i=0; i<N; i++)
         if(degrees[i] > maxDegree)
            maxDegree = degrees[i];
   }

   public boolean isEdge(int v1, int v2)
   {
      boolean check = false;
      for(int i=0; i<degrees[v1-1]; i++)
         if(arcs[v1-1][i] == v2)
         {
            check = true;
            break;
         }

      if(check)
      {
         for(int i=0; i<degrees[v2-1]; i++)
            if(arcs[v2-1][i] == v1)
               return true;
      }

      return false;
   }

   public void addEdge(int v1, int v2)
   {
      if(degrees[v1-1] == maxDegree || degrees[v2-1] == maxDegree)
         setMaxDegree(maxDegree+1);

      arcs[v1-1][degrees[v1-1]++] = v2;
      arcs[v2-1][degrees[v2-1]++] = v1;
   }

   public void addEdges(int [][]newEdges)
   {
      for(int i=0; i<newEdges.length; i++)
         addEdge(newEdges[i][0],newEdges[i][1]);
   }

   public int[][] getArcs()
   {
      return arcs;
   }

   public void setArcs(int [][]a, int []d, int nodes, boolean requiresSorting)
   {
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

   public void swapVertices(int v1, int v2)
   {

      if(v1 == v2 || v1 <= 0 || v2 <= 0 || v1 > N || v2 > N)
         return;

      for(int i=0; i<N; i++)
      //if(i != v1-1 && i != v2-1)
      //{
      {
         boolean changed = false;
         for(int j=0; j<degrees[i]; j++)
         {


            if(arcs[i][j] == v1)
            {
               arcs[i][j] = v2;
               changed = true;
            }

            else if(arcs[i][j] == v2)
            {
               arcs[i][j] = v1;
               changed = true;
            }
         }
         if(changed)
            orderArcs(i+1);
      }
      //}

      for(int i=0; i<maxDegree; i++)
      {
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

   public int[] getDegrees()
   {
      return degrees;
   }

   public double getXPos(int node)
   {
      return nodePosX[node];
   }

   public double getYPos(int node)
   {
      return nodePosY[node];
   }

   public void setXPos(int node, double posX)
   {
      nodePosX[node] = posX;
   }

   public void setYPos(int node, double posY)
   {
      nodePosY[node] = posY;
   }

   public void setAllPos(double []nx, double []ny)
   {
      nodePosX = nx;
      nodePosY = ny;
   }

   public double[] getXPoses()
   {
      return nodePosX;
   }

   public double[] getYPoses()
   {
      return nodePosY;
   }

   public int[] getContour()
   {
      return contour;
   }

   public void setContour(int[] co)
   {
      contour = co;
   }

   public boolean[] getSelected()
   {
      return selected;
   }

   public void setSelected(boolean[] se)
   {
      selected = se;
   }

   public void select(int v)
   {
      selected[v] = true;
   }

   public void deselect(int v)
   {
      selected[v] = false;
   }

   public boolean isSelected(int v)
   {
      return selected[v];
   }


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

   public void createGrid(int copies, boolean vertical, double spacing){

      int width = copies;

      if(vertical){
         width = (int) Math.ceil((N*1.0)/copies);
      }


      for (int i = 0; i < N; i++) {
         double x = (i % width)*spacing;
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

   public boolean isConnected()
   {
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

   public int checkRegular()
   {
      int regular = degrees[0];
      for(int i=1; i<N; i++)
         if(degrees[i] != regular)
            return -1;

      return regular;
   }

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

   public int connectivity(PropertiesDialog pd)
   {
      JProgressBar progressBar = null;
      if(pd != null)
      {
         progressBar = pd.getProgressBar();
         progressBar.setMaximum(N-1);
         progressBar.setMinimum(0);
         progressBar.setValue(0);
      }
      int minMaxFlow = maxFlow(2);
      if(progressBar != null)
         progressBar.setValue(1);
      for(int i=3; i<=N; i++)
      {
         int maxFlow = maxFlow(i);
         if(maxFlow < minMaxFlow)
            minMaxFlow = maxFlow;
         if(progressBar != null)
            progressBar.setValue(i-1);
         //if(maxFlow == 1)
         //System.out.println(i);
      }
      return minMaxFlow;
   }



   public int maxFlow(int v)
   {
      int flow = 0;
      int [][]flowMatrix = new int[N][N];
      for(int i=0; i<N; i++)
         for(int j=0; j<degrees[i]; j++)
            flowMatrix[i][arcs[i][j]-1] = 1;

      int [][]updateFlow = new int[N][N];


      while(true)
      {
         int []path = findPath(flowMatrix, 0, v-1);
         if(path[0] == -1)
            return flow;

         flow++;
         for(int i=1; i<N; i++)
         {
            if(path[i] == -1)
               break;

            flowMatrix[path[i-1]][path[i]] -= 1;
            flowMatrix[path[i]][path[i-1]] += 1;
         }
      }
   }

   public int[] findPath(int [][]flowMatrix, int v1, int v2)
   {
      int nodes = flowMatrix[0].length;
      int []path = new int[nodes];
      for(int i=0; i<nodes; i++)
         path[i] = -1;
      int []levels = new int[nodes];
      int []parents = new int[nodes];
      boolean []checked = new boolean[nodes];
      checked[v1] = true;

      boolean []toCheck = new boolean[nodes];
      boolean goOn = false;
      int current = -1;
      for(int i=0; i<nodes; i++)
         if(flowMatrix[v1][i] > 0)
         {
            if(current == -1)
               current = i;
            toCheck[i] = true;
            parents[i] = v1;
            levels[i] = 1;
            goOn = true;
            if(i == v2)
            {
               path[0] = v1;
               path[1] = v2;
               return path;
            }
         }

      boolean finished = false;

      while(goOn)
      {
         goOn = false;



         for(int i=0; i<nodes; i++)
         {
            if(flowMatrix[current][i] > 0 && !checked[i])
            {
               toCheck[i] = true;
               parents[i] = current;
               levels[i] = levels[current]+1;
               if(i == v2)
               {
                  finished = true;
                  break;
               }
            }
         }

         if(finished)
            break;

         checked[current] = true;
         toCheck[current] = false;

         current = -1;
         for(int i=0; i<nodes; i++)
            if(toCheck[i])
            {
               current = i;
               goOn = true;
               break;
            }
      }

      if(finished)
      {
         current = v2;
         for(int i=levels[v2]; i>=0; i--)
         {
            path[i] = current;
            current = parents[current];
         }

      /*for(int i=0; i<nodes; i++)
      {
         if(path[i] == -1)
            break;
         System.out.print((path[i]+1) + " ");
      }
      System.out.println();*/
      }
      return path;
   }


   public int girth(PropertiesDialog pd)
   {
      JProgressBar progressBar = null;
      if(pd != null)
      {
         progressBar = pd.getProgressBar();
         progressBar.setMaximum(N-1);
         progressBar.setMinimum(0);
         progressBar.setValue(0);
      }
      int girth = N+1;
      for(int i=1; i<=N; i++)
      {
         boolean []S = new boolean[N];
         boolean []R = new boolean[N];
         int []D = new int[N];
         int []parent = new int[N];
         //R[i-1] = true;
         int x = i;
         int sindex = 0;
         boolean goOn = true;
         while(goOn)
         {
            goOn = false;
            S[x-1] = true;
            for(int j=1; j<=N; j++)
            {
               if(parent[x-1] != j)
               {
                  boolean found = false;
                  for(int k=0; k<degrees[x-1]; k++)
                  {
                     if(arcs[x-1][k] == j)
                     {
                        found = true;
                        break;
                     }
                  }
                  if(found)
                  {
                     if(!S[j-1])
                     {
                        parent[j-1] = x;
                        D[j-1] = D[x-1] + 1;
                        R[j-1] = true;
                     }
                     else
                     {
                        girth = Math.min(girth,D[x-1] + D[j-1] + 1);
                        if(girth == 3)
                        {
                           progressBar.setValue(N);
                           return girth;
                        }
                     }
                  }
               }
            }
            for(int j=0; j<N; j++)
               if(R[j])
               {
                  x = j+1;
                  R[j] = false;
                  goOn = true;
                  break;
               }
         }

         if(progressBar != null)
            progressBar.setValue(i);
      }
      if(girth == N+1)
         return -1;
      return girth;
   }


   public int diameter(PropertiesDialog pd)
   {
      JProgressBar progressBar = null;
      if(pd != null)
      {
         progressBar = pd.getProgressBar();
         progressBar.setMaximum(N-1);
         progressBar.setMinimum(0);
         progressBar.setValue(0);
      }
      int diameter = 0;
      for(int i=0; i<N; i++)
      {

         int []dist = new int[N];
         int []previous = new int[N];

         for(int j=0; j<N; j++)
         {
            dist[j] = N+1;
            previous[j] = 0;
         }

         dist[i] = 0;

         boolean []Q = new boolean[N];
         for(int j=0; j<N; j++)
            Q[j] = true;

         boolean goOn = true;
         int u = i;
         Q[u] = false;

         while(goOn)
         {
            goOn = false;
            if(dist[u] == N+1)
               break;

            for(int j=0; j<degrees[u]; j++)
            {
               if(Q[arcs[u][j]-1])
               {
                  int alt = dist[u] + 1;
                  if(alt < dist[arcs[u][j]-1])
                  {
                     dist[arcs[u][j]-1] = alt;
                     previous[arcs[u][j]-1] = u;
                  }
               }
            }

            int min = N+2;
            for(int j=0; j<N; j++)
               if(Q[j])
               {
                  goOn = true;
                  if(dist[j] < min)
                  {
                     min = dist[j];
                     u = j;
                  }
               }

            if(goOn)
            {
               Q[u] = false;
            }
         }

         for(int j=0; j<N; j++)
            if(dist[j] > diameter)
               diameter = dist[j];

         if(progressBar != null)
            progressBar.setValue(i);
      }
      return diameter;
   }

   public int inDomset(int node)
   {
      /*if(domset[node-1] > 0)
         return true;
      else
         return false;*/
      return domset[node-1];
   }

   public void setDomValue(int node, int domValue){
      domset[node] = domValue;
   }

   public boolean[] dominatedVertices(boolean domTotal, boolean domSecure, boolean domConnected, boolean domRoman, boolean domWeakRoman)
   {
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

   public int[] getDomset()
   {
      return domset;
   }

   public void setDomset(int []ds)
   {
      domset = ds;
   }

   public int[] toggleDom(int node)
   {
      if(domset[node] == 0)
         domset[node] = 1;
      else if(domset[node] == 1)
         domset[node] = 2;
      else if(domset[node] == 2)
         domset[node] = 0;
      return domset;
   }

   public void springLayout(int radius){
      //calculatingSpring = true;
      calculateShortestPaths();

      double tolerance = 0.001/N;

      double[] before = getMiddle();


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
            }
            else {
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

      //System.out.println("m = " + m);

      double prevD = 0;
      int count = 0;
      while(count < 1000 && (delta[m] > 0.001 && Math.abs(delta[m] - prevD)/prevD >= tolerance)){
         //System.out.println("Inner m = " + m);
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


            //System.out.println("dX = " +dX + " and dY = " + dY+". F*D - B*B = " + (F*D - B*B) + " and C*D - E*B = " + (C*D - E*B) + ". C = " + C + " and D + " + D + " and E = " + E + " and F = " + F);

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

      /*double[] after = getMiddle();
      double xShift = before[0] - after[0];
      double yShift = before[1] - after[1];

      for (int i = 0; i < N; i++) {
         nodePosX[i] += xShift;
         nodePosY[i] += yShift;
      }*/


      //calculatingSpring = false;
   }


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


   public int dist(int v1, int v2){
      if (dists == null || dists.length != N) {
         //TODO need to update shortest paths when verts/edges/labels changed
         calculateShortestPaths();
      }
      return dists[v1][v2];
   }

   public double distL2(int v1, int v2){
      return Math.pow(getXPos(v1)-getXPos(v2),2) + Math.pow(getYPos(v1)-getYPos(v2),2);
   }

   public int[] twoApartList(int v1){
      //ArrayList<Integer> verts = new ArrayList<>();
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

   public Graph getSubgraph(boolean[] vertices){
      return getSubgraph(vertices, false);
   }

   public Graph getSubgraph(boolean[] vertices, boolean align){
      int subN = 0;
      for (boolean v : vertices) {
         if(v) subN++;
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

   public Graph getSubgraph(int[] vertices){
      return getSubgraph(vertices, false);
   }

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
         g.domset[i] = domset[i];
      }

      if(align) {
         g.alignTopLeft();
      }

      return g;
   }

   public void addVertex(double x, double y){
      this.setN(N+1);

      setXPos(N-1, x);
      setYPos(N-1, y);
   }

   public void addSubgraph(Graph g){

      addSubgraph(g, 0,0, 1);
}

   public void addSubgraph(Graph g, double scale){
      addSubgraph(g, 0, 0, scale);
   }

   public void addSubgraph(Graph g, double xOffset, double yOffset){
      addSubgraph(g, xOffset, yOffset, 1);
   }

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

   public void alignTopLeft(){
      alignTopLeft(0,0, 1);
   }



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

   public void rescale(double scale){

      for (int i = 0; i < N; i++) {
         nodePosX[i] = nodePosX[i]/scale;
         nodePosY[i] = nodePosY[i]/scale;
      }

   }

   public void rescaleSelected(double scale){
      rescaleList(scale, selected);
   }

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

   public Graph getCopy(){
      boolean[] all = new boolean[N];
      Arrays.fill(all, true);

      Graph g =  getSubgraph(all);
      g.selected = selected.clone();
      return g;

   }

   public void alignToGrid(int spacing){
      boolean[] all = new boolean[N];
      Arrays.fill(all, true);

      alignToGrid(spacing, all);
   }

   public void alignToGrid(int spacing, boolean[] toAlign){

      for (int i = 0; i < N; i++) {
         if(toAlign[i]){
            nodePosX[i] = Math.round(nodePosX[i]/spacing)*spacing;
            nodePosY[i] = Math.round(nodePosY[i]/spacing)*spacing;
         }
      }


   }

}

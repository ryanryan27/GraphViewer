import javax.swing.*;

public class SlowPropertiesRunner implements Runnable
   {
      Graph graph;
      PropertiesDialog propertiesDialog;
      int action;
   
      final int ACTIONS_CONNECTIVITY = 0;
      final int ACTIONS_GIRTH = 1;
      final int ACTIONS_DIAMETER = 2;

      int N;
      int[] degrees;
      int[][] arcs;
   
      public SlowPropertiesRunner(Graph g, PropertiesDialog p)
      {
         graph = g;
         propertiesDialog = p;
         action = ACTIONS_CONNECTIVITY;

         N = g.getN();
         degrees = g.getDegrees();
         arcs = g.getArcs();
      }
      
      public void setAction(int a)
      {
         action = a;
      }
   
      public void run()
      {
         if(action == ACTIONS_CONNECTIVITY)
         {
            int connectivity = connectivity(propertiesDialog);
            propertiesDialog.setLabel(action,connectivity);
         }
         if(action == ACTIONS_GIRTH)
         {
            int girth = girth(propertiesDialog);
            propertiesDialog.setLabel(action,girth);
         }
         if(action == ACTIONS_DIAMETER)
         {
            int diameter = diameter(propertiesDialog);
            propertiesDialog.setLabel(action,diameter);
         }
      }



      //TODO move this to somewhere else. give the graph as an argument.
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


      //TODO move this with connectivity()
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

      //TODO move this with connectivity()
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

      //TODO move this with connectivity()
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

      //TODO move this with connectivity()
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
   }
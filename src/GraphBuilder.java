public class GraphBuilder {


 static Graph path(int n){
     Graph g = new Graph(n, 0);

     for (int i = 0; i < n-1; i++) {
         g.addArc(i,i+1);
     }

     g.createGrid(n,false, 100);

     return g;
 }

    static Graph cycle(int n){
        Graph g = new Graph(n, 0);

        for (int i = 0; i < n-1; i++) {
            g.addArc(i,i+1);
        }

        g.addArc(n-2,n-1);

        g.createGrid(n,false, 100);

        return g;
    }


}

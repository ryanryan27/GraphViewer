public class GraphBuilder {


    static Graph cartesian_product(Graph g1, Graph g2){
        return cartesian_product(g1, g2, false);
    }

    static Graph cartesian_product(Graph g1, Graph g2, boolean rotate) {

        int n1 = g1.getN();
        int n2 = g2.getN();
        int n = n1 * n2;

        int[][] arcs1 = g1.getArcs();
        int[][] arcs2 = g2.getArcs();

        int[] degrees1 = g1.getDegrees();
        int[] degrees2 = g2.getDegrees();

        double[] xpos1 = g1.getXPoses();
        double[] ypos1 = g1.getYPoses();
        double[] xpos2 = g2.getXPoses();
        double[] ypos2 = g2.getYPoses();

        Graph product = new Graph(n, 0);

        for (int i = 0; i < n1; i++) {

            for (int j = 0; j < n2; j++) {
                for (int k = 0; k < degrees2[j]; k++) {
                    product.addArc(i * n2 + j + 1, i * n2 + arcs2[j][k]);
                    product.addArc(i * n2 + arcs2[j][k],i * n2 + j + 1);
                }

                for (int k = 0; k < degrees1[i]; k++) {
                    product.addArc(i * n2 + j + 1, (arcs1[i][k] - 1) * n2 + 1 + j);
                    product.addArc( (arcs1[i][k] - 1) * n2 + 1 + j,i * n2 + j + 1);
                }

                if(rotate) {
                    product.setXPos(i * n2 + j, xpos1[i] + ypos2[j]);
                    product.setYPos(i * n2 + j, ypos1[i] + xpos2[j]);
                } else {
                    product.setXPos(i * n2 + j, xpos1[i] + xpos2[j]);
                    product.setYPos(i * n2 + j, ypos1[i] + ypos2[j]);
                }
            }
        }


        return product;
    }


    static Graph path(int n) {
        Graph g = new Graph(n, 0);

        for (int i = 1; i < n; i++) {
            g.addArc(i, i + 1);
            g.addArc(i+1,i);
        }

        g.createGrid(n, false, 100);

        return g;
    }

    static Graph cycle(int n, boolean linear) {
        Graph g = new Graph(n, 0);

        for (int i = 1; i < n; i++) {
            g.addArc(i, i + 1);
            g.addArc(i+1,i);
        }

        g.addArc(n, 1);
        g.addArc(1, n);

        if(linear){
            g.createGrid(n, false, 100);
        } else {
            g.createCircle();
        }

        return g;
    }

    static Graph complete(int n) {
        Graph g = new Graph(n, 0);

        for (int i = 1; i < n; i++) {
            for (int j = i + 1; j <= n; j++) {
                g.addArc(i, j);
                g.addArc(j,i);
            }
        }

        g.createCircle();

        return g;
    }

    static Graph complete_multipartite(int[] subsets){
        //TODO implement this

        return new Graph(0,0);
    }

    static Graph flower_snark(int n){
        Graph flower = new Graph(0,0);

        for (int i = 0; i < n; i++) {
            Graph star = star(4);

            star.swapVertices(3,4);

            flower.addSubgraph(star);
        }

        for (int i = 0; i < n-1; i++) {

            flower.addArc(i*4 + 1, i*4 + 5);
            flower.addArc(i*4 + 2, i*4 + 6);
            flower.addArc(i*4 + 4, i*4 + 8);

            flower.addArc(i*4 + 5, i*4 + 1);
            flower.addArc(i*4 + 6, i*4 + 2);
            flower.addArc(i*4 + 8, i*4 + 4);
        }

        flower.addArc(1, 4*n-2);
        flower.addArc(2, 4*n-3);
        flower.addArc(4, 4*n);

        flower.addArc(4*n-2, 1);
        flower.addArc(4*n-3, 2);
        flower.addArc(4*n, 4);


        flower.createGrid(n,true, 100);
        flower.deselectAll();

        return flower;
    }

    static Graph star(int n){
        Graph g = new Graph(n-1,0);
        g.createCircle();
        double[] mid = g.getMiddle();
        g.addVertex(mid[0],mid[1]);

        for (int i = 1; i < n; i++) {
            g.addArc(i,n);
            g.addArc(n,i);
        }


        return g;
    }


}

public class GraphBuilder {


    static Graph cartesian_product(Graph g1, Graph g2) {

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
                }

                for (int k = 0; k < degrees1[i]; k++) {
                    product.addArc(i * n2 + j + 1, (arcs1[i][k] - 1) * n2 + 1 + j);
                }

                product.setXPos(i * n2 + j, xpos1[i] + ypos2[j]);
                product.setYPos(i * n2 + j, ypos1[i] + xpos2[j]);
            }
        }


        return product;
    }


    static Graph path(int n) {
        Graph g = new Graph(n, 0);

        for (int i = 1; i < n; i++) {
            g.addArc(i, i + 1);
        }

        g.createGrid(n, false, 100);

        return g;
    }

    static Graph cycle(int n) {
        Graph g = new Graph(n, 0);

        for (int i = 1; i < n; i++) {
            g.addArc(i, i + 1);
        }

        g.addArc(n, 1);

        g.createGrid(n, false, 100);

        return g;
    }

    static Graph complete(int n) {
        Graph g = new Graph(n, 0);

        for (int i = 1; i < n; i++) {
            for (int j = i + 1; j <= n; j++) {
                g.addArc(i, j);
            }
        }

        g.createCircle();

        return g;
    }


}

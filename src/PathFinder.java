import java.util.ArrayList;
import java.util.Iterator;

public class PathFinder {

    static final double POSITION_TOLERANCE = 0.01;

    private Graph obstacle_map;

    private PointList all_points;


    public PathFinder(Graph inGraph){

    }


    private EdgeList delaunayToMidpoints(Triangulation delaunay){
        //TODO

        return new EdgeList();
    }



    //Bowyer-watson algorithm for triangulation
    private Triangulation delaunayBW(PointList pointList){

        Triangulation triangulation = new Triangulation();

        triangulation.addTemporaryHull(pointList);

        for (Point p : pointList) {

            Triangulation badTriangles = new Triangulation();

            for (Triangle t : triangulation) {
                if(t.isInCircle(p)){
                    badTriangles.add(t);
                }
            }

            EdgeList polygon = badTriangles.getNonDuplicateEdges();

            for(Triangle t : badTriangles){
                triangulation.remove(t);
            }

            for(Edge e : polygon){
                Triangle t = new Triangle(e, p);
                triangulation.add(t);
            }

        }

        triangulation.removeTemporaryPoints();

        return triangulation;
    }


    PointList pointListFromUGV(Graph g){
        //TODO
        PointList points = new PointList();
        for (int i = 0; i < g.getN(); i++) {
            Point p = new Point(g.getXPos(i), g.getYPos(i));
            points.add(p);
        }

        return points;
    }

    Graph ugvFromTriangulation(Triangulation t){
        //TODO



        return new Graph(0,0);
    }



    private class Point {
        double x;
        double y;
        double bearing;
        boolean temporary;
        int type; //start, obstacle, goal, temporary
        //TODO maybe add an ID?

        Point(double x, double y){
            this.x = x;
            this.y = y;
        }

        double dist(Point p){
            return Math.sqrt(Math.pow(this.x-p.x,2) + Math.pow(this.y-p.y,2));
        }

        double dist(double x, double y){
            return Math.sqrt(Math.pow(this.x-x,2) + Math.pow(this.y-y,2));
        }

        Point midpoint(double x, double y){
            double mx = (this.x + x)/2;
            double my = (this.y + y)/2;

            return new Point(mx,my);
        }

        Point midpoint(Point p){
            return this.midpoint(p.x, p.y);
        }


        double slope(Point p){
            return (this.y-p.y)/(this.x-p.x);
        }

        double getX(){
            return this.x;
        }

        double getY() {
            return this.y;
        }

        void setTemporary(){
            this.temporary = true;
        }

        boolean same(Point a){
            double diff_x = a.getX() - this.getX();
            double diff_y = a.getY() - this.getY();

            return (diff_x < POSITION_TOLERANCE && diff_y < POSITION_TOLERANCE);
        }

    }

    private class Edge {
        Point[] points;
        boolean remove;
        boolean must;
        boolean can;

        Edge(Point a, Point b){
            points = new Point[2];
            points[0] = a;
            points[1] = b;
        }

        Point[] getPoints(){
            return points;
        }

        Point midpoint(){
            return points[0].midpoint(points[1]);
        }

        void setRemove(boolean rem){
            this.remove = rem;
        }

        boolean getRemove(){
            return this.remove;
        }

        boolean same(Point a, Point b){

            return (a.same(points[0]) && b.same(points[1])) || (b.same(points[0]) && a.same(points[1]));

        }

    }

    private class Triangle {

        Point[] points;

        Triangle(Point p1, Point p2, Point p3){

            points = new Point[3];
            points[0] = p1;
            points[1] = p2;
            points[2] = p3;
        }

        Triangle(Edge e, Point p){
            points = new Point[3];
            points[0] = p;
            points[1] = e.getPoints()[0];
            points[2] = e.getPoints()[1];
        }


        boolean isInCircle(Point p){
            //get slopes of edges of triangle
            double m1 = points[0].slope(points[1]);
            double m2 = points[0].slope(points[2]);

            //get midpoints of the same edges of the triangle
            Point ab = points[0].midpoint(points[1]);
            Point ac = points[0].midpoint(points[2]);

            double x1 = ab.getX();
            double y1 = ab.getY();
            double x2 = ac.getX();
            double y2 = ac.getY();

            //get intersection of the lines generated from slopes and midpoints
            double circ_x = (1/(1/m2 - 1/m1))*(y2-y1 - (x1/m1) + (x2/m2));
            double circ_y = (-1/m2)*(circ_x - x2) + y2;

            double rad = points[0].dist(circ_x, circ_y);

            return p.dist(circ_x,circ_y) < rad;
        }

        boolean hasTemporary(){
            for (int i = 0; i < 3; i++) {
                if(points[i].temporary) {
                    return true;
                }
            }
            return false;
        }

        Point[] getPoints(){
            return points;
        }


    }

    private class PointList implements Iterable<Point>{
        ArrayList<Point> points;

        PointList(){
            points =  new ArrayList<>();
        }

        void add(Point p){
            points.add(p);
        }


        @Override
        public Iterator<Point> iterator() {
            return points.iterator();
        }


    }

    private class EdgeList implements Iterable<Edge>{

        ArrayList<Edge> edges;

        EdgeList(){
            edges = new ArrayList<>();
        }

        void add(Edge e){
            edges.add(e);
        }

        void removeMarked(){
            edges.removeIf(e -> e.remove);
        }


        @Override
        public Iterator<Edge> iterator() {
            return edges.iterator();
        }
    }

    private class Triangulation implements Iterable<Triangle>{

        ArrayList<Triangle> triangles;

        Triangulation(){
            triangles = new ArrayList<>();
        }

        void addTemporaryHull(PointList points){
            double xmin = Double.POSITIVE_INFINITY;
            double xmax = Double.NEGATIVE_INFINITY;
            double ymin = Double.POSITIVE_INFINITY;
            double ymax = Double.NEGATIVE_INFINITY;

            for(Point p : points){
                double x = p.getX();
                double y = p.getY();

                if(x < xmin) xmin = x;
                if(y < ymin) ymin = y;
                if(x > xmax) xmax = x;
                if(y > ymax) ymax = y;


            }

            double xoffset = (xmax -xmin)/10;
            double yoffset = (ymax - ymin)/10;

            Point top_left = new Point(xmin - xoffset, ymin - yoffset);
            Point top_right = new Point(xmax + xoffset, ymin - yoffset);
            Point bottom_left = new Point(xmin - xoffset, ymax + yoffset);
            Point bottom_right = new Point(xmax + xoffset, ymax + yoffset);

            top_left.setTemporary();
            top_right.setTemporary();
            bottom_left.setTemporary();
            bottom_right.setTemporary();

            this.add(new Triangle(top_left, bottom_left, bottom_right));
            this.add(new Triangle(top_left, top_right, bottom_right));

        }

        void removeTemporaryPoints(){
            //TODO not sure if this works properly
            triangles.removeIf(Triangle::hasTemporary);
        }


        void add(Triangle t){
            triangles.add(t);
        }

        void remove(Triangle t){
            //TODO not sure if this works how i want it to
            triangles.remove(t);
        }

        EdgeList getNonDuplicateEdges(){

            EdgeList edges = new EdgeList();

            for(Triangle t : this){
                Point a = t.getPoints()[0];
                Point b = t.getPoints()[1];
                Point c = t.getPoints()[2];

                boolean ab = true;
                boolean ac = true;
                boolean bc = true;

                for (Edge e : edges){
                    if(e.same(a,b) ){
                        e.setRemove(true);
                        ab = false;
                    } else if(e.same(a,c)){
                        e.setRemove(true);
                        ac = false;
                    } else if(e.same(b,c)){
                        e.setRemove(true);
                        bc = false;
                    }
                }

                if(ab) edges.add(new Edge(a,b));
                if(ac) edges.add(new Edge(a,c));
                if(bc) edges.add(new Edge(b,c));
            }

            edges.removeMarked();

            return edges;
        }


        @Override
        public Iterator<Triangle> iterator() {
            return triangles.iterator();
        }
    }

    private class Path {
        void add(Point p){
            //TODO
        }
    }





}

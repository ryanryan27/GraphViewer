import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

public class PathFinder implements Runnable{

    static final double POSITION_TOLERANCE = 0.01;

    static int id_counter = 0;

    GraphPane parent;
    Graph graph;



    PathFinder(GraphPane parent, Graph graph){
        this.parent = parent;
        this.graph = graph;
    }


    Graph getPath(Graph g){
        PointList points = pointListFromUGV(g);
        EdgeList edges = edgeListFromUGV(g, points);

        Triangulation delaunay = delaunayBW(points);
        Triangulation midpoints = delaunayToMidpoints(delaunay, edges);

        PGraph graph = graphFromMidpoints(midpoints);


        try {
            //return graph.toUGV();
            return graph.shortestPath().toGraph().toUGV();
        } catch (NullPointerException ex){
            System.out.println("no");
            return null;
        }

    }




    private Triangulation delaunayToMidpoints(Triangulation delaunay, EdgeList block_list){
        //TODO
        Triangulation midpoint_triangulation = new Triangulation();
        PointList midpoints = new PointList();
        for(Triangle t : delaunay){
            Point a = t.getPoints()[0];
            Point b = t.getPoints()[1];
            Point c = t.getPoints()[2];

            Point ab = a.midpoint(b);
            Point ac = a.midpoint(c);
            Point bc = b.midpoint(c);

            System.out.println("points----------------------------------------------------------------");
            System.out.println(ab.getX() + ", " + ab.getY());
            System.out.println(ac.getX() + ", " + ac.getY());
            System.out.println(bc.getX() + ", " + bc.getY());
            System.out.println("--");
            boolean add_ab = true;
            boolean add_ac = true;
            boolean add_bc = true;
            for(Point p: midpoints){
                System.out.println(p.getX() + ", " + p.getY());
                if(add_ab && p.same_spot(ab)){
                    ab = p;
                    add_ab = false;
                    System.out.println("----------------------------------------------");

                }
                if(add_ac && p.same_spot(ac)){
                    ac = p;
                    add_ac = false;
                    System.out.println("----------------------------------------------");

                }
                if(add_bc && p.same_spot(bc)){
                    bc = p;
                    add_bc = false;
                    System.out.println("----------------------------------------------");

                }
            }

            if(add_ab) midpoints.add(ab);
            if(add_ac) midpoints.add(ac);
            if(add_bc) midpoints.add(bc);

            for(Edge e : block_list){
                if(e.same(a,b)){
                    ab.setType(Point.BLOCKED);
                }
                if(e.same(a,c)){
                    ac.setType(Point.BLOCKED);
                }
                if(e.same(b,c)){
                    bc.setType(Point.BLOCKED);
                }
            }



            if(a.getType() == Point.START){
                midpoint_triangulation.setStart(a);
                bc.setType(Point.ENTRY);
                ab.setTemporary();
                ac.setTemporary();
            }
            if(b.getType() == Point.START){
                midpoint_triangulation.setStart(b);
                ac.setType(Point.ENTRY);
                ab.setTemporary();
                bc.setTemporary();
            }
            if(c.getType() == Point.START){
                midpoint_triangulation.setStart(c);
                ab.setType(Point.ENTRY);
                ac.setTemporary();
                bc.setTemporary();
            }
            if(a.getType() == Point.GOAL){
                midpoint_triangulation.setGoal(a);
                bc.setType(Point.EXIT);
                ab.setTemporary();
                ac.setTemporary();
            }
            if(b.getType() == Point.GOAL){
                midpoint_triangulation.setGoal(b);
                ac.setType(Point.EXIT);
                ab.setTemporary();
                bc.setTemporary();
            }
            if(c.getType() == Point.GOAL){
                midpoint_triangulation.setGoal(c);
                ab.setType(Point.EXIT);
                ac.setTemporary();
                bc.setTemporary();
            }

            Triangle new_t = new Triangle(ab, ac, bc);
            midpoint_triangulation.add(new_t);


        }

        for (Point p : midpoints){
            System.out.println("point: " + p.getX() + ", " + p.getY() + ", " + p.getID());
        }

        System.out.println("midpoints.size() = " + midpoints.size());
        return midpoint_triangulation;
    }

    PGraph graphFromMidpoints(Triangulation t){

        EdgeList edges = t.getEdges();
        PointList points = t.getPointList();
        Point start = t.getStart();
        Point goal = t.getGoal();

        PGraph g = new PGraph();

        boolean has_ends = true;

        if(start == null || goal == null){
            has_ends = false;
        }

        if(has_ends) {
            g.add(start);
            g.add(goal);
        }


        for(Point p : points){
            if(has_ends && p.getType() == Point.ENTRY){
                g.add(new Edge(p,start));
            }
            if(has_ends && p.getType() == Point.EXIT){
                g.add(new Edge(p,goal));
            }
            g.add(p);

        }


        for(Edge e : edges){
            g.add(e);
        }

        return g;
    }


    public Graph getTriangulation(Graph g, int level) throws InterruptedException {
        //return ugvFromTriangulation(delaunayBW(pointListFromUGV(g)));

        PointList points = pointListFromUGV(g);

        Triangulation t = delaunayBW(points);
        if(level == 1){
            EdgeList block_list = edgeListFromUGV(g, points);
            t = delaunayToMidpoints(t, block_list);
        }
        return ugvFromTriangulation(t);

    }

    //Bowyer-watson algorithm for triangulation
    private Triangulation delaunayBW(PointList pointList) {

        Triangulation triangulation = new Triangulation();

        triangulation.addTemporaryHull(pointList);

        for (Point p : pointList) {

            Triangulation badTriangles = new Triangulation();

            for (Triangle t : triangulation) {
                if(t.isInCircle(p)){
                    badTriangles.add(t);
                }
            }

            for(Triangle t : badTriangles){
                triangulation.remove(t);
            }

            EdgeList polygon = badTriangles.getNonDuplicateEdges();



            for(Edge e : polygon){
                Triangle t = new Triangle(e, p);
                triangulation.add(t);
            }
            //parent.setGraph(ugvFromTriangulation(triangulation));

        }


        triangulation.removeTemporaryPoints();
        parent.setGraph(ugvFromTriangulation(triangulation));

        return triangulation;
    }


    PointList pointListFromUGV(Graph g){
        //TODO
        PointList points = new PointList();
        for (int i = 0; i < g.getN(); i++) {
            int type = g.getDomset()[i];
            Point p = new Point(g.getXPos(i), g.getYPos(i), type);
            points.add(p);
        }

        
        return points;
    }

    EdgeList edgeListFromUGV(Graph g, PointList points){
        EdgeList edges = new EdgeList();

        for (int i = 0; i < points.size(); i++) {
            for (int j = 0; j < points.size(); j++) {
                if(g.isArc(i+1, j+1)){
                    edges.add(new Edge(points.points.get(i), points.points.get(j)));
                }
            }
        }

        return edges;
    }

    Graph ugvFromTriangulation(Triangulation t){
        //TODO

        //EdgeList edges = t.getNonDuplicateEdges();
        EdgeList edges = t.getEdges();
        PointList points = t.getPointList();
        Point start = t.getStart();
        Point goal = t.getGoal();

        Graph g = new Graph(0,0);

        for(Point p : points){
            if(p.getType() == Point.ENTRY){
                edges.add(new Edge(p,start));
            }
            if(p.getType() == Point.EXIT){
                edges.add(new Edge(p,goal));
            }
            g.addVertex(p.x,p.y);
        }

        if(t.getStart() != null){
            points.add(start);
            g.addVertex(start.x, start.y);
            g.setDomValue(g.getN()-1, 1);
        }

        if(t.getGoal() != null){
            points.add(goal);
            g.addVertex(goal.x, goal.y);
            g.setDomValue(g.getN()-1, 2);
        }

        for(Edge e : edges){
            g.addArc(points.idToIndex(e.points[0].getID())+1, points.idToIndex(e.points[1].getID())+1);
        }

        return g;
    }

    int requestID(){
        return id_counter++;
    }

    @Override
    public void run() {
        try {
            getTriangulation(graph, 1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private class Point {
        int id;
        double x;
        double y;
        double bearing;
        boolean temporary;
        int type;

        static final int OBSTACLE = 0;
        static final int START = 1;
        static final int GOAL = 2;
        static final int BLOCKED = 3;
        static final int OPEN = 4;
        static final int ENTRY = 5;
        static final int EXIT = 6;



        Point(double x, double y, int type){
            this.x = x;
            this.y = y;
            this.id = requestID();
            this.type = type;
            this.temporary = false;
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

            return new Point(mx,my,OPEN);
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

        int getID(){
            return this.id;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        void setTemporary(){
            this.temporary = true;
        }

        boolean same(Point a){
            //double diff_x = a.getX() - this.getX();
            //double diff_y = a.getY() - this.getY();

            //return (diff_x < POSITION_TOLERANCE && diff_y < POSITION_TOLERANCE);
            return this.id == a.getID();
        }

        boolean same_spot(Point a){
            double diff_x = Math.abs(a.getX() - this.getX());
            double diff_y = Math.abs(a.getY() - this.getY());

            return (diff_x < POSITION_TOLERANCE && diff_y < POSITION_TOLERANCE);
        }

    }

    private class Edge {
        Point[] points;
        boolean remove;

        boolean blockade;

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

    private class PGraph {
        EdgeList edges;
        PointList points;

        PGraph(){
            this.edges = new EdgeList();
            this.points = new PointList();
        }

        PGraph(EdgeList edges, PointList points){
            this.edges = edges;
            this.points = points;
        }

        public void add(Edge e){
            edges.add(e);
        }

        public void add(Point p){
            points.add(p);
        }

        Path shortestPath(){

            Path path = new Path();

            int N = points.size();
            if(N < 2) return null;

            double[][] dist_adj = new double[N][N];

            double[] dist = new double[N];
            int[] prev = new int[N];

            boolean[] queue = new boolean[N];

            int start = 0;
            int goal = 0;

            for (Point p : points){
                if(p.getType() == Point.START) {
                    start = points.idToIndex(p.getID());
                }
                if(p.getType() == Point.GOAL) {
                    goal = points.idToIndex(p.getID());
                }
            }




            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    dist_adj[i][j] = Double.MAX_VALUE;
                }
                dist[i] = Double.MAX_VALUE;
                prev[i] = -1;
                queue[i] = true;
            }

            for(Edge e : edges){
                Point a = e.getPoints()[0];
                Point b = e.getPoints()[1];


                int ai = points.idToIndex(a.getID());
                int bi = points.idToIndex(b.getID());

                if(ai == -1 || bi == -1){
                    continue;
                }

                dist_adj[ai][bi] = a.dist(b);
                dist_adj[bi][ai] = a.dist(b);
            }

            dist[start] = 0;


            while(true){
                double minDist = Double.MAX_VALUE;
                int curr = -1;

                for (int i = 0; i < N; i++) {
                    if(queue[i] && dist[i] <= minDist){
                        curr = i;
                        minDist = dist[i];
                    }
                }

                if(curr == -1 || curr == goal){
                    break;
                }

                queue[curr] = false;

                for (int i = 0; i < N; i++) {
                    if(queue[i] && dist_adj[curr][i] < Double.MAX_VALUE){
                        double mid = dist[curr] + dist_adj[curr][i];
                        if(mid < dist[i]){
                            dist[i] = mid;
                            prev[i] = curr;

                        }
                    }
                }

            }

            int curr = goal;

            while(curr != start){
                if(curr == -1){
                    System.out.println("No path exists");
                    return null;
                }

                path.addFirst(points.getFromIndex(curr));
                curr = prev[curr];

            }

            path.addFirst(points.getFromIndex(start));

            return path;
        }



        Graph toUGV(){
            Graph g = new Graph(0,0);

            for(Point p : points){
                g.addVertex(p.getX(), p.getY());
                if(p.getType() == Point.START){
                    g.setDomValue(g.getN()-1, 1);
                }
                if(p.getType() == Point.GOAL){
                    g.setDomValue(g.getN()-1, 2);
                }
            }

            for(Edge e : edges){
                g.addArc(points.idToIndex(e.points[0].getID())+1, points.idToIndex(e.points[1].getID())+1);
            }

            return g;

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

            if(m1 == Double.POSITIVE_INFINITY) m1 = Double.MAX_VALUE/8;
            if(m1 == Double.NEGATIVE_INFINITY) m1 = -1*Double.MAX_VALUE/8;
            if(m1 <0.001 && m1 > -0.001) m1 = 0.001;
            if(m2 == Double.POSITIVE_INFINITY) m2 = Double.MAX_VALUE/8;
            if(m2 == Double.NEGATIVE_INFINITY) m2 = -1*Double.MAX_VALUE/8;
            if(m2 <0.001 && m2 > -0.001) m2 = 0.001;


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

        int size(){
            return points.size();
        }

        int idToIndex(int id){

            for (int i = 0; i < points.size(); i++) {
                if(points.get(i).getID() == id) {
                    return i;
                }
            }
            return -1;
        }

        int indexToID(int index){
            try {
                return points.get(index).getID();
            } catch (NullPointerException ex) {

                return -1;
            }
        }

        Point getFromIndex(int index){
            try {
                return points.get(index);
            } catch (NullPointerException ex) {

                return null;
            }
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
        Point start;
        Point goal;

        Triangulation(){
            triangles = new ArrayList<>();
        }

        int size(){
            return triangles.size();
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

            Point top_left = new Point(xmin - xoffset, ymin - yoffset, Point.OPEN);
            Point top_right = new Point(xmax + xoffset, ymin - yoffset, Point.OPEN);
            Point bottom_left = new Point(xmin - xoffset, ymax + yoffset, Point.OPEN);
            Point bottom_right = new Point(xmax + xoffset, ymax + yoffset, Point.OPEN);

            top_left.setTemporary();
            top_right.setTemporary();
            bottom_left.setTemporary();
            bottom_right.setTemporary();

            this.add(new Triangle(top_left, bottom_left, bottom_right));
            this.add(new Triangle(top_left, top_right, bottom_right));

        }

        void removeTemporaryPoints(){
            //TODO not sure if this works properly
            ArrayList<Triangle> to_remove = new ArrayList<>();

            for(Triangle t : triangles){
                if(t.hasTemporary()){
                    to_remove.add(t);
                }
            }

            for (Triangle t : to_remove){
                triangles.remove(t);
            }
        }


        void add(Triangle t){
            triangles.add(t);
        }

        void remove(Triangle t){
            //TODO not sure if this works how i want it to
            triangles.remove(t);
        }

        EdgeList getEdges(){
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
                        ab = false;
                    } else if(e.same(a,c)){
                        ac = false;
                    } else if(e.same(b,c)){
                        bc = false;
                    }
                }

                if(a.getType() == Point.BLOCKED){
                    ab = false;
                    ac = false;
                }

                if(b.getType() == Point.BLOCKED){
                    ab = false;
                    bc = false;
                }

                if(c.getType() == Point.BLOCKED){
                    ac = false;
                    bc = false;
                }

                if(ab) edges.add(new Edge(a,b));
                if(ac) edges.add(new Edge(a,c));
                if(bc) edges.add(new Edge(b,c));
            }

            //edges.removeMarked();

            return edges;


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
                    }
                    if(e.same(a,c)){
                        e.setRemove(true);
                        ac = false;
                    }
                    if(e.same(b,c)){
                        e.setRemove(true);
                        bc = false;
                    }
                }

                if(ab){
                    edges.add(new Edge(a,b));

                }
                if(ac){
                    edges.add(new Edge(a,c));
                }
                if(bc){
                    edges.add(new Edge(b,c));
                }
            }

            edges.removeMarked();

            return edges;
        }

        PointList getPointList(){
            PointList points = new PointList();

            for (Triangle t : this){
                Point[] pts = t.points;

                for (Point pt : pts){
                    boolean exists = false;
                    for(Point p : points){

                        if(pt.same(p)){//TODO add same_spot, and then fix edge issues
                            exists = true;
                        }
                    }
                    if(!exists){
                        points.add(pt);
                    }
                }
            }
            return points;
        }

        public Point getStart() {
            return start;
        }

        public void setStart(Point start) {
            this.start = start;
        }

        public Point getGoal() {
            return goal;
        }

        public void setGoal(Point goal) {
            this.goal = goal;
        }


        @Override
        public Iterator<Triangle> iterator() {
            return triangles.iterator();
        }
    }


    private class Path {

        LinkedList<Point> points;

        Path(){
            points = new LinkedList<>();
        }

        void addFirst(Point p){
            points.addFirst(p);
        }

        void addLast(Point p){
            points.addLast(p);
        }


        void trimToObstacles(PointList obstacles, EdgeList walls){




        }

        PGraph toGraph(){
            PGraph graph = new PGraph();

            if(points.isEmpty()) return null;

            Point last = points.getFirst();

            for(Point p : points){
                graph.add(p);
                if(p.same(last)) {
                    continue;
                }
                graph.add(new Edge(last, p));
                last = p;
            }

            return graph;


        }


    }





}

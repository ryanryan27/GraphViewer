import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class PathFinder implements Runnable {

    static final double POSITION_TOLERANCE = 0.01;

    static int id_counter = 0;

    GraphPane parent;
    Graph graph;


    PathFinder(GraphPane parent, Graph graph) {
        this.parent = parent;
        this.graph = graph;
    }


    Graph getPath(Graph g, boolean trim) {
        PointList points = pointListFromUGV(g);
        EdgeList edges = edgeListFromUGV(g, points);

        Triangulation delaunay = delaunayBW(points);
        Triangulation midpoints = delaunayToMidpoints(delaunay, edges);

        PGraph graph = graphFromMidpoints(midpoints, points, edges);

        Path initial_path = graph.shortestPath();

        if (initial_path == null) {
            return null;
        }

        try {
            Path trimmed_path = initial_path.trimToObstacles(points, edges);
            Path desired;

            if (trim) {
                desired = trimmed_path;
            } else {
                desired = initial_path;
            }

            PGraph path_graph = desired.toGraph();

            if (path_graph == null) return null;

            return path_graph.toUGV();
        } catch (NullPointerException ex) {
            System.out.println("no");
            return null;
        }

    }


    private Triangulation delaunayToMidpoints(Triangulation delaunay, EdgeList block_list) {
        Triangulation midpoint_triangulation = new Triangulation();
        PointList midpoints = new PointList();
        for (Triangle t : delaunay) {
            Point a = t.getPoints()[0];
            Point b = t.getPoints()[1];
            Point c = t.getPoints()[2];

            Point ab = a.midpoint(b);
            Point ac = a.midpoint(c);
            Point bc = b.midpoint(c);

            boolean add_ab = true;
            boolean add_ac = true;
            boolean add_bc = true;
            for (Point p : midpoints) {
                if (add_ab && p.same_spot(ab)) {
                    ab = p;
                    add_ab = false;

                }
                if (add_ac && p.same_spot(ac)) {
                    ac = p;
                    add_ac = false;

                }
                if (add_bc && p.same_spot(bc)) {
                    bc = p;
                    add_bc = false;

                }
            }

            if (add_ab) midpoints.add(ab);
            if (add_ac) midpoints.add(ac);
            if (add_bc) midpoints.add(bc);


            if (a.getType() == Point.START) {
                midpoint_triangulation.setStart(a);
                bc.setType(Point.ENTRY);
                ab.setTemporary();
                ac.setTemporary();
            }
            if (b.getType() == Point.START) {
                midpoint_triangulation.setStart(b);
                ac.setType(Point.ENTRY);
                ab.setTemporary();
                bc.setTemporary();
            }
            if (c.getType() == Point.START) {
                midpoint_triangulation.setStart(c);
                ab.setType(Point.ENTRY);
                ac.setTemporary();
                bc.setTemporary();
            }
            if (a.getType() == Point.GOAL) {
                midpoint_triangulation.setGoal(a);
                bc.setType(Point.EXIT);
                ab.setTemporary();
                ac.setTemporary();
            }
            if (b.getType() == Point.GOAL) {
                midpoint_triangulation.setGoal(b);
                ac.setType(Point.EXIT);
                ab.setTemporary();
                bc.setTemporary();
            }
            if (c.getType() == Point.GOAL) {
                midpoint_triangulation.setGoal(c);
                ab.setType(Point.EXIT);
                ac.setTemporary();
                bc.setTemporary();
            }

            for (Edge e : block_list) {
                if (e.same(a, b)) {
                    ab.setType(Point.BLOCKED);
                }
                if (e.same(a, c)) {
                    ac.setType(Point.BLOCKED);
                }
                if (e.same(b, c)) {
                    bc.setType(Point.BLOCKED);
                }
            }

            if (a.dist(b) < a.getRadius() + b.getRadius()) {
                ab.setType(Point.BLOCKED);
            }
            if (a.dist(c) < a.getRadius() + c.getRadius()) {
                ac.setType(Point.BLOCKED);
            }
            if (b.dist(c) < b.getRadius() + c.getRadius()) {
                bc.setType(Point.BLOCKED);
            }


            Triangle new_t = new Triangle(ab, ac, bc);
            midpoint_triangulation.add(new_t);


        }

        midpoint_triangulation.removeTemporaryPoints();

        return midpoint_triangulation;
    }

    PGraph graphFromMidpoints(Triangulation t, PointList obstacles, EdgeList block_list) {

        EdgeList edges = t.getEdges();
        PointList points = t.getPointList();
        Point start = t.getStart();
        Point goal = t.getGoal();

        PGraph g = new PGraph();

        boolean has_ends = (start != null && goal != null);

        if (has_ends) {
            g.add(start);
            g.add(goal);
        }


        for (Point p : points) {
            if (has_ends && p.getType() == Point.ENTRY) {
                if (no_collision(p, start, block_list) && no_collision(p, start, obstacles)) {
                    g.add(new Edge(p, start));
                }
            }
            if (has_ends && p.getType() == Point.EXIT) {
                if (no_collision(p, goal, block_list) && no_collision(p, goal, obstacles)) {
                    g.add(new Edge(p, goal));
                }
            }
            g.add(p);

        }


        for (Edge e : edges) {
            Point[] pts = e.getPoints();
            if (no_collision(pts[0], pts[1], block_list) && no_collision(pts[0], pts[1], obstacles)) {
                g.add(e);
            }
        }

        return g;
    }


    public Graph getTriangulation(Graph g, int level) throws InterruptedException {

        PointList points = pointListFromUGV(g);

        Triangulation t = delaunayBW(points);
        if (level == 1) {
            EdgeList block_list = edgeListFromUGV(g, points);
            t = delaunayToMidpoints(t, block_list);
            return graphFromMidpoints(t, points, block_list).toUGV();
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
                if (t.isInCircle(p)) {
                    badTriangles.add(t);
                }
            }

            for (Triangle t : badTriangles) {
                triangulation.remove(t);
            }

            EdgeList polygon = badTriangles.getNonDuplicateEdges();


            for (Edge e : polygon) {
                Triangle t = new Triangle(e, p);
                triangulation.add(t);
            }


        }

        triangulation.removeTemporaryPoints();

        return triangulation;
    }


    PointList pointListFromUGV(Graph g) {
        PointList points = new PointList();
        for (int i = 0; i < g.getN(); i++) {
            int type = g.getDomset()[i];
            Point p = new Point(g.getXPos(i), g.getYPos(i), type);

            p.setRadius(parent.getRadius());

            points.add(p);
        }


        return points;
    }

    EdgeList edgeListFromUGV(Graph g, PointList points) {
        EdgeList edges = new EdgeList();

        for (int i = 0; i < points.size(); i++) {
            for (int j = 0; j < points.size(); j++) {
                if (g.isArc(i + 1, j + 1)) {
                    edges.add(new Edge(points.points.get(i), points.points.get(j)));
                }
            }
        }

        return edges;
    }

    Graph ugvFromTriangulation(Triangulation t) {
        EdgeList edges = t.getEdges();
        PointList points = t.getPointList();
        Point start = t.getStart();
        Point goal = t.getGoal();

        Graph g = new Graph(0, 0);

        for (Point p : points) {
            if (p.getType() == Point.ENTRY) {
                edges.add(new Edge(p, start));
            }
            if (p.getType() == Point.EXIT) {
                edges.add(new Edge(p, goal));
            }
            g.addVertex(p.x, p.y);
        }

        if (t.getStart() != null) {
            points.add(start);
            g.addVertex(start.x, start.y);
            g.setDomValue(g.getN() - 1, 1);
        }

        if (t.getGoal() != null) {
            points.add(goal);
            g.addVertex(goal.x, goal.y);
            g.setDomValue(g.getN() - 1, 2);
        }

        for (Edge e : edges) {
            g.addArc(points.idToIndex(e.points[0].getID()) + 1, points.idToIndex(e.points[1].getID()) + 1);
        }

        return g;
    }

    int requestID() {
        return id_counter++;
    }


    boolean collides(Point a, Point b, Edge wall) {
        Point c = wall.getPoints()[0];
        Point d = wall.getPoints()[1];
        double[] pt = new double[]{a.getX(), a.getY(), b.getX(), b.getY(), c.getX(), c.getY(), d.getX(), d.getY()};
        double determinant = (pt[2] - pt[0]) * (pt[7] - pt[5]) - (pt[6] - pt[4]) * (pt[3] - pt[1]);

        if (determinant == 0) {
            return false;
        }

        double lambda = ((pt[7] - pt[5]) * (pt[6] - pt[0]) + (pt[4] - pt[6]) * (pt[7] - pt[1])) / determinant;
        double gamma = ((pt[1] - pt[3]) * (pt[6] - pt[0]) + (pt[2] - pt[0]) * (pt[7] - pt[1])) / determinant;

        return (0 < lambda && lambda < 1) && (0 < gamma && gamma < 1);
    }

    boolean collides(Point a, Point b, Point obstacle) {

        if (obstacle.getType() != Point.OBSTACLE) {
            return false;
        }

        double slope = 1 / a.slope(b);
        double rad = obstacle.getRadius();
        double dir = Math.sqrt(1 + slope * slope);

        double x1, y1, x2, y2;

        if (slope == Double.POSITIVE_INFINITY || slope == Double.NEGATIVE_INFINITY) {
            x1 = obstacle.getX();
            x2 = obstacle.getX();
            y1 = obstacle.getY() + rad;
            y2 = obstacle.getY() - rad;
        } else if (-0.001 < slope && slope < 0.001) {
            x1 = obstacle.getX() + rad;
            x2 = obstacle.getX() - rad;
            y1 = obstacle.getY();
            y2 = obstacle.getY();
        } else {
            x1 = obstacle.getX() + rad / dir;
            y1 = obstacle.getY() + rad * slope / dir;
            x2 = obstacle.getX() - rad / dir;
            y2 = obstacle.getY() - rad * slope / dir;
        }
        Point temp = new Point(x1, y1, 0);
        Point temp2 = new Point(x2, y2, 0);

        return collides(a, b, new Edge(temp, temp2));
    }

    boolean no_collision(Point a, Point b, EdgeList walls) {
        for (Edge e : walls) {
            if (collides(a, b, e)) {
                return false;
            }
        }
        return true;
    }

    boolean no_collision(Point a, Point b, PointList obstacles) {
        for (Point p : obstacles) {
            if (collides(a, b, p)) {
                return false;
            }
        }
        return true;
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
        //double bearing;
        boolean temporary;
        int type;
        double radius;

        static final int OBSTACLE = 0;
        static final int START = 1;
        static final int GOAL = 2;
        static final int BLOCKED = 3;
        static final int OPEN = 4;
        static final int ENTRY = 5;
        static final int EXIT = 6;


        Point(double x, double y, int type) {
            this.x = x;
            this.y = y;
            this.id = requestID();
            this.type = type;
            this.temporary = false;
        }


        double dist(Point p) {
            return Math.sqrt(Math.pow(this.x - p.x, 2) + Math.pow(this.y - p.y, 2));
        }

        double dist(double x, double y) {
            return Math.sqrt(Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2));
        }

        Point midpoint(double x, double y) {
            double mx = (this.x + x) / 2;
            double my = (this.y + y) / 2;

            return new Point(mx, my, OPEN);
        }

        Point midpoint(Point p) {
            return this.midpoint(p.x, p.y);
        }


        double slope(Point p) {
            return (this.y - p.y) / (this.x - p.x);
        }

        double getX() {
            return this.x;
        }

        double getY() {
            return this.y;
        }

        int getID() {
            return this.id;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        void setTemporary() {
            this.temporary = true;
        }

        public double getRadius() {
            return radius;
        }

        public void setRadius(double radius) {
            this.radius = radius;
        }

        boolean same(Point a) {
            return this.id == a.getID();
        }

        boolean same_spot(Point a) {
            double diff_x = Math.abs(a.getX() - this.getX());
            double diff_y = Math.abs(a.getY() - this.getY());

            return (diff_x < POSITION_TOLERANCE && diff_y < POSITION_TOLERANCE);
        }

    }

    private class Edge {
        Point[] points;
        boolean remove;


        Edge(Point a, Point b) {
            points = new Point[2];
            points[0] = a;
            points[1] = b;
        }

        Point[] getPoints() {
            return points;
        }


        void setRemove() {
            this.remove = true;
        }


        boolean same(Point a, Point b) {

            return (a.same(points[0]) && b.same(points[1])) || (b.same(points[0]) && a.same(points[1]));

        }

    }

    private class PGraph {
        EdgeList edges;
        PointList points;

        PGraph() {
            this.edges = new EdgeList();
            this.points = new PointList();
        }

        public void add(Edge e) {
            edges.add(e);
        }

        public void add(Point p) {
            points.add(p);
        }

        Path shortestPath() {

            Path path = new Path();

            int N = points.size();
            if (N < 2) return null;

            double[][] dist_adj = new double[N][N];

            double[] dist = new double[N];
            int[] prev = new int[N];

            boolean[] queue = new boolean[N];

            int start = 0;
            int goal = 0;

            for (Point p : points) {
                if (p.getType() == Point.START) {
                    start = points.idToIndex(p.getID());
                }
                if (p.getType() == Point.GOAL) {
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

            for (Edge e : edges) {
                Point a = e.getPoints()[0];
                Point b = e.getPoints()[1];


                int ai = points.idToIndex(a.getID());
                int bi = points.idToIndex(b.getID());

                if (ai == -1 || bi == -1) {
                    continue;
                }

                dist_adj[ai][bi] = a.dist(b);
                dist_adj[bi][ai] = a.dist(b);
            }

            dist[start] = 0;


            while (true) {
                double minDist = Double.MAX_VALUE;
                int curr = -1;

                for (int i = 0; i < N; i++) {
                    if (queue[i] && dist[i] <= minDist) {
                        curr = i;
                        minDist = dist[i];
                    }
                }

                if (curr == -1 || curr == goal) {
                    break;
                }

                queue[curr] = false;

                for (int i = 0; i < N; i++) {
                    if (queue[i] && dist_adj[curr][i] < Double.MAX_VALUE) {
                        double mid = dist[curr] + dist_adj[curr][i];
                        if (mid < dist[i]) {
                            dist[i] = mid;
                            prev[i] = curr;

                        }
                    }
                }

            }

            int curr = goal;

            while (curr != start) {
                if (curr == -1) {
                    System.out.println("No path exists");
                    return null;
                }

                path.addFirst(points.getFromIndex(curr));
                curr = prev[curr];

            }

            path.addFirst(points.getFromIndex(start));

            return path;
        }


        Graph toUGV() {
            Graph g = new Graph(0, 0);

            for (Point p : points) {
                g.addVertex(p.getX(), p.getY());
                if (p.getType() == Point.START) {
                    g.setDomValue(g.getN() - 1, 1);
                }
                if (p.getType() == Point.GOAL) {
                    g.setDomValue(g.getN() - 1, 2);
                }
            }

            for (Edge e : edges) {
                g.addArc(points.idToIndex(e.points[0].getID()) + 1, points.idToIndex(e.points[1].getID()) + 1);
            }

            return g;

        }


    }

    private class Triangle {

        Point[] points;

        Triangle(Point p1, Point p2, Point p3) {

            points = new Point[3];
            points[0] = p1;
            points[1] = p2;
            points[2] = p3;
        }

        Triangle(Edge e, Point p) {
            points = new Point[3];
            points[0] = p;
            points[1] = e.getPoints()[0];
            points[2] = e.getPoints()[1];
        }


        boolean isInCircle(Point p) {
            //get slopes of edges of triangle
            double m1 = points[0].slope(points[1]);
            double m2 = points[0].slope(points[2]);
            //get midpoints of the same edges of the triangle
            Point ab = points[0].midpoint(points[1]);
            Point ac = points[0].midpoint(points[2]);

            if (m1 == Double.POSITIVE_INFINITY) m1 = Double.MAX_VALUE / 8;
            if (m1 == Double.NEGATIVE_INFINITY) m1 = -1 * Double.MAX_VALUE / 8;
            if (m1 < 0.001 && m1 > -0.001) m1 = 0.001;
            if (m2 == Double.POSITIVE_INFINITY) m2 = Double.MAX_VALUE / 8;
            if (m2 == Double.NEGATIVE_INFINITY) m2 = -1 * Double.MAX_VALUE / 8;
            if (m2 < 0.001 && m2 > -0.001) m2 = 0.001;


            double x1 = ab.getX();
            double y1 = ab.getY();
            double x2 = ac.getX();
            double y2 = ac.getY();


            //get intersection of the lines generated from slopes and midpoints
            double circ_x = (1 / (1 / m2 - 1 / m1)) * (y2 - y1 - (x1 / m1) + (x2 / m2));
            double circ_y = (-1 / m2) * (circ_x - x2) + y2;


            double rad = points[0].dist(circ_x, circ_y);

            return p.dist(circ_x, circ_y) < rad;
        }

        boolean hasTemporary() {
            for (int i = 0; i < 3; i++) {
                if (points[i].temporary) {
                    return true;
                }
            }
            return false;
        }

        Point[] getPoints() {
            return points;
        }


    }

    private class PointList implements Iterable<Point> {
        ArrayList<Point> points;

        PointList() {
            points = new ArrayList<>();
        }

        void add(Point p) {
            points.add(p);
        }

        int size() {
            return points.size();
        }

        int idToIndex(int id) {

            for (int i = 0; i < points.size(); i++) {
                if (points.get(i).getID() == id) {
                    return i;
                }
            }
            return -1;
        }

        Point getFromIndex(int index) {
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

    private class EdgeList implements Iterable<Edge> {

        ArrayList<Edge> edges;

        EdgeList() {
            edges = new ArrayList<>();
        }

        void add(Edge e) {
            edges.add(e);
        }

        void removeMarked() {
            edges.removeIf(e -> e.remove);
        }


        @Override
        public Iterator<Edge> iterator() {
            return edges.iterator();
        }
    }

    private class Triangulation implements Iterable<Triangle> {

        ArrayList<Triangle> triangles;
        Point start;
        Point goal;

        Triangulation() {
            triangles = new ArrayList<>();
        }

        void addTemporaryHull(PointList points) {
            double xmin = Double.POSITIVE_INFINITY;
            double xmax = Double.NEGATIVE_INFINITY;
            double ymin = Double.POSITIVE_INFINITY;
            double ymax = Double.NEGATIVE_INFINITY;

            for (Point p : points) {
                double x = p.getX();
                double y = p.getY();

                if (x < xmin) xmin = x;
                if (y < ymin) ymin = y;
                if (x > xmax) xmax = x;
                if (y > ymax) ymax = y;


            }

            double xoffset = (xmax - xmin) / 10;
            double yoffset = (ymax - ymin) / 10;

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

        void removeTemporaryPoints() {
            ArrayList<Triangle> to_remove = new ArrayList<>();

            for (Triangle t : triangles) {
                if (t.hasTemporary()) {
                    to_remove.add(t);
                }
            }

            for (Triangle t : to_remove) {
                triangles.remove(t);
            }
        }


        void add(Triangle t) {
            triangles.add(t);
        }

        void remove(Triangle t) {
            triangles.remove(t);
        }

        EdgeList getEdges() {
            EdgeList edges = new EdgeList();

            for (Triangle t : this) {
                Point a = t.getPoints()[0];
                Point b = t.getPoints()[1];
                Point c = t.getPoints()[2];

                boolean ab = true;
                boolean ac = true;
                boolean bc = true;

                for (Edge e : edges) {
                    if (e.same(a, b)) {
                        ab = false;
                    } else if (e.same(a, c)) {
                        ac = false;
                    } else if (e.same(b, c)) {
                        bc = false;
                    }
                }

                if (a.getType() == Point.BLOCKED) {
                    ab = false;
                    ac = false;
                }

                if (b.getType() == Point.BLOCKED) {
                    ab = false;
                    bc = false;
                }

                if (c.getType() == Point.BLOCKED) {
                    ac = false;
                    bc = false;
                }

                if (ab) edges.add(new Edge(a, b));
                if (ac) edges.add(new Edge(a, c));
                if (bc) edges.add(new Edge(b, c));
            }

            return edges;


        }

        EdgeList getNonDuplicateEdges() {

            EdgeList edges = new EdgeList();

            for (Triangle t : this) {
                Point a = t.getPoints()[0];
                Point b = t.getPoints()[1];
                Point c = t.getPoints()[2];

                boolean ab = true;
                boolean ac = true;
                boolean bc = true;

                for (Edge e : edges) {
                    if (e.same(a, b)) {
                        e.setRemove();
                        ab = false;
                    }
                    if (e.same(a, c)) {
                        e.setRemove();
                        ac = false;
                    }
                    if (e.same(b, c)) {
                        e.setRemove();
                        bc = false;
                    }
                }

                if (ab) {
                    edges.add(new Edge(a, b));

                }
                if (ac) {
                    edges.add(new Edge(a, c));
                }
                if (bc) {
                    edges.add(new Edge(b, c));
                }
            }

            edges.removeMarked();

            return edges;
        }

        PointList getPointList() {
            PointList points = new PointList();

            for (Triangle t : this) {
                Point[] pts = t.points;

                for (Point pt : pts) {
                    boolean exists = false;
                    for (Point p : points) {

                        if (pt.same(p)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
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

        Path() {
            points = new LinkedList<>();
        }

        void addFirst(Point p) {
            points.addFirst(p);
        }

        void addLast(Point p) {
            points.addLast(p);
        }


        Path trimToObstacles(PointList obstacles, EdgeList walls) {
            Path trimmed = new Path();

            Point curr = this.points.getFirst();
            Point goal = this.points.getLast();


            while (curr != goal) {

                Iterator<Point> back_iterator = points.descendingIterator();
                Point mid = back_iterator.next();

                trimmed.addLast(curr);

                boolean changed = false;

                while (mid != curr) {
                    if (no_collision(curr, mid, obstacles) && no_collision(curr, mid, walls)) {
                        curr = mid;
                        changed = true;
                        break;
                    }
                    mid = back_iterator.next();
                }

                if (!changed) {
                    System.out.println("No path exists");
                    Iterator<Point> it = this.points.iterator();
                    Point temp = it.next();
                    while (temp != curr) {
                        temp = it.next();
                    }

                    while (it.hasNext()) {
                        trimmed.addLast(it.next());
                    }

                    return trimmed;
                }

            }
            trimmed.addLast(goal);


            return trimmed;
        }


        PGraph toGraph() {
            PGraph graph = new PGraph();

            if (points.isEmpty()) return null;

            Point last = points.getFirst();

            for (Point p : points) {
                graph.add(p);
                if (p.same(last)) {
                    continue;
                }
                graph.add(new Edge(last, p));
                last = p;
            }

            return graph;


        }


    }

}

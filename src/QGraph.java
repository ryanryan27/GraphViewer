import java.util.ArrayList;

public class QGraph {

    ArrayList<QVertex> vertices;
    ArrayList<QEdge> edges;

    int N;

    public QGraph(int N){
        this.N = N;

        vertices = new ArrayList<>(N);
        edges = new ArrayList<>(N);

    }


    public int addVertex(QVertex vertex){
        vertex.id = N+1;
        vertices.add(vertex);
        N++;
        return N;
    }

    public int addVertex(double x, double y){
        QVertex vertex = new QVertex(x, y);
        return addVertex(vertex);
    }

    public QVertex getVertex(int id){
        return vertices.get(id);
    }

    public void deleteVertex(int id){
        QVertex vertex = vertices.get(id);
        edges.removeIf(edge -> edge.source == vertex || edge.target == vertex);

        vertices.remove(vertex);

        N--;

        for(int i = 0; i < N; i++){
            QVertex vert = vertices.get(i);
            vert.id = i;
        }

    }

    public void addEdge(int v1, int v2){
        QEdge edge = new QEdge(vertices.get(v1), vertices.get(v2));
        edges.add(edge);
    }

    public void deleteEdge(int v1, int v2){
        edges.removeIf(edge -> vertices.get(v1) == edge.source && vertices.get(v2) == edge.target);
    }

    public boolean isEdge(int v1, int v2){
        for (QEdge edge : edges){
            if(edge.source == vertices.get(v1) && edge.target == vertices.get(v2)){
                return true;
            }
        }

        return false;
    }

    public QEdge getEdge(int v1, int v2){
        for (QEdge edge : edges){
            if(edge.source == vertices.get(v1) && edge.target == vertices.get(v2)){
                return edge;
            }
        }
        return null;
    }

    public void swapVertices(int v1, int v2){
        QVertex temp = vertices.get(v1);

        vertices.set(v1, vertices.get(v2));
        vertices.get(v1).id = v1;

        temp.id = v2;
        vertices.set(v2, temp);
    }

    public void setPosition(int vertex, double x, double y){
        QVertex vert = vertices.get(vertex);
        vert.x = x;
        vert.y = y;
    }

    public void select(int vertex){
    vertices.get(vertex).selected = true;
    }

    public void deselect(int vertex){
        vertices.get(vertex).selected = false;
    }

    public boolean isSelected(int vertex){
        return vertices.get(vertex).selected;
    }

    public void setDominationValue(int vertex, int value){
        vertices.get(vertex).guards = value;
    }

    public int getDominationValue(int vertex){
        return vertices.get(vertex).guards;
    }

    public int[] getDominationValues(){
        int[] dominationValues = new int[N];

        for (int i = 0; i < N; i++) {
            dominationValues[i] = vertices.get(i).guards;
        }

        return dominationValues;
    }

    public void setDominationValues(int[] dominationValues){
        for (int i = 0; i < N; i++) {
            vertices.get(i).guards =dominationValues[i];
        }
    }

    public void toggleDominationValue(int vertex){
        if(vertices.get(vertex).guards < 2){
            vertices.get(vertex).guards++;
        }
        else{
            vertices.get(vertex).guards = 0;
        }
    }

    public double[] getCentroid(){
        double x = 0;
        double y = 0;

        for (int i = 0; i < N; i++) {
            x += vertices.get(i).x;
            y += vertices.get(i).y;
        }

        x = x/N;
        y = y/N;

        return new double[]{x, y};
    }

    public double[] getCentroid(boolean[] vertexList){

        double x = 0;
        double y = 0;

        int count = 0;

        for (int i = 0; i < N; i++) {
            if(vertexList[i]) {
                count++;
                x += vertices.get(i).x;
                y += vertices.get(i).y;
            }
        }

        x = x/count;
        y = y/count;

        return new double[]{x, y};
    }

    public int vertexEdgeDistance(int v1, int v2){
        return 1;
    }

    public double vertexEuclideanDistance(int v1, int v2){
        return 0.2;
    }

    public QGraph getSubgraph(boolean[] vertices, boolean align){
        return new QGraph(N);
    }

    public QGraph getSubgraph(int[] vertices, boolean align){
        return new QGraph(N);
    }

    public QGraph getSubgraphSelected(boolean align){
        return new QGraph(N);
    }

    public void addSubgraph(QGraph subgraph){

    }

    public void addSubgraph(QGraph subgraph, double xOffset, double yOffset, double scale){

    }

    public void alignTopLeft(){

    }

    public void alignTopLeft(double xOffset, double yOffset, double scale){

    }

    public double[] getBoundingBox(){
        return new double[4];
    }

    public void rescale(double scale){

    }

    public void rescaleSelected(double scale){

    }

    public void rescaleList(double scale, boolean toRescale){

    }

    public QGraph getCopy(){
        return new QGraph(N);
    }

    public void alignToGrid(double spacing, double xOffset, double yOffset){

    }

    public void alignToGrid(boolean[] toAlign, double spacing, double xOffset, double yOffset){

    }

}

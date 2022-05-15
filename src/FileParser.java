import java.io.*;
import java.util.ArrayList;
import java.util.Stack;
import java.util.StringTokenizer;

public class FileParser {

    static final int FILE_GML = 0;
    static final int FILE_UGV = 1;
    static final int FILE_EDGE_LIST = 2;
    static final int FILE_G6 = 3;
    static final int FILE_ASC = 4;
    static final int FILE_HCP = 5;
    static final int FILE_SCD = 6;


    public GraphData[] parseGML(File file){

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            ArrayList<Vertex> vertices = new ArrayList<>();
            ArrayList<Edge> edges = new ArrayList<>();

            Stack<Character> stack = new Stack<>();
            int g_count = -1;

            while(br.ready()){
                String[] tokens = br.readLine().trim().split("\\s+");
                switch (tokens[0]){
                    case "graph":
                        if(stack.empty()) {
                            g_count++;
                            stack.push('g');
                        }
                        break;
                    case "node":
                        if(!stack.empty() && stack.peek() == 'g') {
                            vertices.add(new Vertex(g_count));
                            stack.push('v');
                        }
                        break;
                    case "edge":
                        if(!stack.empty() && stack.peek() == 'g') {
                            edges.add(new Edge(g_count));
                            stack.push('e');
                        }
                        break;
                    case "]":
                        stack.pop();
                        break;
                    case "x":
                        if(!stack.empty() && stack.peek() == 'v'){
                            vertices.get(vertices.size()-1).x = Double.parseDouble(tokens[1]);
                        }
                        break;
                    case "y":
                        if(!stack.empty() && stack.peek() == 'v'){
                            vertices.get(vertices.size()-1).y = Double.parseDouble(tokens[1]);
                        }
                        break;
                    case "weight":
                        if(!stack.empty() && stack.peek() == 'v'){
                            vertices.get(vertices.size()-1).dominating = (int)Double.parseDouble(tokens[1]);
                        }
                        break;
                    case "source":
                        if(!stack.empty() && stack.peek() == 'e'){
                            edges.get(edges.size()-1).source = Integer.parseInt(tokens[1]);
                        }
                        break;
                    case "target":
                        if(!stack.empty() && stack.peek() == 'e'){
                            edges.get(edges.size()-1).target = Integer.parseInt(tokens[1]);
                        }
                        break;
                    case "id":
                        if(!stack.empty() && stack.peek() == 'v'){
                            vertices.get(vertices.size()-1).id = Integer.parseInt(tokens[1]);
                        }
                        break;
                }



            }

            vertices.sort(Vertex::compareTo);

            GraphData[] graphs = new GraphData[g_count + 1];

            for (int i = 0; i <= g_count; i++) {
                graphs[i] = new GraphData(new Graph(0,0));
            }

            for (Vertex v: vertices) {
                Graph g = graphs[v.graph].graph;

                if(v.id != g.getN()){
                    throw new Exception("bad vertex ordering");
                }

                g.addVertex(v.x,v.y);
                g.setDomValue(g.getN()-1,v.dominating);
            }

            for (Edge e: edges){
                Graph g = graphs[e.graph].graph;

                g.addEdge(e.source+1, e.target+1);
            }

            br.close();
            return graphs;


        } catch (Exception e){
            System.out.println(e);
            System.out.println("gml not formatted properly");
        }


        return new GraphData[0];
    }

    public void saveGML(Graph[] graphs, File file, boolean append){

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, append));

            for (Graph graph: graphs) {
                int N = graph.getN();

                writer.newLine();
                writer.write("graph [");
                writer.newLine();

                for (int i = 0; i < N; i++) {
                    writer.write(" node [");
                    writer.newLine();

                    writer.write("  id " + i);
                    writer.newLine();

                    writer.write("  x " + graph.getXPos(i));
                    writer.newLine();

                    writer.write("  y " + graph.getYPos(i));
                    writer.newLine();

                    writer.write("  weight " + graph.inDomset(i+1));
                    writer.newLine();

                    writer.write(" ]");
                    writer.newLine();
                }

                for (int i = 0; i < N; i++) {
                    int deg = graph.getDegrees()[i];

                    for (int j = 0; j < deg; j++) {
                        int target = graph.getArcs()[i][j]-1;

                        if(target > i){
                            writer.write( "edge [");
                            writer.newLine();

                            writer.write("  source " + i);
                            writer.newLine();

                            writer.write("  target " + target);
                            writer.newLine();

                            writer.write(" ]");
                            writer.newLine();
                        }
                    }
                }


                writer.write("]");
                writer.newLine();
            }

            writer.close();


        } catch (Exception ex){
            System.err.println(ex);
            System.out.println("Bad file.");
        }


    }

    public GraphData[] parseUGV(File file){
        GraphData[] graphs = new GraphData[0];

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            int g_count = Integer.parseInt(br.readLine());

            graphs = new GraphData[g_count];

            for (int i = 0; i < g_count; i++) {
                graphs[i] = new GraphData(new Graph(0,0));
            }


            for (int gr = 0; gr < g_count; gr++) {

                int N = Integer.parseInt(br.readLine());
                String line = br.readLine();
                StringTokenizer tokens = new StringTokenizer(line);
                double scale = Double.parseDouble(tokens.nextToken());
                tokens.nextToken(); //skip yScale
                int xTopLeft = Integer.parseInt(tokens.nextToken());
                int yTopLeft = Integer.parseInt(tokens.nextToken());
                int radius = Integer.parseInt(tokens.nextToken());
                String xPosesString = br.readLine();
                String yPosesString = br.readLine();
                StringTokenizer xTokens = new StringTokenizer(xPosesString);
                StringTokenizer yTokens = new StringTokenizer(yPosesString);
                double[] xPos = new double[N];
                double[] yPos = new double[N];
                for (int i = 0; i < N; i++) {
                    xPos[i] = Double.parseDouble(xTokens.nextToken());
                    yPos[i] = Double.parseDouble(yTokens.nextToken());
                }
                int[] degrees = new int[N];
                int maxDegree = 0;
                line = br.readLine();
                tokens = new StringTokenizer(line);
                for (int i = 0; i < N; i++) {
                    degrees[i] = Integer.parseInt(tokens.nextToken());
                    if (degrees[i] > maxDegree) {
                        maxDegree = degrees[i];
                    }
                }

                Graph graph = graphs[gr].graph;
                graph.setN(N);

                for (int i = 0; i < N; i++) {
                    String arcsString = br.readLine();
                    tokens = new StringTokenizer(arcsString);
                    for (int j = 0; j < degrees[i]; j++) {
                        graph.addArc(i + 1, Integer.parseInt(tokens.nextToken()));
                    }
                }

                br.readLine(); // Should be -1


                for (int i = 0; i < N; i++) {
                    graph.setXPos(i, xPos[i]);
                    graph.setYPos(i, yPos[i]);
                }

                graphs[gr].scale = scale;
                graphs[gr].x_offset = xTopLeft;
                graphs[gr].y_offset = yTopLeft;
                graphs[gr].radius = radius;

            }

        } catch (Exception e) {
            System.err.println(e);
        }


        return graphs;
    }


    public GraphData[] parseGraph6(File file){
        GraphData[] graphs = new GraphData[0];

        int maxNode = 0;

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String graphLine = br.readLine();
            int g6count = 0;
            while (graphLine != null) {
                g6count++;
                graphLine = br.readLine();
            }
            br.close();

            graphs = new GraphData[g6count];

            for (int i = 0; i < g6count; i++) {
                graphs[i] = new GraphData(new Graph(0,0));
            }

            br = new BufferedReader(new FileReader(file));
            for (int gr = 0; gr < g6count; gr++) {
                graphLine = br.readLine();

                int lineIndex = 0;

                int headerAscii = graphLine.charAt(lineIndex++);

                if (headerAscii == 126) {
                    int headerAscii2 = graphLine.charAt(lineIndex++);
                    if (headerAscii2 == 126) {
                        System.out.println("UGV does not support graphs of this size.");
                    } else {

                        int headerAscii3 = graphLine.charAt(lineIndex++);
                        int headerAscii4 = graphLine.charAt(lineIndex++);
                        String binaryString = intToBinary(headerAscii2 - 63) + intToBinary(headerAscii3 - 63) + intToBinary(headerAscii4 - 63);

                        maxNode = binaryToInt(binaryString);
                    }
                } else {
                    maxNode = headerAscii - 63;
                }

                String graphString = "";
                for (int i = lineIndex; i < graphLine.length(); i++) {
                    graphString += intToBinary(((int) graphLine.charAt(i)) - 63);
                }

                Graph graph = graphs[gr].graph;
                graph.setN(maxNode);

                int arcCount = 0;
                for (int i = 1; i < maxNode; i++) {
                    for (int j = 0; j < i; j++) {
                        if (graphString.charAt(arcCount++) == '1') {
                            graph.addArc(i + 1, j + 1);
                            graph.addArc(j + 1, i + 1);
                        }
                    }
                }

                graph.createCircle();

            }
            br.close();


        } catch (Exception e) {
            System.err.println(e);
        }



        return graphs;
    }




    public String intToBinary(int number) {
        // Assumes number will be less than 64, the following is for testing purposes only!!!
        if (number > 64)
            System.out.println("NUMBER IS WRONG!!");

        String binary = "";

        for (int i = 5; i >= 0; i--)
            if (number >= Math.pow(2, i)) {
                binary += "1";
                number -= Math.pow(2, i);
            } else
                binary += "0";

        return binary;
    }

    public int binaryToInt(String binary) {
        int number = 0;
        for (int i = 1; i <= binary.length(); i++)
            number += Math.pow(2, Integer.parseInt("" + binary.charAt(binary.length() - i)));

        return number;
    }


    private class Edge {
        int graph;
        int source = -1;
        int target = -1;

        Edge(int graph){
            this.graph = graph;
        }

    }

    private class Vertex implements Comparable<Vertex>{
        int graph;
        int id = -1;
        double x = 0;
        double y = 0;
        int dominating = 0;

        Vertex(int graph){
            this.graph = graph;
        }

        @Override
        public int compareTo(Vertex v) {

            if(this.graph == v.graph){
                return this.id - v.id;
            } else {
                return this.graph - v.graph;
            }
        }


    }


}

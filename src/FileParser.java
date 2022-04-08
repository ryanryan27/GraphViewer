import java.io.*;
import java.util.ArrayList;
import java.util.Stack;

public class FileParser {

    public Graph[] parseGML(File file){

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            ArrayList<Vertex> vertices = new ArrayList<>();
            ArrayList<Edge> edges = new ArrayList<>();

            Stack<Character> stack = new Stack<>();
            int g_count = -1;

            while(br.ready()){
                String[] tokens = br.readLine().trim().split("\\s+");
                //System.out.println(tokens[0]);
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

            Graph[] graphs = new Graph[g_count + 1];

            for (int i = 0; i <= g_count; i++) {
                graphs[i] = new Graph(0,0);
            }

            for (Vertex v: vertices) {
                Graph g = graphs[v.graph];

                if(v.id != g.getN()){
                    throw new Exception("bad vertex ordering");
                }

                g.addVertex(v.x,v.y);
                g.setDomValue(g.getN()-1,v.dominating);
            }

            for (Edge e: edges){
                Graph g = graphs[e.graph];

                g.addEdge(e.source+1, e.target+1);
            }

            br.close();
            return graphs;


        } catch (Exception e){
            System.out.println(e);
            System.out.println("gml not formatted properly");
        }


        return new Graph[0];
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

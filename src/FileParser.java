import javax.swing.*;
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

    public void saveGML(GraphData[] graphs, File file, boolean append){

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, append));

            for (GraphData graphData: graphs) {
                Graph graph = graphData.graph;
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

    public void saveUGV(GraphData[] graphs, File file, boolean append){
        try {
            int newGraphs = graphs.length;

            BufferedWriter bw;

            //DataOutputStream os;
            if (append) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                bw = new BufferedWriter(new FileWriter(file.getName() + ".temp"));

                String line = br.readLine();
                long graph_count = Long.parseLong(line);
                bw.write((graph_count + newGraphs) + "");
                bw.newLine();

                for (int i = 0; i < graph_count; i++) {
                    int N = Integer.parseInt(br.readLine());
                    bw.write(N + "");
                    bw.newLine();
                    // xScale, yScale, xTopLeft, yTopLeft, radius
                    bw.write(br.readLine());
                    bw.newLine();
                    // xPoses, yPoses
                    bw.write(br.readLine());
                    bw.newLine();
                    bw.write(br.readLine());
                    bw.newLine();

                    // degrees
                    bw.write(br.readLine());
                    bw.newLine();
                    for (int j = 0; j < N; j++) // arcs
                    {
                        bw.write(br.readLine());
                        bw.newLine();
                    }
                    bw.write("-1");
                    bw.newLine();
                    br.readLine();
                }
                br.close();
            } else {
                bw = new BufferedWriter(new FileWriter(file));
            }

            if (!append) {
                bw.write(newGraphs + "");
                bw.newLine();
            }

            for (GraphData graphData : graphs) {
                Graph graph = graphData.graph;


                bw.write(graph.getN() + "");
                bw.newLine();
                bw.write(graphData.scale + " " + graphData.scale + " " + graphData.x_offset + " " + graphData.y_offset + " " + graphData.radius);
                bw.newLine();

                String xPosesString = "";
                String yPosesString = "";
                for (int i = 0; i < graph.getN(); i++) {
                    if (i == 0) {
                        xPosesString = (graph.getXPos(0) + "");
                        yPosesString = (graph.getYPos(0) + "");
                    } else {
                        xPosesString += (" " + graph.getXPos(i));
                        yPosesString += (" " + graph.getYPos(i));
                    }
                }

                bw.write(xPosesString);
                bw.newLine();
                bw.write(yPosesString);
                bw.newLine();

                int[] degrees = graph.getDegrees();
                String degreesString = "";
                for (int i = 0; i < degrees.length; i++) {
                    if (i == 0) {
                        degreesString = (degrees[0] + "");
                    }
                    else {
                        degreesString += (" " + degrees[i]);
                    }
                }

                bw.write(degreesString);
                bw.newLine();

                int[][] arcs = graph.getArcs();
                for (int i = 0; i < degrees.length; i++) {
                    String arcsString = "";
                    for (int j = 0; j < degrees[i]; j++) {
                        if (j == 0) {
                            arcsString = (arcs[i][0] + "");
                        }
                        else {
                            arcsString += (" " + arcs[i][j]);
                        }
                    }

                    bw.write(arcsString);
                    bw.newLine();
                }

                bw.write("-1");
                bw.newLine();
            }
            bw.close();

            if (append) {
                File newFile = new File(file.getName() + ".temp");
                if (newFile.exists()) {
                    file.delete();
                    newFile.renameTo(file);
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }
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

    public void saveGraph6(GraphData[] graphs, File file, boolean append){
        try {
            for (GraphData graphData : graphs) {
                BufferedWriter bw = new BufferedWriter(new FileWriter(file, append));
                Graph graph = graphData.graph;

                if (append) bw.newLine();

                String line = "";

                if (graph.getN() < 63) {
                    line += (char) (graph.getN() + 63);
                } else if (graph.getN() < 258048) {
                    line += (char) (126);

                    int number = graph.getN();

                    String binary = "";
                    for (int i = 17; i >= 0; i--)
                        if (number > Math.pow(2, i)) {
                            number -= (int) Math.pow(2, i);
                            binary += "1";
                        } else
                            binary += "0";

                    for (int i = 0; i < 3; i++) {
                        String sixBinary = binary.substring(6 * i, 6 * (i + 1));
                        int binaryNumber = binaryToInt(sixBinary);
                        line += (char) (binaryNumber + 63);
                    }
                } else {
                    System.out.println("UGV does not support graphs of this size.");
                }

                int input = 0;
                int count = 0;
                for (int i = 0; i < graph.getN(); i++)
                    for (int j = 0; j < i; j++) {
                        count++;
                        if (graph.isArc(i + 1, j + 1))
                            input += (int) Math.pow(2, 6 - count);

                        if (count == 6) {
                            line += (char) (63 + input);
                            count = 0;
                            input = 0;
                        }
                    }

                if (count > 0)
                    line += (char) (63 + input);
                bw.write(line);
                bw.close();

                append = true;
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public GraphData[] parseSCD(File file){
        GraphData[] graphs = new GraphData[0];

        DataInputStream di = null;


        int[] scdData = getSCDData(file);

        int maxNode = scdData[0];
        int degree = scdData[1];

        int graphsToDo = scdData[2];
        if (maxNode != 0) {
            int[] degrees = new int[257];
            int read;
            int node;

            try {
                di = new DataInputStream(new FileInputStream(file));
            } catch (Exception e) {
                System.err.println(e);
            }


            graphs = new GraphData[graphsToDo];

            for (int i = 0; i < graphsToDo; i++) {
                graphs[i] = new GraphData(new Graph(0,0));
            }


            for (int gr = 0; gr < graphsToDo; gr++) {

                Graph graph = graphs[gr].graph;
                graph.setN(maxNode);
                graph.setMaxDegree(degree);


                readStream(di);
                for (int i = 0; i < maxNode; i++)
                    degrees[i] = 0;
                node = 1;
                for (int i = 0; i < maxNode * degree / 2; i++) {
                    read = readStream(di);
                    degrees[node - 1]++;
                    degrees[read - 1]++;
                    graph.addArc(node, read);
                    graph.addArc(read, node);
                    while (degrees[node - 1] >= degree)
                        node++;
                }

                graph.createCircle();

            }
        }

        return graphs;
    }

    public void saveSCD(GraphData[] graphs, File file, boolean append){

        Graph graph = graphs[0].graph;
        int[] degrees = graph.getDegrees();
        int degree = degrees[0];
        int maxNode = graph.getN();

        try {
            int[] stream = new int[maxNode * degree / 2];
            if (append) {
                int[] scdData = getSCDData(file);
                if (scdData[0] != maxNode || scdData[1] != degree) {
                    System.err.println(file.getName() + " contains graphs of size " + scdData[0] + " and degree " + scdData[1] + " which are incompatible with graphs of size " + maxNode + " and degree " + degree + ".");
                    return;

                }

                DataInputStream dis = new DataInputStream(new FileInputStream(file));

                int graphsToDo = scdData[2];

                for (int graphcount = 0; graphcount < graphsToDo; graphcount++) {
                    int index = readStream(dis);
                    for (int i = index; i < maxNode * degree / 2; i++) {
                        stream[i] = readStream(dis);
                    }
                }

                dis.close();
            }
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(file, append));
            for (GraphData graphData : graphs) {
                graph = graphData.graph;

                int[] newStream = new int[maxNode * degree / 2];
                int[][] arcs = graph.getArcs();
                int count = 0;

                for (int i = 0; i < maxNode; i++) {
                    for (int j = 0; j < degrees[i]; j++) {
                        if (arcs[i][j] > i + 1) {
                            newStream[count++] = arcs[i][j];
                        }
                    }
                }

                int repeat = 0;
                int index = 0;
                while (index < maxNode * degree / 2 && newStream[index] == stream[index++]) {
                    repeat++;
                }

                dos.writeByte(repeat);
                for (int i = repeat; i < maxNode * degree / 2; i++) {
                    dos.writeByte(newStream[i]);
                }

                stream = newStream;
            }
            dos.close();


        } catch (Exception e) {
            System.err.println(e);
        }

    }

    public GraphData[] parseHCP(File file){
        GraphData[] graphs = new GraphData[0];

        int[][] arcs;
        int maxNode;
        int maxDegree;
        int count;

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;
            br.readLine();
            br.readLine();
            br.readLine();
            line = br.readLine();

            maxNode = Integer.parseInt(line.substring(line.indexOf(":") + 2));

            br.readLine();
            br.readLine();
            line = br.readLine();

            count = 0;

            while (!line.contains("-1")) {
                count++;
                line = br.readLine();
            }
            br.close();

            br = new BufferedReader(new FileReader(file));

            arcs = new int[count][2];

            br.readLine();
            br.readLine();
            br.readLine();
            br.readLine();
            br.readLine();
            br.readLine();
            line = br.readLine();

            count = 0;
            while (!line.contains("-1")) {
                StringTokenizer tokens = new StringTokenizer(line);
                arcs[count][0] = Integer.parseInt(tokens.nextToken());
                arcs[count++][1] = Integer.parseInt(tokens.nextToken());
                line = br.readLine();
            }

            br.close();

            int[] degrees = new int[maxNode];
            for (int i = 0; i < count; i++) {
                degrees[arcs[i][0] - 1]++;
            }

            maxDegree = 0;
            for (int i = 0; i < maxNode; i++) {
                if (degrees[i] > maxDegree) {
                    maxDegree = degrees[i];
                }
            }

            graphs = new GraphData[]{new GraphData(new Graph(maxNode,maxDegree))};

            Graph graph = graphs[0].graph;
            graph.addArcs(arcs);

            for (int i = 0; i < count; i++) {
                boolean both = false;
                for (int j = 0; j < count; j++) {
                    if (arcs[i][0] == arcs[j][1] && arcs[i][1] == arcs[j][0]) {
                        both = true;
                        break;
                    }
                }
                if (!both) {
                    graph.addArc(arcs[i][1], arcs[i][0]);
                }
            }

            graph.createCircle();

        } catch (Exception e) {
            System.err.println(e);
        }

        return graphs;
    }

    public GraphData[] parseEdgeList(File file){
        GraphData[] graphs = new GraphData[0];

        int[][] arcs;
        int maxNode;
        int maxDegree;
        int count;

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line = br.readLine();
            count = 0;

            while (line != null) {
                count++;
                line = br.readLine();
            }
            br.close();

            br = new BufferedReader(new FileReader(file));

            arcs = new int[count][2];

            line = br.readLine();

            count = 0;
            while (line != null) {
                StringTokenizer tokens = new StringTokenizer(line);
                arcs[count][0] = Integer.parseInt(tokens.nextToken());
                arcs[count++][1] = Integer.parseInt(tokens.nextToken());
                line = br.readLine();
            }

            br.close();

            maxNode = 0;

            for (int i = 0; i < count; i++)
                for (int j = 0; j < 2; j++)
                    if (arcs[i][j] > maxNode)
                        maxNode = arcs[i][j];


            int[] degrees = new int[maxNode];
            for (int i = 0; i < count; i++)
                degrees[arcs[i][0] - 1]++;

            maxDegree = 0;
            for (int i = 0; i < maxNode; i++)
                if (degrees[i] > maxDegree)
                    maxDegree = degrees[i];


            graphs = new GraphData[]{new GraphData(new Graph(maxNode,maxDegree))};

            Graph graph = graphs[0].graph;
            graph.addArcs(arcs);


            for (int i = 0; i < count; i++) {
                boolean both = false;
                for (int j = 0; j < count; j++) {
                    if (arcs[i][0] == arcs[j][1] && arcs[i][1] == arcs[j][0]) {
                        both = true;
                        break;
                    }
                }
                if (!both) {

                    graph.addArc(arcs[i][1], arcs[i][0]);
                }
            }


            graph.createCircle();

        } catch (Exception e) {
            System.err.println(e);
        }



        return graphs;
    }


    public GraphData[] parseASC(File file){
        GraphData[] graphs = new GraphData[0];


        try {
            int graphsToDo = 0;


            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            while (line != null) {
                if (line.startsWith("Graph")) {
                    graphsToDo++;
                }
                line = br.readLine();
            }

            graphs = new GraphData[graphsToDo];

            for (int i = 0; i < graphsToDo; i++) {
                graphs[i] = new GraphData(new Graph(0,0));
            }


            for (int gr = 0; gr < graphsToDo; gr++) {

                Graph graph = graphs[gr].graph;

                int node = 0;
                br = new BufferedReader(new FileReader(file));
                line = br.readLine();
                while (!line.startsWith("Graph " + (gr+1))) {
                    line = br.readLine();
                }

                br.readLine();
                line = br.readLine();
                int count = 0;
                while (!line.startsWith("Taillenweite")) {
                    count++;
                    if (count > node) {
                        node = count;
                        graph.setN(node);
                    }
                    StringTokenizer tokens = new StringTokenizer(line);
                    tokens.nextToken();
                    tokens.nextToken();
                    while (tokens.hasMoreTokens()) {
                        int newNode = Integer.parseInt(tokens.nextToken());
                        if (newNode > node) {
                            node = newNode;
                            graph.setN(node);
                        }
                        graph.addArc(count, newNode);
                    }

                    line = br.readLine();
                }

                int[] contour = new int[node];
                for (int i = 0; i < node; i++) {
                    contour[i] = i;
                }
                graph.setContour(contour);
                graph.createCircle();

            }


        } catch (Exception e) {
            System.err.println(e);
        }

        return graphs;
    }

    private int[] getSCDData(File file) {
        DataInputStream di = null;
        try {
            di = new DataInputStream(new FileInputStream(file));
        } catch (Exception e) {
            System.err.println(e);
        }
        int degree = 1;
        int graphsToDo = 0;

        int[] degrees = new int[257];
        readStream(di);
        int oldRead = readStream(di);
        int maxNode = oldRead;
        degrees[oldRead - 1]++;
        int read = readStream(di);
        while (read > oldRead) {

            maxNode = read;
            degrees[0]++;
            degrees[read - 1]++;
            degree++;
            oldRead = read;
            read = readStream(di);
        }

        degrees[0] = degree;

        boolean graphFinished = false;
        int node = 2;
        boolean needToStart = true;
        while (needToStart) {
            needToStart = false;
            while (!graphFinished) {
                degrees[node - 1]++;
                degrees[read - 1]++;
                if (read > maxNode) {
                    maxNode = read;
                }

                graphFinished = true;
                for (int i = 0; i < maxNode; i++) {
                    if (degrees[i] != degree) {
                        graphFinished = false;
                        break;
                    }
                }

                if (!graphFinished) {
                    oldRead = read;
                    read = readStream(di);

                    if ((degrees[node - 1] != degree && read < oldRead) || read <= node || degrees[oldRead - 1] > degree) {
                        degree--;
                        for (int i = 0; i < maxNode; i++) {
                            degrees[i] = 0;
                        }

                        maxNode = 1;
                        node = 1;
                        if (degree < 3) {
                            System.out.println("SCD file is invalid");
                            return new int[3];
                        }
                        try {
                            if (di != null) {
                                di.close();
                            }
                            di = new DataInputStream(new FileInputStream(file));
                        } catch (Exception e) {
                            System.err.println(e);
                        }
                        readStream(di);
                        read = readStream(di);
                    }

                    while (degrees[node - 1] == degree) {
                        node++;
                    }
                }
            }

            graphsToDo++;

            int numberToSkip = readStream(di);
            while (numberToSkip != -1) {
                graphsToDo++;
                for (int i = 0; i < maxNode * degree / 2 - numberToSkip; i++) {
                    if (readStream(di) == -1) {
                        needToStart = true;
                        break;
                    }
                }
                numberToSkip = readStream(di);
            }

            if (needToStart) {
                graphsToDo = 0;
                for (int i = 0; i < maxNode; i++) {
                    degrees[i] = 0;
                }

                degree--;
                maxNode = 1;
                node = 1;
                if (degree < 3) {
                    System.out.println("SCD file is invalid");
                    return new int[3];
                }

                try {
                    if (di != null) {
                        di.close();
                    }
                    di = new DataInputStream(new FileInputStream(file));
                } catch (Exception e) {
                    System.err.println(e);
                }
                readStream(di);
                read = readStream(di);
            }
        }
        int[] scdData = new int[3];
        scdData[0] = maxNode;
        scdData[1] = degree;
        scdData[2] = graphsToDo;
        return scdData;
    }

    private int readStream(DataInputStream di) {
        int read;
        try {
            read = Integer.parseInt("" + di.readByte());
        } catch (Exception e) {
            read = -1;
        }
        return read;
    }

    private String intToBinary(int number) {
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

    private int binaryToInt(String binary) {
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

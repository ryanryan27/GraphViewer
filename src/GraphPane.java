//package UGV;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.Timer;


public class GraphPane extends JPanel implements MouseMotionListener, MouseListener{
    GraphPane thisGraphPane;
    GraphLinkedList gll;

    String minFilename = "temp";
    int startPoint = -1;


    int mouseOffsetX2 = 0; //4
    int mouseOffsetY2 = 0; //53

    UndoRedo undoState;

    Timer timer;

    Timer springTimer;

    Graph graph;
    double[] originalX;
    double[] originalY;
    int nodeSelected = -1;
    int nodeHighlighted = -1;
    int nodeSelectedForEdge = -1;
    int nodeSelectedForErasing = -1;
    int nodeSelectedForRelabelling = -1;
    int nodeSelectedForDom = -1;
    int[] edgeHighlighted = new int[2];
    int[] edgeSelectedForErasing = new int[2];
    int radius = 12;
    double scale = 1;

    double rotateX = -1;
    double rotateY = -1;

    int xTopLeft = 0;
    int yTopLeft = 0;

    int offsetX = 0;
    int offsetY = 0;

    boolean leftButtonPressed = false;
    boolean rightButtonPressed = false;
    int xClicked = 0;
    int yClicked = 0;

    boolean startedCreatingVertex = false;
    boolean startedSelection = false;
    boolean startedRotating = false;

    boolean displayCrossings = false;
    boolean displayDomination = true;
    boolean domTotal = false;
    boolean domSecure = false;
    boolean domConnected = false;
    boolean domRoman = false;
    boolean domWeakRoman = false;
    int crossings = 0;
    int[] crossingsX = new int[0];
    int[] crossingsY = new int[0];
    int[] crossingsE1 = new int[0];
    int[] crossingsE2 = new int[0];
    int[] crossingsE3 = new int[0];
    int[] crossingsE4 = new int[0];

    Cursor defaultCursor;

    int selectedOption;


    final int DEFAULT_OPTION = -1;
    final int VERTEX_OPTION = 0;
    final int EDGE_OPTION = 1;
    final int ERASER_OPTION = 2;
    final int SCISSORS_OPTION = 3;
    final int RELABEL_OPTION = 4;
    final int DOM_OPTION = 5;
    final int GRID_OPTION = 6;
    final int SELECT_OPTION = 11;
    final int ROTATE_OPTION = 12;


    final int SCISSORS_DISTANCE = 15;

    final int CC_BACKGROUND = 1;
    final int CC_DEFAULT = 2;
    final int CC_VERTEXFILL = 3;
    final int CC_VERTEX = 4;
    final int CC_EDGE = 5;
    final int CC_DELETEEDGE = 6;

    final double SCALE_MAX = 5;
    final double SCALE_MIN = 0.1;

    // Alterable properties

    Color backgroundColor = new Color(1f, 1f, 1f);
    Color defaultColor = new Color(0f, 0f, 0f);
    Color highlightedVertexFillColor = new Color(0.8f, 0.9f, 0.9f);
    Color highlightedVertexColor = new Color(0f, 0.5f, 0.5f);
    Color newEdgeColor = new Color(0.9f, 0.35f, 0.25f);
    Color deleteEdgeColor = new Color(1f, 0f, 0f);
    Color crossColor = new Color(0.8f, 0f, 0f);
    Color dominatedColor = new Color(0.6f, 0.95f, 0.75f);

    int textSize = 12;

    boolean displayVertexLabels = true;


    boolean snapToGrid = false;
    boolean showGridlines = false;
    double gridSpacing = 50;
    double gridOffsetX = 0;
    double gridOffsetY = 0;


    UGVViewer parent;
    JMenuItem menuItem = null;


    boolean savingWithTransparentBackground = false;

    BufferedImage image;


    public GraphPane(UGVViewer pa) {
        parent = pa;
        gll = new GraphLinkedList();

        undoState = new UndoRedo();

        edgeHighlighted[0] = -1;
        edgeHighlighted[1] = -1;
        edgeSelectedForErasing[0] = -1;
        edgeSelectedForErasing[1] = -1;

        setDefaultColors(parent.getDefaultColors());

        addMouseMotionListener(this);
        addMouseListener(this);

        addMouseWheelListener(
            new MouseWheelListener() {
                public void mouseWheelMoved(MouseWheelEvent e) {
                    int rotation = e.getWheelRotation();

                    if (selectedOption == GRID_OPTION) {


                        gridSpacing = Math.max(gridSpacing * (1 - rotation / 6.0), 5.0);
                        gridSpacing = Math.min(gridSpacing, 500);


                    } else {
                        double oldScale = scale;
                        setScale(scale *(1-rotation/6.0));

                        int mouseX = Math.round((MouseInfo.getPointerInfo().getLocation().x - getLocationOnScreen().x - mouseOffsetX2));
                        int mouseY = Math.round((MouseInfo.getPointerInfo().getLocation().y - getLocationOnScreen().y - mouseOffsetY2));

                        xTopLeft = xTopLeft + (int) Math.round(mouseX / oldScale - (mouseX) / scale);
                        yTopLeft = yTopLeft + (int) Math.round((mouseY) / oldScale - (mouseY) / scale);
                    }

                    repaint();
                }
            });

        defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        selectedOption = -1;

        thisGraphPane = this;
    }

    public void mouseClicked(MouseEvent e) {

        if (e.getButton() == MouseEvent.BUTTON3) {
            if (nodeSelected != -1) {
                graph.setAllPos(originalX, originalY);
                nodeSelected = -1;
                mouseMoved(e);
                repaint();
            } else if (startedCreatingVertex) {
                startedCreatingVertex = false;
                repaint();
            } else if (nodeSelectedForEdge > -1) {
                nodeSelectedForEdge = -1;
                repaint();
            } else if (nodeSelectedForErasing > -1) {
                nodeSelectedForErasing = -1;
                repaint();
            } else if (edgeSelectedForErasing[0] > -1 && edgeSelectedForErasing[1] > -1) {
                edgeSelectedForErasing[0] = -1;
                edgeSelectedForErasing[1] = -1;
                repaint();
            } else if (nodeSelectedForRelabelling > -1) {
                nodeSelectedForRelabelling = -1;
                repaint();
            } else if (nodeSelectedForDom > -1) {
                nodeSelectedForDom = -1;
                repaint();
            } else {
                defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
                selectedOption = -1;
                edgeHighlighted[0] = -1;
                edgeHighlighted[1] = -1;
                parent.changeCursor(defaultCursor);
                parent.changeSelectedOption(-1);
            }
        }
    }


    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftButtonPressed = true;
            if (rightButtonPressed && nodeSelected == -1)
                setCursor(new Cursor(Cursor.MOVE_CURSOR));

            xClicked = mouseX();
            yClicked = mouseY();
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            rightButtonPressed = true;
            if (leftButtonPressed && nodeSelected == -1)
                setCursor(new Cursor(Cursor.MOVE_CURSOR));
            xClicked = mouseX();
            yClicked = mouseY();


        }
        if (graph != null) {


            int xPos = mouseX();
            int yPos = mouseY();

            if (e.getButton() == MouseEvent.BUTTON1) {
                if (selectedOption == DEFAULT_OPTION) {

                    nodeSelected = vertexContaining(xPos, yPos);

                    if(nodeSelected != -1){
                        setUndoState();
                        originalX = new double[graph.getN()];
                        originalY = new double[graph.getN()];
                        for (int j = 0; j < graph.getN(); j++) {
                            originalX[j] = graph.getXPos(j);
                            originalY[j] = graph.getYPos(j);
                        }
                        offsetX = (int) Math.round(xPos / scale + xTopLeft - graph.getXPos(nodeSelected));
                        offsetY = (int) Math.round(yPos / scale + yTopLeft - graph.getYPos(nodeSelected));
                    }



                }
                if (selectedOption == VERTEX_OPTION) {
                    startedCreatingVertex = true;
                }
                if (selectedOption == EDGE_OPTION) {
                    nodeSelectedForEdge = nodeHighlighted;
                }
                if (selectedOption == ERASER_OPTION) {
                    nodeSelectedForErasing = nodeHighlighted;
                }
                if (selectedOption == SCISSORS_OPTION) {
                    edgeSelectedForErasing[0] = edgeHighlighted[0];
                    edgeSelectedForErasing[1] = edgeHighlighted[1];
                }
                if (selectedOption == RELABEL_OPTION) {
                    nodeSelectedForRelabelling = nodeHighlighted;
                }
                if (selectedOption == DOM_OPTION) {
                    nodeSelectedForDom = nodeHighlighted;
                }
                if (selectedOption == SELECT_OPTION) {
                    startedSelection = true;
                }
                if (selectedOption == ROTATE_OPTION) {
                    timer = new Timer();
                    startedRotating = true;
                    setUndoState();
                    timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                rotateX = (xPos / scale + xTopLeft);
                                rotateY = (yPos / scale + yTopLeft);
                                rotate();
                            }
                        }, 0, 1);

                }

            }

        }
        repaint();
    }

    public void rotate() {
        for (int i = 0; i < graph.getN(); i++)
            if (graph.isSelected(i)) {
                double x = graph.getXPos(i);
                double y = graph.getYPos(i);
                double r = Math.pow(Math.pow(x - rotateX, 2) + Math.pow(y - rotateY, 2), 0.5);
                double theta = Math.atan(Math.abs(((x - rotateX)) / ((y - rotateY))));
                if (x >= rotateX && y >= rotateY)
                    theta = Math.PI / 2.0 - theta;
                if (x < rotateX && y >= rotateY)
                    theta = Math.PI / 2.0 + theta;
                if (x < rotateX && y < rotateY)
                    theta = 3.0 * Math.PI / 2.0 - theta;
                if (x >= rotateX && y < rotateY)
                    theta = 3.0 * Math.PI / 2.0 + theta;
                theta = theta + Math.PI / 360.0;
                double newx = x;
                double newy = y;
                if (r >= 0.001) {
                    newx = (rotateX + r * Math.cos(theta));
                    newy = (rotateY + r * Math.sin(theta));
                }
                graph.setXPos(i, newx);
                graph.setYPos(i, newy);
            }
        repaint();
    }

    public void mouseReleased(MouseEvent e) {

        if (e.getButton() == MouseEvent.BUTTON1) {

            int xPos = mouseX();
            int yPos = mouseY();

            if (selectedOption == DEFAULT_OPTION) {
                if (nodeSelected != -1) {

                    nodeSelected = -1;
                }
            }
            if (selectedOption == VERTEX_OPTION) {
                if (startedCreatingVertex) {
                    setUndoState();

                    startedCreatingVertex = false;
                    graph.setN(graph.getN() + 1);

                    double vertX = (xPos / scale + xTopLeft);
                    double vertY = (yPos / scale + yTopLeft);

                    if (snapToGrid) {
                        vertX = Math.round((vertX - gridOffsetX) / gridSpacing) * gridSpacing + gridOffsetX;
                        vertY = Math.round((vertY - gridOffsetY) / gridSpacing) * gridSpacing + gridOffsetY;
                    }


                    graph.setXPos(graph.getN() - 1, vertX);
                    graph.setYPos(graph.getN() - 1, vertY);


                    validate();
                    repaint();
                }
            }
            if (selectedOption == EDGE_OPTION) {
                if (nodeSelectedForEdge != -1) {
                    if (nodeHighlighted != -1 && nodeHighlighted != nodeSelectedForEdge) {
                        setUndoState();

                        graph.addArc(nodeSelectedForEdge + 1, nodeHighlighted + 1);
                        graph.addArc(nodeHighlighted + 1, nodeSelectedForEdge + 1);
                    }

                    nodeSelectedForEdge = -1;
                    repaint();
                }

            }
            if (selectedOption == ERASER_OPTION) {
                if (nodeSelectedForErasing == nodeHighlighted && nodeSelectedForErasing != -1) {

                    if (graph.isSelected(nodeHighlighted)) {
                        setUndoState();
                        for (int i = graph.getN() - 1; i >= 0; i--) {
                            if (graph.isSelected(i)) {
                                graph.deleteVertex(i + 1);
                            }
                        }
                    } else {
                        setUndoState();


                        graph.deleteVertex(nodeHighlighted + 1);

                    }
                    nodeSelectedForErasing = -1;
                    nodeHighlighted = -1;

                    repaint();

                }
            }
            if (selectedOption == SCISSORS_OPTION) {
                if (edgeSelectedForErasing[0] != -1 && edgeSelectedForErasing[1] != -1 && edgeSelectedForErasing[0] == edgeHighlighted[0] && edgeSelectedForErasing[1] == edgeHighlighted[1]) {
                    setUndoState();

                    graph.deleteArc(edgeSelectedForErasing[0] + 1, edgeSelectedForErasing[1] + 1);
                    graph.deleteArc(edgeSelectedForErasing[1] + 1, edgeSelectedForErasing[0] + 1);
                    edgeSelectedForErasing[0] = -1;
                    edgeSelectedForErasing[1] = -1;
                    edgeHighlighted[0] = -1;
                    edgeHighlighted[1] = -1;
                    repaint();
                }
            }
            if (selectedOption == RELABEL_OPTION) {
                if (nodeSelectedForRelabelling == nodeHighlighted && nodeSelectedForRelabelling != -1) {
                    RelabelDialog rd = new RelabelDialog(parent, (nodeHighlighted + 1), graph.getN());

                    if (!rd.getCancelled()) {


                        int newLabel = rd.getNewLabel();
                        if (newLabel == -1) {
                            System.out.println("Some relabelling problem, should never happen");
                        } else {
                            setUndoState();


                            if (rd.getIncrement()) {
                                // Increment rest
                                if ((nodeHighlighted + 1) < newLabel)
                                    for (int i = nodeHighlighted + 1; i < newLabel; i++)
                                        graph.swapVertices(i, i + 1);
                                else
                                    for (int i = nodeHighlighted + 1; i > newLabel; i--)
                                        graph.swapVertices(i, i - 1);
                            } else {
                                // Swap labels
                                graph.swapVertices(nodeHighlighted + 1, newLabel);
                            }
                        }
                        nodeHighlighted = newLabel - 1;
                        nodeSelectedForRelabelling = newLabel - 1;
                        repaint();
                    }


                    // Pop open relabelling window
                }
            }
            if (selectedOption == DOM_OPTION) {
                if (nodeSelectedForDom == nodeHighlighted && nodeSelectedForDom != -1) {

                    setUndoState();
                    if (graph.isSelected(nodeSelectedForDom)) {
                        graph.toggleDom(nodeSelectedForDom);
                        int val = graph.inDomset(nodeSelectedForDom + 1);
                        for (int i = 0; i < graph.getN(); i++) {
                            if (graph.isSelected(i)) {
                                graph.setDomValue(i, val);

                            }
                        }
                    } else {
                        graph.toggleDom(nodeSelectedForDom);
                    }
                    repaint();
                }
            }
            if (selectedOption == SELECT_OPTION) {
                if (startedSelection) {
                    int leftX = (int) Math.round(xTopLeft + (xClicked) / scale);
                    int rightX = (int) Math.round(xTopLeft + (xPos) / scale);
                    if (leftX > rightX) {
                        int temp = leftX;
                        leftX = rightX;
                        rightX = temp;
                    }

                    int bottomY = (int) Math.round(yTopLeft + (yClicked) / scale);
                    int topY = (int) Math.round(yTopLeft + (yPos) / scale);
                    if (bottomY > topY) {
                        int temp = bottomY;
                        bottomY = topY;
                        topY = temp;
                    }
                    for (int i = 0; i < graph.getN(); i++) {
                        if (graph.getXPos(i) >= leftX && graph.getXPos(i) <= rightX && graph.getYPos(i) >= bottomY && graph.getYPos(i) <= topY) {
                            graph.select(i);
                        } else {
                            graph.deselect(i);
                        }
                    }
                    startedSelection = false;
                    repaint();
                }
            }
            if (selectedOption == ROTATE_OPTION) {
                if (startedRotating) {

                    timer.cancel();
                    startedRotating = false;
                }
            }
        }

        mouseMoved(e);

        if (e.getButton() == MouseEvent.BUTTON1) {
            leftButtonPressed = false;
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            rightButtonPressed = false;
        }

        setCursor(defaultCursor);

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    public int mouseX(){
        return (MouseInfo.getPointerInfo().getLocation().x - getLocationOnScreen().x - mouseOffsetX2);
    }

    public int mouseY(){
        return (MouseInfo.getPointerInfo().getLocation().y - getLocationOnScreen().y - mouseOffsetY2);
    }

    public void mouseDragged(MouseEvent e) {

        int xPos = mouseX();
        int yPos = mouseY();

        if (selectedOption == DEFAULT_OPTION) {
            if (nodeSelected != -1) {
                if (graph.isSelected(nodeSelected)) {
                    double origX = graph.getXPos(nodeSelected);
                    double origY = graph.getYPos(nodeSelected);

                    double newX = xTopLeft + ((xPos) / scale - offsetX);
                    double newY = yTopLeft + ((yPos) / scale - offsetY);

                    if (snapToGrid) {

                        newX = Math.round((newX - gridOffsetX) / gridSpacing) * gridSpacing + gridOffsetX;
                        newY = Math.round((newY - gridOffsetY) / gridSpacing) * gridSpacing + gridOffsetY;
                    }


                    graph.setXPos(nodeSelected, newX);
                    graph.setYPos(nodeSelected, newY);

                    for (int i = 0; i < graph.getN(); i++)
                        if (graph.isSelected(i) && i != nodeSelected) {
                            graph.setXPos(i, graph.getXPos(i) + newX - origX);
                            graph.setYPos(i, graph.getYPos(i) + newY - origY);
                        }
                } else {
                    boolean[] newSelected = new boolean[graph.getN()];
                    graph.setSelected(newSelected);

                    double newX = xTopLeft + ((xPos) / scale - offsetX);
                    double newY = yTopLeft + ((yPos) / scale - offsetY);

                    if (snapToGrid) {

                        newX = Math.round((newX - gridOffsetX) / gridSpacing) * gridSpacing + gridOffsetX;
                        newY = Math.round((newY - gridOffsetY) / gridSpacing) * gridSpacing + gridOffsetY;
                    }

                    graph.setXPos(nodeSelected, newX);
                    graph.setYPos(nodeSelected, newY);
                }
            }
        }
        if (selectedOption == EDGE_OPTION || selectedOption == ERASER_OPTION || selectedOption == RELABEL_OPTION) {
            if (nodeSelectedForEdge != -1) {

                nodeHighlighted = vertexContaining(xPos, yPos);

                repaint();

            }
        }

        if (selectedOption == SCISSORS_OPTION) {

            double xScreen = xPos / scale + xTopLeft;
            double yScreen = yPos / scale + yTopLeft;

            int[][] arcs = graph.getArcs();
            int N = graph.getN();
            int[] degrees = graph.getDegrees();

            boolean edgeFound = false;
            double closest = -1;
            for (int i = 0; i < N; i++)
                for (int j = 0; j < degrees[i]; j++) {
                    int v2 = arcs[i][j] - 1;


                    double x1 = graph.getXPos(i);
                    double y1 = graph.getYPos(i);
                    double x2 = graph.getXPos(v2);
                    double y2 = graph.getYPos(v2);

                    if (Math.abs(x2-x1) > 0.0001) {
                        double theta = Math.atan((y2 - y1) / (x2 - x1));

                        double abs = Math.abs(yScreen - ((y2 - y1) * xScreen / (x2 - x1) - (y2 * x1 - y1 * x2) / (x2 - x1)));
                        if (abs <= SCISSORS_DISTANCE && xScreen + 1 >= Math.min(Math.min(x1, x2) + radius * Math.cos(theta), Math.max(x1, x2) - radius * Math.cos(theta)) && xScreen - 1 <= Math.max(Math.min(x1, x2) + radius * Math.cos(theta), Math.max(x1, x2) - radius * Math.cos(theta)) && yScreen + 1 >= Math.min(Math.min(y1, y2) + radius * Math.sin(theta), Math.max(y1, y2) - radius * Math.sin(theta)) && yScreen - 1 <= Math.max(Math.min(y1, y2) + radius * Math.sin(theta), Math.max(y1, y2) - radius * Math.sin(theta))) {
                            if (closest == -1 || closest > abs)
                                closest = abs;
                            else
                                continue;


                            edgeHighlighted[0] = i;
                            edgeHighlighted[1] = v2;
                            edgeFound = true;


                        }
                    } else {
                        if (Math.abs(xScreen - x1) <= SCISSORS_DISTANCE && yScreen >= Math.min(y1, y2) + radius && yScreen <= Math.max(y1, y2) - radius) {
                            if (closest == -1 || closest > Math.abs(xScreen - x1))
                                closest = Math.abs(xScreen - x1);
                            else
                                continue;

                            edgeHighlighted[0] = i;
                            edgeHighlighted[1] = v2;
                            edgeFound = true;
                        }
                    }

                }

            if (!edgeFound) {
                edgeHighlighted[0] = -1;
                edgeHighlighted[1] = -1;
            }

        }

        if (selectedOption == ROTATE_OPTION)
            if (startedRotating) {
                timer.cancel();
                timer = new Timer();
                startedRotating = true;
                setUndoState();
                timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                rotateX = ((xPos) / scale + xTopLeft);
                                rotateY = ((yPos) / scale + yTopLeft);
                                rotate();
                            }
                        }, 0, 1);


            }


        if (selectedOption == GRID_OPTION) {
            gridOffsetX = gridOffsetX + (int) Math.round(1 * ((xPos) / scale - xClicked / scale));
            gridOffsetY = gridOffsetY + (int) Math.round(1 * ((yPos) / scale - yClicked / scale));
            xClicked = (xPos);
            yClicked = (yPos);
        }

        if ((leftButtonPressed && rightButtonPressed) || (selectedOption == DEFAULT_OPTION && nodeSelected == -1)) {

            xTopLeft = xTopLeft - (int) Math.round(1 * ((xPos) / scale - xClicked / scale));
            yTopLeft = yTopLeft - (int) Math.round(1 * ((yPos) / scale - yClicked / scale));
            xClicked = (xPos);
            yClicked = (yPos);
        }
        mouseMoved(e);
        repaint();
    }

    public void mouseMoved(MouseEvent e) {

        if (selectedOption == DEFAULT_OPTION || selectedOption == EDGE_OPTION || selectedOption == ERASER_OPTION || selectedOption == RELABEL_OPTION || selectedOption == DOM_OPTION) {

            nodeHighlighted = vertexContaining(mouseX(), mouseY());
            repaint();

        }
        if (selectedOption == SCISSORS_OPTION) {
            int xPos = mouseX();
            int yPos = mouseY();
            double xScreen = xPos / scale + xTopLeft;
            double yScreen = yPos / scale + yTopLeft;

            int[][] arcs = graph.getArcs();
            int N = graph.getN();
            int[] degrees = graph.getDegrees();

            boolean edgeFound = false;
            double closest = -1;
            for (int i = 0; i < N; i++)
                for (int j = 0; j < degrees[i]; j++) {
                    int v2 = arcs[i][j] - 1;

                    double x1 = graph.getXPos(i);
                    double y1 = graph.getYPos(i);
                    double x2 = graph.getXPos(v2);
                    double y2 = graph.getYPos(v2);

                    if (Math.abs(x2 - x1) > 0.001) {
                        double theta = Math.atan((y2 - y1) / (x2 - x1));

                        double abs = Math.abs(yScreen - ((y2 - y1) * xScreen / (x2 - x1) - (y2 * x1 - y1 * x2) / (x2 - x1)));
                        if (abs <= SCISSORS_DISTANCE && xScreen + 1 >= Math.min(Math.min(x1, x2) + radius * Math.cos(theta), Math.max(x1, x2) - radius * Math.cos(theta)) && xScreen - 1 <= Math.max(Math.min(x1, x2) + radius * Math.cos(theta), Math.max(x1, x2) - radius * Math.cos(theta)) && yScreen + 1 >= Math.min(Math.min(y1, y2) + radius * Math.sin(theta), Math.max(y1, y2) - radius * Math.sin(theta)) && yScreen - 1 <= Math.max(Math.min(y1, y2) + radius * Math.sin(theta), Math.max(y1, y2) - radius * Math.sin(theta))) {
                            if (closest == -1 || closest > abs)
                                closest = abs;
                            else
                                continue;


                            edgeHighlighted[0] = i;
                            edgeHighlighted[1] = v2;
                            edgeFound = true;


                        }
                    } else {
                        if (Math.abs(xScreen - x1) <= SCISSORS_DISTANCE && yScreen >= Math.min(y1, y2) + radius && yScreen <= Math.max(y1, y2) - radius) {
                            if (closest == -1 || closest > Math.abs(xScreen - x1))
                                closest = Math.abs(xScreen - x1);
                            else
                                continue;

                            edgeHighlighted[0] = i;
                            edgeHighlighted[1] = v2;
                            edgeFound = true;
                        }
                    }

                }

            if (!edgeFound) {
                edgeHighlighted[0] = -1;
                edgeHighlighted[1] = -1;
            }
            repaint();


        }

    }


    public Color[] getDefaultColors() {
        Color[] tempColors = new Color[6];
        tempColors[0] = backgroundColor;
        tempColors[1] = defaultColor;
        tempColors[2] = highlightedVertexFillColor;
        tempColors[3] = highlightedVertexColor;
        tempColors[4] = newEdgeColor;
        tempColors[5] = deleteEdgeColor;
        return tempColors;
    }

    public void setDefaultColors(Color[] colors) {
        backgroundColor = colors[0];
        defaultColor = colors[1];
        highlightedVertexFillColor = colors[2];
        highlightedVertexColor = colors[3];
        newEdgeColor = colors[4];
        deleteEdgeColor = colors[5];
    }

    private BufferedImage TransformColorToTransparency(BufferedImage image, Color c1) {
        // Primitive test, just an example
        final int r1 = c1.getRed();
        final int g1 = c1.getGreen();
        final int b1 = c1.getBlue();
        ImageFilter filter =
                new RGBImageFilter() {
                    public int filterRGB(int x, int y, int rgb) {
                        int r = (rgb & 0xFF0000) >> 16;
                        int g = (rgb & 0xFF00) >> 8;
                        int b = rgb & 0xFF;
                        if (r == r1 && g == g1 && b == b1) {
                            // Set fully transparent but keep color
                            return rgb & 0xFFFFFF;
                        }
                        return rgb;
                    }
                };

        ImageProducer ip = new FilteredImageSource(image.getSource(), filter);
        return ImageToBufferedImage(Toolkit.getDefaultToolkit().createImage(ip), image.getWidth(), image.getHeight());
    }

    private BufferedImage ImageToBufferedImage(Image image, int width, int height) {
        BufferedImage dest = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = dest.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return dest;
    }

    public BufferedImage makeTransparent(BufferedImage image, int x, int y) {
        ColorModel cm = image.getColorModel();
        if (!(cm instanceof IndexColorModel))
            return image; //sorry...
        IndexColorModel icm = (IndexColorModel) cm;
        WritableRaster raster = image.getRaster();
        int pixel = raster.getSample(x, y, 0); //pixel is offset in ICM's palette
        int size = icm.getMapSize();
        byte[] reds = new byte[size];
        byte[] greens = new byte[size];
        byte[] blues = new byte[size];
        icm.getReds(reds);
        icm.getGreens(greens);
        icm.getBlues(blues);
        IndexColorModel icm2 = new IndexColorModel(8, size, reds, greens, blues, pixel);
        return new BufferedImage(icm2, raster, image.isAlphaPremultiplied(), null);
    }

    public void paintComponent(Graphics gra) {

        if(graph == null) return;

        image = new BufferedImage((int) Math.round(getSize().getWidth()), (int) Math.round(getSize().getHeight()), BufferedImage.TYPE_BYTE_INDEXED);
        Graphics2D g = (Graphics2D) image.getGraphics();

        g.setPaint(backgroundColor);

        if (savingWithTransparentBackground) {
            image = makeTransparent(image, 0, 0);
            g = (Graphics2D) image.getGraphics();
        } else {
            g.fill(new Rectangle(0, 0, (int) Math.round(getSize().getWidth()), (int) Math.round(getSize().getHeight())));
        }

        g.scale(scale, scale);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        //g.setPaint(defaultColor);

        //g.setFont(g.getFont().deriveFont((float) textSize));



        int N = graph.getN();

        if (showGridlines) {
            drawGridlines(g);
        }


        if (displayCrossings) {
            drawCrossings(g);
        }


        if (displayDomination) {
            drawDominationText(g);
        }

        for (int i = 0; i < N; i++) {
            if (graph.isSelected(i)) {

                final float[] dash1 = {2.0f};
                g.setColor(defaultColor);
                g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f));
                g.drawRect((int) Math.round((-xTopLeft + graph.getXPos(i))) - radius - 6, (int) Math.round((-yTopLeft + graph.getYPos(i))) - radius - 6, 2 * radius + 12, 2 * radius + 12);
                g.setStroke(new BasicStroke(1));

            }

            if(i == nodeHighlighted || i == nodeSelectedForEdge) continue;

            drawVertex(g, i);

        }

        drawVertex(g, nodeHighlighted);
        drawVertex(g, nodeSelectedForEdge);

        int mouseX = (int) Math.round(mouseX() / scale);
        int mouseY = (int) Math.round(mouseY() / scale);

        if (startedSelection) {
            drawSelectionBox(g, mouseX, mouseY, (int) Math.round((xClicked) / scale), (int) Math.round((yClicked) / scale), defaultColor);
        }


        int[][] arcs = graph.getArcs();
        int[] degrees = graph.getDegrees();

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < degrees[i]; j++) {
                int v2 = arcs[i][j] - 1;

                if(v2 <= i) continue;

                if ((nodeHighlighted != i && nodeHighlighted != v2) || (edgeHighlighted[0] == i && edgeHighlighted[1] == v2) || (edgeHighlighted[0] == v2 && edgeHighlighted[1] == i)) {
                    drawEdge(g, i, v2, defaultColor, 1);
                } else {
                    drawEdge(g, i, v2, highlightedVertexColor, (float) Math.max(2f, 1.5f / scale));
                }
            }
        }


        if (nodeSelectedForEdge != -1) {

            double centre1X = -xTopLeft + graph.getXPos(nodeSelectedForEdge);
            double centre1Y = -yTopLeft + graph.getYPos(nodeSelectedForEdge);

            drawEdge(g, centre1X, centre1Y, radius, mouseX, mouseY, 0, newEdgeColor, (float)Math.max(2f, 1.5f / scale));

        }

        if (startedCreatingVertex) {

            double newVertX = mouseX;
            double newVertY = mouseY;

            if (snapToGrid) {
                newVertX = Math.round((newVertX + xTopLeft - gridOffsetX) / gridSpacing) * gridSpacing + gridOffsetX - xTopLeft;
                newVertY = Math.round((newVertY + yTopLeft - gridOffsetY) / gridSpacing) * gridSpacing + gridOffsetY - yTopLeft;
            }

            drawVertex(g, newVertX, newVertY, radius, defaultColor, 1f);
        }

        if (edgeHighlighted[0] != -1 && edgeHighlighted[1] != -1) {
            drawEdge(g, edgeHighlighted[0], edgeHighlighted[1], deleteEdgeColor, (float) Math.max(2f, 1.5f / Math.max(scale, scale)));
        }

        gra.drawImage(image, 0, 0, null);
    }



    private void drawVertex(Graphics2D g, int vertex){

        if(vertex == -1) return;

        boolean[] dv = graph.dominatedVertices(domTotal, domSecure, domConnected, domRoman, domWeakRoman);

        float weight = 1;
        Color stroke = defaultColor;
        Color fill = Color.WHITE;
        Font font = g.getFont().deriveFont(Font.PLAIN).deriveFont((float)textSize);

        if (dv[vertex] && displayDomination) {
            fill = dominatedColor;
        }

        if (vertex == nodeHighlighted || vertex == nodeSelectedForEdge) {
            stroke = highlightedVertexColor;
            fill = highlightedVertexFillColor;
            font = g.getFont().deriveFont(Font.BOLD);
            weight = (float)Math.max(2f, 1.5f / scale);
        }


        double x = -xTopLeft + graph.getXPos(vertex);
        double y = -yTopLeft + graph.getYPos(vertex);

        if (graph.inDomset(vertex + 1) == 2 && displayDomination) {
            drawVertex(g, x, y, radius + 4, stroke, fill, 4);
        }


        if (graph.inDomset(vertex + 1) == 1 && displayDomination){
            weight = 6;
        }

        if(displayVertexLabels){
            String label = "" + (vertex + 1);
            drawVertex(g, x, y, radius, stroke, fill, weight, label, font);
        } else {
            drawVertex(g, x, y, radius, stroke, fill, weight);
        }

    }

    private void drawVertex(Graphics2D g, double x, double y, int rad, Color stroke, float weight){
        g.setStroke(new BasicStroke(weight));
        g.setColor(stroke);

        g.drawOval((int)Math.round(x) - rad, (int)Math.round(y)-rad, 2*rad, 2*rad);
    }

    private void drawVertex(Graphics2D g, double x, double y, int rad, Color stroke, Color fill, float weight){
        g.setColor(fill);
        g.fillOval((int)Math.round(x) - rad, (int)Math.round(y)-rad, 2*rad, 2*rad);

        drawVertex(g, x, y, rad, stroke, weight);
    }

    private void drawVertex(Graphics2D g, double x, double y, int rad, Color stroke, float weight, String label, Font font){
        drawVertex(g, x, y, rad, stroke, weight);

        g.setColor(stroke);
        g.setFont(font);

        FontMetrics fm = g.getFontMetrics();

        int text_x = (int)Math.round(x-fm.stringWidth(label)/2.0);
        int text_y = (int)Math.round(y-fm.getHeight()/2.0) +fm.getAscent();


        g.drawString(label, text_x, text_y);
    }

    private void drawVertex(Graphics2D g, double x, double y, int rad, Color stroke, Color fill, float weight, String label, Font font){
        g.setColor(fill);
        g.fillOval((int)Math.round(x) - rad, (int)Math.round(y)-rad, 2*rad, 2*rad);

        drawVertex(g, x, y, rad, stroke, weight, label, font);
    }

    private void drawEdge(Graphics2D g, int v1, int v2, Color c, float weight){

        double x1 = -xTopLeft + graph.getXPos(v1);
        double y1 = -yTopLeft + graph.getYPos(v1);
        double x2 = -xTopLeft + graph.getXPos(v2);
        double y2 = -yTopLeft + graph.getYPos(v2);

        drawEdge(g, x1, y1, radius, x2, y2, radius, c, weight);
    }

    private void drawEdge(Graphics2D g, double x1, double y1, int r1, double x2, double y2, int r2, Color c, float weight){

        g.setStroke(new BasicStroke(weight));
        g.setColor(c);

        double theta;

        int start_x1;
        int start_x2;
        int start_y1;
        int start_y2;

        if (x2 == x1) {
            if (y1 > y2) {
                theta = Math.PI / 2;
            } else {
                theta = 3 * Math.PI / 2;
            }
        } else {
            theta = Math.atan((0.0 + y2 - y1) / (x2 - x1));
        }

        double y1_offset;
        double y2_offset;

        if (x1 < x2) {
            start_x1 = (int) Math.ceil(x1 + r1 * Math.cos(theta));
            start_x2 = (int) Math.floor(x2 + r2 * Math.cos(Math.PI + theta));
            y1_offset = 0;
            y2_offset = Math.PI;
        } else if (x1 > x2) {
            start_x1 = (int) Math.floor(x1 + r1 * Math.cos(Math.PI + theta));
            start_x2 = (int) Math.ceil(x2 + r2 * Math.cos(theta));
            y1_offset = Math.PI;
            y2_offset = 0;
        } else {
            start_x1 = (int) Math.floor(x1 + r1 * Math.cos(Math.PI + theta));
            start_x2 = (int) Math.floor(x2 + r2 * Math.cos(theta));
            y1_offset = Math.PI;
            y2_offset = 0;
        }

        if(y1 < y2){
            start_y1 = (int) Math.ceil(y1 + r1 * Math.sin(theta+y1_offset));
            start_y2 = (int) Math.floor(y2 + r2 * Math.sin(theta+y2_offset));
        } else if (y1 > y2){
            start_y1 = (int) Math.floor(y1 + r1 * Math.sin(theta+y1_offset));
            start_y2 = (int) Math.ceil(y2 + r2 * Math.sin(theta+y2_offset));
        } else {
            start_y1 = (int) Math.floor(y1 + r1 * Math.sin(theta+y1_offset));
            start_y2 = (int) Math.floor(y2 + r2 * Math.sin(theta+y2_offset));
        }

        g.drawLine(start_x1, start_y1, start_x2, start_y2);

    }

    private void drawGridlines(Graphics2D g){
        //find boundary af drawn area
        Rectangle boundingBox = new Rectangle(0, 0, (int) (getSize().getWidth() / scale), (int) (getSize().getHeight() / scale));


        //find where first vertical line goes, and how many
        double startX = boundingBox.x - gridSpacing - (boundingBox.x - gridOffsetX + xTopLeft) % (gridSpacing);
        int numLinesX = 3 + (int) Math.floor((boundingBox.x + boundingBox.width) / gridSpacing) - (int) Math.floor(boundingBox.x / gridSpacing);

        //find where first horizontal line goes, and how many
        double startY = boundingBox.y - gridSpacing - (boundingBox.y - gridOffsetY + yTopLeft) % (gridSpacing);
        int numLinesY = 3 + (int) Math.floor((boundingBox.y + boundingBox.height) / gridSpacing) - (int) Math.floor(boundingBox.y / gridSpacing);

        //draw the lines
        g.setColor(Color.lightGray);
        g.setStroke(new BasicStroke(1));
        for (int i = 0; i < numLinesX; i++) {
            g.drawLine((int) (startX + i * gridSpacing), boundingBox.y - (int) gridSpacing, (int) (startX + i * gridSpacing), boundingBox.y + boundingBox.height + (int) gridSpacing);
        }
        for (int i = 0; i < numLinesY; i++) {
            g.drawLine(boundingBox.x - (int) gridSpacing, (int) (startY + i * gridSpacing), boundingBox.x + boundingBox.width + (int) gridSpacing, (int) (startY + i * gridSpacing));
        }
    }

    private void drawCrossings(Graphics2D g){
        findCrossings();
        g.setColor(crossColor);
        for (int i = 0; i < crossings; i++) {
            g.fillOval(Math.round(-xTopLeft + crossingsX[i]) - radius, Math.round(-yTopLeft + crossingsY[i]) - radius, 2 * radius, 2 * radius);
        }

        g.setFont(g.getFont().deriveFont((float) (20 / scale)));
        g.drawString(("Crossings: " + crossings), Math.round(20 / scale), Math.round(20 / scale));
    }

    private void drawDominationText(Graphics2D g){
        boolean[] dv = graph.dominatedVertices(domTotal, domSecure, domConnected, domRoman, domWeakRoman);


        int ud = 0;
        for (boolean b : dv) {
            if (!b) {
                ud++;
            }
        }

        g.scale(1/scale, 1/scale);

        g.setColor(crossColor);
        g.setFont(g.getFont().deriveFont((float) (20)));

        g.drawString(("Undominated Vertices: " + ud + " (" + graph.getDomSize() + ")"),getWidth() - 300,20);

        String types = "";
        StringJoiner sj = new StringJoiner(", ");

        if (domTotal) sj.add("Total");
        if (domConnected) sj.add("Connected");
        if (domSecure) sj.add("Secure");
        if (domWeakRoman)  sj.add("Weak Roman");
        if (domRoman) sj.add("Roman");
        if (sj.length() > 0) {
            types = "(" + sj.toString() + ")";

            g.setFont(g.getFont().deriveFont((float) (14)));
            g.drawString(types, getWidth() - 75 - 6 * types.length(), 32);

        }

        g.scale(scale, scale);
    }

    private void drawSelectionBox(Graphics2D g, int x1, int y1, int x2, int y2, Color c){
        if (x1 > x2) {
            int tempX = x1;
            x1 = x2;
            x2 = tempX;
        }

        if (y1 > y2) {
            int tempY = y1;
            y1 = y2;
            y2 = tempY;
        }

        final float[] dash1 = {2.0f};
        g.setColor(c);
        g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f));
        g.drawRect(x1, y1, x2 - x1, y2 - y1);

    }

    public void setGraph(Graph gr) {
        graph = gr;
    }

    public Graph getGraph() {
        return graph;
    }


    public void setScale(double new_scale) {

        scale = new_scale;

        if(scale > SCALE_MAX) scale = SCALE_MAX;
        if(scale < SCALE_MIN) scale = SCALE_MIN;

    }

    public void setTopLeft(int xl, int yl) {
        xTopLeft = xl;
        yTopLeft = yl;
    }

    public void setRadius(int ra) {
        radius = ra;
    }

    public int getRadius() {
        return radius;
    }

    public double getXScale() {
        return scale;
    }

    public double getYScale() {
        return scale;
    }

    public int getXTopLeft() {
        return xTopLeft;
    }

    public int getYTopLeft() {
        return yTopLeft;
    }

    public void setDefaultCursor(Cursor cursor) {
        defaultCursor = cursor;
    }

    public void setSelectedOption(int option) {
        selectedOption = option;
    }

    public void changeColor(int colorCode, Color color) {
        if (colorCode == CC_BACKGROUND)
            backgroundColor = color;
        if (colorCode == CC_DEFAULT)
            defaultColor = color;
        if (colorCode == CC_VERTEXFILL)
            highlightedVertexFillColor = color;
        if (colorCode == CC_VERTEX)
            highlightedVertexColor = color;
        if (colorCode == CC_EDGE)
            newEdgeColor = color;
        if (colorCode == CC_DELETEEDGE)
            deleteEdgeColor = color;
    }

    public boolean getDisplayVertexLabels() {
        return displayVertexLabels;
    }

    public void setDisplayVertexLabels(boolean dvl) {
        displayVertexLabels = dvl;
    }

    public boolean getDisplayCrossings() {
        return displayCrossings;
    }

    public void setDisplayCrossings(boolean dc) {
        displayCrossings = dc;
        if (dc) {
            findCrossings();
            System.out.println();
            System.out.println("Crossings: " + crossings);
            for (int i = 0; i < crossings; i++){
                System.out.println("((" + (crossingsE1[i] + 1) + "," + (crossingsE2[i] + 1) + "), (" + (crossingsE3[i] + 1) + "," + (crossingsE4[i] + 1) + "))");
            }
        }
    }

    public boolean getDisplayDomination() {
        return displayDomination;
    }

    public void setDisplayDomination(boolean dd) {
        displayDomination = dd;
    }

    public boolean getDomTotal() {
        return domTotal;
    }

    public void setDomTotal(boolean dt) {
        domTotal = dt;
    }

    public boolean getDomSecure() {
        return domSecure;
    }

    public void setDomSecure(boolean ds) {
        domSecure = ds;
    }

    public boolean getDomConnected() {
        return domConnected;
    }

    public void setDomConnected(boolean dc) {
        domConnected = dc;
    }

    public boolean getDomRoman() {
        return domRoman;
    }

    public void setDomRoman(boolean dr) {
        domRoman = dr;
    }

    public boolean getDomWeakRoman() {
        return domWeakRoman;
    }

    public void setDomWeakRoman(boolean dwr) {
        domWeakRoman = dwr;
    }

    public void beginSpring() {

        setUndoState();

        graph.calculatingSpring = true;

        springTimer = new Timer();
        springTimer.schedule(
            new TimerTask() {
                @Override
                public void run() {
                    graph.springLayout(radius);
                    validate();
                    repaint();
                }
            }, 0, 100);

    }

    public void cancelSpring() {
        if (graph.calculatingSpring) {
            springTimer.cancel();
            graph.calculatingSpring = false;
        }
    }


    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int size) {
        textSize = size;
    }

    public void setSavingWithTransparentBackground(boolean swtb) {
        savingWithTransparentBackground = swtb;
    }

    public BufferedImage getImage() {
        return image;
    }

    public JMenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(JMenuItem mi) {
        menuItem = mi;
    }


    public void setUndoState() {
        undoState.addItem(graph);
        setUndoAvailable(true);
        setRedoAvailable(false);
        parent.checkSave();
    }

    public void undo() {
        cancelSpring();
        graph = undoState.undo(graph);
        parent.checkSave();

        if (!undoState.canUndo())
            parent.setUndoAvailable(false);
        parent.setRedoAvailable(true);
        validate();
        repaint();
    }

    public void redo() {

        graph = undoState.redo(graph);
        parent.checkSave();
        if (!undoState.canRedo())
            parent.setRedoAvailable(false);
        parent.setUndoAvailable(true);
        validate();
        repaint();
    }

    public void setUndoAvailable(boolean available) {
        parent.setUndoAvailable(available);
    }

    public void setRedoAvailable(boolean available) {
        parent.setRedoAvailable(available);
    }

    public UndoRedo getUndoState() {
        return undoState;
    }

    public UGVViewer getParent() {
        return parent;
    }

    public int vertexContaining(double mouseX, double mouseY) {
        for (int i = 0; i < graph.getN(); i++) {

            if (Math.pow(Math.pow(mouseX + xTopLeft * scale - graph.getXPos(i) * scale, 2) + Math.pow(mouseY + yTopLeft * scale - graph.getYPos(i) * scale, 2), 0.5) <= radius * Math.min(scale, scale)) {
                return i;
            }

        }

        return -1;

    }


    public void findCrossings() {
        int[][] arcs = graph.getArcs();
        int[] degrees = graph.getDegrees();
        int N = graph.getN();
        crossings = 0;
        crossingsX = new int[100000];
        crossingsY = new int[100000];
        crossingsE1 = new int[100000];
        crossingsE2 = new int[100000];
        crossingsE3 = new int[100000];
        crossingsE4 = new int[100000];
        for (int i = 0; i < N; i++)
            for (int j = 0; j < degrees[i]; j++) {
                if (i > arcs[i][j] - 1)
                    continue;

                for (int k = i; k < N; k++)
                    for (int l = 0; l < degrees[k]; l++) {
                        int v1 = i;
                        int v2 = arcs[i][j] - 1;
                        int v3 = k;
                        int v4 = arcs[k][l] - 1;
                        if (v1 == v3 || v1 == v4 || v2 == v3 || v2 == v4)
                            continue;
                        if (v3 > v4)
                            continue;

                        double p1x = graph.getXPos(v1);
                        double p1y = graph.getYPos(v1);
                        double p2x = graph.getXPos(v2);
                        double p2y = graph.getYPos(v2);
                        double p3x = graph.getXPos(v3);
                        double p3y = graph.getYPos(v3);
                        double p4x = graph.getXPos(v4);
                        double p4y = graph.getYPos(v4);

                        double s1x = p2x - p1x;
                        double s1y = p2y - p1y;
                        double s2x = p4x - p3x;
                        double s2y = p4y - p3y;

                        double s = (-s1y * (p1x - p3x) + s1x * (p1y - p3y)) / (-s2x * s1y + s1x * s2y);
                        double t = (s2x * (p1y - p3y) - s2y * (p1x - p3x)) / (-s2x * s1y + s1x * s2y);

                        if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
                            crossingsX[crossings] = (int) Math.round(p1x + (t * s1x));
                            crossingsY[crossings] = (int) Math.round(p1y + (t * s1y));
                            crossingsE1[crossings] = v1;
                            crossingsE2[crossings] = v2;
                            crossingsE3[crossings] = v3;
                            crossingsE4[crossings] = v4;
                            crossings++;
                        }
                    }
            }
    }

    public void checkCrossings() {
   /*int [][]jjj = graph.getArcs();
   for(int i=0; i<39; i++)
   System.out.println("ARCS : " + jjj[i][0] + " " + jjj[i][1]);*/
        findCrossings();
        try {
            boolean writeFile = true;
            for (int i = 1; i <= crossings; i++) {
                File temp = new File("./checkby/" + minFilename + ".cr" + i + ".cross");
                if (temp.exists()) {
                    System.out.println("Not writing " + minFilename + ".cr" + crossings + ".cross because " + minFilename + ".cr" + i + ".cross already exists.");
                    writeFile = false;
                    break;
                }
            }
            if (writeFile && crossings <= 10) {
                BufferedWriter bw = new BufferedWriter(new FileWriter("./checkby/" + minFilename + ".cr" + crossings + ".cross"));
                for (int i = 0; i < crossings; i++) {
                    bw.write("((" + (crossingsE1[i] + 1) + "," + (crossingsE2[i] + 1) + "), (" + (crossingsE3[i] + 1) + "," + (crossingsE4[i] + 1) + "))");
                    bw.newLine();
                }
                System.out.println("Writing file : " + minFilename + ".cr" + crossings + ".cross");
                bw.close();
                for (int i = crossings + 1; i < 10000; i++) {
                    File temp = new File("./checkby/" + minFilename + ".cr" + i + ".cross");
                    if (temp.exists()) {
                        System.out.println("Deleting " + temp.getName() + "!");
                        temp.delete();
                    }
                }
                System.out.println("Checked for other files.");
            }
        } catch (Exception e) {
            System.out.println(e);
        }


        int minCrossings = 1000;
        File minFile = null;

        File checkDir = new File("./checkby");
        File[] files = checkDir.listFiles();
        System.out.print("Checking files ");
        double mark = 0.01;
        int divideup = 1;
        int rn = (int) (Math.random() * divideup);
        if (startPoint == -1)
            startPoint = rn;
        int counti = 0;
        for (int ii = Math.max(0, startPoint - 20 * divideup); ii < files.length + startPoint; ii += divideup) {
            counti++;
            if (counti == 1000)
                divideup = 5;
            int i = 0;
            if (ii >= files.length)
                i = ii - files.length;
            else
                i = ii + 0;
            if (files[i].getName().indexOf(".tar") != -1 || files[i].getName().indexOf(".cross") != -1)
                continue;

            if ((counti + 0.0) / files.length > mark) {
                mark += 0.01;
                System.out.print(".");
            }

            File tempcr1 = new File("./checkby/" + files[i].getName() + ".cr1.cross");
            File tempcr2 = new File("./checkby/" + files[i].getName() + ".cr2.cross");
            File tempcr3 = new File("./checkby/" + files[i].getName() + ".cr3.cross");
            File tempcr4 = new File("./checkby/" + files[i].getName() + ".cr4.cross");
            File tempcr5 = new File("./checkby/" + files[i].getName() + ".cr5.cross");
            File tempcr6 = new File("./checkby/" + files[i].getName() + ".cr6.cross");
            File tempcr7 = new File("./checkby/" + files[i].getName() + ".cr7.cross");
            File tempcr8 = new File("./checkby/" + files[i].getName() + ".cr8.cross");
            File tempcr9 = new File("./checkby/" + files[i].getName() + ".cr9.cross");
            File tempcr10 = new File("./checkby/" + files[i].getName() + ".cr10.cross");
            if (!files[i].getName().equals(minFilename) && files[i].getName().indexOf(".cross") == -1 && !tempcr1.exists() && !tempcr2.exists() && !tempcr3.exists() && !tempcr4.exists() && !tempcr5.exists() && !tempcr6.exists() && !tempcr7.exists() && !tempcr8.exists() && !tempcr9.exists() && !tempcr10.exists()) {
                //System.out.println(files[i].getName());
                //System.out.println("Opening " + files[i].getName());
                int[][] newArcs = new int[26][3];
                int[] d = new int[26];
                try {
                    BufferedReader br = new BufferedReader(new FileReader(files[i]));
                    for (int j = 0; j < 39; j++) {
                        String line = br.readLine();
                        StringTokenizer tokens = new StringTokenizer(line);
                        int v1 = Integer.parseInt(tokens.nextToken());
                        int v2 = Integer.parseInt(tokens.nextToken());
                        newArcs[v1 - 1][d[v1 - 1]++] = v2;
                        newArcs[v2 - 1][d[v2 - 1]++] = v1;
                    }
                    br.close();
                    graph.setArcs(newArcs, d, 26, true);
                    findCrossings();
                    if (crossings < minCrossings) {
                        minCrossings = crossings;
                        minFile = files[i];
                        startPoint = i;
                    }
                    if (minCrossings <= 10) {
                        //startPoint = i;
                        break;
                    }


                } catch (Exception e) {
                    System.out.println(e);
                    System.out.println(files[i].getName());
                }

            }
        }
        //if(minCrossings > 10)
        //startPoint = -1;
        System.out.println(".");
        int[][] newArcs = new int[26][3];
        int[] d = new int[26];

        minFilename = minFile.getName();
        try {
            BufferedReader br = new BufferedReader(new FileReader(minFile));
            for (int j = 0; j < 39; j++) {
                String line = br.readLine();
                StringTokenizer tokens = new StringTokenizer(line);
                int v1 = Integer.parseInt(tokens.nextToken());
                int v2 = Integer.parseInt(tokens.nextToken());
                newArcs[v1 - 1][d[v1 - 1]++] = v2;
                newArcs[v2 - 1][d[v2 - 1]++] = v1;
            }
            br.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        graph.setArcs(newArcs, d, 26, true);
        findCrossings();
        // System.out.println("Opening " + minFilename + " : Crossings = " + crossings);
        validate();
        repaint();
    }


    public void pasteGraph(Graph g) {

        setUndoState();
        graph.addSubgraph(g, xTopLeft + 2 * radius, yTopLeft + 2 * radius, 1);

    }

    public double[] getGridData() {

        return new double[]{gridSpacing, gridOffsetX, gridOffsetY};
    }

    public void setGridlines(boolean gridlines) {
        showGridlines = gridlines;
    }

    public void setSnapToGrid(boolean snap) {
        snapToGrid = snap;
    }


}
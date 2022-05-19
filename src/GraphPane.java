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
    int nodeToSelect = -1;
    int nodeToRelabel = -1;
    int nodeToDelete = -1;
    int nodeToDominate = -1;
    int[] edgeToDelete = new int[2];
    int[] edgeHighlighted = new int[2];
    int radius = 12;
    double scale = 1;

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
        edgeToDelete[0] = -1;
        edgeToDelete[1] = -1;

        if(System.getProperty("java.version").startsWith("1.")){
            mouseOffsetX2 = 8;
            mouseOffsetY2 = 52;
        }


        setDefaultColors(parent.getDefaultColors());

        addMouseMotionListener(this);
        addMouseListener(this);

        addMouseWheelListener(
            new MouseWheelListener() {
                public void mouseWheelMoved(MouseWheelEvent e) {
                    int rotation = e.getWheelRotation();
                    mouseScrolled(rotation);
                }
            });

        defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        parent.changeSelectedOption(-1);

        thisGraphPane = this;
    }

    /**
     * Rescales either the gridlines or the graph when mouse wheel is scrolled
     * @param rotation how far the scroll wheel has moved
     */
    private void mouseScrolled(int rotation){
        if (selectedOption == GRID_OPTION) {
            gridSpacing = Math.max(gridSpacing * (1 - rotation / 6.0), 5.0);
            gridSpacing = Math.min(gridSpacing, 500);

        } else {
            double oldScale = scale;
            setScale(scale *(1-rotation/6.0));

            int mouseX = mouseX();
            int mouseY = mouseY();

            xTopLeft = xTopLeft + (int) Math.round(mouseX / oldScale - (mouseX) / scale);
            yTopLeft = yTopLeft + (int) Math.round((mouseY) / oldScale - (mouseY) / scale);
        }

        repaint();
    }

    /**
     * Required function for MouseListener. Runs functions for when the mouse is pressed then released without being moved.
     * @param e the provided MouseEvent
     */
    public void mouseClicked(MouseEvent e) {

        if (e.getButton() == MouseEvent.BUTTON3) {

            if(selectedOption != SELECT_OPTION){
                graph.deselectAll();
            }

            resetMouseVars();

        }

    }

    /**
     * Required function for MouseListener. Runs functions for when the mouse is pressed.
     * @param e the provided MouseEvent
     */
    public void mousePressed(MouseEvent e) {

        int mouseX = mouseX();
        int mouseY = mouseY();

        if (e.getButton() == MouseEvent.BUTTON1) {
            leftButtonPressed = true;
            if (rightButtonPressed && nodeSelected == -1) {
                setCursor(new Cursor(Cursor.MOVE_CURSOR));
            }
            xClicked = mouseX;
            yClicked = mouseY;
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            rightButtonPressed = true;
            if (leftButtonPressed && nodeSelected == -1) {
                setCursor(new Cursor(Cursor.MOVE_CURSOR));
            }
            xClicked = mouseX;
            yClicked = mouseY;
        }

        if (graph == null) return;

        int vertex_hovered = vertexContaining(mouseX, mouseY);

        if (e.getButton() == MouseEvent.BUTTON1) {
            if (selectedOption == DEFAULT_OPTION) {
                startDraggingVertex(mouseX, mouseY);
            }
            if (selectedOption == VERTEX_OPTION) {
                startedCreatingVertex = true;
            }
            if (selectedOption == EDGE_OPTION) {
                nodeSelectedForEdge = nodeHighlighted;
            }
            if (selectedOption == SELECT_OPTION) {
                if(nodeHighlighted == -1) startedSelection = true;
                nodeToSelect = vertex_hovered;
            }
            if (selectedOption == ROTATE_OPTION) {
                beginRotating(mouseX, mouseY);
            }
            if(selectedOption == RELABEL_OPTION){
                nodeToRelabel = vertex_hovered;
            }
            if(selectedOption == ERASER_OPTION){
                nodeToDelete = vertex_hovered;
            }
            if(selectedOption == SCISSORS_OPTION){
                edgeToDelete[0] = edgeHighlighted[0];
                edgeToDelete[1] = edgeHighlighted[1];
            }
            if(selectedOption == DOM_OPTION){
                nodeToDominate = vertex_hovered;
            }

        }
        repaint();
    }

    /**
     * Required function for MouseListener. Runs functions for when the mouse is released.
     * @param e the provided MouseEvent
     */
    public void mouseReleased(MouseEvent e) {

        if (e.getButton() == MouseEvent.BUTTON1) {

            int mouseX = mouseX();
            int mouseY = mouseY();

            int vertex_hovered = vertexContaining(mouseX, mouseY);

            if (selectedOption == DEFAULT_OPTION) {
                nodeSelected = -1;
            }
            if (selectedOption == VERTEX_OPTION) {
                createVertex(mouseX, mouseY);
            }
            if (selectedOption == EDGE_OPTION) {
                finishEdge();
            }
            if (selectedOption == SELECT_OPTION) {
                finishSelectionBox(mouseX, mouseY);
                toggleSelection(vertex_hovered);
            }
            if (selectedOption == DOM_OPTION){
                dominateVertex(vertex_hovered);
            }
            if (selectedOption == RELABEL_OPTION){
                relabelVertex(vertex_hovered);
            }
            if (selectedOption == ERASER_OPTION){
                eraseVertex(vertex_hovered);
            }
            if (selectedOption == SCISSORS_OPTION){
                deleteHighlightedEdge();
            }
            if (selectedOption == ROTATE_OPTION) {
                stopRotating();
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

    /**
     * Required function for MouseListener. Runs functions for when the mouse is moved onto the GraphPane.
     * @param e the provided MouseEvent
     */
    @Override
    public void mouseEntered(MouseEvent e) {

    }

    /**
     * Required function for MouseListener. Runs functions for when the mouse is moved off of the GraphPane.
     * @param e the provided MouseEvent
     */
    @Override
    public void mouseExited(MouseEvent e) {

    }

    /**
     * Required function for MouseListener. Runs functions for when the mouse button is held, then the pointer moved.
     * @param e the provided MouseEvent
     */
    public void mouseDragged(MouseEvent e) {

        int mouseX = mouseX();
        int mouseY = mouseY();

        if (selectedOption == DEFAULT_OPTION) {
            dragVertex(mouseX, mouseY);
        }
        if (selectedOption == SCISSORS_OPTION) {
            highlightEdge(mouseX, mouseY);
        }
        if (selectedOption == ROTATE_OPTION) {
            stopRotating();
        }
        if (selectedOption == GRID_OPTION) {
            panGrid(mouseX, mouseY);
        }

        if ((leftButtonPressed && rightButtonPressed) || (selectedOption == DEFAULT_OPTION && nodeSelected == -1)) {
            panGraph(mouseX, mouseY);
        }

        mouseMoved(e);
        repaint();
    }

    /**
     * Required function for MouseListener. Runs functions for when the mouse is moved on the GraphPane.
     * @param e the provided MouseEvent
     */
    public void mouseMoved(MouseEvent e) {

        if (selectedOption == DEFAULT_OPTION || selectedOption == EDGE_OPTION || selectedOption == ERASER_OPTION || selectedOption == RELABEL_OPTION || selectedOption == DOM_OPTION || selectedOption == SELECT_OPTION) {
            nodeHighlighted = vertexContaining(mouseX(), mouseY());
        }
        if (selectedOption == SCISSORS_OPTION) {
            highlightEdge(mouseX(), mouseY());
        }
        repaint();
    }

    /**
     * Calculates the x-coordinate of the mouse pointer on the GraphPane
     * @return x-coordinate of pointer relative to top left of GraphPane
     */
    public int mouseX(){
        return (MouseInfo.getPointerInfo().getLocation().x - getLocationOnScreen().x - mouseOffsetX2);
    }

    /**
     * Calculates the y-coordinate of the mouse pointer on the GraphPane
     * @return y-coordinate of pointer relative to top left of GraphPane
     */
    public int mouseY(){
        return (MouseInfo.getPointerInfo().getLocation().y - getLocationOnScreen().y - mouseOffsetY2);
    }

    /**
     * Resets the various variables used for mouse related functions.
     */
    private void resetMouseVars(){
        if (nodeSelected != -1) {
            graph.setAllPos(originalX, originalY);
            nodeSelected = -1;
            repaint();
        }
        if (startedCreatingVertex) {
            startedCreatingVertex = false;
            repaint();
        }
        if (nodeSelectedForEdge > -1) {
            nodeSelectedForEdge = -1;
            repaint();
        } else {
            defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
            parent.changeSelectedOption(-1);
            startedSelection = false;
            edgeHighlighted[0] = -1;
            edgeHighlighted[1] = -1;
            parent.changeCursor(defaultCursor);
            parent.changeSelectedOption(-1);
        }
    }

    /**
     * Erases the vertex specified on mouse release, only if it was under the pointer when the mouse was pressed.
     * @param vertex the vertex to be removed.
     */
    public void eraseVertex(int vertex){
        if(vertex == -1) return;
        if(vertex != nodeToDelete) return;

        if (graph.isSelected(vertex)) {
            setUndoState();
            for (int i = graph.getN() - 1; i >= 0; i--) {
                if (graph.isSelected(i)) {
                    graph.deleteVertex(i + 1);
                }
            }
        } else {
            setUndoState();

            graph.deleteVertex(vertex + 1);

        }

        nodeHighlighted = -1;
        repaint();


    }

    /**
     * Toggles domination of the vertex specified on mouse release, only if it was under the pointer when the mouse was pressed.
     * @param vertex the vertex whose domination state is to be toggled.
     */
    public void dominateVertex(int vertex){

        if(vertex == -1) return;
        if(vertex != nodeToDominate) return;

        setUndoState();
        if (graph.isSelected(vertex)) {
            graph.toggleDom(vertex);
            int val = graph.inDomset(vertex + 1);
            for (int i = 0; i < graph.getN(); i++) {
                if (graph.isSelected(i)) {
                    graph.setDomValue(i, val);
                }
            }
        } else {
            graph.toggleDom(vertex);
        }
        repaint();

    }

    /**
     * Triggers relabel dialog for the vertex specified on mouse release, only if it was under the pointer when the mouse was pressed.
     * @param vertex the vertex to be relabeled.
     */
    public void relabelVertex(int vertex){
        if(vertex == -1) return;
        if(vertex != nodeToRelabel) return;

        RelabelDialog rd = new RelabelDialog(parent, (vertex + 1), graph.getN());

        if (!rd.getCancelled()) {

            int newLabel = rd.getNewLabel();

            setUndoState();

            if (rd.getIncrement()) {
                // Increment rest
                if ((vertex + 1) < newLabel) {
                    for (int i = vertex + 1; i < newLabel; i++) {
                        graph.swapVertices(i, i + 1);
                    }
                }
                else {
                    for (int i = vertex + 1; i > newLabel; i--) {
                        graph.swapVertices(i, i - 1);
                    }
                }
            } else {
                // Swap labels
                graph.swapVertices(vertex + 1, newLabel);
            }

            repaint();
        }


    }

    /**
     * Initiates the dragging procedure for the currently highlighted vertex.
     * @param mouseX initial x coordinate of the pointer when the vertex was highlighted.
     * @param mouseY initial y coordinate of the pointer when the vertex was highlighted.
     */
    private void startDraggingVertex(int mouseX, int mouseY){
        nodeSelected = vertexContaining(mouseX, mouseY);

        if(nodeSelected != -1){
            setUndoState();
            originalX = new double[graph.getN()];
            originalY = new double[graph.getN()];
            for (int j = 0; j < graph.getN(); j++) {
                originalX[j] = graph.getXPos(j);
                originalY[j] = graph.getYPos(j);
            }
            offsetX = (int) Math.round(mouseX / scale + xTopLeft - graph.getXPos(nodeSelected));
            offsetY = (int) Math.round(mouseY / scale + yTopLeft - graph.getYPos(nodeSelected));
        }

    }

    /**
     * Updates the location of the currently held vertex to the given mouse coordinates.
     * Only functions if startDraggingVertex has been called.
     * @param mouseX current y position of the pointer on GraphPane
     * @param mouseY current y position of the pointer on GraphPane
     */
    private void dragVertex(int mouseX, int mouseY){
        if (nodeSelected == -1) return;

        double newX = xTopLeft + (mouseX / scale - offsetX);
        double newY = yTopLeft + (mouseY / scale - offsetY);

        if (snapToGrid) {
            newX = Math.round((newX - gridOffsetX) / gridSpacing) * gridSpacing + gridOffsetX;
            newY = Math.round((newY - gridOffsetY) / gridSpacing) * gridSpacing + gridOffsetY;
        }

        if (graph.isSelected(nodeSelected)) {
            double origX = graph.getXPos(nodeSelected);
            double origY = graph.getYPos(nodeSelected);

            for (int i = 0; i < graph.getN(); i++) {
                if (graph.isSelected(i) && i != nodeSelected) {
                    graph.setXPos(i, graph.getXPos(i) + newX - origX);
                    graph.setYPos(i, graph.getYPos(i) + newY - origY);
                }
            }
        }

        graph.setXPos(nodeSelected, newX);
        graph.setYPos(nodeSelected, newY);

    }

    /**
     * Creates a new vertex at the given pointer coordinates.
     * Only functions if startedCreatingVertex is true.
     * @param mouseX x coordinate of the pointer in GraphPane.
     * @param mouseY y coordinate of the pointer in GraphPane.
     */
    public void createVertex(int mouseX, int mouseY){
        if (startedCreatingVertex) {
            setUndoState();

            startedCreatingVertex = false;
            graph.setN(graph.getN() + 1);

            double vertX = (mouseX / scale + xTopLeft);
            double vertY = (mouseY / scale + yTopLeft);

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

    /**
     * Deletes the currently highlighted edge when the mouse is released.
     * Only functions if the same edge was highlighted when the mouse was pressed.
     */
    public void deleteHighlightedEdge(){
        if (edgeHighlighted[0] == -1 || edgeHighlighted[1] == -1) return;
        if (edgeToDelete[0] != edgeHighlighted[0] || edgeToDelete[1] != edgeHighlighted[1]) return;

        setUndoState();

        graph.deleteArc(edgeHighlighted[0] + 1, edgeHighlighted[1] + 1);
        graph.deleteArc(edgeHighlighted[1] + 1, edgeHighlighted[0] + 1);
        edgeHighlighted[0] = -1;
        edgeHighlighted[1] = -1;
        repaint();

    }

    /**
     * Toggles the selection state of the highlighted vertex when mouse is released.
     * Only functions if this vertex was highlighted when the mouse was pressed.
     * @param vertex vertex whose selection state is to be toggled.
     */
    public void toggleSelection(int vertex){
        if(vertex == -1) return;
        if(vertex != nodeToSelect) return;

        if(graph.isSelected(vertex)){
            graph.deselect(vertex);
        } else {
            graph.select(vertex);
        }
    }

    /**
     * Changes to selection state of the vertices within a box determined by the coordinates of the pointer at the time of
     * mouse being pressed and mouse being released.
     * @param mouseX x coordinate of the pointer relative to GraphPane at time of mouse release.
     * @param mouseY y coordinate of the pointer relative to GraphPane at time of mouse release.
     */
    private void finishSelectionBox(int mouseX, int mouseY){
        if (!startedSelection) return;

        int leftX = (int) Math.round(xTopLeft + (xClicked) / scale);
        int rightX = (int) Math.round(xTopLeft + (mouseX) / scale);
        if (leftX > rightX) {
            int temp = leftX;
            leftX = rightX;
            rightX = temp;
        }

        int bottomY = (int) Math.round(yTopLeft + (yClicked) / scale);
        int topY = (int) Math.round(yTopLeft + (mouseY) / scale);
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

    /**
     * Creates a new edge between the vertex that was highlighted at the time of mouse press and the vertex that was
     * highlighted at the timed of mouse release.
     */
    private void finishEdge(){
        if (nodeSelectedForEdge == -1) return;

        if (nodeHighlighted != -1 && nodeHighlighted != nodeSelectedForEdge) {
            setUndoState();

            graph.addArc(nodeSelectedForEdge + 1, nodeHighlighted + 1);
            graph.addArc(nodeHighlighted + 1, nodeSelectedForEdge + 1);
        }

        nodeSelectedForEdge = -1;
        repaint();

    }

    /**
     * Highlights the closest edge within SCISSORS_DISTANCE of the pointer.
     * @param mouseX x coordinate of the pointer relative to the GraphPane.
     * @param mouseY y coordinate of the pointer relative to the GraphPane.
     */
    private void highlightEdge(int mouseX, int mouseY){
        double xScreen = mouseX / scale + xTopLeft;
        double yScreen = mouseY / scale + yTopLeft;

        int[][] arcs = graph.getArcs();
        int N = graph.getN();
        int[] degrees = graph.getDegrees();

        boolean edgeFound = false;
        double closest = -1;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < degrees[i]; j++) {
                int v2 = arcs[i][j] - 1;


                double x1 = graph.getXPos(i);
                double y1 = graph.getYPos(i);
                double x2 = graph.getXPos(v2);
                double y2 = graph.getYPos(v2);

                double dist = Math.sqrt(distSquareToEdge(xScreen, yScreen, x1, y1, x2, y2));

                if(dist <= SCISSORS_DISTANCE){

                    if(closest < dist &&closest != -1){
                        continue;
                    }

                    closest = dist;

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


    /**
     * Calculates the square of the distance from (x,y) to line segment (x1,y1)--(x2,y2), within the lines
     * perpendicular to the line segment, that meet each endpoint of the line segment.
     * If (x,y) is not within this section, it returns a value greater than the square of SCISSORS_DISTANCE.
     * @param x x coordinate of the point.
     * @param y y coordinate of the point.
     * @param x1 x coordinate of the first endpoint of the line segment.
     * @param y1 y coordinate of the first endpoint of the line segment.
     * @param x2 x coordinate of the second endpoint of the line segment.
     * @param y2 y coordinate of the second endpoint of the line segment.
     * @return the square of the distance from the point to the line segment.
     */
    private double distSquareToEdge(double x, double y, double x1, double y1, double x2, double y2){
        double dist;

        double xp1 = x - x1;
        double yp1 = y - y1;

        double xp2 = x - x2;
        double yp2 = y - y2;

        double x12 = x1 - x2;
        double y12 = y1 - y2;

        double dotP1 = xp1*x12 + yp1*y12;
        double dotP2 = xp2*x12 + yp2*y12;

        if(dotP1 > 0){
            dist = 4*SCISSORS_DISTANCE*SCISSORS_DISTANCE;
        } else if(dotP2 < 0){
            dist = 4*SCISSORS_DISTANCE*SCISSORS_DISTANCE;
        } else {
            dist = (x12*yp1 - y12*xp1)*(x12*yp1 - y12*xp1)/(x12*x12 + y12*y12);
        }

        return dist;
    }

    /**
     * Initiates the rotation of the selected vertices of the graph around the mouse pointer.
     * @param mouseX x coordinate of the mouse pointer, relative to GraphPane.
     * @param mouseY y coordinate of the mouse pointer, relative to GraphPane.
     */
    private void beginRotating(int mouseX, int mouseY){
        timer = new Timer();
        startedRotating = true;
        setUndoState();
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        rotate((mouseX / scale + xTopLeft), (mouseY / scale + yTopLeft));
                    }
                }, 0, 1);

    }

    /**
     * Cancels the rotation of the graph around the pointer.
     */
    private void stopRotating(){
        if (startedRotating) {
            timer.cancel();
            startedRotating = false;
        }
    }

    /**
     * Updates the offset of the grid, relative to the change in mouse position since it was last clicked.
     * @param mouseX current x coordinate of the pointer relative to GraphPane.
     * @param mouseY current y coordinate of the pointer relative to GraphPane.
     */
    private void panGrid(int mouseX, int mouseY){
        gridOffsetX = gridOffsetX + (int) Math.round(1 * ((mouseX) / scale - xClicked / scale));
        gridOffsetY = gridOffsetY + (int) Math.round(1 * ((mouseY) / scale - yClicked / scale));
        xClicked = (mouseX);
        yClicked = (mouseY);
    }

    /**
     * Updates the offset of the viewpoint of GraphPane, relative to the change in mouse position since it was last clicked.
     * @param mouseX current x coordinate of the pointer relative to GraphPane.
     * @param mouseY current y coordinate of the pointer relative to GraphPane.
     */
    private void panGraph(int mouseX, int mouseY){
        xTopLeft = xTopLeft - (int) Math.round(1 * ((mouseX) / scale - xClicked / scale));
        yTopLeft = yTopLeft - (int) Math.round(1 * ((mouseY) / scale - yClicked / scale));
        xClicked = (mouseX);
        yClicked = (mouseY);
    }

    /**
     * Gets an array containing the default colours for this GraphPane.
     * @return the array of default colours.
     */
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

    /**
     * Sets the given colours as the new defaults.
     * @param colors the colours to be assigned as default colours.
     */
    public void setDefaultColors(Color[] colors) {
        backgroundColor = colors[0];
        defaultColor = colors[1];
        highlightedVertexFillColor = colors[2];
        highlightedVertexColor = colors[3];
        newEdgeColor = colors[4];
        deleteEdgeColor = colors[5];
    }


    /**
     * Makes the pixel at coordinate (x,y) of image transparent.
     * @param image the original image to make a modified copy of.
     * @param x the x coordinate of the pixel to make transparent.
     * @param y the y coordinate of the pixel to make transparent.
     * @return a new image with the specified pixel transparent.
     */
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

    /**
     * Updates the visible component of GraphPane with the current state of vertices, edges, and text.
     * @param gra the provided graphics object to be updated, which will be printed to the screen.
     */
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


        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g.scale(scale, scale);


        int N = graph.getN();

        boolean[] dv = graph.dominatedVertices(domTotal, domSecure, domConnected, domRoman, domWeakRoman);

        if (showGridlines) {
            drawGridlines(g);
        }


        if (displayCrossings) {
            drawCrossings(g);
        }


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


        for (int i = 0; i < N; i++) {
            if(i == nodeHighlighted || i == nodeSelectedForEdge) continue;
            if(i == edgeHighlighted[0] || i == edgeHighlighted[1]) continue;
            drawVertex(g, dv, i);

        }


        if (nodeSelectedForEdge != -1) {

            double centre1X = -xTopLeft + graph.getXPos(nodeSelectedForEdge);
            double centre1Y = -yTopLeft + graph.getYPos(nodeSelectedForEdge);

            drawEdge(g, centre1X, centre1Y, mouseX, mouseY, newEdgeColor, (float)Math.max(2f, 1.5f / scale));

        }


        if (edgeHighlighted[0] != -1 && edgeHighlighted[1] != -1) {
            drawEdge(g, edgeHighlighted[0], edgeHighlighted[1], deleteEdgeColor, (float) Math.max(2f, 1.5f / scale));
            drawVertex(g, dv, edgeHighlighted[0], deleteEdgeColor, true);
            drawVertex(g, dv, edgeHighlighted[1], deleteEdgeColor, true);
        }


        drawVertex(g, dv, nodeHighlighted);
        drawVertex(g, dv, nodeSelectedForEdge);


        if (startedCreatingVertex) {

            double newVertX = mouseX;
            double newVertY = mouseY;

            if (snapToGrid) {
                newVertX = Math.round((newVertX + xTopLeft - gridOffsetX) / gridSpacing) * gridSpacing + gridOffsetX - xTopLeft;
                newVertY = Math.round((newVertY + yTopLeft - gridOffsetY) / gridSpacing) * gridSpacing + gridOffsetY - yTopLeft;
            }

            drawVertex(g, newVertX, newVertY, radius, defaultColor, 1f);
        }


        if (displayDomination) {
            drawDominationText(g);
        }


        gra.drawImage(image, 0, 0, null);
    }

    /**
     * Draw a given vertex to the graphic g
     * @param g the graphic to draw the vertex onto.
     * @param dv the list of vertices which are dominated.
     * @param vertex the vertex to be drawn.
     */
    private void drawVertex(Graphics2D g,  boolean[] dv, int vertex){
        drawVertex(g, dv, vertex, defaultColor, false);
    }

    /**
     * Draw a given vertex to the graphic g
     * @param g the graphic to draw the vertex onto.
     * @param dv the list of vertices which are dominated.
     * @param vertex the vertex to be drawn.
     * @param st the default colour of the border of the vertex
     * @param override whether or not to force the vertex to be drawn with st as the border colour.
     */
    private void drawVertex(Graphics2D g, boolean[] dv, int vertex, Color st, boolean override){

        if(vertex == -1) return;

        float weight = 1;
        Color fill = Color.WHITE;
        Color stroke = st;
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


        if(override){
            stroke = st;
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

        if(graph.isSelected(vertex)){
            int spacing = radius + 6;
            drawSelectionBox(g, (int)x - spacing, (int)y - spacing, (int)x + spacing, (int)y + spacing, defaultColor);
        }

    }

    /**
     * Draw a given vertex to the graphic g
     * @param g the graphic to draw the vertex onto.
     * @param x the x coordinate of the vertex.
     * @param y the y coordinate of the vertex.
     * @param rad the radius of the vertex.
     * @param stroke the colour of the border of the vertex.
     * @param weight the thickness of the border of the vertex.
     */
    private void drawVertex(Graphics2D g, double x, double y, int rad, Color stroke, float weight){
        g.setStroke(new BasicStroke(weight));
        g.setColor(stroke);

        g.drawOval((int)Math.round(x) - rad, (int)Math.round(y)-rad, 2*rad, 2*rad);
    }

    /**
     * Draw a given vertex to the graphic g
     * @param g the graphic to draw the vertex onto.
     * @param x the x coordinate of the vertex.
     * @param y the y coordinate of the vertex.
     * @param rad the radius of the vertex.
     * @param stroke the colour of the border of the vertex.
     * @param fill the colour of the interior of the vertex.
     * @param weight the thickness of the border of the vertex.
     */
    private void drawVertex(Graphics2D g, double x, double y, int rad, Color stroke, Color fill, float weight){
        g.setColor(fill);
        g.fillOval((int)Math.round(x) - rad, (int)Math.round(y)-rad, 2*rad, 2*rad);

        drawVertex(g, x, y, rad, stroke, weight);
    }

    /**
     * Draw a given vertex to the graphic g
     * @param g the graphic to draw the vertex onto.
     * @param x the x coordinate of the vertex.
     * @param y the y coordinate of the vertex.
     * @param rad the radius of the vertex.
     * @param stroke the colour of the border of the vertex.
     * @param weight the thickness of the border of the vertex.
     * @param label text to be drawn at the centre of the vertex.
     * @param font the font of the label.
     */
    private void drawVertex(Graphics2D g, double x, double y, int rad, Color stroke, float weight, String label, Font font){
        drawVertex(g, x, y, rad, stroke, weight);

        g.setColor(stroke);
        g.setFont(font);

        FontMetrics fm = g.getFontMetrics();

        int text_x = (int)Math.round(x-fm.stringWidth(label)/2.0);
        int text_y = (int)Math.round(y-fm.getHeight()/2.0) +fm.getAscent();


        g.drawString(label, text_x, text_y);
    }

    /**
     * Draw a given vertex to the graphic g
     * @param g the graphic to draw the vertex onto.
     * @param x the x coordinate of the vertex.
     * @param y the y coordinate of the vertex.
     * @param rad the radius of the vertex.
     * @param stroke the colour of the border of the vertex.
     * @param fill the colour of the interior of the vertex.
     * @param weight the thickness of the border of the vertex.
     * @param label text to be drawn at the centre of the vertex.
     * @param font the font of the label.
     */
    private void drawVertex(Graphics2D g, double x, double y, int rad, Color stroke, Color fill, float weight, String label, Font font){
        g.setColor(fill);
        g.fillOval((int)Math.round(x) - rad, (int)Math.round(y)-rad, 2*rad, 2*rad);

        drawVertex(g, x, y, rad, stroke, weight, label, font);
    }

    /**
     * Draw a straight line edge between vertices v1 and v2.
     * @param g graphics to be drawn to.
     * @param v1 first vertex.
     * @param v2 second vertex.
     * @param c colour of the edge.
     * @param weight thickness of the edge.
     */
    private void drawEdge(Graphics2D g, int v1, int v2, Color c, float weight){

        double x1 = -xTopLeft + graph.getXPos(v1);
        double y1 = -yTopLeft + graph.getYPos(v1);
        double x2 = -xTopLeft + graph.getXPos(v2);
        double y2 = -yTopLeft + graph.getYPos(v2);

        drawEdge(g, x1, y1, x2, y2, c, weight);
    }

    /**
     * Draw a straight line edge between points (x1,y1) and (x2,y2).
     * @param g graphics to be drawn to.
     * @param x1 x coordinate of the first point.
     * @param y1 y coordinate of the first point.
     * @param x2 x coordinate of the second point.
     * @param y2 y coordinate of the second point.
     * @param c colour of the edge.
     * @param weight thickness of the edge.
     */
    private void drawEdge(Graphics2D g, double x1, double y1, double x2, double y2, Color c, float weight){

        g.setStroke(new BasicStroke(weight));
        g.setColor(c);

        g.drawLine((int)Math.round(x1), (int)Math.round(y1), (int)Math.round(x2), (int)Math.round(y2));

    }

    /**
     * Draws the background gridlines to the GraphPane.
     * @param g the graphic object to be drawn to.
     */
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

    /**
     * Draws a circle at the point of crossings and displays the text showing the number of crossings.
     * @param g graphic object to be drawn to.
     */
    private void drawCrossings(Graphics2D g){
        findCrossings();
        g.setColor(crossColor);
        for (int i = 0; i < crossings; i++) {
            g.fillOval(Math.round(-xTopLeft + crossingsX[i]) - radius, Math.round(-yTopLeft + crossingsY[i]) - radius, 2 * radius, 2 * radius);
        }

        g.setFont(g.getFont().deriveFont((float) (20 / scale)));
        g.drawString(("Crossings: " + crossings), Math.round(20 / scale), Math.round(20 / scale));
    }

    /**
     * Draws the domination text to the GraphPane.
     * @param g graphic object to be drawn to.
     */
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
        g.setFont(g.getFont().deriveFont((float) (20)).deriveFont(Font.PLAIN));

        g.drawString(("Undominated Vertices: " + ud + " (" + graph.getDomSize() + ")"),getWidth() - 300,20);


        StringJoiner sj = new StringJoiner(", ");

        if (domTotal) sj.add("Total");
        if (domConnected) sj.add("Connected");
        if (domSecure) sj.add("Secure");
        if (domWeakRoman)  sj.add("Weak Roman");
        if (domRoman) sj.add("Roman");
        if (sj.length() > 0) {
            String types = "(" + sj.toString() + ")";
            g.setFont(g.getFont().deriveFont((float) (14)));
            g.drawString(types, getWidth() - 75 - 6 * types.length(), 32);
        }

        g.scale(scale, scale);
    }

    /**
     * Draws a rectangular dashed box specified by coordinates.
     * @param g graphic object to be drawn to.
     * @param x1 x coordinate of one corner.
     * @param y1 y coordinate of one corner.
     * @param x2 x coordinate of the diagonally opposite corner.
     * @param y2 y coordinate of the diagonally opposite corner.
     * @param c colour of the dashed line.
     */
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

    /**
     * Changes the Graph object associated with this GraphPane.
     * @param gr a Graph.
     */
    public void setGraph(Graph gr) {
        graph = gr;
    }

    /**
     * Gets the graph object associated with this GraphPane.
     * @return the graph object being drawn on this panel.
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * Sets the zoom level of the viewport of this GraphPane.
     * @param new_scale the new zoom level.
     */
    public void setScale(double new_scale) {

        scale = new_scale;

        if(scale > SCALE_MAX) scale = SCALE_MAX;
        if(scale < SCALE_MIN) scale = SCALE_MIN;

    }

    /**
     * Sets the upper left corner of the viewport of this GraphPane.
     * @param xl the x coordinate of the offset.
     * @param yl the y coordinate of the offset.
     */
    public void setTopLeft(int xl, int yl) {
        xTopLeft = xl;
        yTopLeft = yl;
    }

    /**
     * Sets the visual radius of the vertices of the current graph.
     * @param ra the radius.
     */
    public void setRadius(int ra) {
        radius = ra;
    }

    /**
     * Gets the current visual radius of the vertices of the current graph.
     * @return the visual radius of vertices.
     */
    public int getRadius() {
        return radius;
    }

    /**
     * Gets the zoom level of this GraphPane.
     * @return the zoom level.
     */
    public double getScale() {
        return scale;
    }

    /**
     * Gets the x offset of the current viewport of the this GraphPane.
     * @return x offset of the current viewport.
     */
    public int getXTopLeft() {
        return xTopLeft;
    }

    /**
     * Gets the y offset of the current viewport of this GraphPane
     * @return y offset of the current viewport.
     */
    public int getYTopLeft() {
        return yTopLeft;
    }

    /**
     * Sets the new default cursor.
     * @param cursor the new look of the cursor.
     */
    public void setDefaultCursor(Cursor cursor) {
        defaultCursor = cursor;
    }

    /**
     * Changes the currently selected tool or function.
     * @param option the new option. One of those specified in this GraphPane.
     */
    public void setSelectedOption(int option) {
        selectedOption = option;
    }

    /**
     * Gets whether or not vertex labels should be displayed while drawing the graph.
     * @return whether or not labels should be displayed.
     */
    public boolean getDisplayVertexLabels() {
        return displayVertexLabels;
    }

    /**
     * Sets whether or not vertex labels should be displayed.
     * @param dvl whether or not vertex labels should be displayed.
     */
    public void setDisplayVertexLabels(boolean dvl) {
        displayVertexLabels = dvl;
    }

    /**
     * Gets whether or not edge crossings should be marked.
     * @return whether or not edge crossings should be marked.
     */
    public boolean getDisplayCrossings() {
        return displayCrossings;
    }

    /**
     * Sets whether or not edge crossings should be marked.
     * @param dc whether or not edge crossings should be marked.
     */
    public void setDisplayCrossings(boolean dc) {
        displayCrossings = dc;
    }

    /**
     * Gets whether or not the domination state of vertices should be displayed, along with the top right domination status.
     * @return whether or not domination status should be displayed.
     */
    public boolean getDisplayDomination() {
        return displayDomination;
    }

    /**
     * Sets whether or not the domination state of vertices should be displayed, along with the top right domination status.
     * @param dd whether or not domination should be displayed.
     */
    public void setDisplayDomination(boolean dd) {
        displayDomination = dd;
    }

    /**
     * Gets whether or not the domination status to show includes total domination conditions.
     * @return whether or not total domination status is displayed.
     */
    public boolean getDomTotal() {
        return domTotal;
    }

    /**
     * Sets whether or not the domination status to show includes total domination conditions.
     * @param dt whether or not total domination status is displayed.
     */
    public void setDomTotal(boolean dt) {
        domTotal = dt;
    }

    /**
     * Gets whether or not the domination status to show includes secure domination conditions.
     * @return whether or not secure domination status is displayed.
     */
    public boolean getDomSecure() {
        return domSecure;
    }

    /**
     * Sets whether or not the domination status to show includes secure domination conditions.
     * @param ds whether or not secure domination status is displayed.
     */
    public void setDomSecure(boolean ds) {
        domSecure = ds;
    }

    /**
     * Gets whether or not the domination status to show includes connected domination conditions.
     * @return whether or not connected domination status is displayed.
     */
    public boolean getDomConnected() {
        return domConnected;
    }

    /**
     * Sets whether or not the domination status to show includes connected domination conditions.
     * @param dc whether or not connected domination status is displayed.
     */
    public void setDomConnected(boolean dc) {
        domConnected = dc;
    }

    /**
     * Gets whether or not the domination status to show includes roman domination conditions.
     * @return whether or not roman domination status is displayed.
     */
    public boolean getDomRoman() {
        return domRoman;
    }

    /**
     * Sets whether or not the domination status to show includes roman domination conditions.
     * @param dr whether or not roman domination status is displayed.
     */
    public void setDomRoman(boolean dr) {
        domRoman = dr;
    }

    /**
     * Gets whether or not the domination status to show includes weak roman domination conditions.
     * @return whether or not weak roman domination status is displayed.
     */
    public boolean getDomWeakRoman() {
        return domWeakRoman;
    }

    /**
     * Sets whether or not the domination status to show includes weak roman domination conditions.
     * @param dwr whether or not weak roman domination status is displayed.
     */
    public void setDomWeakRoman(boolean dwr) {
        domWeakRoman = dwr;
    }

    /**
     * Begins a timer that updates the vertex positions using a spring algorithm.
     */
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

    /**
     * Cancels the timer that updates vertex positions using a spring condition.
     */
    public void cancelSpring() {
        if (graph.calculatingSpring) {
            springTimer.cancel();
            graph.calculatingSpring = false;
        }
    }

    /**
     * Rotates all selected vertices around a specified point.
     * @param rotateX x coordinate of the point of rotation.
     * @param rotateY y coordinate of the point of rotatin.
     */
    public void rotate(double rotateX, double rotateY) {
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

    /**
     * Gets the size of vertex label text.
     * @return the size of the vertex label text.
     */
    public int getTextSize() {
        return textSize;
    }

    /**
     * Sets the size of the vertex label text.
     * @param size the new size of the vertex label text.
     */
    public void setTextSize(int size) {
        textSize = size;
    }

    /**
     * Sets whether or not an exported image will have a transparent background.
     * @param swtb whether or not an exported image will have a transparent background.
     */
    public void setSavingWithTransparentBackground(boolean swtb) {
        savingWithTransparentBackground = swtb;
    }

    /**
     * Gets the image currently being displayed on this tab.
     * @return the image currently being displayed.
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Gets the menu item for this graph, listed under the window option.
     * @return the menu item for this graph.
     */
    public JMenuItem getMenuItem() {
        return menuItem;
    }

    /**
     * Sets the menu item for this graph, listed under the window option.
     * @param mi the new menu item for this graph.
     */
    public void setMenuItem(JMenuItem mi) {
        menuItem = mi;
    }

    /**
     * Creates a new undo point.
     */
    public void setUndoState() {
        undoState.addItem(graph);
        setUndoAvailable(true);
        setRedoAvailable(false);
        parent.checkSave();
    }

    /**
     * Reverts the graph to the previous undo point.
     */
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

    /**
     * Reverts the graph from the most recent undo point.
     */
    public void redo() {

        graph = undoState.redo(graph);
        parent.checkSave();
        if (!undoState.canRedo())
            parent.setRedoAvailable(false);
        parent.setUndoAvailable(true);
        validate();
        repaint();
    }

    /**
     * Sets the ability to use the undo menu item or keybinding.
     * @param available whether or not undo can be used.
     */
    public void setUndoAvailable(boolean available) {
        parent.setUndoAvailable(available);
    }

    /**
     * Sets the ability to use the redo menu item or keybinding.
     * @param available whether or not redo can be used.
     */
    public void setRedoAvailable(boolean available) {
        parent.setRedoAvailable(available);
    }

    /**
     * Gets the stacks of undo and redo states for this graph.
     * @return the UndoRedo object for this graph.
     */
    public UndoRedo getUndoState() {
        return undoState;
    }

    /**
     * Gets the parent component of this tab.
     * @return the parent component of this tab.
     */
    public UGVViewer getParent() {
        return parent;
    }

    /**
     * Returns the smallest index of any vertex currently under the mouse coordinates specified.
     * @param mouseX the x coordinate of the mouse.
     * @param mouseY the y coordinate of the mouse.
     * @return the smallest index of a vertex under the mouse.
     */
    public int vertexContaining(double mouseX, double mouseY) {
        for (int i = 0; i < graph.getN(); i++) {

            if (Math.pow(Math.pow(mouseX + xTopLeft * scale - graph.getXPos(i) * scale, 2) + Math.pow(mouseY + yTopLeft * scale - graph.getYPos(i) * scale, 2), 0.5) <= radius * scale) {
                return i;
            }
        }
        return -1;

    }

    /**
     * Finds and stores all crossings of the edges of the current graph.
     */
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


    /**
     * Pastes the specified graph relative to the top left of the current viewport of this graph.
     * @param g the graph to paste.
     */
    public void pasteGraph(Graph g) {

        setUndoState();
        graph.addSubgraph(g, xTopLeft + 2 * radius, yTopLeft + 2 * radius, 1);

    }

    /**
     * Gets the variables associated with the spacing and offset of the background gridlines.
     * @return a 3 element array [grid spacing, x offset, y offset].
     */
    public double[] getGridData() {

        return new double[]{gridSpacing, gridOffsetX, gridOffsetY};
    }

    /**
     * Set whether or not gridlines should be visible.
     * @param gridlines whether or not gridlines should be visible.
     */
    public void setGridlines(boolean gridlines) {
        showGridlines = gridlines;
    }

    /**
     * Set whether or not new vertices can only be placed on intersections of the gridlines.
     * @param snap whether or not new vertices can only be placed on intersections of the gridlines.
     */
    public void setSnapToGrid(boolean snap) {
        snapToGrid = snap;
    }

}
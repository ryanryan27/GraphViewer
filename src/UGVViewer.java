//package UGV;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;
import java.awt.image.*;
import java.awt.*;
import javax.swing.filechooser.*;
import javax.imageio.ImageIO;


@SuppressWarnings("Convert2Lambda")
public class UGVViewer extends JFrame implements MouseListener, WindowListener//, KeyListener
{
    //Holder for each tab
    JTabbedPane tabbedPane;

    //List of GraphPanes held in the tabbedPane
    GraphLinkedList graphPanels;

    //Menu items with global properties
    JMenu windowItem;
    JMenuItem saveFileItem;
    JMenuItem saveMultipleGraphsFileItem;
    JMenuItem undoEditItem;
    JMenuItem redoEditItem;
    JMenuItem copyItem;
    JMenuItem pasteItem;
    JMenuItem exportAsImageFileItem;
    JCheckBoxMenuItem displayVertexLabelsItem;
    JCheckBoxMenuItem displayCrossingsItem;
    JCheckBoxMenuItem displayDominationItem;
    JCheckBoxMenuItem domTotalItem;
    JCheckBoxMenuItem domSecureItem;
    JCheckBoxMenuItem domConnectedItem;
    JCheckBoxMenuItem domRomanItem;
    JCheckBoxMenuItem domWeakRomanItem;
    JCheckBoxMenuItem gridSnapItem;
    JCheckBoxMenuItem gridlinesItem;

    //Toolbar Components
    JPanel graphEditPane;
    JPanel buttonPane;
    JPanel bottomButtonPane;
    JPanel selectButtonPane;
    JSlider vertexSizeSlider;
    JSlider labelSizeSlider;
    JTextField vertexSizeField;
    JTextField labelSizeField;
    JLabel[] labels;

    //icons for the toolbar buttons
    ImageIcon[][] icons;

    //stores the state of each button
    boolean[] highlighted;
    boolean[] pressed;
    boolean[] chosen;


    //Number of graph interaction buttons in the toolbar.
    final int choices = 7;

    //number of graph property buttons in the toolbar
    final int bottomChoices = 4;

    //number of selection based buttons in the toolbar
    final int selectChoices = 2;

    //total number of buttons in the toolbar
    final int totalChoices = choices + bottomChoices + selectChoices;


    final Color toolbarColour = new Color(0.7529f, 0.7529f, 0.7529f);

    final int MIN_VERTEX_SIZE = 1;
    final int MAX_VERTEX_SIZE = 50;
    final int DEFAULT_VERTEX_SIZE = 12;
    final int MIN_LABEL_SIZE = 5;
    final int MAX_LABEL_SIZE = 50;
    final int DEFAULT_LABEL_SIZE = 12;

    final int BOTTOM_ARRANGE = choices;
    final int BOTTOM_PROPERTIES = choices + 1;
    final int BOTTOM_EDIT_EDGES = choices + 2;
    final int BOTTOM_SPRING = choices + 3;

    //currently selected tool
    int selectedOption = -1;

    //temporary storage for a graph being copied, allowing it to pasted into a different tab
    Graph copiedGraph;

    Cursor defaultCursor;

    JFrame parent = this;


    // Settings data

    File settingsFile = new File("settings.cfg");

    int settings_frameWidth = 640;
    int settings_frameHeight = 640;
    int settings_locationX = 0;
    int settings_locationY = 0;
    int settings_loadFilter = 0;
    int settings_saveFilter = 5;
    int settings_saveImageFilter = 2;
    boolean settings_displayVertexLabels = true;

    Color defaultBackgroundColor = new Color(1f, 1f, 1f);
    Color defaultDefaultColor = new Color(0f, 0f, 0f);
    Color defaultHighlightedVertexFillColor = new Color(0.8f, 0.9f, 0.9f);
    Color defaultHighlightedVertexColor = new Color(0f, 0.5f, 0.5f);
    Color defaultNewEdgeColor = new Color(0.9f, 0.35f, 0.25f);
    Color defaultDeleteEdgeColor = new Color(1f, 0f, 0f);

    final int colorTypes = 6;
    Color[] originalDefaultColors = new Color[colorTypes];
    Color[] defaultColors = new Color[colorTypes];
    String[] colorStrings = new String[colorTypes];


    public UGVViewer() {
        setTitle("Universal Graph Viewer");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());
        setAlwaysOnTop(false);
        addWindowListener(this);

        updateLocationSettings();

        initialiseColours();

        loadSettings();

        setLocation(settings_locationX, settings_locationY);

        createMenuBar();

        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.addChangeListener(
                new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        updateTabInfo();
                    }
                });

        add(tabbedPane, BorderLayout.CENTER);

        createGraphEditPane();

        add(graphEditPane, BorderLayout.EAST);

        setSize(settings_frameWidth, settings_frameHeight);
        setVisible(true);
        defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);

        graphPanels = new GraphLinkedList();
    }

    /**
     * Reads in and loads settings from the settings.cfg file.
     */
    public void loadSettings() {
        DataInputStream di = null;
        try {
            di = new DataInputStream(new FileInputStream(settingsFile));

            settings_frameWidth = di.readInt();
            settings_frameHeight = di.readInt();
            settings_locationX = di.readInt();
            settings_locationY = di.readInt();
            settings_loadFilter = di.readInt();
            settings_saveFilter = di.readInt();
            settings_saveImageFilter = di.readInt();
            settings_displayVertexLabels = di.readBoolean();

            for (int i = 0; i < defaultColors.length; i++)
                defaultColors[i] = new Color(di.readInt(), di.readInt(), di.readInt());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (di != null)
                di.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    /**
     * Saves the settings variables to the settings.cfg file.
     */
    public void saveSettings() {
        try {
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(settingsFile));

            dos.writeInt(settings_frameWidth);
            dos.writeInt(settings_frameHeight);
            dos.writeInt(settings_locationX);
            dos.writeInt(settings_locationY);
            dos.writeInt(settings_loadFilter);
            dos.writeInt(settings_saveFilter);
            dos.writeInt(settings_saveImageFilter);
            dos.writeBoolean(settings_displayVertexLabels);

            for (Color defaultColor : defaultColors) {
                dos.writeInt(defaultColor.getRed());
                dos.writeInt(defaultColor.getGreen());
                dos.writeInt(defaultColor.getBlue());
            }

            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes all tabs and then the application. If tabs are unsaved a prompt appears.
     */
    public void exit() {
        int tabs = tabbedPane.getTabCount();
        int offset = 0;

        for (int i = 0; i < tabs; i++) {
            if (!((GraphPane) tabbedPane.getComponentAt(i - offset)).getUndoState().getLastSave()) {
                String name = tabbedPane.getTitleAt(i - offset);
                name = name.substring(0, name.length() - 2);
                int value = JOptionPane.showConfirmDialog(parent, "Graph " + name + " has not been saved! Are you sure you want to close?", "Graph not saved", JOptionPane.YES_NO_OPTION);
                if (value == JOptionPane.NO_OPTION) return;
            }
        }
        System.exit(0);
    }

    /**
     * Changes the look of the cursor to that specified by cursor.
     * @param cursor the cursor object to apply.
     */
    public void changeCursor(Cursor cursor) {
        setCursor(cursor);
        defaultCursor = cursor;
        graphPanels.setDefaultCursors(cursor);
        repaint();
        validate();
    }

    /**
     * Changes the current tool to that specified by so.
     * @param so the ID of the tool to change to.
     */
    public void changeSelectedOption(int so) {
        selectedOption = so;
        graphPanels.setSelectedOptions(so);
        for (int i = 0; i < choices; i++) {
            if (selectedOption == i) {
                chosen[i] = true;
                labels[i].setIcon(icons[i][3]);
            } else {
                chosen[i] = false;
                labels[i].setIcon(icons[i][0]);
            }
        }

        for (int i = choices+bottomChoices; i < choices+bottomChoices+selectChoices; i++) {
            if(selectedOption == i){
                chosen[i] = true;
                labels[i].setIcon(icons[i][3]);
            } else {
                chosen[i] = false;
                labels[i].setIcon(icons[i][0]);
            }
        }
    }

    /**
     * Executes the function associated with the clicked function button.
     * @param function the ID of the clicked function button.
     */
    public void executeBottomFunction(int function) {
        if (function < choices || function >= choices + bottomChoices)
            return;

        if (function == BOTTOM_ARRANGE) {
            Graph gr = ((GraphPane) tabbedPane.getSelectedComponent()).getGraph();
            ArrangeDialog ad = new ArrangeDialog(this, gr.getN(), gr.getContour());

            if (!ad.getCancelled()) {
                ((GraphPane) tabbedPane.getSelectedComponent()).setUndoState();

                gr.setContour(ad.getContour());
                gr.createCircle();
                fitToScreen();
            }
        }
        if (function == BOTTOM_PROPERTIES) {
            GraphPane gp = ((GraphPane) tabbedPane.getSelectedComponent());
            new PropertiesDialog(this, gp);
        }
        if (function == BOTTOM_EDIT_EDGES) {
            editEdgeList();
        }
        if (function == BOTTOM_SPRING) {
            GraphPane gp = ((GraphPane) tabbedPane.getSelectedComponent());
            Graph gr = gp.getGraph();
            if (!gr.calculatingSpring) {
                gp.beginSpring();
            }
        }

        chosen[function] = false;
        labels[function].setIcon(icons[function][0]);
        repaint();

    }

    /**
     * Opens a dialog box to edit the edge list of the currently viewed graph.
     */
    public void editEdgeList() {
        if (tabbedPane.getSelectedIndex() != -1) {
            GraphPane gp = ((GraphPane) tabbedPane.getSelectedComponent());
            Graph gr = gp.getGraph();
            int N = gr.getN();
            EditEdgesDialog eed = new EditEdgesDialog(this, gr.getArcs(), gr.getDegrees(), N);

            if (!eed.getCancelled()) {
                gp.setUndoState();

                gr.setArcs(eed.getArcs(), eed.getDegrees(), eed.getNodes(), true);
                if (eed.getArrangeContour()) {
                    gr.createCircle();
                    fitToScreen();
                } else {
                    gp.repaint();
                }
            }
        }
    }

    /**
     * Increases the spacing of the currently selected vertices by 10 percent.
     */
    public void growSelected(){
        if (tabbedPane.getSelectedIndex() != -1) {
            GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();
            gp.setUndoState();
            Graph g = gp.getGraph();
            g.rescaleSelected(1.0 / 1.1);
            validate();
            repaint();
        }
    }


    /**
     * Shrinks the spacing of the currently selected vertices by 10 percent.
     */
    public void shrinkSelected(){
        if (tabbedPane.getSelectedIndex() != -1) {
            GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();
            gp.setUndoState();
            Graph g = gp.getGraph();
            g.rescaleSelected(1.1);

            validate();
            repaint();

        }
    }

    /**
     * Moves vertices of the current graph to the nearest intersection of gridlines.
     */
    public void snapToGrid(){
        if (tabbedPane.getSelectedIndex() != -1) {
            GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();
            gp.setUndoState();
            Graph g = gp.getGraph();

            double[] gridData = gp.getGridData();
            g.alignToGrid(gridData[0], gridData[1], gridData[2]);

            validate();
            repaint();
        }
    }

    /**
     * Opens a dialog box to align the currently viewed graph to a grid.
     */
    public void alignToGrid() {
        if (tabbedPane.getSelectedIndex() != -1) {
            GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();

            Graph g = gp.getGraph();

            ArrangeGridDialog ad = new ArrangeGridDialog(parent);

            if (!ad.getCancelled()) {
                gp.setUndoState();

                g.createGrid(ad.getGridSize(), ad.getVertical(), ad.getSpacing());
                fitToScreen();
            }

        }
    }

    /**
     * Opens a dialog box to edit the dominating set of the currently viewed graph.
     */
    public void editDominatingSet() {
        if (tabbedPane.getSelectedIndex() != -1) {
            GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();

            Graph g = gp.getGraph();

            DominationDialog dd = new DominationDialog(parent, g.getDomset());

            if (!dd.getCancelled()) {
                gp.setUndoState();

                g.setDomset(dd.getDomset());
                validate();
                repaint();
            }

        }
    }

    /**
     * Creates a new GraphPane and adds it to a new tab.
     */
    public void buildNewGraph() {

        GraphPane graphPanel = createGraphPanel(new Graph(0,0));

        int newGraph = 1;
        String newName = ("New Graph #" + newGraph);
        int tabCount = tabbedPane.getTabCount();
        int index = 0;
        while (index < tabCount) {
            String compareName = tabbedPane.getTitleAt(index);
            if (compareName.endsWith(" *"))
                compareName = compareName.substring(0, compareName.length() - 2);
            if (newName.equals(compareName)) {
                newName = ("New Graph #" + ++newGraph);
                index = 0;
            } else
                index++;
        }


        graphPanel.setDisplayVertexLabels(settings_displayVertexLabels);

        tabbedPane.add(graphPanel, newName);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        createWindowItem(graphPanel, newName);

        validate();

    }

    /**
     * Creates a new GraphPane for each graph stored in the file, with a format indicated by type.
     * @param file the file to open.
     * @param type the type of graph to read from the file. Use one of the variable specified in FileParser.
     */
    public void openFile(File file, int type){
        FileParser fp = new FileParser();
        GraphData[] graphs;


        if(type == FileParser.FILE_GML){
            graphs = fp.parseGML(file);
        } else if(type == FileParser.FILE_UGV){
            graphs = fp.parseUGV(file);
        } else if(type == FileParser.FILE_G6){
            graphs = fp.parseGraph6(file);
        } else if(type == FileParser.FILE_SCD){
            graphs = fp.parseSCD(file);
        } else if(type == FileParser.FILE_HCP){
            graphs = fp.parseHCP(file);
        } else if(type == FileParser.FILE_EDGE_LIST){
            graphs = fp.parseEdgeList(file);
        } else if(type == FileParser.FILE_ASC){
            graphs = fp.parseASC(file);
        }
        else {
            graphs = new GraphData[0];
        }


        boolean[] openGraph = null;

        if (graphs.length > 1) {
            SelectFiles sf = new SelectFiles(this, file.getName(), graphs.length);

            if (sf.getCancelled())
                return;

            openGraph = sf.getOpenGraphs();
        }
        for (int i = 0; i < graphs.length; i++) {

            if (graphs.length > 1 && !openGraph[i]) {
                continue;
            }

            GraphData g = graphs[i];
            GraphPane panel = createGraphPanel(g.graph);

            String name = file.getName();

            if (graphs.length > 1) {
                name = name + " #" + (i + 1);
            }

            panel.setScale(g.scale);
            panel.setTopLeft(g.x_offset, g.y_offset);
            panel.setRadius(g.radius);

            tabbedPane.add(panel, name);
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
            createWindowItem(panel, name);


            validate();
            fitToScreen();
        }


    }

    /**
     * Saves the graph from gp to a file, with filetype type.
     * @param filename the full path and name of the file to be written to.
     * @param gp the GraphPane which the graph will be read from.
     * @param type the type of graph to store in the file. Use one of the variable specified in FileParser.
     */
    public void saveGraph(String filename, GraphPane gp, int type){
        File file = new File(filename);


        boolean save = true;

        if (file.exists()) {
            String[] options = new String[2];
            options[0] = "Overwrite";
            options[1] = "Cancel";

            JOptionPane jop = new JOptionPane("", JOptionPane.WARNING_MESSAGE);
            int option = JOptionPane.showOptionDialog(parent, "File " + filename + " already exists. Do you want to overwrite the file?", "WARNING: File already exists!", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, jop.getIcon(), options, options[1]);
            if (option == JOptionPane.NO_OPTION)
                save = false;
        }

        if(!save){
            return;
        }

        GraphData graphData = new GraphData(gp.getGraph());

        graphData.scale = gp.getScale();
        graphData.x_offset = gp.getXTopLeft();
        graphData.y_offset = gp.getYTopLeft();
        graphData.radius = gp.getRadius();

        FileParser fp = new FileParser();

        if(type == FileParser.FILE_EDGE_LIST){
            fp.saveEdgeList(graphData, file);
        } else if(type == FileParser.FILE_HCP){
            fp.saveHCP(graphData, file);
        }

    }

    /**
     * Saves the graph from gp to a file, with filetype type. Has the option to append the graph to an existing file.
     * @param filename the full path and name of the file to be written to.
     * @param gp the GraphPane which the graph will be read from.
     * @param type the type of graph to store in the file. Use one of the variable specified in FileParser.
     */
    public void saveGraphMultiple(String filename, GraphPane gp, int type) {
        GraphPane[] gps = new GraphPane[1];
        gps[0] = gp;
        saveGraphMultiple(filename, gps, type);

    }

    /**
     * Saves a graph from each GraphPane to a file, with filetype type. Has the option to append the graphs to an existing file.
     * @param filename the full path and name of the file to be written to.
     * @param graphPanes the GraphPanes which the graphs will be read from.
     * @param type the type of graph to be stored in the file. Use one of the variable specified in FileParser.
     */
    public void saveGraphMultiple(String filename, GraphPane[] graphPanes, int type) {

        File file = new File(filename);

        boolean save = true;
        boolean append = false;

        if (file.exists()) {
            String[] options = new String[3];
            options[0] = "Overwrite";
            options[1] = "Append";
            options[2] = "Cancel";

            JOptionPane jop = new JOptionPane("", JOptionPane.WARNING_MESSAGE);
            int option = JOptionPane.showOptionDialog(parent, "File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?", "WARNING: File already exists!", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, jop.getIcon(), options, options[2]);
            if (option == JOptionPane.NO_OPTION) {
                append = true;
            }
            if (option == JOptionPane.CANCEL_OPTION) {
                save = false;
            }
        }

        if (!save) {
            return;
        }

        GraphData[] graphs = new GraphData[graphPanes.length];

        for (int i = 0; i < graphPanes.length; i++) {
            GraphPane gp = graphPanes[i];
            graphs[i] = new GraphData(gp.getGraph());
            graphs[i].scale = gp.getScale();
            graphs[i].x_offset = gp.getXTopLeft();
            graphs[i].y_offset = gp.getYTopLeft();
            graphs[i].radius = gp.getRadius();
        }

        FileParser fp = new FileParser();

        if(type == FileParser.FILE_GML) {
            fp.saveGML(graphs, file, append);
        } else if(type == FileParser.FILE_UGV){
            fp.saveUGV(graphs, file, append);
        } else if(type == FileParser.FILE_SCD){
            fp.saveSCD(graphs, file, append);
        } else if(type == FileParser.FILE_G6){
            fp.saveGraph6(graphs, file, append);
        } else if(type == FileParser.FILE_ASC){
            fp.saveASC(graphs, file, append);
        }

    }

    /**
     * Triggers a dialog box to open a new graph or graphs from a file.
     */
    public void openFileDialog(){
        JFileChooser jfc = new JFileChooser(".");
        jfc.setAcceptAllFileFilterUsed(false);

        String ascName = "GENREG ASC Format (*.asc)";
        String graph6Name = "Graph6 Format (*.g6)";
        String hcpName = "HCP Format (*.hcp)";
        String scdName = "GENREG SCD Format (*.scd)";
        String edgeListName = "Edge List Format (*.txt)";
        String ugvName = "UGV Format (*.ugv)";
        String gmlName = "GML Format (*.gml)";

        FileNameExtensionFilter[] extensions = new FileNameExtensionFilter[7];
        extensions[0] = new FileNameExtensionFilter(ascName, "asc");
        extensions[1] = new FileNameExtensionFilter(graph6Name, "g6");
        extensions[2] = new FileNameExtensionFilter(hcpName, "hcp");
        extensions[3] = new FileNameExtensionFilter(scdName, "scd");
        extensions[4] = new FileNameExtensionFilter(edgeListName, "txt");
        extensions[5] = new FileNameExtensionFilter(ugvName, "ugv");
        extensions[6] = new FileNameExtensionFilter(gmlName, "gml");

        for (FileNameExtensionFilter extension : extensions) {
            jfc.addChoosableFileFilter(extension);
        }
        if (settings_loadFilter >= 0 && settings_loadFilter < extensions.length)
            jfc.setFileFilter(extensions[settings_loadFilter]);
        else
            jfc.setFileFilter(extensions[0]);

        int choice = jfc.showOpenDialog(null);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File file = jfc.getSelectedFile();
            String extensionName = jfc.getFileFilter().getDescription();

            if (extensionName.equals(ascName)) {
                settings_loadFilter = 0;
                openFile(file, FileParser.FILE_ASC);
            } else if (extensionName.equals(graph6Name)) {
                settings_loadFilter = 1;
                openFile(file, FileParser.FILE_G6);
            } else if (extensionName.equals(hcpName)) {
                settings_loadFilter = 2;
                openFile(file, FileParser.FILE_HCP);
            } else if (extensionName.equals(scdName)) {
                settings_loadFilter = 3;
                openFile(file, FileParser.FILE_SCD);
            } else if (extensionName.equals(edgeListName)) {
                settings_loadFilter = 4;
                openFile(file, FileParser.FILE_EDGE_LIST);
            } else if (extensionName.equals(ugvName)) {
                settings_loadFilter = 5;
                openFile(file, FileParser.FILE_UGV);
            } else if (extensionName.equals(gmlName)) {
                settings_loadFilter = 6;
                openFile(file, FileParser.FILE_GML);
            }

            saveSettings();

        }
        repaint();
    }

    /**
     * Triggers a dialog box to save the current graph.
     */
    public void saveGraphDialog(){
        if (tabbedPane.getSelectedIndex() != -1) {
            JFileChooser jfc = new JFileChooser(".");
            jfc.setAcceptAllFileFilterUsed(false);

            String ascName = "GENREG ASC Format (*.asc)";
            String graph6Name = "Graph6 Format (*.g6)";
            String hcpName = "HCP Format (*.hcp)";
            String scdName = "GENREG SCD Format (*.scd)";
            String edgeListName = "Edge List Format (*.txt)";
            String ugvName = "UGV Format (*.ugv)";
            String gmlName = "GML Format (*.gml)";

            FileNameExtensionFilter[] extensions = new FileNameExtensionFilter[7];
            extensions[0] = new FileNameExtensionFilter(ascName, "asc");
            extensions[1] = new FileNameExtensionFilter(graph6Name, "g6");
            extensions[2] = new FileNameExtensionFilter(hcpName, "hcp");
            extensions[3] = new FileNameExtensionFilter(scdName, "scd");
            extensions[4] = new FileNameExtensionFilter(edgeListName, "txt");
            extensions[5] = new FileNameExtensionFilter(ugvName, "ugv");
            extensions[6] = new FileNameExtensionFilter(gmlName, "gml");


            for (FileNameExtensionFilter extension : extensions) {
                jfc.addChoosableFileFilter(extension);
            }
            if (settings_saveFilter >= 0 && settings_saveFilter < extensions.length)
                jfc.setFileFilter(extensions[settings_saveFilter]);
            else
                jfc.setFileFilter(extensions[0]);


            int choice = jfc.showSaveDialog(null);
            if (choice == JFileChooser.APPROVE_OPTION) {
                String extensionName = jfc.getFileFilter().getDescription();

                GraphPane graphPane = (GraphPane) tabbedPane.getSelectedComponent();

                if (extensionName.equals(ascName)) {
                    settings_saveFilter = 0;
                    String filename = jfc.getSelectedFile().getName();
                    if (!filename.endsWith(".asc")) {
                        filename = filename + ".asc";
                    }

                    saveGraphMultiple(jfc.getSelectedFile().getParent() + "//" + filename, graphPane, FileParser.FILE_ASC);
                } else if (extensionName.equals(graph6Name)) {
                    settings_saveFilter = 1;
                    String filename = jfc.getSelectedFile().getName();
                    if (!filename.endsWith(".g6")) {
                        filename = filename + ".g6";
                    }
                    saveGraphMultiple(jfc.getSelectedFile().getParent() + "//" + filename, graphPane, FileParser.FILE_G6);
                } else if (extensionName.equals(hcpName)) {
                    settings_saveFilter = 2;
                    String filename = jfc.getSelectedFile().getName();
                    if (!filename.endsWith(".hcp")) {
                        filename = filename + ".hcp";
                    }

                    saveGraph(jfc.getSelectedFile().getParent() + "//" + filename, graphPane, FileParser.FILE_HCP);

                } else if (extensionName.equals(scdName)) {
                    settings_saveFilter = 3;
                    String filename = jfc.getSelectedFile().getName();
                    if (!filename.endsWith(".scd")) {
                        filename = filename + ".scd";
                    }

                    saveGraphMultiple(jfc.getSelectedFile().getParent() + "//" + filename, graphPane, FileParser.FILE_SCD);

                } else if (extensionName.equals(edgeListName)) {
                    settings_saveFilter = 4;
                    String filename = jfc.getSelectedFile().getName();
                    if (!filename.endsWith(".txt")) {
                        filename = filename + ".txt";
                    }

                    saveGraph(jfc.getSelectedFile().getParent() + "//" + filename, graphPane, FileParser.FILE_EDGE_LIST);

                } else if (extensionName.equals(ugvName)) {
                    settings_saveFilter = 5;
                    String filename = jfc.getSelectedFile().getName();
                    if (!filename.endsWith(".ugv")) {
                        filename = filename + ".ugv";
                    }
                    saveGraphMultiple(jfc.getSelectedFile().getParent() + "//" + filename, graphPane, FileParser.FILE_UGV);
                } else if (extensionName.equals(gmlName)) {
                    settings_saveFilter = 6;
                    String filename = jfc.getSelectedFile().getName();
                    if (!filename.endsWith(".gml")) {
                        filename = filename + ".gml";
                    }
                    saveGraphMultiple(jfc.getSelectedFile().getParent() + "//" + filename, graphPane, FileParser.FILE_GML);
                }

                graphPane.getUndoState().setLastSave();
                checkSave(tabbedPane.getSelectedIndex());


                saveSettings();
            }
            repaint();
        }
    }

    /**
     * Triggers a dialog box to save a graph or multiple graphs int a file type that allows for multiple graph.
     */
    public void saveMultipleGraphDialog(){

        if (tabbedPane.getSelectedIndex() != -1) {
            String[] graphNames = new String[tabbedPane.getTabCount()];
            for (int i = 0; i < graphNames.length; i++)
                graphNames[i] = tabbedPane.getTitleAt(i);
            SaveMultipleGraphs smg = new SaveMultipleGraphs(parent, graphNames);

            if (!smg.getCancelled()) {
                boolean[] saveGraphs = smg.getSaveGraphs();
                int[] graphsToSave = new int[saveGraphs.length];
                int count = 0;
                for (int i = 0; i < saveGraphs.length; i++)
                    if (saveGraphs[i])
                        graphsToSave[count++] = i;

                int[] trimmedGraphsToSave = new int[count];
                System.arraycopy(graphsToSave, 0, trimmedGraphsToSave, 0, count);
                graphsToSave = trimmedGraphsToSave;

                if (count > 0) {
                    JFileChooser jfc = new JFileChooser(".");
                    jfc.setAcceptAllFileFilterUsed(false);

                    String ascName = "GENREG ASC Format (*.asc)";
                    String graph6Name = "Graph6 Format (*.g6)";
                    String scdName = "GENREG SCD Format (*.scd)";
                    String ugvName = "UGV Format (*.ugv)";
                    String gmlName = "GML Format (*.gml)";

                    FileNameExtensionFilter[] extensions = new FileNameExtensionFilter[5];
                    extensions[0] = new FileNameExtensionFilter(ascName, "asc");
                    extensions[1] = new FileNameExtensionFilter(graph6Name, "g6");
                    extensions[2] = new FileNameExtensionFilter(scdName, "scd");
                    extensions[3] = new FileNameExtensionFilter(ugvName, "ugv");
                    extensions[4] = new FileNameExtensionFilter(gmlName, "gml");


                    for (FileNameExtensionFilter extension : extensions) {
                        jfc.addChoosableFileFilter(extension);
                    }
                    if (settings_saveFilter >= 0 && settings_saveFilter < extensions.length)
                        jfc.setFileFilter(extensions[settings_saveFilter]);
                    else
                        jfc.setFileFilter(extensions[0]);


                    int choice = jfc.showSaveDialog(null);
                    if (choice == JFileChooser.APPROVE_OPTION) {
                        String extensionName = jfc.getFileFilter().getDescription();

                        GraphPane[] graphPanes = new GraphPane[count];
                        for (int i = 0; i < count; i++) {
                            graphPanes[i] = (GraphPane) tabbedPane.getComponentAt(graphsToSave[i]);
                        }

                        if (extensionName.equals(ascName)) {
                            settings_saveFilter = 0;
                            String filename = jfc.getSelectedFile().getName();
                            if (!filename.endsWith(".asc")) {
                                filename = filename + ".asc";
                            }

                            saveGraphMultiple(jfc.getSelectedFile().getParent() + "//" + filename, graphPanes, FileParser.FILE_ASC);
                        } else if (extensionName.equals(graph6Name)) {
                            settings_saveFilter = 1;
                            String filename = jfc.getSelectedFile().getName();
                            if (!filename.endsWith(".g6")) {
                                filename = filename + ".g6";
                            }

                            saveGraphMultiple(jfc.getSelectedFile().getParent() + "//" + filename, graphPanes, FileParser.FILE_G6);
                        } else if (extensionName.equals(scdName)) {
                            settings_saveFilter = 3;
                            String filename = jfc.getSelectedFile().getName();
                            if (!filename.endsWith(".scd")) {
                                filename = filename + ".scd";
                            }

                            saveGraphMultiple(jfc.getSelectedFile().getParent() + "//" + filename, graphPanes, FileParser.FILE_SCD);

                        } else if (extensionName.equals(ugvName)) {
                            settings_saveFilter = 5;
                            String filename = jfc.getSelectedFile().getName();
                            if (!filename.endsWith(".ugv")) {
                                filename = filename + ".ugv";
                            }
                            saveGraphMultiple(jfc.getSelectedFile().getParent() + "//" + filename, graphPanes, FileParser.FILE_UGV);
                        } else if (extensionName.equals(gmlName)) {
                            settings_saveFilter = 6;
                            String filename = jfc.getSelectedFile().getName();
                            if (!filename.endsWith(".gml")) {
                                filename = filename + ".gml";
                            }
                            saveGraphMultiple(jfc.getSelectedFile().getParent() + "//" + filename, graphPanes, FileParser.FILE_GML);
                        }

                        for (int i = 0; i < graphPanes.length; i++) {
                            graphPanes[i].getUndoState().setLastSave();
                            checkSave(graphsToSave[i]);
                        }
                        saveSettings();
                    }
                }
            }
            repaint();
        }
    }

    /**
     * Triggers a dialog box to create a new graph from a given list of families or operations.
     */
    public void generateNewGraph(){
        GraphBuilderDialog gbd = new GraphBuilderDialog(parent, tabbedPane);

        if (!gbd.cancelled()) {
            buildNewGraph();
            GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();
            tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), gbd.getName());
            gp.setUndoState();
            gp.setGraph(gbd.getGraph());
            fitToScreen();
        }

        validate();
        repaint();
    }

    /**
     * Adjusts the scale and viewport to ensure the entire graph is in view.
     */
    public void fitToScreen() {
        int index = tabbedPane.getSelectedIndex();
        if (index == -1) return;
        GraphPane graphPane = graphPanels.get(index + 1);
        int radius = graphPane.getRadius();
        Graph thisGraph = graphPane.getGraph();

        if (thisGraph.getN() <= 0) return;

        double minX = thisGraph.getXPos(0) - radius;
        double maxX = thisGraph.getXPos(0) + radius;
        double minY = thisGraph.getYPos(0) - radius;
        double maxY = thisGraph.getYPos(0) + radius;
        for (int i = 0; i < thisGraph.getN(); i++) {
            if (minX > thisGraph.getXPos(i) - radius) minX = thisGraph.getXPos(i) - radius;
            if (maxX < thisGraph.getXPos(i) + radius) maxX = thisGraph.getXPos(i) + radius;
            if (minY > thisGraph.getYPos(i) - radius) minY = thisGraph.getYPos(i) - radius;
            if (maxY < thisGraph.getYPos(i) + radius) maxY = thisGraph.getYPos(i) + radius;
        }
        double scale = Math.min(0.9 * graphPane.getSize().getWidth() * 1.0 / (maxX - minX), 0.9 * graphPane.getSize().getHeight() * 1.0 / (maxY - minY));
        graphPane.setScale(scale);
        graphPane.setTopLeft((int) Math.round(minX - ((graphPane.getSize().getWidth()) / scale - (maxX - minX)) / 2.0), (int) Math.round(minY - ((graphPane.getSize().getHeight()) / scale - (maxY - minY)) / 2.0));
        graphPane.repaint();

    }

    /**
     * Sets whether or not vertices of the current graph should be aligned to a grid when moved or added.
     * Based on the state of the snap to grid menu checkbox.
     */
    public void setSnapToGrid(){
        if (tabbedPane.getSelectedIndex() != -1) {
            ((GraphPane) tabbedPane.getSelectedComponent()).setSnapToGrid(gridSnapItem.getState());
            tabbedPane.getSelectedComponent().repaint();
        }
    }

    /**
     * Sets whether or not gridlines should be shown in the current view, based on the state of the gridlines menu item.
     */
    public void setShowGridLines(){
        if (tabbedPane.getSelectedIndex() != -1) {
            ((GraphPane) tabbedPane.getSelectedComponent()).setGridlines(gridlinesItem.getState());
            tabbedPane.getSelectedComponent().repaint();
        }
    }

    /**
     * Sets the state of displaying vertex labels for the current graph, based on the menu item state.
     */
    public void setVertexLabelDisplay() {
        settings_displayVertexLabels = displayVertexLabelsItem.getState();
        saveSettings();
        if (tabbedPane.getSelectedIndex() != -1) {
            ((GraphPane) tabbedPane.getSelectedComponent()).setDisplayVertexLabels(displayVertexLabelsItem.getState());
            tabbedPane.getSelectedComponent().repaint();
        }
    }


    /**
     * Sets the state of displaying crossings for the current graph, based on the menu item state.
     */
    public void setCrossingDisplay() {
        if (tabbedPane.getSelectedIndex() != -1) {
            ((GraphPane) tabbedPane.getSelectedComponent()).setDisplayCrossings(displayCrossingsItem.getState());
            tabbedPane.getSelectedComponent().repaint();
        }
    }

    /**
     * Sets the state of displaying domination for the current graph, based on the menu item state.
     */
    public void setDominationDisplay() {
        if (tabbedPane.getSelectedIndex() != -1) {
            ((GraphPane) tabbedPane.getSelectedComponent()).setDisplayDomination(displayDominationItem.getState());
            tabbedPane.getSelectedComponent().repaint();
        }
    }

    /**
     * Sets the state of calculating total domination for the current graph, based on the menu item state.
     */
    public void setDomTotal() {
        if (tabbedPane.getSelectedIndex() != -1) {
            ((GraphPane) tabbedPane.getSelectedComponent()).setDomTotal(domTotalItem.getState());
            tabbedPane.getSelectedComponent().repaint();
        }
    }

    /**
     * Sets the state of calculating connected domination for the current graph, based on the menu item state.
     */
    public void setDomConnected() {
        if (tabbedPane.getSelectedIndex() != -1) {
            ((GraphPane) tabbedPane.getSelectedComponent()).setDomConnected(domConnectedItem.getState());
            tabbedPane.getSelectedComponent().repaint();
        }
    }

    /**
     * Sets the state of calculating secure domination for the current graph, based on the menu item state.
     */
    public void setDomSecure() {
        if (tabbedPane.getSelectedIndex() != -1) {
            ((GraphPane) tabbedPane.getSelectedComponent()).setDomSecure(domSecureItem.getState());
            tabbedPane.getSelectedComponent().repaint();
        }
    }

    /**
     * Sets the state of calculating roman domination for the current graph, based on the menu item state.
     */
    public void setDomRoman(){
        if (tabbedPane.getSelectedIndex() != -1) {
            ((GraphPane) tabbedPane.getSelectedComponent()).setDomRoman(domRomanItem.getState());
            tabbedPane.getSelectedComponent().repaint();
        }
    }

    /**
     * Sets the state of calculating weak roman domination for the current graph, based on the menu item state.
     */
    public void setDomWeakRoman(){
        if (tabbedPane.getSelectedIndex() != -1) {
            ((GraphPane) tabbedPane.getSelectedComponent()).setDomWeakRoman(domWeakRomanItem.getState());
            tabbedPane.getSelectedComponent().repaint();
        }
    }

    /**
     * Toggles the ability to click the undo menu item, based on whether or not an undo is available.
     * @param available the desired state of menu item usability.
     */
    public void setUndoAvailable(boolean available) {
        undoEditItem.setEnabled(available);
    }

    /**
     * Toggles the ability to click the redo menu item, based on whether or not a redo is available.
     * @param available the desired state of menu item usability.
     */
    public void setRedoAvailable(boolean available) {
        redoEditItem.setEnabled(available);
    }

    /**
     * Makes a new graph from the selected vertices of the current graph, and makes it available for pasting.
     * This graph can be pasted in any tab.
     */
    public void copySelected() {
        if (tabbedPane.getSelectedIndex() != -1) {
            GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();
            Graph g = gp.getGraph();
            copiedGraph = g.getSubgraph(g.getSelected(), true);
            pasteItem.setEnabled(true);
        }
    }



    /**
     * Reverts the previous action from the currently viewed graph.
     */
    public void undo(){
        if (tabbedPane.getSelectedIndex() != -1) {
            ((GraphPane) tabbedPane.getSelectedComponent()).undo();
        }
    }

    /**
     * Reverts the previous undo action from the currently viewed graph.
     */
    public void redo(){
        if (tabbedPane.getSelectedIndex() != -1) {
            ((GraphPane) tabbedPane.getSelectedComponent()).redo();
        }
    }

    /**
     * Paste the copied graph to the top left of the current tab.
     */
    public void pasteGraph() {
        if (copiedGraph != null && tabbedPane.getSelectedIndex() != -1) {
            GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();
            gp.pasteGraph(copiedGraph);
            validate();
            repaint();
        }
    }

    /**
     * Trigger a dialog box to convert the current graph to tex code.
     */
    public void texDialog(){
        if (tabbedPane.getSelectedIndex() != -1) {
            GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();

            Graph g = gp.getGraph();

            new TexDialog(parent, g);
        }
    }

    /**
     * Trigger a dialog box to run a CPLEX instance for some implemented domination variant.
     */
    public void solverDialog(){
        if (tabbedPane.getSelectedIndex() != -1) {
            GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();

            SolverDialog sd = new SolverDialog(parent, gp.getGraph());

            if (!sd.getCancelled()) {
                gp.setUndoState();

                runMILP(sd.getDomtype(), sd.getFixed());
                validate();
                repaint();
            }

        }
    }

    /**
     * Run a CPLEX instance to solve a domination problem for the current graph.
     * @param domType the type of domination problem to solve.
     * @param fixed the value for which each vertex should be fixed in the solve. (-1: not fixed ,0: no guard ,1: one guard,2: two guards).
     */
    public void runMILP(int domType, int[] fixed) {
        runMILP(domType, false, fixed);
    }

    /**
     * Run a CPLEX instance to solve a domination problem for the current graph.
     * @param domType the type of domination problem to solve.
     * @param preserve whether or not the current guards should remain dominated. Does not apply unless fixed is null.
     * @param fixed the value for which each vertex should be fixed in the solve. (-1: not fixed ,0: no guard ,1: one guard,2: two guards).
     */
    public void runMILP(int domType, boolean preserve, int[] fixed) {
        if (tabbedPane.getSelectedIndex() != -1) {
            GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();
            Graph g = gp.getGraph();

            MILPRunner runner;

            if (fixed != null) {
                runner = new MILPRunner(domType, g, fixed);
            } else if (preserve) {
                runner = new MILPRunner(domType, g, true);
            } else {
                runner = new MILPRunner(domType, g);
            }

            try {
                double[] solution = runner.run();

                int[] domset = new int[g.getN()];

                for (int i = 0; i < g.getN(); i++) {
                    domset[i] = (int) Math.round(solution[i]);

                    if (domType == MILPRunner.WEAK_ROMAN_DOMINATION || domType == MILPRunner.ROMAN_DOMINATION) {
                        domset[i] = (int) Math.round(solution[i] + 2 * solution[i + g.getN()]);
                    }

                }
                g.setDomset(domset);
                validate();
                repaint();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Opens a dialog to export the current view of the tab as an image. This includes the graph and any overlaying text.
     */
    public void exportAsImage(){
        JFileChooser jfc = new JFileChooser(".");
        jfc.setAcceptAllFileFilterUsed(false);

        String bmpName = "Bitmap (*.bmp)";
        String gifName = "GIF (*.gif)";
        String gifTransparentName = "GIF with transparent background (*.gif)";
        String jpgName = "JPEG (*.jpg)";
        String pngName = "Portable Network Graphic (*.png)";

        FileNameExtensionFilter[] extensions = new FileNameExtensionFilter[5];
        extensions[0] = new FileNameExtensionFilter(bmpName, "bmp");
        extensions[1] = new FileNameExtensionFilter(gifName, "gif");
        extensions[2] = new FileNameExtensionFilter(gifTransparentName, "gif");
        extensions[3] = new FileNameExtensionFilter(jpgName, "jpg");
        extensions[4] = new FileNameExtensionFilter(pngName, "png");

        for (FileNameExtensionFilter extension : extensions) {
            jfc.addChoosableFileFilter(extension);
        }
        if (settings_saveImageFilter >= 0 && settings_saveImageFilter < extensions.length) {
            jfc.setFileFilter(extensions[settings_saveImageFilter]);
        }
        else {
            jfc.setFileFilter(extensions[0]);
        }


        int choice = jfc.showSaveDialog(null);
        if (choice == JFileChooser.APPROVE_OPTION) {
            String filename = jfc.getSelectedFile().getName();
            String extensionName = jfc.getFileFilter().getDescription();
            String fileFormat = "";

            GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();
            BufferedImage img;

            if (extensionName.equals(gifTransparentName)) {
                gp.setSavingWithTransparentBackground(true);
            }

            img = new BufferedImage(gp.getSize().width, gp.getSize().height, BufferedImage.TYPE_BYTE_INDEXED);
            gp.paintComponent(img.getGraphics());
            img = gp.getImage();


            if (extensionName.equals(bmpName)) {
                settings_saveImageFilter = 0;
                fileFormat = "BMP";

                if (!filename.endsWith(".bmp")) {
                    filename = filename + ".bmp";
                }
            } else if (extensionName.equals(gifName)) {
                settings_saveImageFilter = 1;
                fileFormat = "GIF";

                if (!filename.endsWith(".gif")) {
                    filename = filename + ".gif";
                }
            } else if (extensionName.equals(gifTransparentName)) {
                settings_saveImageFilter = 2;
                fileFormat = "GIF";

                if (!filename.endsWith(".gif")) {
                    filename = filename + ".gif";
                }
            } else if (extensionName.equals(jpgName)) {
                settings_saveImageFilter = 3;
                fileFormat = "JPG";

                if (!filename.endsWith(".jpg")) {
                    filename = filename + ".jpg";
                }
            } else if (extensionName.equals(pngName)) {
                settings_saveImageFilter = 4;
                fileFormat = "PNG";

                if (!filename.endsWith(".png")) {
                    filename = filename + ".png";
                }
            }
            try {
                File file = new File(filename);
                boolean save = true;
                if (file.exists()) {
                    int value = JOptionPane.showConfirmDialog(parent, "File " + filename + " already exists! Do you want to overwrite this file?", "File already exists!", JOptionPane.YES_NO_OPTION);
                    if (value == JOptionPane.NO_OPTION) {
                        save = false;
                    }
                }
                if (save)
                    ImageIO.write(img, fileFormat, file);
            } catch (Exception e) {
                e.printStackTrace();
            }
            gp.setSavingWithTransparentBackground(false);
            saveSettings();
        }
        repaint();
    }

    /**
     * Updates the status of the current tab, to whether or not is has been edited since creation or its previous save.
     */
    public void checkSave() {
        checkSave(tabbedPane.getSelectedIndex());
    }

    /**
     * Updates the status of the tab at index, to whether or not is has been edited since creation or its previous save.
     * @param index the index of the tab to check.
     */
    public void checkSave(int index) {
        if (tabbedPane.getSelectedIndex() != -1) {
            GraphPane gp = (GraphPane) tabbedPane.getComponentAt(index);
            if (gp.getUndoState().getLastSave()) {
                String title = tabbedPane.getTitleAt(index);
                if (title.endsWith(" *")) title = title.substring(0, title.length() - 2);
                tabbedPane.setTitleAt(index, title);
                gp.getMenuItem().setText(title);
            } else {
                String title = tabbedPane.getTitleAt(index);
                if (!title.endsWith(" *")) title = (title + " *");
                tabbedPane.setTitleAt(index, title);
                gp.getMenuItem().setText(title);
            }
            setTitle("Universal Graph Viewer - " + tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()));
        }
    }

    /**
     * Closes the current tab. Triggers a warning if the graph has been edited since its last save.
     */
    public void closeFile(){
        if (tabbedPane.getSelectedIndex() != -1) {
            if (!((GraphPane) tabbedPane.getSelectedComponent()).getUndoState().getLastSave()) {
                String name = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
                name = name.substring(0, name.length() - 2);
                int value = JOptionPane.showConfirmDialog(parent, "Graph " + name + " has not been saved! Are you sure you want to close?", "Graph not saved", JOptionPane.YES_NO_OPTION);
                if (value == JOptionPane.NO_OPTION)
                    return;
            }
            windowItem.remove(graphPanels.get(tabbedPane.getSelectedIndex() + 1).getMenuItem());
            graphPanels.delete(tabbedPane.getSelectedIndex() + 1);
            tabbedPane.remove(tabbedPane.getSelectedIndex());
        }
    }

    /**
     * Closes all tabs. Triggers a warning if any graph has been edited since its last save.
     */
    public void closeAllWindows(){
        int tabs = tabbedPane.getTabCount();
        int offset = 0;
        for (int i = 0; i < tabs; i++) {
            if (!((GraphPane) tabbedPane.getComponentAt(i - offset)).getUndoState().getLastSave()) {
                String name = tabbedPane.getTitleAt(i - offset);
                name = name.substring(0, name.length() - 2);
                int value = JOptionPane.showConfirmDialog(parent, "Graph " + name + " has not been saved! Are you sure you want to close?", "Graph not saved", JOptionPane.YES_NO_OPTION);
                if (value == JOptionPane.NO_OPTION) continue;
            }

            windowItem.remove(graphPanels.get(i + 1 - offset).getMenuItem());
            graphPanels.delete(i + 1 - offset);
            tabbedPane.remove(i - offset);
            offset++;
        }
    }

    /**
     * Opens a dialog to change the colours used in either the current graph or all graphs.
     */
    public void changeColours(){
        ColorSettingsDialog csd;
        if (tabbedPane.getSelectedIndex() != -1) {
            csd = new ColorSettingsDialog(parent, ((GraphPane) tabbedPane.getSelectedComponent()).getDefaultColors(), colorStrings, true);
        } else {
            Color[] tempColors = new Color[defaultColors.length];
            System.arraycopy(defaultColors, 0, tempColors, 0, defaultColors.length);
            csd = new ColorSettingsDialog(parent, tempColors, colorStrings, false);
        }
        if (!csd.getCancelled() && csd.whichGraphs() != -1) {
            if (csd.whichGraphs() == 1) {
                graphPanels.setDefaultColors(csd.getDefaultColors());
            } else
                ((GraphPane) tabbedPane.getSelectedComponent()).setDefaultColors(csd.getDefaultColors());

            tabbedPane.getSelectedComponent().repaint();

        }
    }

    /**
     * Gets an array containing the saved default colours.
     * @return an array containing the saved default colours.
     */
    public Color[] getDefaultColors() {
        Color[] tempColors = new Color[defaultColors.length];
        System.arraycopy(defaultColors, 0, tempColors, 0, defaultColors.length);
        return tempColors;
    }

    /**
     * Saves the array of colours as the new default colours.
     * @param dc the array of colours to save.
     */
    public void saveDefaultColors(Color[] dc) {
        System.arraycopy(dc, 0, defaultColors, 0, dc.length);
        saveSettings();
    }

    /**
     * Gets an array containing the original colour scheme.
     * @return the array containing the original colours.
     */
    public Color[] getOriginalDefaultColors() {
        Color[] tempColors = new Color[originalDefaultColors.length];
        System.arraycopy(originalDefaultColors, 0, tempColors, 0, originalDefaultColors.length);
        return tempColors;
    }

    /**
     * Loads colours into arrays for use.
     */
    private void initialiseColours(){
        defaultColors[0] = defaultBackgroundColor;
        defaultColors[1] = defaultDefaultColor;
        defaultColors[2] = defaultHighlightedVertexFillColor;
        defaultColors[3] = defaultHighlightedVertexColor;
        defaultColors[4] = defaultNewEdgeColor;
        defaultColors[5] = defaultDeleteEdgeColor;

        System.arraycopy(defaultColors, 0, originalDefaultColors, 0, originalDefaultColors.length);

        colorStrings[0] = "Background colour";
        colorStrings[1] = "Vertex/arc colour";
        colorStrings[2] = "Highlighted vertex fill colour";
        colorStrings[3] = "Highlighted vertex/arc colour";
        colorStrings[4] = "New edge colour";
        colorStrings[5] = "Edge deletion colour";
    }

    /**
     * Updates the frame location and size settings whenever the windows is moved or stretched.
     */
    private void updateLocationSettings(){
        getContentPane().addHierarchyBoundsListener(
                new HierarchyBoundsListener() {

                    @Override
                    public void ancestorMoved(HierarchyEvent e) {
                        Component parent = e.getChanged();
                        settings_locationX = parent.getLocation().x;
                        settings_locationY = parent.getLocation().y;
                        saveSettings();
                    }

                    @Override
                    public void ancestorResized(HierarchyEvent e) {
                        Component parent = e.getChanged();
                        settings_locationX = parent.getLocation().x;
                        settings_locationY = parent.getLocation().y;
                        settings_frameWidth = parent.getSize().width;
                        settings_frameHeight = parent.getSize().height;
                        saveSettings();
                    }
                });
    }

    /**
     * Updates various visual components to reflect the settings of the current tab.
     */
    private void updateTabInfo(){
        if (tabbedPane.getSelectedIndex() == -1) {
            setTitle("Universal Graph Viewer");
            vertexSizeSlider.setValue(DEFAULT_VERTEX_SIZE);
            vertexSizeField.setText("" + DEFAULT_VERTEX_SIZE);
            labelSizeSlider.setValue(DEFAULT_LABEL_SIZE);
            labelSizeField.setText("" + DEFAULT_LABEL_SIZE);

            vertexSizeSlider.setEnabled(false);
            vertexSizeField.setEnabled(false);
            labelSizeSlider.setEnabled(false);
            labelSizeField.setEnabled(false);

            saveFileItem.setEnabled(false);
            saveMultipleGraphsFileItem.setEnabled(false);
            exportAsImageFileItem.setEnabled(false);
            undoEditItem.setEnabled(false);
            redoEditItem.setEnabled(false);
        } else {
            setTitle("Universal Graph Viewer - " + tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()));
            GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();
            displayVertexLabelsItem.setState(gp.getDisplayVertexLabels());
            displayCrossingsItem.setState(gp.getDisplayCrossings());
            displayDominationItem.setState(gp.getDisplayDomination());

            vertexSizeSlider.setValue(gp.getRadius());
            vertexSizeField.setText("" + gp.getRadius());
            labelSizeSlider.setValue(gp.getTextSize());
            labelSizeField.setText("" + gp.getTextSize());

            vertexSizeSlider.setEnabled(true);
            vertexSizeField.setEnabled(true);
            labelSizeSlider.setEnabled(true);
            labelSizeField.setEnabled(true);

            domTotalItem.setState(gp.getDomTotal());
            domSecureItem.setState(gp.getDomSecure());
            domConnectedItem.setState(gp.getDomConnected());
            domRomanItem.setState(gp.getDomRoman());
            domWeakRomanItem.setState(gp.getDomWeakRoman());

            saveFileItem.setEnabled(true);
            saveMultipleGraphsFileItem.setEnabled(true);
            exportAsImageFileItem.setEnabled(true);

        }
    }

    /**
     * Updates the vertex size based on the text box value.
     */
    public void updateVertexTextBox(){
        try {
            int size = Integer.parseInt(vertexSizeField.getText());
            if (size < MIN_VERTEX_SIZE){
                vertexSizeField.setText("" + MIN_VERTEX_SIZE);
                size = MIN_VERTEX_SIZE;
            }
            else if (size > MAX_VERTEX_SIZE){
                vertexSizeField.setText("" + MAX_VERTEX_SIZE);
                size = MAX_VERTEX_SIZE;
            }

            if (tabbedPane.getSelectedIndex() != -1) {
                ((GraphPane) tabbedPane.getSelectedComponent()).setRadius(size);
                tabbedPane.getSelectedComponent().repaint();
            }
            if (vertexSizeSlider != null) {
                vertexSizeSlider.setValue(size);
            }
        } catch (Exception e) {
            if (tabbedPane.getSelectedIndex() != -1) {
                vertexSizeField.setText("" + ((GraphPane) tabbedPane.getSelectedComponent()).getRadius());
            } else
                vertexSizeField.setText("" + DEFAULT_VERTEX_SIZE);
        }
    }

    /**
     * Updates the label size based on the text box value.
     */
    public void updateLabelTextBox(){
        try {
            int size = Integer.parseInt(labelSizeField.getText());
            if (size < MIN_LABEL_SIZE){
                labelSizeField.setText("" + MIN_LABEL_SIZE);
                size = MIN_LABEL_SIZE;
            }
            if (size > MAX_LABEL_SIZE){
                labelSizeField.setText("" + MAX_LABEL_SIZE);
                size = MAX_LABEL_SIZE;
            }

            if (tabbedPane.getSelectedIndex() != -1) {
                ((GraphPane) tabbedPane.getSelectedComponent()).setTextSize(size);
                tabbedPane.getSelectedComponent().repaint();
            }
            if (labelSizeSlider != null) {
                labelSizeSlider.setValue(size);
            }

        } catch (Exception e) {
            if (tabbedPane.getSelectedIndex() != -1) {
                labelSizeField.setText("" + ((GraphPane) tabbedPane.getSelectedComponent()).getTextSize());
            } else {
                labelSizeField.setText("" + DEFAULT_LABEL_SIZE);
            }
        }
    }

    /**
     * Updates the label size based on the slider value.
     */
    public void updateVertexSlider(){
        if (tabbedPane.getSelectedIndex() != -1) {
            ((GraphPane) tabbedPane.getSelectedComponent()).setRadius(vertexSizeSlider.getValue());
            if (vertexSizeField != null)
                vertexSizeField.setText("" + vertexSizeSlider.getValue());
            tabbedPane.getSelectedComponent().repaint();
        }
    }

    /**
     * Updates the label size based on the slider value.
     */
    public void updateLabelSlider(){
        if (tabbedPane.getSelectedIndex() != -1) {
            ((GraphPane) tabbedPane.getSelectedComponent()).setTextSize(labelSizeSlider.getValue());
            if (labelSizeField != null)
                labelSizeField.setText("" + labelSizeSlider.getValue());
            tabbedPane.getSelectedComponent().repaint();
        }
    }

    /**
     * Updates the tooltip text, background colour, and slide action of the given JSlider.
     * @param slider the JSlider to update.
     * @param tooltip desired tooltip text.
     * @param background desired background colour.
     * @param action method to call upon moving slider.
     * @param enabled true if the slider is usable, false otherwise.
     * @param size size of the slider bar, as a Dimension.
     */
    public void setSliderOptions(JSlider slider, String tooltip, Color background, Runnable action, boolean enabled, Dimension size){
        slider.setToolTipText(tooltip);

        slider.setBackground(background);
        slider.addChangeListener(
                new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        action.run();
                    }
                });
        slider.setEnabled(enabled);
        slider.setPreferredSize(size);
    }

    /**
     * Updates the tooltip text, box width, update action, and enabled setting of the given JTextField.
     * @param box which text field to update.
     * @param tooltip desired tooltip text.
     * @param columns number of text columns in the box.
     * @param action action to run upon updating the text.
     * @param enabled true if box is usable, false otherwise.
     */
    public void setSliderBoxOptions(JTextField box, String tooltip, int columns, Runnable action, boolean enabled){
        box.setColumns(columns);
        box.setToolTipText(tooltip);
        box.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
        box.setEnabled(enabled);
    }

    /**
     * Creates a new panel using the specified graph.
     * @param g the graph to assign to the tab.
     * @return a new tab with default settings.
     */
    private GraphPane createGraphPanel(Graph g) {
        GraphPane graphPanel = new GraphPane(this);
        graphPanel.setBackground(Color.WHITE);
        graphPanel.setSize(5000, 5000);

        graphPanel.setGraph(g);
        graphPanel.setSelectedOption(selectedOption);
        graphPanel.setDefaultCursor(defaultCursor);

        graphPanel.setDisplayVertexLabels(settings_displayVertexLabels);

        graphPanels.add(graphPanel);

        graphPanel.addMouseListener(this);

        return graphPanel;
    }

    /**
     * Initialises the toolbar with buttons and sliders.
     */
    private void createGraphEditPane() {

        graphEditPane = new JPanel();
        graphEditPane.setBackground(toolbarColour);
        graphEditPane.addMouseListener(this);

        buttonPane = new JPanel();
        buttonPane.setBackground(toolbarColour);
        buttonPane.addMouseListener(this);
        buttonPane.setLayout(new GridLayout((int) Math.ceil(choices / 2.0), 2));

        String[][] labelStrings = new String[totalChoices][2];

        labelStrings[0] = new String[]{"Insert vertex","node"};
        labelStrings[1] = new String[]{"Insert edge", "edge"};
        labelStrings[2] = new String[]{"Delete vertex", "eraser"};
        labelStrings[3] = new String[]{"Delete edge", "scissors"};
        labelStrings[4] = new String[]{"Relabel vertex", "relabel"};
        labelStrings[5] = new String[]{"Toggle dominating set", "dom"};
        labelStrings[6] = new String[]{"Modify Gridlines", "grid"};
        labelStrings[7] = new String[]{"Arrange vertices into a circle", "arrange"};
        labelStrings[8] = new String[]{"Check graph properties", "properties"};
        labelStrings[9] = new String[]{"Edit edge list", "editedges"};
        labelStrings[10] = new String[]{"Spring layout", "spring"};
        labelStrings[11] = new String[]{"Select vertices", "select"};
        labelStrings[12] = new String[]{"Rotate vertices", "rotate"};


        icons = new ImageIcon[totalChoices][4];

        for (int i = 0; i < totalChoices; i++) {
            icons[i][0] = new ImageIcon("pics/"+labelStrings[i][1]+".png");
            icons[i][1] = new ImageIcon("pics/"+labelStrings[i][1]+"_highlight.png");
            icons[i][2] = new ImageIcon("pics/"+labelStrings[i][1]+"_select.png");
            icons[i][3] = new ImageIcon("pics/"+labelStrings[i][1]+"_chosen.png");
        }


        highlighted = new boolean[totalChoices];
        pressed = new boolean[totalChoices];
        chosen = new boolean[totalChoices];

        labels = new JLabel[totalChoices];

        for (int i = 0; i < choices; i++) {
            labels[i] = new JLabel(icons[i][0]);
            labels[i].addMouseListener(this);
            labels[i].setMaximumSize(new Dimension(icons[i][0].getIconWidth(), icons[i][0].getIconHeight()));
            labels[i].setToolTipText(labelStrings[i][0]);
            buttonPane.add(labels[i]);
        }

        JPanel sliderPanel = new JPanel();
        sliderPanel.setBackground(toolbarColour);
        sliderPanel.setLayout(new GridLayout(1, 3));

        vertexSizeSlider = new JSlider(SwingConstants.VERTICAL, MIN_VERTEX_SIZE, MAX_VERTEX_SIZE, DEFAULT_VERTEX_SIZE);
        labelSizeSlider = new JSlider(SwingConstants.VERTICAL, MIN_LABEL_SIZE, MAX_LABEL_SIZE, DEFAULT_LABEL_SIZE);

        Dimension sliderSize = new Dimension(16, 150);

        setSliderOptions(vertexSizeSlider, "Resize vertices", toolbarColour, this::updateVertexSlider, false, sliderSize);
        setSliderOptions(labelSizeSlider, "Resize vertex labels", toolbarColour, this::updateLabelSlider, false, sliderSize);

        vertexSizeField = new JTextField("" + DEFAULT_VERTEX_SIZE);
        labelSizeField = new JTextField("" + DEFAULT_LABEL_SIZE);

        setSliderBoxOptions(vertexSizeField, "Resize vertices", 2, this::updateVertexTextBox, false);
        setSliderBoxOptions(labelSizeField, "Resize vertex labels", 2, this::updateLabelTextBox, false);


        JPanel vertexSizeSliderPanel = new JPanel();
        vertexSizeSliderPanel.setBackground(toolbarColour);
        vertexSizeSliderPanel.setLayout(new BorderLayout());
        vertexSizeSliderPanel.add(new JLabel(new ImageIcon("pics/vertexResizeBig.png")), BorderLayout.PAGE_START);
        vertexSizeSliderPanel.add(vertexSizeSlider, BorderLayout.CENTER);
        vertexSizeSliderPanel.add(vertexSizeField, BorderLayout.PAGE_END);

        JPanel labelSizeSliderPanel = new JPanel();
        labelSizeSliderPanel.setBackground(toolbarColour);
        labelSizeSliderPanel.setLayout(new BorderLayout());
        labelSizeSliderPanel.add(new JLabel(new ImageIcon("pics/labelResizeBig.png")), BorderLayout.PAGE_START);
        labelSizeSliderPanel.add(labelSizeSlider, BorderLayout.CENTER);
        labelSizeSliderPanel.add(labelSizeField, BorderLayout.PAGE_END);

        sliderPanel.add(vertexSizeSliderPanel);
        sliderPanel.add(new JLabel(""));
        sliderPanel.add(labelSizeSliderPanel);

        bottomButtonPane = new JPanel();
        bottomButtonPane.setBackground(toolbarColour);

        bottomButtonPane.addMouseListener(this);

        bottomButtonPane.setLayout(new GridLayout((int) Math.ceil(bottomChoices / 2.0), 2));

        for (int i = choices; i < choices + bottomChoices; i++) {
            labels[i] = new JLabel(icons[i][0]);
            labels[i].addMouseListener(this);
            labels[i].setMaximumSize(new Dimension(icons[i][0].getIconWidth(), icons[i][0].getIconHeight()));
            labels[i].setToolTipText(labelStrings[i][0]);
            bottomButtonPane.add(labels[i]);
        }


        int blanks = 4;
        JPanel[] blankPanels = new JPanel[blanks];
        JLabel[] blankLabels = new JLabel[blanks];
        for (int i = 0; i < blanks; i++) {
            blankPanels[i] = new JPanel();
            blankPanels[i].setBackground(toolbarColour);
            blankLabels[i] = new JLabel(new ImageIcon("pics/blank.png"));
            blankPanels[i].add(blankLabels[i]);
        }

        selectButtonPane = new JPanel();
        selectButtonPane.setBackground(toolbarColour);
        selectButtonPane.addMouseListener(this);
        selectButtonPane.setLayout(new GridLayout((int) Math.ceil(selectChoices / 2.0), 2));

        for (int i = choices + bottomChoices; i < totalChoices; i++) {
            labels[i] = new JLabel(icons[i][0]);
            labels[i].addMouseListener(this);
            labels[i].setMaximumSize(new Dimension(icons[i][0].getIconWidth(), icons[i][0].getIconHeight()));
            labels[i].setToolTipText(labelStrings[i][0]);
            selectButtonPane.add(labels[i]);
        }



        int padding = 20;

        SpringLayout layout = new SpringLayout();
        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, buttonPane, 0, SpringLayout.HORIZONTAL_CENTER, graphEditPane);
        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, sliderPanel, 0, SpringLayout.HORIZONTAL_CENTER, graphEditPane);
        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, bottomButtonPane, 0, SpringLayout.HORIZONTAL_CENTER, graphEditPane);
        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, selectButtonPane, 0, SpringLayout.HORIZONTAL_CENTER, graphEditPane);
        layout.putConstraint(SpringLayout.NORTH, buttonPane, padding, SpringLayout.NORTH, graphEditPane);
        layout.putConstraint(SpringLayout.NORTH, sliderPanel, padding, SpringLayout.SOUTH, buttonPane);
        layout.putConstraint(SpringLayout.NORTH, bottomButtonPane, padding, SpringLayout.SOUTH, sliderPanel);
        layout.putConstraint(SpringLayout.NORTH, selectButtonPane, padding, SpringLayout.SOUTH, bottomButtonPane);

        graphEditPane.setLayout(layout);
        graphEditPane.setPreferredSize(new Dimension(sliderPanel.getPreferredSize().width+padding, this.getHeight()));


        graphEditPane.add(buttonPane);
        graphEditPane.add(sliderPanel);
        graphEditPane.add(bottomButtonPane);
        graphEditPane.add(selectButtonPane);

    }

    /**
     * Creates a new tab using the specified panel and name.
     * @param graphPanel the panel to be assigned to the new tab.
     * @param name the name to be assigned to the new tab.
     */
    private void createWindowItem(GraphPane graphPanel, String name) {
        JMenuItem newGraphItem = new JMenuItem(name);
        windowItem.add(newGraphItem);
        graphPanel.setMenuItem(newGraphItem);
        newGraphItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JMenuItem menuItem = (JMenuItem) e.getSource();
                        for (int i = 1; i <= graphPanels.getNumber(); i++) {
                            if (graphPanels.get(i).getMenuItem().equals(menuItem)) {
                                tabbedPane.setSelectedIndex(i - 1);
                                break;
                            }
                        }
                    }
                });
    }

    /**
     * Initialises the top menu bar with all of the options and functions.
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileItem = new JMenu("File");


        JMenuItem newFileItem = createMenuItem("New", this::buildNewGraph, KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK);
        JMenuItem openFileItem = createMenuItem("Open", this::openFileDialog, KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK);
        JMenuItem closeFileItem = createMenuItem("Close", this::closeFile, KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK);
        saveFileItem = createMenuItem("Save", this::saveGraphDialog, KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);
        saveMultipleGraphsFileItem = createMenuItem("Save multiple graphs...", this::saveMultipleGraphDialog, KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK);
        exportAsImageFileItem = createMenuItem("Export as image..", this::exportAsImage, KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
        JMenuItem texItem = createMenuItem("Export to Tex", this::texDialog, KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK);
        JMenuItem exitFileItem = createMenuItem("Exit", this::exit, KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK);

        exportAsImageFileItem.setEnabled(false);
        saveMultipleGraphsFileItem.setEnabled(false);
        saveFileItem.setEnabled(false);

        fileItem.add(newFileItem);
        fileItem.add(openFileItem);
        fileItem.add(closeFileItem);
        fileItem.addSeparator();
        fileItem.add(saveFileItem);
        fileItem.add(saveMultipleGraphsFileItem);
        fileItem.addSeparator();
        fileItem.add(exportAsImageFileItem);
        fileItem.add(texItem);
        fileItem.addSeparator();
        fileItem.add(exitFileItem);
        fileItem.setMnemonic(KeyEvent.VK_F);

        JMenu editItem = menuBar.add(new JMenu("Edit"));
        undoEditItem = createMenuItem("Undo", this::undo, KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK);
        redoEditItem = createMenuItem("Redo", this::redo, KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK);
        copyItem = createMenuItem("Copy Selected", this::copySelected, KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK);
        pasteItem = createMenuItem("Paste Selected", this::pasteGraph, KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK);
        JMenuItem growItem = createMenuItem("Enlarge Selected", this::growSelected, KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK);
        JMenuItem shrinkItem = createMenuItem("Shrink Selected", this::shrinkSelected, KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK);
        JMenuItem snapGridItem = createMenuItem("Snap to Grid", this::snapToGrid,KeyEvent.VK_SLASH, InputEvent.CTRL_DOWN_MASK);
        JMenuItem gridItem = createMenuItem("Arrange Grid", this::alignToGrid, KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK);
        JMenuItem domsetItem = createMenuItem("Edit Dominating Set", this::editDominatingSet, KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK);
        JMenuItem generateItem = createMenuItem("Generate New Graph", this::generateNewGraph, KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
        JMenuItem editEdgeItem = createMenuItem("Edit Edge List", this::editEdgeList, KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK);
        JMenuItem solverItem = createMenuItem("Run Solver", this::solverDialog, KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);

        editItem.add(copyItem);
        editItem.add(pasteItem);
        editItem.addSeparator();
        editItem.add(undoEditItem);
        editItem.add(redoEditItem);
        editItem.addSeparator();
        editItem.add(growItem);
        editItem.add(shrinkItem);
        editItem.add(snapGridItem);
        editItem.addSeparator();
        editItem.add(generateItem);
        editItem.add(editEdgeItem);
        editItem.add(domsetItem);
        editItem.add(gridItem);
        editItem.add(solverItem);

        editItem.setMnemonic(KeyEvent.VK_E);
        undoEditItem.setEnabled(false);
        redoEditItem.setEnabled(false);
        pasteItem.setEnabled(false);


        JMenu viewItem = new JMenu("View");

        JMenuItem fitToScreenItem = createMenuItem("Fit to screen", this::fitToScreen, KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK);
        JMenu displayDominationTypeMenu = new JMenu("Domination type");
        JMenuItem colourSettingsItem = createMenuItem("Colour settings...", this::changeColours, KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK);
        displayVertexLabelsItem = createCheckboxMenuItem("Display vertex labels", this::setVertexLabelDisplay, KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK);
        displayCrossingsItem = createCheckboxMenuItem("Display crossings", this::setCrossingDisplay, KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK);
        displayDominationItem = createCheckboxMenuItem("Display domination", this::setDominationDisplay, KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK);
        domTotalItem = createCheckboxMenuItem("Total domination", this::setDomTotal, KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK);
        domConnectedItem = createCheckboxMenuItem("Connected domination", this::setDomConnected, KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK);
        domSecureItem = createCheckboxMenuItem("Secure domination", this::setDomSecure, KeyEvent.VK_3, InputEvent.CTRL_DOWN_MASK);
        domWeakRomanItem = createCheckboxMenuItem("Weak Roman Domination", this::setDomWeakRoman, KeyEvent.VK_4, InputEvent.CTRL_DOWN_MASK);
        domRomanItem = createCheckboxMenuItem("Roman Domination", this::setDomRoman, KeyEvent.VK_5, InputEvent.CTRL_DOWN_MASK);
        gridlinesItem = createCheckboxMenuItem("Show gridlines", this::setShowGridLines, KeyEvent.VK_PERIOD, InputEvent.CTRL_DOWN_MASK);
        gridSnapItem = createCheckboxMenuItem("Snap vertices to gridlines", this::setSnapToGrid, KeyEvent.VK_COMMA, InputEvent.CTRL_DOWN_MASK);


        displayDominationTypeMenu.add(domTotalItem);
        displayDominationTypeMenu.add(domConnectedItem);
        displayDominationTypeMenu.add(domSecureItem);
        displayDominationTypeMenu.add(domWeakRomanItem);
        displayDominationTypeMenu.add(domRomanItem);

        viewItem.add(fitToScreenItem);
        viewItem.add(displayVertexLabelsItem);
        viewItem.add(colourSettingsItem);
        viewItem.add(gridlinesItem);
        viewItem.add(gridSnapItem);
        viewItem.add(displayCrossingsItem);
        viewItem.add(displayDominationItem);
        viewItem.add(displayDominationTypeMenu);
        viewItem.setMnemonic(KeyEvent.VK_V);

        JMenuItem closeAllWindowItem = createMenuItem("Close All", this::closeAllWindows, KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK);

        windowItem = new JMenu("Window");
        windowItem.add(closeAllWindowItem);
        windowItem.addSeparator();
        windowItem.setMnemonic(KeyEvent.VK_W);

        menuBar.add(fileItem);
        menuBar.add(editItem);
        menuBar.add(viewItem);
        menuBar.add(windowItem);

        setJMenuBar(menuBar);

    }

    /**
     * Creates a new JMenuItem with the given properties.
     * @param name the name of the menu item, displayed in the menu.
     * @param method the method that clicking this menu item triggers
     * @param hotkey a KeyEvent int value referring to a key. -1 if no keypress should be mapped.
     * @param hotkey_modifier an InputEvent keydown mask.
     * @return a new JMenuItem with the given properties.
     */
    private JMenuItem createMenuItem(String name, Runnable method, int hotkey, int hotkey_modifier){
        JMenuItem menuItem = new JMenuItem(name);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                method.run();
            }
        });
        if(hotkey != -1){
            menuItem.setAccelerator(KeyStroke.getKeyStroke(hotkey, hotkey_modifier));
        }

        return menuItem;
    }

    /**
     * Creates a new JCheckboxMenuItem with the given properties.
     * @param name the name of the menu item, displayed in the menu.
     * @param method the method that clicking this menu item triggers
     * @param hotkey a KeyEvent int value referring to a key. -1 if no keypress should be mapped.
     * @param hotkey_modifier an InputEvent keydown mask.
     * @return a new JCheckboxMenuItem with the given properties.
     */
    private JCheckBoxMenuItem createCheckboxMenuItem(String name, Runnable method, int hotkey, int hotkey_modifier){
        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(name);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                method.run();
            }
        });
        if(hotkey != -1){
            menuItem.setAccelerator(KeyStroke.getKeyStroke(hotkey, hotkey_modifier));
        }

        return menuItem;
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        exit();
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
        if (e.getSource() == graphEditPane || e.getSource() == buttonPane) {
            setCursor(defaultCursor);
        }
        if (tabbedPane.getSelectedIndex() != -1) {
            if (e.getSource() == tabbedPane.getSelectedComponent()) {
                setCursor(defaultCursor);
            }
        }

        for (int i = 0; i < totalChoices; i++) {
            if (e.getSource() == labels[i]) {
                setCursor(defaultCursor);
                if (chosen[i]) {
                    labels[i].setIcon(icons[i][3]);
                } else if (pressed[i]) {
                    labels[i].setIcon(icons[i][2]);
                } else {
                    labels[i].setIcon(icons[i][1]);
                }
                highlighted[i] = true;
            }
        }
    }

    public void mouseExited(MouseEvent e) {
        if (e.getSource() == graphEditPane || e.getSource() == buttonPane || e.getSource() == bottomButtonPane) {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
        if (tabbedPane.getSelectedIndex() != -1) {
            if (e.getSource() == tabbedPane.getSelectedComponent()) {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
        for (int i = 0; i < totalChoices; i++) {
            if (e.getSource() == labels[i]) {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                if (!pressed[i]) {
                    if (chosen[i]) {
                        labels[i].setIcon(icons[i][3]);
                    } else {
                        labels[i].setIcon(icons[i][0]);
                    }
                }
                highlighted[i] = false;
            }
        }
    }

    public void mousePressed(MouseEvent e) {

        if (e.getButton() == MouseEvent.BUTTON1) {
            for (int i = 0; i < totalChoices; i++)
                if (e.getSource() == labels[i]) {
                    labels[i].setIcon(icons[i][2]);
                    pressed[i] = true;
                }
        }

        //cancel spring timer here
        GraphPane gp = ((GraphPane) tabbedPane.getSelectedComponent());
        if (gp != null) {
            gp.cancelSpring();
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            for (int i = 0; i < choices; i++){
                if (e.getSource() == labels[i]) {
                    if (pressed[i] && highlighted[i]) {

                        if (chosen[i]) {
                            chosen[i] = false;
                            changeSelectedOption(-1);
                            changeCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                            labels[i].setIcon(icons[i][1]);
                        } else {
                            for (int j = 0; j < totalChoices; j++) {
                                if (j >= choices && j < choices + bottomChoices)
                                    continue;
                                if (j == i) {
                                    chosen[i] = true;
                                    changeSelectedOption(i);
                                    changeCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                                    labels[i].setIcon(icons[i][3]);
                                } else {
                                    chosen[j] = false;
                                    labels[j].setIcon(icons[j][0]);
                                }
                            }
                        }
                    }
                }
            }

            for (int i = choices; i < choices + bottomChoices; i++) {
                if (e.getSource() == labels[i]) {
                    if (pressed[i] && highlighted[i]) {
                        if (tabbedPane.getSelectedIndex() != -1) {
                            chosen[i] = true;
                            labels[i].setIcon(icons[i][3]);
                            repaint();
                            executeBottomFunction(i);
                            chosen[i] = false;
                            repaint();
                        }
                    }
                }
            }

            for (int i = choices + bottomChoices; i < totalChoices; i++) {
                if (e.getSource() == labels[i]) {
                    if (pressed[i] && highlighted[i]) {
                        if (chosen[i]) {
                            chosen[i] = false;
                            changeSelectedOption(-1);
                            changeCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                            labels[i].setIcon(icons[i][1]);
                        } else {
                            for (int j = 0; j < totalChoices; j++) {
                                if (j >= choices && j < choices + bottomChoices) continue;
                                if (j == i) {
                                    chosen[i] = true;
                                    changeSelectedOption(i);
                                    changeCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                                    labels[i].setIcon(icons[i][3]);
                                } else {
                                    chosen[j] = false;
                                    labels[j].setIcon(icons[j][0]);
                                }
                            }
                        }
                    }
                }
            }


            for (int i = 0; i < pressed.length; i++) {
                pressed[i] = false;
                if (chosen[i]){
                    labels[i].setIcon(icons[i][3]);
                } else if (highlighted[i]) {
                    labels[i].setIcon(icons[i][1]);
                } else {
                    labels[i].setIcon(icons[i][0]);
                }
            }
        }
    }

}
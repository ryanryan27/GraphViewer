//package UGV;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;
import java.util.StringTokenizer;
import java.awt.image.*;
import java.awt.*;
import javax.swing.filechooser.*;
import javax.imageio.ImageIO;


@SuppressWarnings("Convert2Lambda")
public class UGVViewer extends JFrame implements MouseListener, WindowListener//, KeyListener
{
    public UGVViewer() {
        setTitle("Universal Graph Viewer");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());
        setAlwaysOnTop(false);
        addWindowListener(this);

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

        loadSettings();

        setLocation(settings_locationX, settings_locationY);

        createMenuBar();

        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.addChangeListener(
                new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
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
                });

        add(tabbedPane, BorderLayout.CENTER);

        createGraphEditPane();

        add(graphEditPane, BorderLayout.EAST);

        setSize(settings_frameWidth, settings_frameHeight);
        setVisible(true);
        defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);

        graphPanels = new GraphLinkedList();
    }

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
            System.err.println(e);
        }
        try {
            if (di != null)
                di.close();
        } catch (Exception e) {
            System.err.println(e);

        }
    }

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
            System.err.println(e);
        }
    }

    public void createGraphEditPane() {
        graphEditPane = new JPanel();
        graphEditPane.setLayout(new VerticalLayout());
        graphEditPane.setBackground(rightColor);
        graphEditPane.addMouseListener(this);

        buttonPane = new JPanel();
        buttonPane.setBackground(rightColor);
        buttonPane.addMouseListener(this);
        buttonPane.setLayout(new GridLayout((int) Math.ceil(choices / 2.0), 2));

        String[] toolTipsForLabels = new String[choices + bottomChoices + selectChoices];

        toolTipsForLabels[0] = "Insert vertex";
        toolTipsForLabels[1] = "Insert edge";
        toolTipsForLabels[2] = "Delete vertex";
        toolTipsForLabels[3] = "Delete edge";
        toolTipsForLabels[4] = "Relabel vertex";
        toolTipsForLabels[5] = "Toggle dominating set";
        toolTipsForLabels[6] = "Modify Gridlines";
        toolTipsForLabels[7] = "Arrange vertices into a circle";
        toolTipsForLabels[8] = "Check graph properties";
        toolTipsForLabels[9] = "Edit edge list";
        toolTipsForLabels[10] = "Spring layout";
        toolTipsForLabels[11] = "Select vertices";
        toolTipsForLabels[12] = "Rotate vertices";


        icons = new ImageIcon[choices + bottomChoices + selectChoices][4];
        icons[0][0] = new ImageIcon("pics/node.png");
        icons[0][1] = new ImageIcon("pics/node_highlight.png");
        icons[0][2] = new ImageIcon("pics/node_select.png");
        icons[0][3] = new ImageIcon("pics/node_chosen.png");


        icons[1][0] = new ImageIcon("pics/edge.png");
        icons[1][1] = new ImageIcon("pics/edge_highlight.png");
        icons[1][2] = new ImageIcon("pics/edge_select.png");
        icons[1][3] = new ImageIcon("pics/edge_chosen.png");


        icons[2][0] = new ImageIcon("pics/eraser.png");
        icons[2][1] = new ImageIcon("pics/eraser_highlight.png");
        icons[2][2] = new ImageIcon("pics/eraser_select.png");
        icons[2][3] = new ImageIcon("pics/eraser_chosen.png");


        icons[3][0] = new ImageIcon("pics/scissors.png");
        icons[3][1] = new ImageIcon("pics/scissors_highlight.png");
        icons[3][2] = new ImageIcon("pics/scissors_select.png");
        icons[3][3] = new ImageIcon("pics/scissors_chosen.png");

        icons[4][0] = new ImageIcon("pics/relabel.png");
        icons[4][1] = new ImageIcon("pics/relabel_highlight.png");
        icons[4][2] = new ImageIcon("pics/relabel_select.png");
        icons[4][3] = new ImageIcon("pics/relabel_chosen.png");

        icons[5][0] = new ImageIcon("pics/dom.png");
        icons[5][1] = new ImageIcon("pics/dom_highlight.png");
        icons[5][2] = new ImageIcon("pics/dom_select.png");
        icons[5][3] = new ImageIcon("pics/dom_chosen.png");

        icons[6][0] = new ImageIcon("pics/grid.png");
        icons[6][1] = new ImageIcon("pics/grid_highlight.png");
        icons[6][2] = new ImageIcon("pics/grid_select.png");
        icons[6][3] = new ImageIcon("pics/grid_chosen.png");

        icons[7][0] = new ImageIcon("pics/arrange.png");
        icons[7][1] = new ImageIcon("pics/arrange_highlight.png");
        icons[7][2] = new ImageIcon("pics/arrange_select.png");
        icons[7][3] = new ImageIcon("pics/arrange_chosen.png");

        icons[8][0] = new ImageIcon("pics/properties.png");
        icons[8][1] = new ImageIcon("pics/properties_highlight.png");
        icons[8][2] = new ImageIcon("pics/properties_select.png");
        icons[8][3] = new ImageIcon("pics/properties_chosen.png");

        icons[9][0] = new ImageIcon("pics/editedges.png");
        icons[9][1] = new ImageIcon("pics/editedges_highlight.png");
        icons[9][2] = new ImageIcon("pics/editedges_select.png");
        icons[9][3] = new ImageIcon("pics/editedges_chosen.png");

        icons[10][0] = new ImageIcon("pics/spring.png");
        icons[10][1] = new ImageIcon("pics/spring_highlight.png");
        icons[10][2] = new ImageIcon("pics/spring_select.png");
        icons[10][3] = new ImageIcon("pics/spring_chosen.png");

        icons[11][0] = new ImageIcon("pics/select.png");
        icons[11][1] = new ImageIcon("pics/select_highlight.png");
        icons[11][2] = new ImageIcon("pics/select_select.png");
        icons[11][3] = new ImageIcon("pics/select_chosen.png");

        icons[12][0] = new ImageIcon("pics/rotate.png");
        icons[12][1] = new ImageIcon("pics/rotate_highlight.png");
        icons[12][2] = new ImageIcon("pics/rotate_select.png");
        icons[12][3] = new ImageIcon("pics/rotate_chosen.png");

        highlighted = new boolean[choices + bottomChoices + selectChoices];
        pressed = new boolean[choices + bottomChoices + selectChoices];
        chosen = new boolean[choices + bottomChoices + selectChoices];

        labels = new JLabel[choices + bottomChoices + selectChoices];

        for (int i = 0; i < choices; i++) {
            labels[i] = new JLabel(icons[i][0]);
            labels[i].addMouseListener(this);
            labels[i].setMaximumSize(new Dimension(icons[i][0].getIconWidth(), icons[i][0].getIconHeight()));
            labels[i].setToolTipText(toolTipsForLabels[i]);
            buttonPane.add(labels[i]);
        }

        JPanel sliderPanel = new JPanel();
        sliderPanel.setBackground(rightColor);
        sliderPanel.setLayout(new GridLayout(1, 3));

        vertexSizeSlider = new JSlider(SwingConstants.VERTICAL, MIN_VERTEX_SIZE, MAX_VERTEX_SIZE, DEFAULT_VERTEX_SIZE);
        vertexSizeSlider.setToolTipText("Resize vertices");
        labelSizeSlider = new JSlider(SwingConstants.VERTICAL, MIN_LABEL_SIZE, MAX_LABEL_SIZE, DEFAULT_LABEL_SIZE);
        labelSizeSlider.setToolTipText("Resize vertex labels");

        vertexSizeSlider.setBackground(rightColor);
        vertexSizeSlider.addChangeListener(
                new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        if (tabbedPane.getSelectedIndex() != -1) {
                            ((GraphPane) tabbedPane.getSelectedComponent()).setRadius(vertexSizeSlider.getValue());
                            if (vertexSizeField != null)
                                vertexSizeField.setText("" + vertexSizeSlider.getValue());
                            tabbedPane.getSelectedComponent().repaint();
                        }
                    }
                });

        labelSizeSlider.setBackground(rightColor);
        labelSizeSlider.addChangeListener(
                new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        if (tabbedPane.getSelectedIndex() != -1) {
                            ((GraphPane) tabbedPane.getSelectedComponent()).setTextSize(labelSizeSlider.getValue());
                            if (labelSizeField != null)
                                labelSizeField.setText("" + labelSizeSlider.getValue());
                            tabbedPane.getSelectedComponent().repaint();
                        }
                    }
                });

        vertexSizeField = new JTextField("" + DEFAULT_VERTEX_SIZE);
        vertexSizeField.setColumns(2);
        vertexSizeField.setToolTipText("Resize vertices");
        vertexSizeField.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        try {
                            int size = Integer.parseInt(vertexSizeField.getText());
                            if (size < MIN_VERTEX_SIZE) vertexSizeField.setText("" + MIN_VERTEX_SIZE);
                            if (size > MAX_VERTEX_SIZE) vertexSizeField.setText("" + MAX_VERTEX_SIZE);
                        } catch (Exception e) {
                            if (tabbedPane.getSelectedIndex() != -1) {
                                vertexSizeField.setText("" + ((GraphPane) tabbedPane.getSelectedComponent()).getRadius());
                            } else
                                vertexSizeField.setText("" + DEFAULT_VERTEX_SIZE);
                        }
                    }
                });
        vertexSizeField.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void removeUpdate(DocumentEvent de) {}

                    @Override
                    public void insertUpdate(DocumentEvent de) {
                        try {
                            int size = Integer.parseInt(vertexSizeField.getText());
                            if (size < MIN_VERTEX_SIZE) size = MIN_VERTEX_SIZE;

                            if (size > MAX_VERTEX_SIZE) size = MAX_VERTEX_SIZE;


                            if (tabbedPane.getSelectedIndex() != -1) {
                                ((GraphPane) tabbedPane.getSelectedComponent()).setRadius(size);
                                tabbedPane.getSelectedComponent().repaint();
                            }
                            if (vertexSizeSlider != null) vertexSizeSlider.setValue(size);

                        } catch (Exception e) {
                            System.err.println(e);
                        }
                    }

                    @Override
                    public void changedUpdate(DocumentEvent arg0) {
                        try {
                            int size = Integer.parseInt(vertexSizeField.getText());
                            if (size < MIN_VERTEX_SIZE) size = MIN_VERTEX_SIZE;
                            if (size > MAX_VERTEX_SIZE) size = MAX_VERTEX_SIZE;

                            if (tabbedPane.getSelectedIndex() != -1) {
                                ((GraphPane) tabbedPane.getSelectedComponent()).setRadius(size);
                                tabbedPane.getSelectedComponent().repaint();
                            }
                            if (vertexSizeSlider != null) {
                                vertexSizeSlider.setValue(size);
                            }
                        } catch (Exception e) {
                            System.err.println(e);
                        }
                    }
                });


        labelSizeField = new JTextField("" + DEFAULT_LABEL_SIZE);
        labelSizeField.setColumns(2);
        labelSizeField.setToolTipText("Resize vertex labels");
        labelSizeField.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        try {
                            int size = Integer.parseInt(labelSizeField.getText());
                            if (size < MIN_LABEL_SIZE) labelSizeField.setText("" + MIN_LABEL_SIZE);
                            if (size > MAX_LABEL_SIZE) labelSizeField.setText("" + MAX_LABEL_SIZE);
                        } catch (Exception e) {
                            if (tabbedPane.getSelectedIndex() != -1) {
                                labelSizeField.setText("" + ((GraphPane) tabbedPane.getSelectedComponent()).getTextSize());
                            } else {
                                labelSizeField.setText("" + DEFAULT_LABEL_SIZE);
                            }
                        }
                    }
                });

        labelSizeField.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void removeUpdate(DocumentEvent de) {
                        try {

                            int size = Integer.parseInt(labelSizeField.getText());
                            if (size < MIN_LABEL_SIZE) size = MIN_LABEL_SIZE;
                            if (size > MAX_LABEL_SIZE) size = MAX_LABEL_SIZE;

                            if (tabbedPane.getSelectedIndex() != -1) {
                                ((GraphPane) tabbedPane.getSelectedComponent()).setTextSize(size);
                                tabbedPane.getSelectedComponent().repaint();
                            }
                            if (labelSizeSlider != null) {
                                labelSizeSlider.setValue(size);
                            }
                        } catch (Exception e) {
                            System.err.println(e);
                        }

                    }

                    @Override
                    public void insertUpdate(DocumentEvent de) {
                        try {

                            int size = Integer.parseInt(labelSizeField.getText());
                            if (size < MIN_LABEL_SIZE) size = MIN_LABEL_SIZE;
                            if (size > MAX_LABEL_SIZE) size = MAX_LABEL_SIZE;
                            if (tabbedPane.getSelectedIndex() != -1) {
                                ((GraphPane) tabbedPane.getSelectedComponent()).setTextSize(size);

                                tabbedPane.getSelectedComponent().repaint();
                            }

                            if (labelSizeSlider != null) {
                                labelSizeSlider.setValue(size);
                            }
                        } catch (Exception e) {
                            System.err.println(e);
                        }
                    }

                    @Override
                    public void changedUpdate(DocumentEvent arg0) {
                        try {

                            int size = Integer.parseInt(labelSizeField.getText());
                            if (size < MIN_LABEL_SIZE) size = MIN_LABEL_SIZE;
                            if (size > MAX_LABEL_SIZE) size = MAX_LABEL_SIZE;

                            if (tabbedPane.getSelectedIndex() != -1) {
                                ((GraphPane) tabbedPane.getSelectedComponent()).setTextSize(size);
                                tabbedPane.getSelectedComponent().repaint();
                            }
                            if (labelSizeSlider != null)
                                labelSizeSlider.setValue(size);

                        } catch (Exception e) {
                            System.err.println(e);
                        }
                    }
                });


        vertexSizeSlider.setEnabled(false);
        vertexSizeField.setEnabled(false);
        labelSizeSlider.setEnabled(false);
        labelSizeField.setEnabled(false);
        vertexSizeSlider.setPreferredSize(new Dimension(16, 150));
        labelSizeSlider.setPreferredSize(new Dimension(16, 150));

        JPanel vertexSizeSliderPanel = new JPanel();
        vertexSizeSliderPanel.setBackground(rightColor);
        vertexSizeSliderPanel.setLayout(new VerticalLayout());
        vertexSizeSliderPanel.add(new JLabel(new ImageIcon("pics/vertexResizeBig.png")));
        vertexSizeSliderPanel.add(vertexSizeSlider);
        vertexSizeSliderPanel.add(new JLabel(new ImageIcon("pics/vertexResizeSmall.png")));
        vertexSizeSliderPanel.add(vertexSizeField);

        JPanel labelSizeSliderPanel = new JPanel();
        labelSizeSliderPanel.setBackground(rightColor);
        labelSizeSliderPanel.setLayout(new VerticalLayout());
        labelSizeSliderPanel.add(new JLabel(new ImageIcon("pics/labelResizeBig.png")));
        labelSizeSliderPanel.add(labelSizeSlider);
        labelSizeSliderPanel.add(new JLabel(new ImageIcon("pics/labelResizeSmall.png")));
        labelSizeSliderPanel.add(labelSizeField);

        sliderPanel.add(vertexSizeSliderPanel);
        sliderPanel.add(new JLabel(""));
        sliderPanel.add(labelSizeSliderPanel);

        bottomButtonPane = new JPanel();
        bottomButtonPane.setBackground(rightColor);

        bottomButtonPane.addMouseListener(this);

        bottomButtonPane.setLayout(new GridLayout((int) Math.ceil(bottomChoices / 2.0), 2));

        for (int i = choices; i < choices + bottomChoices; i++) {
            labels[i] = new JLabel(icons[i][0]);
            labels[i].addMouseListener(this);
            labels[i].setMaximumSize(new Dimension(icons[i][0].getIconWidth(), icons[i][0].getIconHeight()));
            labels[i].setToolTipText(toolTipsForLabels[i]);
            bottomButtonPane.add(labels[i]);
        }


        int blanks = 4;
        JPanel[] blankPanels = new JPanel[blanks];
        JLabel[] blankLabels = new JLabel[blanks];
        for (int i = 0; i < blanks; i++) {
            blankPanels[i] = new JPanel();
            blankPanels[i].setBackground(rightColor);
            blankLabels[i] = new JLabel(new ImageIcon("pics/blank.png"));
            blankPanels[i].add(blankLabels[i]);
        }

        selectButtonPane = new JPanel();
        selectButtonPane.setBackground(rightColor);
        selectButtonPane.addMouseListener(this);
        selectButtonPane.setLayout(new GridLayout((int) Math.ceil(selectChoices / 2.0), 2));

        for (int i = choices + bottomChoices; i < choices + bottomChoices + selectChoices; i++) {
            labels[i] = new JLabel(icons[i][0]);
            labels[i].addMouseListener(this);
            labels[i].setMaximumSize(new Dimension(icons[i][0].getIconWidth(), icons[i][0].getIconHeight()));
            labels[i].setToolTipText(toolTipsForLabels[i]);
            selectButtonPane.add(labels[i]);
        }

        int blankCounter = 0;
        graphEditPane.add(blankPanels[blankCounter++]);
        graphEditPane.add(buttonPane);
        graphEditPane.add(blankPanels[blankCounter++]);
        graphEditPane.add(sliderPanel);
        graphEditPane.add(blankPanels[blankCounter++]);
        graphEditPane.add(bottomButtonPane);
        graphEditPane.add(blankPanels[blankCounter]);
        graphEditPane.add(selectButtonPane);

    }

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

        for (int i = 0; i < choices + bottomChoices + selectChoices; i++)
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

    public void mouseExited(MouseEvent e) {
        if (e.getSource() == graphEditPane || e.getSource() == buttonPane || e.getSource() == bottomButtonPane) {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
        if (tabbedPane.getSelectedIndex() != -1) {
            if (e.getSource() == tabbedPane.getSelectedComponent()) {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
        for (int i = 0; i < choices + bottomChoices + selectChoices; i++)
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

    public void mousePressed(MouseEvent e) {

        if (e.getButton() == MouseEvent.BUTTON1) {
            for (int i = 0; i < choices + bottomChoices + selectChoices; i++)
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
                            for (int j = 0; j < choices + bottomChoices + selectChoices; j++) {
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

            for (int i = choices; i < choices + bottomChoices; i++)
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

            for (int i = choices + bottomChoices; i < choices + bottomChoices + selectChoices; i++)
                if (e.getSource() == labels[i]) {
                    if (pressed[i] && highlighted[i]) {
                        if (chosen[i]) {
                            chosen[i] = false;
                            changeSelectedOption(-1);
                            changeCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                            labels[i].setIcon(icons[i][1]);
                        } else {
                            for (int j = 0; j < choices + bottomChoices + selectChoices; j++) {
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


            for (int i = 0; i < pressed.length; i++) {
                pressed[i] = false;
                if (chosen[i]) labels[i].setIcon(icons[i][3]);
                else if (highlighted[i]) labels[i].setIcon(icons[i][1]);
                else labels[i].setIcon(icons[i][0]);
            }
        }
    }


    public void changeCursor(Cursor cursor) {
        setCursor(cursor);
        defaultCursor = cursor;
        graphPanels.setDefaultCursors(cursor);
        repaint();
        validate();
    }

    public void changeSelectedOption(int so) {
        selectedOption = so;
        graphPanels.setSelectedOptions(so);
        for (int i = 0; i < choices; i++)
            if (selectedOption == i) {
                chosen[i] = true;
                labels[i].setIcon(icons[i][3]);
            } else {
                chosen[i] = false;
                labels[i].setIcon(icons[i][0]);
            }
    }

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
        if (function == BOTTOM_EDITEDGES) {
            bottomFunctionEditEdges();
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

    public void bottomFunctionEditEdges() {
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
            } else
                gp.repaint();
        }
    }

    public GraphPane makeGraphPanel() {
        return makeGraphPanel(graph);
    }

    public GraphPane makeGraphPanel(Graph g) {
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


    public void createWindowItem(GraphPane graphPanel, String name) {
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

    public int readStream(DataInputStream di) {
        int read;
        try {
            read = Integer.parseInt("" + di.readByte());
        } catch (Exception e) {
            read = -1;
        }
        return read;
    }

    public long[] getSCDData(File file) {
        DataInputStream di = null;
        try {
            di = new DataInputStream(new FileInputStream(file));
        } catch (Exception e) {
            System.err.println(e);
        }
        int degree = 1;
        long graphsToDo = 0;

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
                if (read > maxNode)
                    maxNode = read;

                graphFinished = true;
                for (int i = 0; i < maxNode; i++)
                    if (degrees[i] != degree) {
                        graphFinished = false;
                        break;
                    }

                if (!graphFinished) {
                    oldRead = read;
                    read = readStream(di);

                    if ((degrees[node - 1] != degree && read < oldRead) || read <= node || degrees[oldRead - 1] > degree) {
                        degree--;
                        for (int i = 0; i < maxNode; i++)
                            degrees[i] = 0;


                        maxNode = 1;
                        node = 1;
                        if (degree < 3) {
                            System.out.println("SCD file is invalid");
                            return new long[3];
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

                    while (degrees[node - 1] == degree)
                        node++;

                }

            }

            graphsToDo++;

            int numberToSkip = readStream(di);
            while (numberToSkip != -1) {
                graphsToDo++;
                for (int i = 0; i < maxNode * degree / 2 - numberToSkip; i++)
                    if (readStream(di) == -1) {
                        needToStart = true;
                        break;
                    }
                numberToSkip = readStream(di);
            }

            if (needToStart) {
                graphsToDo = 0;
                for (int i = 0; i < maxNode; i++)
                    degrees[i] = 0;

                degree--;
                maxNode = 1;
                node = 1;
                if (degree < 3) {
                    System.out.println("SCD file is invalid");
                    return new long[3];
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
        long[] scdData = new long[3];
        scdData[0] = maxNode;
        scdData[1] = degree;
        scdData[2] = graphsToDo;
        return scdData;
    }


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
            GraphPane panel = makeGraphPanel(g.graph);

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


    public void buildNewGraph() {
        graph = new Graph(0, 0);

        GraphPane graphPanel = makeGraphPanel();

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

    public int binaryToInt(String binary) {
        int number = 0;
        for (int i = 1; i <= binary.length(); i++)
            number += Math.pow(2, Integer.parseInt("" + binary.charAt(binary.length() - i)));

        return number;
    }


    public void saveGraphASC(String filename, GraphPane graphPane) {
        File fileToSave = new File(filename);

        Graph graph = graphPane.getGraph();

        boolean save = true;
        boolean append = false;

        if (fileToSave.exists()) {
            String[] options = new String[3];
            options[0] = "Overwrite";
            options[1] = "Append";
            options[2] = "Cancel";

            JOptionPane jop = new JOptionPane("", JOptionPane.WARNING_MESSAGE);
            int option = JOptionPane.showOptionDialog(parent, "File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?", "WARNING: File already exists!", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, jop.getIcon(), options, options[2]);
            if (option == JOptionPane.NO_OPTION)
                append = true;
            if (option == JOptionPane.CANCEL_OPTION)
                save = false;
        }
        if (save) {
            try {
                int latestGraph = 0;
                if (append && fileToSave.exists()) {
                    BufferedReader br = new BufferedReader(new FileReader(fileToSave));
                    String line = br.readLine();
                    while (line != null) {
                        if (line.length() > 5 && line.startsWith("Graph")) {
                            int graphNumber = Integer.parseInt(line.substring(6, line.indexOf(":")));
                            if (graphNumber > latestGraph)
                                latestGraph = graphNumber;
                        }
                        line = br.readLine();
                    }
                    br.close();
                }
                BufferedWriter bw = new BufferedWriter(new FileWriter(fileToSave, append));

                bw.newLine();

                latestGraph++;

                int N = graph.getN();
                int[][] arcs = graph.getArcs();
                int[] degrees = graph.getDegrees();


                bw.write(("Graph " + latestGraph + ":"));
                bw.newLine();
                bw.newLine();

                for (int i = 0; i < N; i++) {
                    String writeLine = ((i + 1) + " :");
                    for (int j = 0; j < degrees[i]; j++)
                        writeLine += (" " + arcs[i][j]);
                    bw.write(writeLine);
                    bw.newLine();
                }
                bw.write("Taillenweite: ");
                bw.newLine();
                bw.newLine();
                bw.newLine();

                bw.close();


            } catch (Exception e) {
                System.err.println(e);
            }
        }

    }

    public void saveMultipleGraphsASC(String filename, GraphPane[] graphPanes) {
        File fileToSave = new File(filename);

        boolean save = true;
        boolean append = false;

        if (fileToSave.exists()) {
            String[] options = new String[3];
            options[0] = "Overwrite";
            options[1] = "Append";
            options[2] = "Cancel";

            JOptionPane jop = new JOptionPane("", JOptionPane.WARNING_MESSAGE);
            int option = JOptionPane.showOptionDialog(parent, "File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?", "WARNING: File already exists!", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, jop.getIcon(), options, options[2]);
            if (option == JOptionPane.NO_OPTION)
                append = true;
            if (option == JOptionPane.CANCEL_OPTION)
                save = false;
        }
        if (save) {

            try {
                int latestGraph = 0;
                if (append && fileToSave.exists()) {
                    BufferedReader br = new BufferedReader(new FileReader(fileToSave));
                    String line = br.readLine();
                    while (line != null) {
                        if (line.length() > 5 && line.startsWith("Graph")) {
                            int graphNumber = Integer.parseInt(line.substring(6, line.indexOf(":")));
                            if (graphNumber > latestGraph)
                                latestGraph = graphNumber;
                        }
                        line = br.readLine();
                    }
                    br.close();
                }

                for (GraphPane graphPane : graphPanes) {
                    Graph graph = graphPane.getGraph();


                    BufferedWriter bw = new BufferedWriter(new FileWriter(fileToSave, append));

                    bw.newLine();

                    latestGraph++;

                    int N = graph.getN();
                    int[][] arcs = graph.getArcs();
                    int[] degrees = graph.getDegrees();


                    bw.write(("Graph " + latestGraph + ":"));
                    bw.newLine();
                    bw.newLine();

                    for (int i = 0; i < N; i++) {
                        String writeLine = ((i + 1) + " :");
                        for (int j = 0; j < degrees[i]; j++)
                            writeLine += (" " + arcs[i][j]);
                        bw.write(writeLine);
                        bw.newLine();
                    }
                    bw.write("Taillenweite: ");
                    bw.newLine();
                    bw.newLine();
                    bw.newLine();

                    bw.close();

                    append = true;
                }

            } catch (Exception e) {
                System.err.println(e);
            }
        }

    }

    public void saveGraphEdgeList(String filename, GraphPane graphPane) {
        File fileToSave = new File(filename);

        Graph graph = graphPane.getGraph();

        boolean save = true;

        if (fileToSave.exists()) {
            String[] options = new String[2];
            options[0] = "Overwrite";
            options[1] = "Cancel";

            JOptionPane jop = new JOptionPane("", JOptionPane.WARNING_MESSAGE);
            int option = JOptionPane.showOptionDialog(parent, "File " + filename + " already exists. Do you want to overwrite the file?", "WARNING: File already exists!", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, jop.getIcon(), options, options[1]);
            if (option == JOptionPane.NO_OPTION)
                save = false;
        }
        if (save) {
            try {
                int[][] arcs = graph.getArcs();
                int[] degrees = graph.getDegrees();

                if (degrees[degrees.length - 1] == 0) {
                    String[] options = new String[2];
                    options[0] = "Yes";
                    options[1] = "No";

                    JOptionPane jop = new JOptionPane("", JOptionPane.WARNING_MESSAGE);
                    int option = JOptionPane.showOptionDialog(parent, "Edge-list format does not retain isolated vertices if they are labelled with the largest number. Are you sure you want to save?", "WARNING: Edge-list format will not save full graph!", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, jop.getIcon(), options, options[1]);
                    if (option == JOptionPane.NO_OPTION)
                        return;
                }


                BufferedWriter bw = new BufferedWriter(new FileWriter(filename));

                for (int i = 0; i < degrees.length; i++)
                    for (int j = 0; j < degrees[i]; j++)
                        if (arcs[i][j] > i + 1) {
                            bw.write((i + 1) + " " + arcs[i][j]);
                            bw.newLine();
                        }

                bw.close();

            } catch (Exception e) {
                System.err.println(e);
            }
        }


    }

    public void saveGraph6(String filename, GraphPane graphPane) {

        Graph graph = graphPane.getGraph();

        File fileToSave = new File(filename);
        boolean save = true;
        boolean append = false;

        if (fileToSave.exists()) {
            String[] options = new String[3];
            options[0] = "Overwrite";
            options[1] = "Append";
            options[2] = "Cancel";

            JOptionPane jop = new JOptionPane("", JOptionPane.WARNING_MESSAGE);
            int option = JOptionPane.showOptionDialog(parent, "File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?", "WARNING: File already exists!", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, jop.getIcon(), options, options[2]);
            if (option == JOptionPane.NO_OPTION)
                append = true;
            if (option == JOptionPane.CANCEL_OPTION)
                save = false;
        }

        if (save) {
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(fileToSave, append));

                if (append)
                    bw.newLine();

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

                //System.out.println("c");
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

            } catch (Exception e) {
                System.err.println(e);
            }

        }

    }

    public void saveMultipleGraphs6(String filename, GraphPane[] graphPanes) {
        File fileToSave = new File(filename);
        boolean save = true;
        boolean append = false;

        if (fileToSave.exists()) {
            String[] options = new String[3];
            options[0] = "Overwrite";
            options[1] = "Append";
            options[2] = "Cancel";

            JOptionPane jop = new JOptionPane("", JOptionPane.WARNING_MESSAGE);
            int option = JOptionPane.showOptionDialog(parent, "File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?", "WARNING: File already exists!", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, jop.getIcon(), options, options[2]);
            if (option == JOptionPane.NO_OPTION)
                append = true;
            if (option == JOptionPane.CANCEL_OPTION)
                save = false;
        }

        if (save) {
            try {
                for (GraphPane graphPane : graphPanes) {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(fileToSave, append));
                    Graph graph = graphPane.getGraph();

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

    }

    public void saveGraphHCP(String filename, GraphPane graphPane) {
        File fileToSave = new File(filename);

        Graph graph = graphPane.getGraph();

        boolean save = true;

        if (fileToSave.exists()) {
            String[] options = new String[2];
            options[0] = "Overwrite";
            options[1] = "Cancel";

            JOptionPane jop = new JOptionPane("", JOptionPane.WARNING_MESSAGE);
            int option = JOptionPane.showOptionDialog(parent, "File " + filename + " already exists. Do you want to overwrite the file?", "WARNING: File already exists!", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, jop.getIcon(), options, options[1]);
            if (option == JOptionPane.NO_OPTION)
                save = false;
        }
        if (save) {
            try {
                int N = graph.getN();
                int[][] arcs = graph.getArcs();
                int[] degrees = graph.getDegrees();

                BufferedWriter bw = new BufferedWriter(new FileWriter(fileToSave));

                bw.write(("NAME : " + filename));
                bw.newLine();
                bw.write("COMMENT : Hamiltonian cycle problem (Erbacci)");
                bw.newLine();
                bw.write("TYPE : HCP");
                bw.newLine();
                bw.write(("DIMENSION : " + N));
                bw.newLine();
                bw.write("EDGE_DATA_FORMAT : EDGE_LIST");
                bw.newLine();
                bw.write("EDGE_DATA_SECTION");
                bw.newLine();

                for (int i = 0; i < N; i++)
                    for (int j = 0; j < degrees[i]; j++)
                        if ((i + 1) < arcs[i][j]) {
                            bw.write((i + 1) + " " + arcs[i][j]);
                            bw.newLine();
                        }

                bw.write("-1");
                bw.newLine();
                bw.write("EOF");
                bw.newLine();
                bw.close();
            } catch (Exception e) {
                System.err.println(e);
            }
        }

    }

    public void saveGraphSCD(String filename, GraphPane graphPane) {

        Graph graph = graphPane.getGraph();

        if (!graph.isConnected()) {
            JOptionPane.showMessageDialog(parent, ("The graph is disconnected. SCD format requires connected regular graphs."), "Error saving as SCD", JOptionPane.ERROR_MESSAGE);
            return;

        }

        File fileToSave = new File(filename);

        int[] degrees = graph.getDegrees();
        for (int i = 1; i < degrees.length; i++)
            if (degrees[i] != degrees[0]) {
                JOptionPane.showMessageDialog(parent, "Only regular graphs may be saved in SCD format!", "Error saving as SCD", JOptionPane.ERROR_MESSAGE);
                return;
            }

        int degree = degrees[0];
        int maxNode = graph.getN();

        boolean save = true;
        boolean append = false;

        if (fileToSave.exists()) {

            String[] options = new String[3];
            options[0] = "Overwrite";
            options[1] = "Append";
            options[2] = "Cancel";

            JOptionPane jop = new JOptionPane("", JOptionPane.WARNING_MESSAGE);
            int option = JOptionPane.showOptionDialog(parent, "File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?", "WARNING: File already exists!", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, jop.getIcon(), options, options[2]);
            if (option == JOptionPane.NO_OPTION) append = true;
            if (option == JOptionPane.CANCEL_OPTION) save = false;


        }

        if (save) {
            try {
                if (append) {
                    long[] scdData = getSCDData(fileToSave);
                    if ((int) scdData[0] != maxNode || (int) scdData[1] != degree) {
                        JOptionPane.showMessageDialog(parent, (fileToSave.getName() + " contains graphs of size " + (int) scdData[0] + " and degree " + (int) scdData[1] + " which are incompatible with this graph of size " + maxNode + " and degree " + degree + "."), "Error saving as SCD", JOptionPane.ERROR_MESSAGE);
                        return;

                    }

                    DataInputStream dis = new DataInputStream(new FileInputStream(fileToSave));

                    long graphsToDo = scdData[2];

                    int[] stream = new int[maxNode * degree / 2];

                    for (long graphcount = 0; graphcount < graphsToDo; graphcount++) {
                        int index = readStream(dis);
                        for (int i = index; i < maxNode * degree / 2; i++)
                            stream[i] = readStream(dis);
                    }

                    dis.close();

                    int[] newStream = new int[maxNode * degree / 2];
                    int[][] arcs = graph.getArcs();
                    int count = 0;
                    for (int i = 0; i < maxNode; i++)
                        for (int j = 0; j < degrees[i]; j++)
                            if (arcs[i][j] > i + 1)
                                newStream[count++] = arcs[i][j];

                    int repeat = 0;
                    int index = 0;
                    while (index < maxNode * degree / 2 && newStream[index] == stream[index++]) {
                        repeat++;
                    }

                    DataOutputStream dos = new DataOutputStream(new FileOutputStream(fileToSave, true));

                    dos.writeByte(repeat);
                    for (int i = repeat; i < maxNode * degree / 2; i++) {
                        dos.writeByte(newStream[i]);
                    }
                    dos.close();

                } else {

                    DataOutputStream dos = new DataOutputStream(new FileOutputStream(fileToSave));
                    dos.writeByte(0);
                    int[][] arcs = graph.getArcs();
                    for (int i = 0; i < maxNode; i++)
                        for (int j = 0; j < degrees[i]; j++)
                            if (arcs[i][j] > i + 1)
                                dos.writeByte(arcs[i][j]);
                    dos.close();

                }

            } catch (Exception e) {
                System.err.println(e);
            }
        }


    }

    public void saveMultipleGraphsSCD(String filename, GraphPane[] graphPanes) {
        Graph graph = graphPanes[0].getGraph();
        int[] degrees = graph.getDegrees();
        int degree = degrees[0];
        int maxNode = graph.getN();

        for (int graphs = 0; graphs < graphPanes.length; graphs++) {
            graph = graphPanes[graphs].getGraph();
            if (!graph.isConnected()) {
                JOptionPane.showMessageDialog(parent, ("Graph " + (graphs + 1) + " is disconnected. SCD format requires connected regular graphs."), "Error saving as SCD", JOptionPane.ERROR_MESSAGE);
                return;

            }


            degrees = graph.getDegrees();
            for (int i = 1; i < degrees.length; i++)
                if (degrees[i] != degrees[0]) {
                    JOptionPane.showMessageDialog(parent, "Only regular graphs may be saved in SCD format! Graph " + (graphs + 1) + " is irregular!", "Error saving as SCD", JOptionPane.ERROR_MESSAGE);
                    return;
                }

            if (degrees[0] != degree || graph.getN() != maxNode) {
                JOptionPane.showMessageDialog(parent, "All graphs must have the same number of vertices, and have the same vertex degree!", "Error saving as SCD", JOptionPane.ERROR_MESSAGE);
                return;

            }

        }

        File fileToSave = new File(filename);

        boolean save = true;
        boolean append = false;

        if (fileToSave.exists()) {

            String[] options = new String[3];
            options[0] = "Overwrite";
            options[1] = "Append";
            options[2] = "Cancel";

            JOptionPane jop = new JOptionPane("", JOptionPane.WARNING_MESSAGE);
            int option = JOptionPane.showOptionDialog(parent, "File " + filename + " already exists. Do you want to overwrite the file, append to the file, or cancel saving?", "WARNING: File already exists!", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, jop.getIcon(), options, options[2]);
            if (option == JOptionPane.NO_OPTION)
                append = true;
            if (option == JOptionPane.CANCEL_OPTION)
                save = false;


        }

        if (save) {
            try {
                int[] stream = new int[maxNode * degree / 2];
                if (append) {
                    long[] scdData = getSCDData(fileToSave);
                    if ((int) scdData[0] != maxNode || (int) scdData[1] != degree) {
                        JOptionPane.showMessageDialog(parent, (fileToSave.getName() + " contains graphs of size " + (int) scdData[0] + " and degree " + (int) scdData[1] + " which are incompatible with graphs of size " + maxNode + " and degree " + degree + "."), "Error saving as SCD", JOptionPane.ERROR_MESSAGE);
                        return;

                    }

                    DataInputStream dis = new DataInputStream(new FileInputStream(fileToSave));

                    long graphsToDo = scdData[2];

                    for (long graphcount = 0; graphcount < graphsToDo; graphcount++) {
                        int index = readStream(dis);
                        for (int i = index; i < maxNode * degree / 2; i++)
                            stream[i] = readStream(dis);
                    }

                    dis.close();
                }
                DataOutputStream dos = new DataOutputStream(new FileOutputStream(fileToSave, append));
                for (GraphPane graphPane : graphPanes) {
                    graph = graphPane.getGraph();

                    int[] newStream = new int[maxNode * degree / 2];
                    int[][] arcs = graph.getArcs();
                    int count = 0;

                    for (int i = 0; i < maxNode; i++) {
                        for (int j = 0; j < degrees[i]; j++) {
                            if (arcs[i][j] > i + 1) newStream[count++] = arcs[i][j];
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

    }


    public void saveGraph(String filename, GraphPane gp, int type) {
        GraphPane[] gps = new GraphPane[1];
        gps[0] = gp;
        saveGraph(filename, gps, type);

    }

    public void saveGraph(String filename, GraphPane[] graphPanes, int type) {

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
            if (option == JOptionPane.NO_OPTION)
                append = true;
            if (option == JOptionPane.CANCEL_OPTION)
                save = false;


        }

        if (!save) {
            return;
        }

        GraphData[] graphs = new GraphData[graphPanes.length];

        for (int i = 0; i < graphPanes.length; i++) {
            GraphPane gp = graphPanes[i];
            graphs[i] = new GraphData(gp.getGraph());
            graphs[i].scale = gp.getXScale();
            graphs[i].x_offset = gp.getXTopLeft();
            graphs[i].y_offset = gp.getYTopLeft();
            graphs[i].radius = gp.getRadius();
        }

        FileParser fp = new FileParser();

        if(type == FileParser.FILE_GML) {
            fp.saveGML(graphs, file, append);
        } else if(type == FileParser.FILE_UGV){
            fp.saveUGV(graphs, file, append);
        }

    }


    public void createMenuBar() {
        menuBar = new JMenuBar();

        JMenu fileItem = new JMenu("File");
        JMenuItem newFileItem = new JMenuItem("New");
        JMenuItem openFileItem = new JMenuItem("Open");
        JMenuItem closeFileItem = new JMenuItem("Close");
        saveFileItem = new JMenuItem("Save");
        saveMultipleGraphsFileItem = new JMenuItem("Save multiple graphs...");
        exportAsImageFileItem = new JMenuItem("Export as image..");
        JMenuItem exitFileItem = new JMenuItem("Exit");

        newFileItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        buildNewGraph();
                    }
                });
        newFileItem.setMnemonic(KeyEvent.VK_N);
        newFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));

        openFileItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
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
                });
        openFileItem.setMnemonic(KeyEvent.VK_O);
        openFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));

        closeFileItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
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
                });
        closeFileItem.setMnemonic(KeyEvent.VK_C);
        closeFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));

        saveFileItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
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

                                    saveGraphASC(jfc.getSelectedFile().getParent() + "//" + filename, graphPane);
                                } else if (extensionName.equals(graph6Name)) {
                                    settings_saveFilter = 1;
                                    String filename = jfc.getSelectedFile().getName();
                                    if (!filename.endsWith(".g6")) {
                                        filename = filename + ".g6";
                                    }


                                    saveGraph6(jfc.getSelectedFile().getParent() + "//" + filename, graphPane);
                                } else if (extensionName.equals(hcpName)) {
                                    settings_saveFilter = 2;
                                    String filename = jfc.getSelectedFile().getName();
                                    if (!filename.endsWith(".hcp")) {
                                        filename = filename + ".hcp";
                                    }

                                    saveGraphHCP(jfc.getSelectedFile().getParent() + "//" + filename, graphPane);

                                } else if (extensionName.equals(scdName)) {
                                    settings_saveFilter = 3;
                                    String filename = jfc.getSelectedFile().getName();
                                    if (!filename.endsWith(".scd")) {
                                        filename = filename + ".scd";
                                    }

                                    saveGraphSCD(jfc.getSelectedFile().getParent() + "//" + filename, graphPane);

                                } else if (extensionName.equals(edgeListName)) {
                                    settings_saveFilter = 4;
                                    String filename = jfc.getSelectedFile().getName();
                                    if (!filename.endsWith(".txt")) {
                                        filename = filename + ".txt";
                                    }

                                    saveGraphEdgeList(jfc.getSelectedFile().getParent() + "//" + filename, graphPane);

                                } else if (extensionName.equals(ugvName)) {
                                    settings_saveFilter = 5;
                                    String filename = jfc.getSelectedFile().getName();
                                    if (!filename.endsWith(".ugv")) {
                                        filename = filename + ".ugv";
                                    }
                                    saveGraph(jfc.getSelectedFile().getParent() + "//" + filename, graphPane, FileParser.FILE_UGV);
                                } else if (extensionName.equals(gmlName)) {
                                    settings_saveFilter = 6;
                                    String filename = jfc.getSelectedFile().getName();
                                    if (!filename.endsWith(".gml")) {
                                        filename = filename + ".gml";
                                    }
                                    saveGraph(jfc.getSelectedFile().getParent() + "//" + filename, graphPane, FileParser.FILE_GML);
                                }

                                graphPane.getUndoState().setLastSave();
                                checkSave(tabbedPane.getSelectedIndex());


                                saveSettings();
                            }
                            repaint();
                        }
                    }
                });
        saveFileItem.setMnemonic(KeyEvent.VK_S);
        saveFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveFileItem.setEnabled(false);

        saveMultipleGraphsFileItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
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

                                            saveMultipleGraphsASC(jfc.getSelectedFile().getParent() + "//" + filename, graphPanes);
                                        } else if (extensionName.equals(graph6Name)) {
                                            settings_saveFilter = 1;
                                            String filename = jfc.getSelectedFile().getName();
                                            if (!filename.endsWith(".g6")) {
                                                filename = filename + ".g6";
                                            }


                                            saveMultipleGraphs6(jfc.getSelectedFile().getParent() + "//" + filename, graphPanes);
                                        } else if (extensionName.equals(scdName)) {
                                            settings_saveFilter = 3;
                                            String filename = jfc.getSelectedFile().getName();
                                            if (!filename.endsWith(".scd")) {
                                                filename = filename + ".scd";
                                            }

                                            saveMultipleGraphsSCD(jfc.getSelectedFile().getParent() + "//" + filename, graphPanes);

                                        } else if (extensionName.equals(ugvName)) {
                                            settings_saveFilter = 5;
                                            String filename = jfc.getSelectedFile().getName();
                                            if (!filename.endsWith(".ugv")) {
                                                filename = filename + ".ugv";
                                            }
                                            saveGraph(jfc.getSelectedFile().getParent() + "//" + filename, graphPanes, FileParser.FILE_UGV);
                                        } else if (extensionName.equals(gmlName)) {
                                            settings_saveFilter = 6;
                                            String filename = jfc.getSelectedFile().getName();
                                            if (!filename.endsWith(".gml")) {
                                                filename = filename + ".gml";
                                            }
                                            saveGraph(jfc.getSelectedFile().getParent() + "//" + filename, graphPanes, FileParser.FILE_GML);
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
                });
        saveMultipleGraphsFileItem.setMnemonic(KeyEvent.VK_M);
        saveMultipleGraphsFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));
        saveMultipleGraphsFileItem.setEnabled(false);

        exportAsImageFileItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent de) {
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
                                System.err.println(e);
                            }
                            gp.setSavingWithTransparentBackground(false);
                            saveSettings();
                        }
                        repaint();
                    }
                });
        exportAsImageFileItem.setMnemonic(KeyEvent.VK_E);
        exportAsImageFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        exportAsImageFileItem.setEnabled(false);


        texItem = new JMenuItem("Export to Tex");
        texItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (tabbedPane.getSelectedIndex() != -1) {
                            GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();

                            Graph g = gp.getGraph();

                            new TexDialog(parent, g);
                        }
                    }
                });
        texItem.setMnemonic(KeyEvent.VK_T);
        texItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));


        exitFileItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        exit();
                    }
                });
        exitFileItem.setMnemonic(KeyEvent.VK_X);
        exitFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));


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

        undoEditItem = new JMenuItem("Undo");
        undoEditItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (tabbedPane.getSelectedIndex() != -1) {
                            ((GraphPane) tabbedPane.getSelectedComponent()).undo();
                        }
                    }
                });
        undoEditItem.setMnemonic(KeyEvent.VK_U);
        undoEditItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));

        redoEditItem = new JMenuItem("Redo");
        redoEditItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (tabbedPane.getSelectedIndex() != -1) {
                            ((GraphPane) tabbedPane.getSelectedComponent()).redo();
                        }
                    }
                });
        redoEditItem.setMnemonic(KeyEvent.VK_R);
        redoEditItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));

        checkCrossingsItem = new JMenuItem("Check Crossings");
        checkCrossingsItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (tabbedPane.getSelectedIndex() != -1) {
                            ((GraphPane) tabbedPane.getSelectedComponent()).checkCrossings();
                        }
                    }
                });
        checkCrossingsItem.setMnemonic(KeyEvent.VK_C);
        checkCrossingsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK));

        copyItem = new JMenuItem("Copy Selected");
        copyItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        copySelected();

                    }
                });
        copyItem.setMnemonic(KeyEvent.VK_K);
        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));

        pasteItem = new JMenuItem("Paste Selected");
        pasteItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        pasteGraph();
                        //copySelected();
                        validate();
                        repaint();
                    }
                });
        pasteItem.setMnemonic(KeyEvent.VK_C);
        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));

        growItem = new JMenuItem("Enlarge Selected");
        growItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (tabbedPane.getSelectedIndex() != -1) {
                            GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();
                            gp.setUndoState();
                            Graph g = gp.getGraph();
                            g.rescaleSelected(1.0 / 1.1);
                            validate();
                            repaint();
                        }

                    }
                });
        growItem.setMnemonic(KeyEvent.VK_K);
        growItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK));


        shrinkItem = new JMenuItem("Shrink Selected");
        shrinkItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (tabbedPane.getSelectedIndex() != -1) {
                            GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();
                            gp.setUndoState();
                            Graph g = gp.getGraph();
                            g.rescaleSelected(1.1);

                            validate();
                            repaint();

                        }

                    }
                });
        shrinkItem.setMnemonic(KeyEvent.VK_K);
        shrinkItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));

        snapGridItem = new JMenuItem("Snap to Grid");
        snapGridItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
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
                });
        snapGridItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, InputEvent.CTRL_DOWN_MASK));

        gridItem = new JMenuItem("Arrange Grid");
        gridItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
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
                });
        gridItem.setMnemonic(KeyEvent.VK_K);
        gridItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK));

        domsetItem = new JMenuItem("Edit Dominating Set");
        domsetItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
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
                });
        domsetItem.setMnemonic(KeyEvent.VK_D);
        domsetItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));

        generateItem = new JMenuItem("Generate New Graph");
        generateItem.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {


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
                }
        );
        generateItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));

        editEdgeItem = new JMenuItem("Edit Edge List");
        editEdgeItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (tabbedPane.getSelectedIndex() != -1) {
                            bottomFunctionEditEdges();

                        }

                    }
                });
        editEdgeItem.setMnemonic(KeyEvent.VK_E);
        editEdgeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));


        solverItem = new JMenuItem("Run Solver");
        solverItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
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
                });
        solverItem.setMnemonic(KeyEvent.VK_D);
        solverItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));

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

        JMenuItem fitToScreenItem = new JMenuItem("Fit to screen");
        fitToScreenItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        fitToScreen();
                    }
                });
        fitToScreenItem.setMnemonic(KeyEvent.VK_F);
        fitToScreenItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));

        displayVertexLabelsItem = new JCheckBoxMenuItem("Display vertex labels", settings_displayVertexLabels);
        displayVertexLabelsItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        settings_displayVertexLabels = displayVertexLabelsItem.getState();
                        saveSettings();
                        if (tabbedPane.getSelectedIndex() != -1) {
                            ((GraphPane) tabbedPane.getSelectedComponent()).setDisplayVertexLabels(displayVertexLabelsItem.getState());
                            tabbedPane.getSelectedComponent().repaint();
                        }
                    }
                });
        displayVertexLabelsItem.setMnemonic(KeyEvent.VK_B);
        displayVertexLabelsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));

        JMenuItem colourSettingsItem = new JMenuItem("Colour settings...");
        colourSettingsItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
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

                });
        colourSettingsItem.setMnemonic(KeyEvent.VK_L);
        colourSettingsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));

        displayCrossingsItem = new JCheckBoxMenuItem("Display crossings");
        displayCrossingsItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (tabbedPane.getSelectedIndex() != -1) {
                            ((GraphPane) tabbedPane.getSelectedComponent()).setDisplayCrossings(displayCrossingsItem.getState());
                            tabbedPane.getSelectedComponent().repaint();
                        }
                    }
                });
        displayCrossingsItem.setMnemonic(KeyEvent.VK_R);
        displayCrossingsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));

        displayDominationItem = new JCheckBoxMenuItem("Display domination");
        displayDominationItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (tabbedPane.getSelectedIndex() != -1) {
                            ((GraphPane) tabbedPane.getSelectedComponent()).setDisplayDomination(displayDominationItem.getState());
                            tabbedPane.getSelectedComponent().repaint();
                        }
                    }
                });
        displayDominationItem.setMnemonic(KeyEvent.VK_I);
        displayDominationItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));

        displayDominationTypeMenu = new JMenu("Domination type");
        domTotalItem = new JCheckBoxMenuItem("Total domination");
        domTotalItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (tabbedPane.getSelectedIndex() != -1) {
                            ((GraphPane) tabbedPane.getSelectedComponent()).setDomTotal(domTotalItem.getState());
                            tabbedPane.getSelectedComponent().repaint();
                        }
                    }
                });
        domTotalItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK));

        domConnectedItem = new JCheckBoxMenuItem("Connected domination");
        domConnectedItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (tabbedPane.getSelectedIndex() != -1) {
                            ((GraphPane) tabbedPane.getSelectedComponent()).setDomConnected(domConnectedItem.getState());
                            tabbedPane.getSelectedComponent().repaint();
                        }
                    }
                });
        domConnectedItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK));

        domSecureItem = new JCheckBoxMenuItem("Secure domination");
        domSecureItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (tabbedPane.getSelectedIndex() != -1) {
                            ((GraphPane) tabbedPane.getSelectedComponent()).setDomSecure(domSecureItem.getState());
                            tabbedPane.getSelectedComponent().repaint();
                        }
                    }
                });
        domSecureItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_DOWN_MASK));

        domWeakRomanItem = new JCheckBoxMenuItem("Weak Roman domination");
        domWeakRomanItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (tabbedPane.getSelectedIndex() != -1) {
                            ((GraphPane) tabbedPane.getSelectedComponent()).setDomWeakRoman(domWeakRomanItem.getState());
                            tabbedPane.getSelectedComponent().repaint();
                        }
                    }
                });
        domWeakRomanItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_DOWN_MASK));

        domRomanItem = new JCheckBoxMenuItem("Roman domination");
        domRomanItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (tabbedPane.getSelectedIndex() != -1) {
                            ((GraphPane) tabbedPane.getSelectedComponent()).setDomRoman(domRomanItem.getState());
                            tabbedPane.getSelectedComponent().repaint();
                        }
                    }
                });
        domRomanItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.CTRL_DOWN_MASK));


        gridlinesItem = new JCheckBoxMenuItem("Show Gridlines");
        gridlinesItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (tabbedPane.getSelectedIndex() != -1) {
                            ((GraphPane) tabbedPane.getSelectedComponent()).setGridlines(gridlinesItem.getState());
                            tabbedPane.getSelectedComponent().repaint();
                        }

                    }
                });
        gridlinesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, InputEvent.CTRL_DOWN_MASK));

        gridSnapItem = new JCheckBoxMenuItem("Snap vertices to gridlines");
        gridSnapItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (tabbedPane.getSelectedIndex() != -1) {
                            ((GraphPane) tabbedPane.getSelectedComponent()).setSnapToGrid(gridSnapItem.getState());
                            tabbedPane.getSelectedComponent().repaint();
                        }
                    }
                });
        gridSnapItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, InputEvent.CTRL_DOWN_MASK));

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

        JMenuItem closeAllWindowItem = new JMenuItem("Close All");
        closeAllWindowItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
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
                });
        closeAllWindowItem.setMnemonic(KeyEvent.VK_A);
        closeAllWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));

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

    public void setUndoAvailable(boolean available) {
        undoEditItem.setEnabled(available);
    }

    public void setRedoAvailable(boolean available) {
        redoEditItem.setEnabled(available);
    }

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


    public Color[] getDefaultColors() {
        Color[] tempColors = new Color[defaultColors.length];
        System.arraycopy(defaultColors, 0, tempColors, 0, defaultColors.length);
        return tempColors;
    }

    public void saveDefaultColors(Color[] dc) {
        System.arraycopy(dc, 0, defaultColors, 0, dc.length);
        saveSettings();
    }

    public Color[] getOriginalDefaultColors() {
        Color[] tempColors = new Color[originalDefaultColors.length];
        System.arraycopy(originalDefaultColors, 0, tempColors, 0, originalDefaultColors.length);
        return tempColors;
    }

    public void checkSave() {
        checkSave(tabbedPane.getSelectedIndex());
    }

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


    public void copySelected() {
        if (tabbedPane.getSelectedIndex() != -1) {
            GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();
            Graph g = gp.getGraph();
            copiedGraph = g.getSubgraph(g.getSelected(), true);
            pasteItem.setEnabled(true);
        }
    }

    public void pasteGraph() {
        if (copiedGraph != null && tabbedPane.getSelectedIndex() != -1) {
            GraphPane gp = (GraphPane) tabbedPane.getSelectedComponent();
            gp.pasteGraph(copiedGraph);
        }
    }

    public void runMILP(int domType, boolean preserve) {
        runMILP(domType, preserve, null);
    }

    public void runMILP(int domType, int[] fixed) {
        runMILP(domType, false, fixed);
    }

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
                System.err.println(e);
            }
        }

    }

    Graph graph;
    JMenuBar menuBar;

    Graph copiedGraph;

    JTabbedPane tabbedPane;
    GraphLinkedList graphPanels;

    JSlider vertexSizeSlider, labelSizeSlider;
    JTextField vertexSizeField, labelSizeField;

    JCheckBoxMenuItem displayVertexLabelsItem;
    JCheckBoxMenuItem displayCrossingsItem;
    JCheckBoxMenuItem displayDominationItem;
    JMenu displayDominationTypeMenu;
    JCheckBoxMenuItem domTotalItem;
    JCheckBoxMenuItem domSecureItem;
    JCheckBoxMenuItem domConnectedItem;
    JCheckBoxMenuItem domRomanItem;
    JCheckBoxMenuItem domWeakRomanItem;
    JMenu windowItem;
    JMenuItem texItem;
    JMenuItem saveFileItem, saveMultipleGraphsFileItem, exportAsImageFileItem, undoEditItem, redoEditItem, checkCrossingsItem, copyItem, pasteItem;
    JMenuItem growItem, shrinkItem;
    JMenuItem gridItem, domsetItem, solverItem, editEdgeItem, generateItem;
    JCheckBoxMenuItem gridlinesItem, gridSnapItem;
    JMenuItem snapGridItem;

    JPanel graphEditPane, buttonPane, bottomButtonPane, selectButtonPane;
    JLabel[] labels;
    ImageIcon[][] icons;
    boolean[] highlighted;
    boolean[] pressed;
    boolean[] chosen;
    int choices = 7;
    int bottomChoices = 4;
    int selectChoices = 2;
    int selectedOption = -1;

    final Color rightColor = new Color(0.7529f, 0.7529f, 0.7529f);

    final int MIN_VERTEX_SIZE = 1;
    final int MAX_VERTEX_SIZE = 50;
    final int DEFAULT_VERTEX_SIZE = 12;
    final int MIN_LABEL_SIZE = 5;
    final int MAX_LABEL_SIZE = 50;
    final int DEFAULT_LABEL_SIZE = 12;

    final int BOTTOM_ARRANGE = choices;
    final int BOTTOM_PROPERTIES = choices + 1;
    final int BOTTOM_EDITEDGES = choices + 2;
    final int BOTTOM_SPRING = choices + 3;

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

}
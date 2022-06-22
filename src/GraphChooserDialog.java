import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;


public class GraphChooserDialog extends JDialog implements ActionListener {

    JFrame parent;
    JTabbedPane openGraphs;

    JList<ListItem> graphList;

    JButton confirmButton;
    JButton cancelButton;

    String chosenName;
    Graph chosenGraph;
    boolean cancelled;

    /**
     * Creates a dialog box to select a currently open graph.
     * @param parent the parent frame from which to create this dialog.
     * @param openGraphs Tabbed pane that holds all open graph panes.
     */
    public GraphChooserDialog(JFrame parent, JTabbedPane openGraphs){
        super(parent,true);

        this.parent = parent;
        this.openGraphs = openGraphs;
        cancelled = true;

        setTitle("Choose Open Graph");
        setSize(300,200);
        setResizable(true);
        setLocationRelativeTo(parent);

        getContentPane().setLayout(new BorderLayout());

        confirmButton = new JButton("Confirm");
        cancelButton = new JButton("Cancel");

        confirmButton.addActionListener(this);
        cancelButton.addActionListener(this);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout());
        buttonPane.add(confirmButton);
        buttonPane.add(cancelButton);


        buildList();
        getContentPane().add(new JScrollPane(graphList), BorderLayout.NORTH);
        getContentPane().add(buttonPane);


        getRootPane().setDefaultButton(confirmButton);

        setVisible(true);

    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == confirmButton){

            if(openGraphs.getTabCount() > 0) {

                chosenGraph = graphList.getSelectedValue().graph;
                chosenName = graphList.getSelectedValue().name;
                cancelled = false;
            }
            setVisible(false);
            dispose();
        }
        if(e.getSource() == cancelButton){
            setVisible(false);
            dispose();
        }

    }

    /**
     * Builds the displayed list of currently open graphs.
     */
    private void buildList(){

        if(openGraphs.getTabCount() < 1){
            graphList = new JList<>();
        } else {

            Vector<ListItem> items = new Vector<>();

            for (int i = 0; i < openGraphs.getTabCount(); i++) {
                GraphPane gp = (GraphPane) openGraphs.getComponentAt(i);
                ListItem item = new ListItem(gp.getGraph(), openGraphs.getTitleAt(i));
                items.add(item);
            }

            graphList = new JList<>(items);
            graphList.setSelectedIndex(0);
        }
        graphList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    }

    /**
     * Returns whether the dialog was closed before being completed.
     * @return whether the dialog was cancelled.
     */
    public boolean cancelled(){
        return cancelled;
    }

    /**
     * Get the name of the selected graph.
     * @return the name of the selected graph.
     */
    public String getChosenName(){
        return chosenName;
    }

    /**
     * Get the selected graph.
     * @return the selected graph.
     */
    public Graph getChosenGraph(){
        return chosenGraph;
    }

    private static class ListItem{
        Graph graph;
        String name;

        /**
         * Creates a new item to add to the list.
         * @param graph the graph associated with the list item.
         * @param name the name of the graph.
         */
        ListItem(Graph graph, String name){
            this.graph = graph;
            this.name = name;
        }

        /**
         * The name of the graph associated with this list item.
         * @return the name of the graph.
         */
        @Override
        public String toString(){
            return this.name;
        }

    }
}

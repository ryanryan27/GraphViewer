//package UGV;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.Vector;

public class GraphBuilderDialog extends JDialog implements ActionListener
{
   final int MAX_INT_INPUT = 2;
   final int MAX_GRAPH_INPUT = 2;


   JFrame parent;
   JTabbedPane openGraphs;

   JPanel leftPanel;
   JList<ChoiceItem> choiceList;

   JPanel rightPanel;
   JPanel inputPanel;
   JPanel buttonPanel;
   JButton confirmButton;
   JButton cancelButton;

   JPanel[] intInputs;
   JPanel[] graphInputs;

   JTextField[] intValues;

   JTextField[] graphNameBoxes;

   JButton[] graphBuildButtons;
   JButton[] graphSelectButtons;

   Graph[] graphs;
   String[] graphNames;

   Graph output;
   String output_name;

   boolean cancelled;

   /**
    * Creates a dialog box to specify a new graph to generate, along with required inputs for that graph.
    * @param frame parent frame from which to create this dialog.
    * @param openGraphs Tabbed pane that holds all open graph panes.
    */
   public GraphBuilderDialog(JFrame frame, JTabbedPane openGraphs)
   {
      super(frame, true);

      parent = frame;
      this.openGraphs = openGraphs;
      cancelled = true;
      graphs = new Graph[MAX_GRAPH_INPUT];
      graphNames = new String[MAX_GRAPH_INPUT];

      setTitle("Generate New Graph");
      setSize(800,400);
      setResizable(true);
      setLocationRelativeTo(parent);

      getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.X_AXIS));

      buildLeftPanel();
      buildRightPanel();

      getContentPane().add(leftPanel);
      getContentPane().add(rightPanel);

      getRootPane().setDefaultButton(confirmButton);

      setVisible(true);
   }

   public void actionPerformed(ActionEvent e)
   {

      for (int i = 0; i < MAX_GRAPH_INPUT; i++) {
         if(e.getSource() == graphBuildButtons[i]){
            buildGraph(i);
         }
         if(e.getSource() == graphSelectButtons[i]){
            selectGraph(i);
         }
      }


      if(e.getSource() == confirmButton){
         evaluateGraph();


      }

      if(e.getSource() == cancelButton)
      {
         setVisible(false);
         dispose();
      }

   }

   /**
    * Builds the component holding list of available graph families or operators.
    */
   private void buildLeftPanel(){
      buildChoiceList();
      leftPanel = new JPanel();
      leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

      JLabel lb = new JLabel("Graph Type");
      lb.setAlignmentX(JLabel.LEFT_ALIGNMENT);

      JScrollPane jsp = new JScrollPane(choiceList);
      jsp.setAlignmentX(JScrollPane.LEFT_ALIGNMENT);

      leftPanel.add(lb);
      leftPanel.add(jsp);
      leftPanel.setPreferredSize(new Dimension(400, 400));


   }

   /**
    * Builds the input options for each graph family or operator.
    */
   private void buildRightPanel(){
      buildInputList();
      rightPanel = new JPanel();
      buttonPanel = new JPanel();

      confirmButton = new JButton("Confirm");
      confirmButton.addActionListener(this);
      cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(this);

      buttonPanel.setLayout(new FlowLayout());
      buttonPanel.add(confirmButton);
      buttonPanel.add(cancelButton);

      rightPanel.setLayout(new BorderLayout());
      rightPanel.add(inputPanel, BorderLayout.NORTH);
      rightPanel.add(buttonPanel, BorderLayout.SOUTH);
      rightPanel.setPreferredSize(new Dimension(400,400));

   }

   /**
    * Builds the list component of the available graph families or operators.
    * Specify new ones here.
    */
   private void buildChoiceList(){

      Vector<ChoiceItem> choices = new Vector<>();

      choices.add(new ChoiceItem("Cartesian Product", 0, 2));
      choices.add(new ChoiceItem("Path", 1, 0));
      choices.add(new ChoiceItem("Path (Vertical)", 1, 0));
      choices.add(new ChoiceItem("Cycle", 1, 0));
      choices.add(new ChoiceItem("Cycle (Linear)", 1, 0));
      choices.add(new ChoiceItem("Grid", 2, 0));
      choices.add(new ChoiceItem("Torus Grid", 2, 0));
      choices.add(new ChoiceItem("Flower Snark", 1, 0));
      choices.add(new ChoiceItem("Complete", 1, 0));
      choices.add(new ChoiceItem("Star", 1, 0));



      choiceList = new JList<>(choices);
      choiceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      choiceList.addListSelectionListener(new ListSelectionListener() {
         @Override
         public void valueChanged(ListSelectionEvent e) {
            updateInputList(choiceList.getSelectedValue());
         }
      });


   }

   /**
    * Builds each individual input option for each graph family or operator.
    */
   private void buildInputList(){
      inputPanel = new JPanel();
      inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.PAGE_AXIS));

      intValues = new JTextField[MAX_INT_INPUT];
      intInputs = new JPanel[MAX_INT_INPUT];

      for (int i = 0; i < MAX_INT_INPUT; i++) {
         intValues[i] = new JTextField();
         intValues[i].setColumns(8);
         intInputs[i] = new JPanel();
         intInputs[i].setLayout(new FlowLayout(FlowLayout.LEFT));
         intInputs[i].add(new JLabel("Input "+ (i + 1) +":"));
         intInputs[i].add(intValues[i]);
         intInputs[i].setVisible(false);
         intInputs[i].setAlignmentX(JPanel.LEFT_ALIGNMENT);
      }

      graphNameBoxes = new JTextField[MAX_GRAPH_INPUT];
      graphBuildButtons = new JButton[MAX_GRAPH_INPUT];
      graphSelectButtons = new JButton[MAX_GRAPH_INPUT];
      graphInputs = new JPanel[MAX_GRAPH_INPUT];

      for (int i = 0; i < MAX_GRAPH_INPUT; i++) {

         graphNameBoxes[i] = new JTextField();
         graphNameBoxes[i].setEditable(false);
         graphNameBoxes[i].setColumns(30);
         graphBuildButtons[i] = new JButton("Build");
         graphBuildButtons[i].addActionListener(this);
         graphSelectButtons[i] = new JButton("Select");
         graphSelectButtons[i].addActionListener(this);
         graphInputs[i] = new JPanel();
         graphInputs[i].setLayout(new FlowLayout(FlowLayout.LEFT));
         graphInputs[i].add(graphNameBoxes[i]);
         graphInputs[i].add(graphBuildButtons[i]);
         graphInputs[i].add(graphSelectButtons[i]);
         graphInputs[i].setVisible(false);
         graphInputs[i].setAlignmentX(JPanel.LEFT_ALIGNMENT);
      }

      JLabel lb = new JLabel("Inputs");
      lb.setAlignmentX(JLabel.LEFT_ALIGNMENT);
      inputPanel.add(lb);

      for (int i = 0; i < MAX_INT_INPUT; i++) {
         inputPanel.add(intInputs[i]);
      }
      for (int i = 0; i < MAX_GRAPH_INPUT; i++) {
         inputPanel.add(graphInputs[i]);
      }

   }

   /**
    * Changes visibility of different input options based on which graph family or operator is selected.
    * @param choice the currently selected choice from the list.
    */
   private void updateInputList(ChoiceItem choice){

      for (int i = 0; i < MAX_INT_INPUT; i++) {
         intInputs[i].setVisible(i < choice.int_inputs);

      }for (int i = 0; i < MAX_GRAPH_INPUT; i++) {
         graphInputs[i].setVisible(i < choice.graph_inputs);
      }

      getContentPane().revalidate();
      getContentPane().repaint();

   }

   /**
    * Calls graph builder to generate the graph from the selected option and inputs.
    */
   private void evaluateGraph(){
      ChoiceItem choice = choiceList.getSelectedValue();

      if(choice == null) return;


      if(Objects.equals(choice.name, "Path")){
         output = GraphBuilder.path(Integer.parseInt(intValues[0].getText()));
         output_name = choice.name +"("+ intValues[0].getText()+")";
      }
      else if(Objects.equals(choice.name, "Path (Vertical)")){
         output = GraphBuilder.path(Integer.parseInt(intValues[0].getText()), true);
         output_name = choice.name +"("+ intValues[0].getText()+")";
      }
      else if(Objects.equals(choice.name, "Cycle")){
         output = GraphBuilder.cycle(Integer.parseInt(intValues[0].getText()), false);
         output_name = choice.name +"("+ intValues[0].getText()+")";
      }
      else if(Objects.equals(choice.name, "Cycle (Linear)")){
         output = GraphBuilder.cycle(Integer.parseInt(intValues[0].getText()), true);
         output_name = choice.name +"("+ intValues[0].getText()+")";
      }
      else if(Objects.equals(choice.name, "Torus Grid")){
         String int_one = intValues[0].getText();
         String int_two = intValues[1].getText();
         output = GraphBuilder.cartesian_product(GraphBuilder.cycle(Integer.parseInt(int_two), true), GraphBuilder.cycle(Integer.parseInt(int_one), true), true);
         output_name = choice.name + "(" + int_one +","+int_two+")";
      }
      else if(Objects.equals(choice.name, "Grid")){
         String int_one = intValues[0].getText();
         String int_two = intValues[1].getText();
         output = GraphBuilder.grid(Integer.parseInt(int_two), Integer.parseInt(int_one));
         output_name = choice.name + "(" + int_one +","+int_two+")";
      }
      else if(Objects.equals(choice.name, "Cartesian Product")){
         output = GraphBuilder.cartesian_product(graphs[0],graphs[1]);
         output_name = graphNames[0] + " X " + graphNames[1];
      }
      else if(Objects.equals(choice.name, "Flower Snark")){
         output = GraphBuilder.flower_snark(Integer.parseInt(intValues[0].getText()));
         output_name = choice.name +"("+ intValues[0].getText()+")";
      }
      else if(Objects.equals(choice.name, "Complete")){
         output = GraphBuilder.complete(Integer.parseInt(intValues[0].getText()));
         output_name = choice.name +"("+ intValues[0].getText()+")";
      }
      else if(Objects.equals(choice.name, "Star")){
         output = GraphBuilder.star(Integer.parseInt(intValues[0].getText()));
         output_name = choice.name +"("+ intValues[0].getText()+")";
      }



      if(output == null) return;

      cancelled = false;
      setVisible(false);
      dispose();


   }

   /**
    * Creates a dialog box to generate a new graph for the Build button.
    * @param input_num which input slot was clicked.
    */
   private void buildGraph(int input_num){
      GraphBuilderDialog gbd = new GraphBuilderDialog(parent, openGraphs);

      if(!gbd.cancelled()){
         graphs[input_num] = gbd.getGraph();
         graphNames[input_num] = gbd.getName();
         graphNameBoxes[input_num].setText(graphNames[input_num]);
      }
   }

   /**
    * Creates a dialog box to select an existing graph for the Select button.
    * @param input_num which input slot was clicked.
    */
   private void selectGraph(int input_num){

      GraphChooserDialog gcd = new GraphChooserDialog(parent, openGraphs);

      if(!gcd.cancelled()){
         if(!gcd.cancelled()){
            graphs[input_num] = gcd.getChosenGraph();
            graphNames[input_num] = gcd.getChosenName();
            graphNameBoxes[input_num].setText(graphNames[input_num]);
         }
      }



   }

   /**
    * Returns the graph generated from the selected options.
    * @return the generated graph.
    */
   public Graph getGraph(){
      return output;
   }

   /**
    * Returns the name of the generated graph.
    * @return the name of the generated graph.
    */
   public String getName(){
      return output_name;
   }

   /**
    * Returns whether the dialog was closed before being completed.
    * @return whether the dialog was cancelled.
    */
   public boolean cancelled(){
      return cancelled;
   }


   private static class ChoiceItem{

      String name;

      int graph_inputs;
      int int_inputs;

      /**
       * Creates a new ChoiceItem for the graph build selection menu.
       * @param name The option name that shows in the menu.
       * @param int_inputs The number of integer inputs for the graph builder.
       * @param graph_inputs The number of graph inputs for the graph builder.
       */
      ChoiceItem(String name, int int_inputs, int graph_inputs){
         this.name = name;
         this.int_inputs = int_inputs;
         this.graph_inputs = graph_inputs;
      }


      /**
       * Returns the specified name of list item.
       * @return the name assigned to the list item.
       */
      @Override
      public String toString(){
         return name;
      }

   }

}
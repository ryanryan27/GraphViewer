//package UGV;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.Vector;

public class GraphBuilderDialog extends JDialog implements ActionListener
{
   JFrame parent;
   JTabbedPane openGraphs;

   JPanel leftPanel;
   JList<ChoiceItem> choiceList;

   JPanel rightPanel;
   JPanel inputPanel;
   JPanel buttonPanel;
   JButton confirmButton;
   JButton cancelButton;

   JPanel intInputOne;
   JPanel intInputTwo;
   JPanel graphInputOne;
   JPanel graphInputTwo;

   JTextField intOneValue;
   JTextField intTwoValue;

   JTextField graphOneName;
   JTextField graphTwoName;

   JButton graphOneBuildButton;
   JButton graphOneSelectButton;
   JButton graphTwoBuildButton;
   JButton graphTwoSelectButton;



   Graph g1;
   Graph g2;
   String g1_name;
   String g2_name;

   Graph output;
   String output_name;

   boolean cancelled;

   public GraphBuilderDialog(JFrame frame, JTabbedPane openGraphs)
   {
      super(frame, true);

      parent = frame;
      this.openGraphs = openGraphs;
      cancelled = true;

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


      if(e.getSource() == graphOneBuildButton){
         buildGraph(1);
      }
      if(e.getSource() == graphTwoBuildButton){
         buildGraph(2);
      }
      if(e.getSource() == graphOneSelectButton){
         selectGraph(1);
      }
      if(e.getSource() == graphTwoSelectButton){
         selectGraph(2);
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

   private void buildChoiceList(){

      Vector<ChoiceItem> choices = new Vector<>();

      choices.add(new ChoiceItem("Cartesian Product", ChoiceItem.GRAPH, ChoiceItem.GRAPH));
      choices.add(new ChoiceItem("Path", ChoiceItem.INTEGER));
      choices.add(new ChoiceItem("Cycle", ChoiceItem.INTEGER));
      choices.add(new ChoiceItem("Cycle (Linear)", ChoiceItem.INTEGER));
      choices.add(new ChoiceItem("Torus Grid", ChoiceItem.INTEGER, ChoiceItem.INTEGER));
      choices.add(new ChoiceItem("Flower Snark", ChoiceItem.INTEGER));
      choices.add(new ChoiceItem("Complete", ChoiceItem.INTEGER));
      choices.add(new ChoiceItem("Star", ChoiceItem.INTEGER));



      choiceList = new JList<>(choices);
      choiceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      choiceList.addListSelectionListener(new ListSelectionListener() {
         @Override
         public void valueChanged(ListSelectionEvent e) {
            updateInputList(choiceList.getSelectedValue());
         }
      });


   }

   private void buildInputList(){
      inputPanel = new JPanel();
      inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.PAGE_AXIS));

      intOneValue = new JTextField();
      intOneValue.setColumns(8);
      intInputOne = new JPanel();
      intInputOne.setLayout(new FlowLayout(FlowLayout.LEFT));
      intInputOne.add(new JLabel("Input 1:"));
      intInputOne.add(intOneValue);
      intInputOne.setVisible(false);
      intInputOne.setAlignmentX(JPanel.LEFT_ALIGNMENT);

      intTwoValue = new JTextField();
      intTwoValue.setColumns(8);
      intInputTwo = new JPanel();
      intInputTwo.setLayout(new FlowLayout(FlowLayout.LEFT));
      intInputTwo.add(new JLabel("Input 2:"));
      intInputTwo.add(intTwoValue);
      intInputTwo.setVisible(false);
      intInputTwo.setAlignmentX(JPanel.LEFT_ALIGNMENT);

      graphOneName = new JTextField();
      graphOneName.setEditable(false);
      graphOneName.setColumns(30);
      graphOneBuildButton = new JButton("Build");
      graphOneBuildButton.addActionListener(this);
      graphOneSelectButton = new JButton("Select");
      graphOneSelectButton.addActionListener(this);
      graphInputOne = new JPanel();
      graphInputOne.setLayout(new FlowLayout(FlowLayout.LEFT));
      graphInputOne.add(graphOneName);
      graphInputOne.add(graphOneBuildButton);
      graphInputOne.add(graphOneSelectButton);
      graphInputOne.setVisible(false);
      graphInputOne.setAlignmentX(JPanel.LEFT_ALIGNMENT);

      graphTwoName = new JTextField();
      graphTwoName.setEditable(false);
      graphTwoName.setColumns(30);
      graphTwoBuildButton = new JButton("Build");
      graphTwoBuildButton.addActionListener(this);
      graphTwoSelectButton = new JButton("Select");
      graphTwoSelectButton.addActionListener(this);
      graphInputTwo = new JPanel();
      graphInputTwo.setLayout(new FlowLayout(FlowLayout.LEFT));
      graphInputTwo.add(graphTwoName);
      graphInputTwo.add(graphTwoBuildButton);
      graphInputTwo.add(graphTwoSelectButton);
      graphInputTwo.setVisible(false);
      graphInputTwo.setAlignmentX(JPanel.LEFT_ALIGNMENT);

      JLabel lb = new JLabel("Inputs");
      lb.setAlignmentX(JLabel.LEFT_ALIGNMENT);
      inputPanel.add(lb);
      inputPanel.add(intInputOne);
      inputPanel.add(intInputTwo);
      inputPanel.add(graphInputOne);
      inputPanel.add(graphInputTwo);

   }

   private void updateInputList(ChoiceItem choice){

      intInputOne.setVisible(false);
      intInputTwo.setVisible(false);
      graphInputOne.setVisible(false);
      graphInputTwo.setVisible(false);


      switch (choice.inputs[0]) {
         case ChoiceItem.INTEGER: intInputOne.setVisible(true); break;
         case ChoiceItem.GRAPH: graphInputOne.setVisible(true); break;
      }


      if(choice.inputs.length == 2) {

         switch (choice.inputs[1]) {
            case ChoiceItem.INTEGER: intInputTwo.setVisible(true);break;
            case ChoiceItem.GRAPH: graphInputTwo.setVisible(true);break;
         }
      }

      getContentPane().revalidate();
      getContentPane().repaint();

   }

   private void evaluateGraph(){
      ChoiceItem choice = choiceList.getSelectedValue();

      if(choice == null) return;


      if(Objects.equals(choice.name, "Path")){
         output = GraphBuilder.path(Integer.parseInt(intOneValue.getText()));
         output_name = choice.name +"("+ intOneValue.getText()+")";
      }
      else if(Objects.equals(choice.name, "Cycle")){
         output = GraphBuilder.cycle(Integer.parseInt(intOneValue.getText()), false);
         output_name = choice.name +"("+ intOneValue.getText()+")";
      }
      else if(Objects.equals(choice.name, "Cycle (Linear)")){
         output = GraphBuilder.cycle(Integer.parseInt(intOneValue.getText()), true);
         output_name = choice.name +"("+ intOneValue.getText()+")";
      }
      else if(Objects.equals(choice.name, "Torus Grid")){
         String int_one = intOneValue.getText();
         String int_two = intTwoValue.getText();
         output = GraphBuilder.cartesian_product(GraphBuilder.cycle(Integer.parseInt(int_two), true), GraphBuilder.cycle(Integer.parseInt(int_one), true), true);
         output_name = choice.name + "(" + int_one +","+int_two+")";
      }
      else if(Objects.equals(choice.name, "Cartesian Product")){
         output = GraphBuilder.cartesian_product(g1,g2);
         output_name = g1_name + " X " + g2_name;
      }
      else if(Objects.equals(choice.name, "Flower Snark")){
         output = GraphBuilder.flower_snark(Integer.parseInt(intOneValue.getText()));
         output_name = choice.name +"("+ intOneValue.getText()+")";
      }
      else if(Objects.equals(choice.name, "Complete")){
         output = GraphBuilder.complete(Integer.parseInt(intOneValue.getText()));
         output_name = choice.name +"("+ intOneValue.getText()+")";
      }
      else if(Objects.equals(choice.name, "Star")){
         output = GraphBuilder.star(Integer.parseInt(intOneValue.getText()));
         output_name = choice.name +"("+ intOneValue.getText()+")";
      }



      if(output == null) return;

      cancelled = false;
      setVisible(false);
      dispose();


   }

   private void buildGraph(int input_num){
      GraphBuilderDialog gbd = new GraphBuilderDialog(parent, openGraphs);

      if(!gbd.cancelled()){
         if(input_num == 1){
            g1 = gbd.getGraph();
            g1_name = gbd.getName();
            graphOneName.setText(g1_name);
         } else {
            g2 = gbd.getGraph();
            g2_name = gbd.output_name;
            graphTwoName.setText(g2_name);
         }
      }
   }

   private void selectGraph(int input_num){

      GraphChooserDialog gcd = new GraphChooserDialog(parent, openGraphs);

      if(!gcd.cancelled()){
         if(input_num == 1){
            g1 = gcd.getChosenGraph();
            g1_name = gcd.getChosenName();
            graphOneName.setText(g1_name);
         } else {
            g2 = gcd.getChosenGraph();
            g2_name = gcd.getChosenName();
            graphOneName.setText(g2_name);
         }
      }



   }

   public Graph getGraph(){
      return output;
   }

   public String getName(){
      return output_name;
   }

   public boolean cancelled(){
      return cancelled;
   }

   private class ChoiceItem{

      static final int INTEGER = 0;
      static final int GRAPH = 1;

      int[] inputs;
      String name;

      ChoiceItem(String name, int input_type){
         inputs = new int[]{input_type};
         this.name = name;
      }

      ChoiceItem(String name, int input1, int input2){
         inputs = new int[]{input1, input2};
         this.name = name;
      }

      @Override
      public String toString(){
         return name;
      }

   }

}
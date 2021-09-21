import java.util.Stack;


//stores before state and after states of graph. need to input current state on undo/redo

public class UndoRedo {

    private final Stack<Graph> undo;
    private final Stack<Graph> redo;

    int stepsSinceSave;


    public UndoRedo(){
        undo = new Stack<>();
        redo = new Stack<>();

        stepsSinceSave = 0;
    }

    public void addItem(Graph g){

        redo.clear();


        undo.push(g.getCopy());
        if(stepsSinceSave >= 0) {
            stepsSinceSave++;
        }


    }


    //call like graph = undo(graph);
    public Graph undo(Graph beforeUndo){


        if(!canUndo()) return null;
        Graph g = undo.pop();
        stepsSinceSave--;
        redo.push(beforeUndo);
        return g;
    }

    //cal like graph = redo(graph)
    public Graph redo(Graph beforeRedo){

        if(!canRedo()) return null;

        Graph g = redo.pop();
        undo.push(beforeRedo);
        stepsSinceSave++;
        return g;
    }

    public boolean canRedo(){
        return (!redo.empty());
    }

    public boolean canUndo(){

        return  (!undo.empty());
    }

    public boolean getLastSave(){
        return stepsSinceSave == 0;
    }

    public void setLastSave(){
        stepsSinceSave = 0;
    }





}

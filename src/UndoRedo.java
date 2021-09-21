import java.util.Stack;


//stores before state and after states of graph. need to input current state on undo/redo

public class UndoRedo {

    private final Stack<Graph> undo;
    private final Stack<Graph> redo;


    public UndoRedo(){
        undo = new Stack<>();
        redo = new Stack<>();
    }

    public void addItem(Graph g){

        undo.push(g);
        redo.clear();

    }


    //call like graph = undo(graph);
    public Graph undo(Graph beforeUndo){

        if(!canUndo()) return null;

        Graph g = undo.pop();
        redo.push(beforeUndo);
        return g;
    }

    //cal like graph = redo(graph)
    public Graph redo(Graph beforeRedo){

        if(!canRedo()) return null;

        Graph g = redo.pop();
        undo.push(beforeRedo);
        return g;
    }

    public boolean canRedo(){
        return (redo.peek() != null);
    }

    public boolean canUndo(){
        return  (undo.peek() != null);
    }



}

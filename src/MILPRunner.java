import ilog.concert.*;
import ilog.cplex.*;

import java.util.Arrays;

public class MILPRunner {


    private final Graph graph;
    private final int domType;

    public static int DOMINATION = 1;
    public static int TOTAL_DOMINATION = 2;


    private IloCplex model;
    private IloNumVar[][] variables;
    private IloRange[][] constraints;

    public MILPRunner(int domType, Graph graph){
        this.graph = graph;
        this.domType = domType;
    }


    public double[] run() throws IloException {

        buildModel();
        model.solve();

        double[] solution = model.getValues(variables[0]);

        /*
         for (int j = 0; j < solution.length; ++j) {
             System.out.println("Variable " + j + ": Value = " + solution[j]);
         }
        */
        model.end();

        return solution;
    }

    private void buildModel() throws IloException {

        int N = graph.getN();

        if(N == 0) return;

        model = new IloCplex();
        variables = new IloNumVar[1][];
        constraints = new IloRange[1][];

        setBinaryVariables(N);
        setDominationConstraints(N);

    }

    //gives the model N binary variables
    private void setBinaryVariables(int N) throws IloException {
        double[] xlb = new double[N];
        double[] xub = new double[N];

        //set upper bounds to 1
        Arrays.fill(xub, 1);

        //set variable type to binary
        IloNumVarType[] xtype = new IloNumVarType[N];
        Arrays.fill(xtype, IloNumVarType.Bool);

        //give the model the variables
        IloNumVar[] x = model.numVarArray(N, xlb, xub, xtype);

        variables[0] = x;

        //add the objective function
        model.addMinimize(model.scalProd(variables[0], xub));
    }

    //sets the domination constraints
    private void setDominationConstraints(int N) throws IloException {

        //N total constraints in domination LP
        constraints[0] = new IloRange[N];

        for (int i = 0; i < N; i++) {

            //set this to 0 for total domination
            double domSelf = 1.0;
            if(domType == TOTAL_DOMINATION) {
                domSelf = 0;
            }
            IloNumExpr constr = model.prod(domSelf, variables[0][i]);
            for (int j = 0; j < N; j++) {
                if(graph.isArc(i+1,j+1)){
                    constr = model.sum(constr, model.prod(1.0, variables[0][j]));
                }
            }
            constraints[0][i] = model.addGe(constr, 1.0);

        }

    }


}

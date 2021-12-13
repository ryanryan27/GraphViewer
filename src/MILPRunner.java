import ilog.concert.*;
import ilog.cplex.*;

import java.util.Arrays;

public class MILPRunner {


    private final Graph graph;
    private final int domType;
    private final boolean preserveDom;
    private int domcount = 0;

    public final static int DOMINATION = 1;
    public final static int TOTAL_DOMINATION = 2;
    public final static int SECURE_DOMINATION = 3;
    public final static int ROMAN_DOMINATION = 4;
    public final static int WEAK_ROMAN_DOMINATION = 5;
    public final static int CONNECTED_DOMINATION = 6;
    public final static int UPPER_DOMINATION = 7;
    public final static int UPPER_DOMINATION_ALT = 8;


    private IloCplex model;
    private IloNumVar[][] variables;
    private IloRange[][] constraints;

    private final int N;
    private final int M;

    public MILPRunner(int domType, Graph graph){
       this(domType, graph, false);
    }

    public MILPRunner(int domType, Graph graph, boolean preserveDom){
        this.graph = graph;
        this.domType = domType;
        this.preserveDom = preserveDom;

        N= graph.getN();
        M = graph.getEdgeCount();
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

        if(N == 0) return;

        model = new IloCplex();
        variables = new IloNumVar[1][];
        constraints = new IloRange[2][];

        setVariables();
        setConstraints();
        //setDominationConstraints();
        model.setOut(null);
    }


    private void setVariables() throws IloException {

        int binary;
        int continuous;

        switch (domType) {
            case SECURE_DOMINATION:
                binary = N;
                continuous = N * N;
                break;
            case ROMAN_DOMINATION:
            case UPPER_DOMINATION:
                binary = 2 * N;
                continuous = 0;
                break;
            case WEAK_ROMAN_DOMINATION:
                binary = 2 * N;
                continuous = N * N;
                break;
            case CONNECTED_DOMINATION:
                binary = 2 * M + 3 * N + 1;
                continuous = N + 1;
                break;
            case UPPER_DOMINATION_ALT:
                binary = 2 * N + 2 * M;
                continuous = 0;
                break;
//DOMINATION, TOTAL_DOMINATION
            default:
                binary = N;
                continuous = 0;
                break;
        }

        int total = binary + continuous;
        double[] xlb = new double[total];
        double[] xub = new double[total];

        if(domType == CONNECTED_DOMINATION){
            Arrays.fill(xub, 0, binary, 1);
            Arrays.fill(xub, binary, total, Integer.MAX_VALUE);
        }else {
            Arrays.fill(xub, 1);
        }

        IloNumVarType[] xtype = new IloNumVarType[total];
        Arrays.fill(xtype, 0, binary, IloNumVarType.Bool);
        Arrays.fill(xtype, binary, binary+continuous, IloNumVarType.Float);
        Arrays.fill(xtype, binary+continuous, total, IloNumVarType.Int);



        IloNumVar[] x = model.numVarArray(total, xlb, xub, xtype);

        variables[0] = x;

        double[] objMultipliers = new double[total];

        switch (domType) {
            case ROMAN_DOMINATION:
            case WEAK_ROMAN_DOMINATION:
                Arrays.fill(objMultipliers, 0, N, 1);
                Arrays.fill(objMultipliers, N, 2 * N, 2);
                break;
            default:
                Arrays.fill(objMultipliers, 0, N, 1);
                break;
        }

        if(domType == UPPER_DOMINATION || domType == UPPER_DOMINATION_ALT) {
            model.addMaximize(model.scalProd(variables[0], objMultipliers));
        } else{
            model.addMinimize(model.scalProd(variables[0], objMultipliers));
        }

    }

    private void setConstraints() throws IloException {
        int numContraintsEq;
        int numContraintsIneq;

        int[][] twoApartList;
        int twoApartCount = 0;

        if(domType == SECURE_DOMINATION || domType == WEAK_ROMAN_DOMINATION){
            twoApartList = new int[N][];
            for (int i = 0; i < N; i++) {
                twoApartList[i] = graph.twoApartList(i);
                twoApartCount += twoApartList[i].length;
            }
            System.out.println("twoApartCount = " + twoApartCount);
        } else {
            twoApartList = new int[1][];
        }

        //SD and wrd needs to be calculated
        switch (domType) {
            case SECURE_DOMINATION:
            case WEAK_ROMAN_DOMINATION:
                numContraintsIneq = N + 2 * M + twoApartCount;
                numContraintsEq = N;
                break;
            case ROMAN_DOMINATION:
                numContraintsIneq = 3 * N;
                numContraintsEq = 0;
                break;
            case CONNECTED_DOMINATION:
                numContraintsIneq = 3 * N + 4 * M + 1;
                numContraintsEq = 2 * N + 2;
                break;
            case UPPER_DOMINATION:
                numContraintsIneq = 4 * N;
                numContraintsEq = 0;
                break;
            case UPPER_DOMINATION_ALT:
                numContraintsIneq = 3 * N + 2 * M;
                numContraintsEq = 0;
                break;
            default:
                numContraintsIneq = N;
                numContraintsEq = 0;
                break;
        }

        if(preserveDom){

            //calculate number of verts with guards
            int[] domset = graph.getDomset();

            domcount = 0;

            for (int i = 0; i < N; i++) {
                if (domset[i] > 0){
                    domcount++;
                }
            }

            numContraintsEq += domcount;

        }


        constraints[0] = new IloRange[numContraintsIneq];
        constraints[1] = new IloRange[numContraintsEq];

        switch (domType) {
            case SECURE_DOMINATION:
            case WEAK_ROMAN_DOMINATION:
                setSDConstraints(twoApartList);
                break;
            case CONNECTED_DOMINATION:
                setCDConstraints();
                break;
            case UPPER_DOMINATION:
                setUDConstraints();
                break;
            case UPPER_DOMINATION_ALT:
                setUDAConstraints();
                break;
            default:
                setDominationConstraints();
                break;
        }


    }

    //sets the base domination constraints
    private void setDominationConstraints() throws IloException {
        //N total constraints in domination LP

        for (int i = 0; i < N; i++) {

            //set this to 0 for total domination
            double domSelf = 1.0;
            if(domType == TOTAL_DOMINATION) {
                domSelf = 0;
            }
            IloNumExpr constr = model.prod(domSelf, variables[0][i]);

            if(domType == WEAK_ROMAN_DOMINATION || domType == ROMAN_DOMINATION){
                constr = model.sum(constr, variables[0][N+i]);
            }

            for (int j = 0; j < N; j++) {
                if(graph.isArc(i+1,j+1)){

                    if(domType != ROMAN_DOMINATION) {
                        constr = model.sum(constr, model.prod(1.0, variables[0][j]));
                    }

                    if(domType == WEAK_ROMAN_DOMINATION || domType == ROMAN_DOMINATION){
                        constr = model.sum(constr, variables[0][N+j]);
                    }
                }
            }
            constraints[0][i] = model.addGe(constr, 1.0);

        }

        if(preserveDom){

            //calculate number of verts with guards
            int[] domset = graph.getDomset();

            int count = 0;

            for (int i = 0; i < N; i++) {
                if (domset[i] > 0){

                    IloNumExpr constr = model.prod(1, variables[0][i + N*(domset[i]-1)]);
                    int index = constraints[1].length - domcount + count;
                    constraints[1][index] = model.addEq(constr, 1);
                    count++;
                }
            }

        }

    }

    //secure domination and weak roman domination
    private void setSDConstraints(int[][] twoApartList) throws IloException {
        int count = N;
        int offset = 0;

        if(domType == WEAK_ROMAN_DOMINATION){
            offset = N;
        }


        //constraint set 1 - N constraints
        setDominationConstraints();

        //constraint 2 yij - xi <= 0
        for (int i = 0; i < N; i++) {
            int[] neighbours = graph.getArcs()[i];
            for (int j = 0; j < graph.getDegrees()[i]; j++) {

                //-xi
                IloNumExpr constr = model.prod(-1, variables[0][i]);

                //-wi
                if(domType == WEAK_ROMAN_DOMINATION){
                    constr = model.sum(constr, model.prod(-1, variables[0][N+i]));
                }

                //yij
                constr = model.sum(constr, variables[0][offset + N + i*N + neighbours[j]-1]);

                // <= 0
                constraints[0][count] = model.addLe(constr, 0);
                count++;
            }
        }


        //constraint 3
        for (int j = 0; j < N; j++) {
            int[] twoAway = twoApartList[j];
            for (int ii = 0; ii < twoAway.length; ii++) {

                int i = twoAway[ii];

                //xi
                IloNumExpr constr = model.prod(1, variables[0][i]);

                //2*wi
                if(domType == WEAK_ROMAN_DOMINATION){
                    constr = model.sum(constr, model.prod(2, variables[0][N+i]));
                }


                int[] neighbours = graph.getArcs()[i];
                int ni = graph.getDegrees()[i];
                for (int k = 0; k < ni; k++) {
                    //xk - k in neighbourhood i
                    constr = model.sum(constr, variables[0][neighbours[k]-1]);

                    if(domType == WEAK_ROMAN_DOMINATION){
                        constr = model.sum(constr, model.prod(2,variables[0][N+neighbours[k]-1]));
                    }
                }


                for (int kk = 0; kk < ni; kk++) {
                    int k = neighbours[kk]-1;

                    if(graph.isArc(j+1,k+1)){
                        //ykj - k in neighbourhood i and j
                        constr = model.sum(constr, model.prod(-1,variables[0][N + k*N + j+offset]));
                    }
                }

                constraints[0][count] = model.addGe(constr, 1);
                count++;

            }

        }

        count = 0;
        //constraint 4
        for (int j = 0; j < N; j++) {
            //xj
            IloNumExpr constr = model.prod(1, variables[0][j]);

            if(domType == WEAK_ROMAN_DOMINATION){
                constr = model.sum(constr, variables[0][N+j]);
            }

            int[] neighbours = graph.getArcs()[j];
            for (int ii = 0; ii < graph.getDegrees()[j]; ii++) {
                int i = neighbours[ii]-1;
                //yij - i neighbour j
                constr = model.sum(constr, variables[0][offset+N + i*N + j]);
            }
            constraints[1][count] = model.addEq(constr, 1);
            count++;
        }

    }

    private void setCDConstraints(){


    }

    private void setUDConstraints(){

    }

    private void setUDAConstraints(){

    }


}

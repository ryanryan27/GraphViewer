import ilog.concert.*;
import ilog.cplex.*;

import java.util.Arrays;

public class MILPRunner {


    private final Graph graph;
    private final int domType;
    private final boolean preserveDom;
    private int domcount = 0;

    private String run_state = "Not Started";

    public final static int DOMINATION = 1;
    public final static int TOTAL_DOMINATION = 2;
    public final static int SECURE_DOMINATION = 3;
    public final static int ROMAN_DOMINATION = 4;
    public final static int WEAK_ROMAN_DOMINATION = 5;
    public final static int CONNECTED_DOMINATION = 6;
    public final static int UPPER_DOMINATION = 7;
    public final static int UPPER_DOMINATION_ALT = 8;
    public final static int TWO_DOMINATION = 9;


    private final int[] fixed;

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
        this.fixed = null;

        N= graph.getN();
        M = graph.getEdgeCount();
        
    }

    public MILPRunner(int domType, Graph graph, int[] fixed){
        this.graph = graph;
        this.domType = domType;
        this.fixed = fixed;
        this.preserveDom = false;

        N = graph.getN();
        M = graph.getEdgeCount();
    }


    public double[] run(double timeout) throws IloException {

        buildModel();
        MILPCallback mc = new MILPCallback(timeout);
        model.use(mc);
        model.solve();

        double[] solution = model.getValues(variables[0]);

        /*
         for (int j = 0; j < solution.length; ++j) {
             System.out.pri     ntln("Variable " + j + ": Value = " + solution[j]);
         }
        */
        run_state = model.getStatus().toString();

        model.end();



        return solution;
    }

    public String getRunState(){
        return run_state;
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
        int integer;

        switch (domType) {
            case SECURE_DOMINATION:
                binary = N;
                continuous = N * N;
                integer = 0;
                break;
            case ROMAN_DOMINATION:
            case UPPER_DOMINATION:
                binary = 2 * N;
                continuous = 0;
                integer = 0;
                break;
            case WEAK_ROMAN_DOMINATION:
                binary = 2 * N;
                continuous = N * N;
                integer = 0;
                break;
            case CONNECTED_DOMINATION:
                binary = 2 * M + 3 * N + 1;
                continuous = 0;
                integer = N + 1;
                break;
            case UPPER_DOMINATION_ALT:
                binary = 2 * N + 2 * M;
                continuous = 0;
                integer = 0;
                break;
//DOMINATION, TOTAL_DOMINATION, TWO_DOMINATION
            default:
                binary = N;
                continuous = 0;
                integer = 0;
                break;
        }

        int total = binary + continuous + integer;
        double[] xlb = new double[total];
        double[] xub = new double[total];

        if(domType == CONNECTED_DOMINATION){
            Arrays.fill(xub, 0, binary, 1);
            Arrays.fill(xub, binary, total, N+1);
            Arrays.fill(xlb, binary, total, 1);
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
        else if(fixed != null){
            domcount = 0;

            for (int i = 0; i < N; i++) {
                if(fixed[i] == 1 || fixed[i] == -1) {
                    domcount++;
                }
                if((domType == ROMAN_DOMINATION || domType == WEAK_ROMAN_DOMINATION) && (fixed[i] == 2 || fixed[i] == -1)){
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

                    if(domType == TWO_DOMINATION) {
                        constr = model.sum(constr, model.prod(0.5, variables[0][j]));
                        continue;
                    }

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
                    int vindex = i;

                    if(domType == ROMAN_DOMINATION || domType == WEAK_ROMAN_DOMINATION){
                        vindex = i + N*(domset[i]-1);
                    }
                    IloNumExpr constr = model.prod(1, variables[0][vindex]);
                    int index = constraints[1].length - domcount + count;
                    constraints[1][index] = model.addEq(constr, 1);
                    count++;
                }
            }

        } else if(fixed != null){
            int count = 0;
            for (int i = 0; i < N; i++) {
                if (fixed[i] == 0) {
                    continue;
                }

                if(fixed[i] == 2 && !(domType == ROMAN_DOMINATION || domType == WEAK_ROMAN_DOMINATION)){
                    continue;
                }

                int vindex = i;
                int val = fixed[i];

                if(fixed[i] == 2 && (domType == ROMAN_DOMINATION || domType == WEAK_ROMAN_DOMINATION)){
                    vindex = i + N;
                }

                if(fixed[i] == -1){
                    val = 0;

                }

                IloNumExpr constr = model.prod(1, variables[0][vindex]);
                int index = constraints[1].length - domcount + count;
                constraints[1][index] = model.addEq(constr, val);
                count++;

                if(fixed[i] == -1 && (domType == ROMAN_DOMINATION || domType == WEAK_ROMAN_DOMINATION)){
                    constr = model.prod(1, variables[0][vindex+N]);
                    index = constraints[1].length - domcount + count;
                    constraints[1][index] = model.addEq(constr, 0);
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

    private void setCDConstraints() throws IloException{
        setDominationConstraints();
        
        

        //set up w-lookup

        int[][] wlookup = new int[N+2][N+2];

        int count = N;
        

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if(graph.isArc(i+1, j+1)){
                    wlookup[i][j] = count;
                    count++;
                }
            }
        }

        for (int i = 0; i < N; i++) {
            wlookup[N][i] = count;
            count++;
        }

        wlookup[N][N+1] = count;
        count++;

        for (int i = 0; i < N; i++) {
            wlookup[N+1][i] = count;
            count++;
        }

        int uoffset = count;

        int icount = N;
        int ecount = 0;

        //constraint 2
        IloNumExpr constr = model.constant(0);
        for (int i = 0; i < N; i++) {
            constr = model.sum(constr, variables[0][wlookup[N+1][i]]);
        }
        constraints[1][ecount] = model.addEq(constr, 1);
        ecount++;

        //constraint 3
        for (int j = 0; j < N; j++) {
            constr = model.constant(0);
            for (int i = 0; i < wlookup.length; i++) {
                if(wlookup[i][j] > 0){
                    constr = model.sum(constr, variables[0][wlookup[i][j]]);
                }
            }
            constraints[1][ecount] = model.addEq(constr,1);
            ecount++;
        }


        //constraint 4
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if(graph.isArc(i+1, j+1)){
                    constr = model.prod(1, variables[0][wlookup[N][i]]);
                    constr = model.sum(constr, variables[0][wlookup[i][j]]);
                    constraints[0][icount] = model.addLe(constr, 1);
                    icount++;
                }
            }
        }

        //constraint 5
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if(graph.isArc(i+1, j+1)){
                    constr = model.prod(N+1, variables[0][wlookup[i][j]]);
                    constr = model.sum(constr, model.prod(N-1, variables[0][wlookup[j][i]]));
                    constr = model.sum(constr, variables[0][uoffset+i]);
                    constr = model.sum(constr, model.prod(-1, variables[0][uoffset+j]));
                    constraints[0][icount] = model.addLe(constr, N);
                    icount++;
                }
            }
        }

        //constraint 6
        for (int i = 0; i < wlookup.length; i++) {
            for (int j = 0; j < wlookup.length; j++) {
                if((i >= N || j >= N) && wlookup[i][j] > 0){
                    constr = model.prod(N+1, variables[0][wlookup[i][j]]);

                    if(i < N){
                        constr = model.sum(constr, variables[0][uoffset+i]);
                    } else if (i == N+1){
                        constr = model.sum(constr, variables[0][uoffset + N]);
                    }

                    if(j < N){
                        constr = model.sum(constr, model.prod(-1,variables[0][uoffset+j]));
                    } else if (j == N+1){
                        constr = model.sum(constr, model.prod(-1,variables[0][uoffset + N]));
                    }

                    constraints[0][icount] = model.addLe(constr, N);
                    icount++;
                }
            }
        }

        //constraint 7
        constraints[1][ecount] = model.addEq(1, model.prod(1, variables[0][wlookup[N][N+1]]));
        ecount++;
        

        //constraint 8
        
        for (int i = 0; i < N; i++) {
            constr = model.prod(1, variables[0][i]);
            constr = model.sum(constr, variables[0][wlookup[N][i]]);
            constraints[1][ecount] = model.addEq(1, constr);
            ecount++;
        }

        System.out.println(constraints[0].length + ", " + icount + ": " + constraints[1].length + ", " + ecount + " : " + variables[0].length + ", " + (uoffset+N+1));


    }

    private void setUDConstraints(){

    }

    private void setUDAConstraints(){

    }


}

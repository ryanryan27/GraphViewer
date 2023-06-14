import ilog.concert.IloException;
import ilog.cplex.IloCplex;

public class MILPCallback extends IloCplex.MIPInfoCallback {

    double timeout;

    public MILPCallback(double timeout){
        this.timeout = timeout;
    }

    @Override
    protected void main() throws IloException {
        double time_taken = this.getCplexTime() - this.getStartTime();

        
        //System.out.println("obj: " + this.getIncumbentObjValue() + " in: " + time_taken);

        if(timeout != 0 && time_taken > timeout){
            //System.out.println("times up!");
            this.abort();
        }



    }
}

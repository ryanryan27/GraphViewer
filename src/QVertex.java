import java.util.ArrayList;

public class QVertex {

    int id;

    double x;
    double y;

    boolean selected;

    int guards;

    public QVertex(double x, double y){
        this.x = x;
        this.y = y;

        selected = false;
        guards = 0;
    }


}

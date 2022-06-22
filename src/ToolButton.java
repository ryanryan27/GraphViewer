import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ToolButton extends JLabel implements MouseListener {



    UGVViewer parent;
    String tooltip;
    String icon_prefix;
    int option_number;

    ImageIcon icon_default;
    ImageIcon icon_highlighted;
    ImageIcon icon_clicked;
    ImageIcon icon_selected;


    Runnable click_action;

    boolean highlighted = false;
    boolean started_click = false;
    boolean selected = false;

    ToolButton(UGVViewer parent, String tooltip, String icon_prefix, int option_number, Runnable click_action){
        this.parent = parent;
        this.tooltip = tooltip;
        this.icon_prefix = icon_prefix;
        this.option_number = option_number;
        this.click_action = click_action;

        icon_default = new ImageIcon("pics/" + icon_prefix + ".png");
        icon_highlighted = new ImageIcon("pics/" + icon_prefix + "_highlight.png");
        icon_clicked = new ImageIcon("pics/" + icon_prefix + "_select.png");
        icon_selected = new ImageIcon("pics/" + icon_prefix + "_chosen.png");

        setIcon(icon_default);

        addMouseListener(this);
        setMaximumSize(new Dimension(icon_default.getIconWidth(), icon_default.getIconHeight()));
    }

    /**
     * Deselects this tool.
     */
    public void deselect(){
        setIcon(icon_default);
        selected = false;
        validate();
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        started_click = highlighted;

        if(started_click){
            setIcon(icon_clicked);
            validate();
        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {

        if(highlighted && started_click){
            if(!selected && option_number > -1){
                parent.changeTool(option_number);
            }

            selected = !selected;
            setIcon(icon_selected);

            validate();

            if(click_action != null){
                click_action.run();
            }

            if(option_number < -1){
                deselect();
            }
            if(!selected && option_number > -1){
                deselect();
                parent.changeTool(GraphPane.DEFAULT_OPTION);
            }
        }

        started_click = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        highlighted = true;

        if(!selected){
            setIcon(icon_highlighted);
            validate();
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        highlighted = false;
        started_click = false;

        if(!selected){
            setIcon(icon_default);
            validate();
        }
    }
}

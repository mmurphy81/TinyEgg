import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameDisplay extends JFrame {
    private GameEngine engine;
    private ShotMeter meter;
    private Point dragStart;
    private Point currentMousePos;
    private boolean isDragging;
    private boolean isCharging;
    public static final int WINDOW_WIDTH = 1000;
    public static final int WINDOW_HEIGHT = 1000;

    public GameDisplay(GameEngine engine){
        this.engine = engine;
        this.setTitle("GameDisplay");
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public void paintComponent(Graphics g){
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    private void drawEgg(Graphics g){

    }


    private void drawMap1(Graphics g){
        // Background
        g.setColor(new Color(34, 139, 34));
        g.fillRect(0, 0, getWidth(), getHeight());

        // Drawing the ice
        g.setColor(new Color(0, 191, 255));
        g.fillRect(113, 105, 245, 197);
        g.fillRect(396, 487, 283, 237);

        // Drawing the walls
        g.setColor(new Color(220, 80, 80));
        g.fillRect(519, 79, 38, 316);
        g.fillRect(245, 632, 38, 290);

        // Bird's nest (top right)
        g.setColor(new Color(205, 133, 63));
        g.fillOval(736, 66, 200, 100);
        g.setColor(new Color(122, 75, 29));
        g.fillOval(750, 80, 160, 50);

        // Drawing the grass
        g.setColor(new Color(0, 100, 0));

        // Top-left grass patch
        //Initiates the coordinates for each point of the grass
        int[] x1 = {90, 120, 150, 180, 210, 240, 270};
        int[] y1 = {370, 330, 370, 330, 370, 330, 370};
        g.drawPolyline(x1, y1, x1.length);

        // Top-right grass patch
        int[] x2 = {620, 650, 680, 710, 740, 770, 800};
        int[] y2 = {290, 250, 290, 250, 290, 250, 290};
        g.drawPolyline(x2, y2, x2.length);

        // Bottom-right grass patch
        int[] x3 = {680, 710, 740, 770, 800, 830, 860};
        int[] y3 = {710, 670, 710, 670, 710, 670, 710};
        g.drawPolyline(x3, y3, x3.length);
    }

    private void drawUI(Graphics g){

    }
    private void drawShotPreview(Graphics g){

    }
    public void mousePressed(MouseEvent e){

    }
    public void mouseDragged(MouseEvent e){

    }
    public void mouseReleased(MouseEvent e){

    }
    public void paint(Graphics g){
        this.drawMap1(g);
    }


}

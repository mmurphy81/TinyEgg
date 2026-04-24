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

    private static final Color GRASS = new Color(34, 139, 34);
    private static final Color ICE = new Color(0, 191, 255);
    private static final Color CORAL = new Color(220, 80, 80);
    private static final Color LIGHT_ORANGE = new Color(205, 133, 63);
    private static final Color BROWN = new Color(122, 75, 29);
    private static final Color DARK_GREEN = new Color(0, 100, 0);


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
        g.setColor(GRASS);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Drawing the ice
        g.setColor(ICE);
        g.fillRect(113, 105, 245, 197);
        g.fillRect(396, 487, 283, 237);

        // Drawing the walls
        g.setColor(CORAL);
        g.fillRect(519, 79, 38, 316);
        g.fillRect(245, 632, 38, 290);

        // Bird's nest (top right)
        g.setColor(LIGHT_ORANGE);
        g.fillOval(736, 66, 200, 100);

        g.setColor(BROWN);
        g.fillOval(750, 80, 160, 50);

        // Drawing the grass
        g.setColor(DARK_GREEN);

        // Top-left grass patch
        //Initiates the coordinates for each point of the grass and moves them to the next coordinates
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

    private void drawMap2(Graphics g){
        // Background
        g.setColor(GRASS);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Right side walls
        g.setColor(CORAL);
        g.fillRect(200, 0, 25, 320);
        g.fillRect(250, 500, 25, 300);
        g.fillRect(475, 0, 25, 300);

        //Left side walls
        g.fillRect(700, 0, 25, 400);
        g.fillRect(475, 470, 25, 300);
        g.fillRect(900, 0, 25, 600);

        // Ice
        g.setColor(ICE);
        g.fillRect(0, 200, 200, 120);
        g.fillRect(275, 600, 200, 120);
        g.fillRect(500, 260, 200, 120);

        // Nest
        g.setColor(LIGHT_ORANGE);
        g.fillOval(750, 650, 200, 100);
        g.setColor(BROWN);
        g.fillOval(760, 660, 160, 50);

        // Grass
        g.setColor(DARK_GREEN);
        int[] x1 = {390, 415, 440, 465, 490, 515};
        int[] y1 = {430, 400, 430, 400, 430, 400};
        g.drawPolyline(x1, y1, x1.length);

        int[] x2 = {720, 748, 776, 804, 832, 860, 888};
        int[] y2 = {530, 500, 530, 500, 530, 500, 530};
        g.drawPolyline(x2, y2, x2.length);

        int[] x3 = {560, 588, 616, 644, 672, 700};
        int[] y3 = {720, 690, 720, 690, 720, 690};
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
        this.drawMap2(g);
    }


}

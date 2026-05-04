import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;


public class GameDisplay extends JFrame {
    // Instance variables

    // Allows access to the shotmeter and backend for the frontend
    private GameEngine engine;
    private ShotMeter meter;

    // the Point where the drag started and where the mouse currently is
    private Point dragStart;
    private Point currentMousePos;

    // Checking to see if user is dragging
    private boolean isDragging;

    // Constants
    public static final int WINDOW_WIDTH = 1000;
    public static final int WINDOW_HEIGHT = 1000;

    //Magic numbers for all of our colors so the colors are easier to understand
    public  static  final Color GRASS_2 = new Color(85, 155, 70);
    public static final Color GRASS_3 = new Color(70, 140, 60);
    public static final Color GRASS_H = new Color(60, 120, 55);
    public static final Color TREE_LEAF = new Color(34, 139, 34);
    public static Color TREE_LEAF2 = new Color(30, 120, 30);
    public static final Color GRASS = new Color(34, 139, 34);
    public static final Color DARK_GREEN = new Color(0, 100, 0);
    public static final Color TOP_SKY = new Color(135, 206, 235);
    public static final Color MID_SKY = new Color(176, 226, 255);
    public static final Color HORIZON_SKY = new Color(200, 240, 255);
    public static final Color ICE = new Color(0, 191, 255);
    public static final Color CORAL = new Color(220, 80, 80);
    public static final Color LIGHT_ORANGE = new Color(205, 133, 63);
    public static final Color BROWN = new Color(122, 75, 29);
    public static final Color TREE_BASE = new Color(101, 67, 33);


    public GameDisplay(GameEngine engine, ShotMeter meter){
        // Access to backend
        this.engine = engine;
        this.meter = meter;

        this.setTitle("GameDisplay");
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        createBufferStrategy(2);
    }

    // Bufferstrategy in order for no glitches
    public void paint(Graphics g){
        BufferStrategy bf = getBufferStrategy();
        if (bf == null) {
            createBufferStrategy(2);
            return;
        }
        Graphics g2 = null;
        try {
            g2 = bf.getDrawGraphics();
            myPaint(g2);
        }
        finally {
            g2.dispose();
        }
        bf.show();
        Toolkit.getDefaultToolkit().sync();
    }

    private void myPaint(Graphics g) {
        // If we are in opening stages of the game, draw the begining animation
        if (engine.getGameState() == GameEngine.STATE_OPENING) {
            //drawOpening(g);

        }
        else if(engine.getGameState() == GameEngine.STATE_PLAYING){
            this.drawMap1(g);
            meter.drawMeter(g);
        }

        // If the egg has landed, then we draw the crack on the egg
        if (engine.getEgg().getState() == 5) {
            engine.getEgg().drawCrack(g, (int)engine.getEgg().getX(), (int)engine.getEgg().getY() - 10);
        }
    }

    private void drawEgg(Graphics g){

    }

    // Draw the tree branch
    private void drawBranch(Graphics g) {
        g.setColor(BROWN);
        g.fillRect(540, 400, 200, 20);
    }

    // Draws the begining of the game animatio
    public void drawOpening(Graphics g) {
        drawBackground(g);
        drawTree(g);
        drawBranch(g);
        drawNest(g);

        engine.getEgg().draw(g);
    }


    private void drawBackground(Graphics g){

        // sky gradient effect (fake gradient using bands)
        g.setColor(TOP_SKY); // top sky
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        g.setColor(MID_SKY); // softer mid sky
        g.fillRect(0, 0, WINDOW_WIDTH, 500);

        g.setColor(HORIZON_SKY); // near horizon glow
        g.fillRect(0, 0, WINDOW_WIDTH, 250);

        // ground
        g.setColor(GRASS_2);
        g.fillRect(0, 650, WINDOW_WIDTH, 350);

        // subtle ground variation (patchy grass feel)
        g.setColor(GRASS_3);
        for(int i = 0; i < WINDOW_WIDTH; i += 40){
            g.fillRect(i, 650 + (i % 3) * 3, 20, 100);
        }

        // distant horizon line
        g.setColor(GRASS_H);
        g.drawLine(0, 650, WINDOW_WIDTH, 650);
    }

    private void drawTree(Graphics g){

        // trunk
        g.setColor(TREE_BASE);
        g.fillRect(450, 250, 100, 450);

        // trunk outline (adds depth)
        g.setColor(Color.BLACK);
        g.drawRect(450, 250, 100, 450);

        // foliage base
        g.setColor(TREE_LEAF);
        g.fillOval(350, 80, 300, 250);

        // extra foliage blobs for shape variation
        g.setColor(TREE_LEAF2);
        g.fillOval(300, 150, 200, 180);
        g.fillOval(450, 120, 220, 200);

        // outline for main canopy
        g.setColor(Color.BLACK);
        g.drawOval(350, 80, 300, 250);
    }

    private void drawNest(Graphics g){

        // nest base
        g.setColor(BROWN);
        g.fillOval(560, 350, 120, 50);

        // inner shading
        g.setColor(LIGHT_ORANGE);
        g.fillOval(570, 350, 100, 40);

        // outline
        g.setColor(Color.BLACK);
        g.drawOval(560, 350, 120, 50);
    }

    private void drawMap(Graphics g){}


    private void drawMap1(Graphics g){
        // Background
        g.setColor(GRASS);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Drawing the ice
        g.setColor(ICE);
        g.fillRect(113, 105, 245, 197);
        g.fillRect(396, 487, 283, 237);

        engine.addLevel1Walls();
        for (int i =0; i<engine.getObstacles().size(); i++){
            engine.getObstacles().get(i).draw(g);
        }

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

        //Draws the walls
        engine.addLevel2Walls();
        for (int i =0; i<engine.getObstacles().size(); i++){
            engine.getObstacles().get(i).draw(g);
        }


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



}
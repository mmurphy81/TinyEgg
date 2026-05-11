import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.Graphics;


public class GameDisplay extends JFrame implements MouseListener, MouseMotionListener{
    // Instance variables

    // Allows access to the shotmeter and backend for the frontend
    private GameEngine engine;
    private ShotMeter meter;

    // the Point where the drag started and where the mouse currently is
    private Point dragStart;
    private Point currentMousePos;

    // Checking to see if user is dragging
    private boolean isDragging;

    private Point fakeMouse = new Point(300, 500);

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
    public static final Color LIGHT_PURPLE = new Color(210, 180, 255);

    private double pendingVX = 0;
    private double pendingVY = 0;
    private boolean waitingForMeter = false;


    Rectangle watchButton = new Rectangle(250, 300, 200, 50);
    Rectangle skipButton = new Rectangle(250, 400, 200, 50);

    public GameDisplay(GameEngine engine, ShotMeter meter) {
        // Access to backend
        this.engine = engine;
        this.meter = meter;
        // Create mouse listener so that the computer knows when user is shooting
        addMouseListener(this);
        addMouseMotionListener(this);

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
        if (engine.getGameState() == GameEngine.STATE_OPENING) {
            drawOpening(g);
        }
        else if (engine.getGameState() == GameEngine.STATE_PLAYING) {
            int map = engine.getCurrentMap();
            if      (map == 1) drawMap1(g);
            else if (map == 2) drawMap2(g);
            else if (map == 3) drawMap3(g);
            else               drawMap4(g);
            meter.drawMeter(g);
            engine.getEgg().draw(g);
            drawHUD(g);
        }
        else if (engine.getGameState() == GameEngine.STATE_TUNNEL) {
            drawTunnel(g);
        }

        if (isDragging && !engine.getEgg().isMoving()) {
            drawShotPreview(g);
        }

        if (engine.getEgg().getState() == 5
                && engine.getGameState() != GameEngine.STATE_PLAYING) {
            engine.getEgg().drawCrack(g,
                    (int) engine.getEgg().getX(),
                    (int) engine.getEgg().getY() - 10);
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



        //Drawing all the obstacles
        for (int i =0; i<engine.getObstacles().size(); i++){
            engine.getObstacles().get(i).draw(g);
        }

        // Bird's nest (top right)
        g.setColor(LIGHT_ORANGE);
        g.fillOval(736, 66, 200, 100);

        g.setColor(BROWN);
        g.fillOval(750, 80, 160, 50);



        // Drawing the grass details that the user sees
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

        for (int i =0; i<engine.getObstacles().size(); i++){
            engine.getObstacles().get(i).draw(g);
        }


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

    private static final Color SAND        = new Color(240, 220, 160);
    private static final Color DARK_SAND   = new Color(210, 185, 130);
    private static final Color WATER       = new Color(110, 190, 220);
    private static final Color WATER_DARK  = new Color( 80, 160, 200);
    private static final Color PALM_TRUNK  = new Color(100,  65,  35);
    private static final Color PALM_FRONDS = new Color( 40, 130,  50);
    private static final Color PALM_SHADOW = new Color( 20,  90,  30);
    private static final Color SUN_COLOR   = new Color(255, 230, 100);

    private void drawTropicalBackground(Graphics g) {
        g.setColor(SAND);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(WATER);
        g.fillRect(0, 0, getWidth(), 60);
        g.setColor(WATER_DARK);
        for (int x = 0; x < getWidth(); x += 40) g.drawArc(x, 40, 40, 20, 0, 180);
        g.setColor(DARK_SAND);
        int[][] patches = {
                {120, 200, 80, 40}, {820, 350, 70, 35}, {400, 740, 90, 45},
                {660, 180, 60, 30}, {300, 880, 100, 35}, {850, 720, 65, 30}
        };
        for (int[] p : patches) g.fillOval(p[0], p[1], p[2], p[3]);
        drawSun(g, 920, 130);
    }

    private void drawSun(Graphics g, int cx, int cy) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(SUN_COLOR);
        g2.fillOval(cx - 35, cy - 35, 70, 70);
        g2.setColor(new Color(255, 200, 90));
        g2.drawOval(cx - 35, cy - 35, 70, 70);
        g2.setStroke(new BasicStroke(4));
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4;
            int x1 = (int)(cx + Math.cos(a) * 42);
            int y1 = (int)(cy + Math.sin(a) * 42);
            int x2 = (int)(cx + Math.cos(a) * 62);
            int y2 = (int)(cy + Math.sin(a) * 62);
            g2.drawLine(x1, y1, x2, y2);
        }
        g2.setStroke(new BasicStroke(1));
    }

    private void drawPalmTree(Graphics g, int baseX, int baseY) {
        g.setColor(PALM_TRUNK);
        g.fillRect(baseX - 8, baseY - 90, 16, 90);
        g.setColor(BROWN);
        g.drawRect(baseX - 8, baseY - 90, 16, 90);
        int topX = baseX, topY = baseY - 90;
        g.setColor(PALM_FRONDS);
        g.fillOval(topX - 55, topY - 25, 110, 40);
        g.fillOval(topX - 35, topY - 50,  70, 60);
        g.fillOval(topX - 65, topY -  5,  55, 30);
        g.fillOval(topX + 10, topY -  5,  55, 30);
        g.setColor(PALM_SHADOW);
        g.drawOval(topX - 55, topY - 25, 110, 40);
        g.drawOval(topX - 35, topY - 50,  70, 60);
    }

    private void drawBarrier(Graphics g) {
        int by = (int) engine.getBarrierY();
        g.setColor(CORAL);
        g.fillRect(GameEngine.BARRIER_X, by, GameEngine.BARRIER_W, GameEngine.BARRIER_H);
        g.setColor(new Color(150, 50, 50));
        g.drawRect(GameEngine.BARRIER_X, by, GameEngine.BARRIER_W, GameEngine.BARRIER_H);
        g.setColor(Color.WHITE);
        int mid = GameEngine.BARRIER_X + GameEngine.BARRIER_W / 2;
        g.drawLine(mid - 5, by + 5, mid, by - 3);
        g.drawLine(mid, by - 3, mid + 5, by + 5);
        g.drawLine(mid - 5, by + GameEngine.BARRIER_H - 5, mid, by + GameEngine.BARRIER_H + 3);
        g.drawLine(mid, by + GameEngine.BARRIER_H + 3, mid + 5, by + GameEngine.BARRIER_H - 5);
    }

    private void drawMap3(Graphics g) {
        drawTropicalBackground(g);
        drawPalmTree(g,  90, 950);
        drawPalmTree(g,  90, 240);
        drawPalmTree(g, 600, 980);
        for (int i = 0; i < engine.getObstacles().size(); i++) {
            engine.getObstacles().get(i).draw(g);
        }
        g.setColor(new Color(20, 20, 30));
        g.fillRect(GameEngine.GATE_X, GameEngine.GATE_Y_TOP,
                getWidth() - GameEngine.GATE_X,
                GameEngine.GATE_Y_BOT - GameEngine.GATE_Y_TOP);
        drawBarrier(g);
        g.setColor(DARK_GREEN);
        int[] gx1 = {775, 800, 825, 850};
        int[] gy1 = {450, 435, 450, 435};
        g.drawPolyline(gx1, gy1, gx1.length);
        int[] gx2 = {775, 800, 825, 850};
        int[] gy2 = {570, 555, 570, 555};
        g.drawPolyline(gx2, gy2, gx2.length);
    }

    private void drawMap4(Graphics g) {
        drawTropicalBackground(g);
        drawPalmTree(g,  90, 970);
        drawPalmTree(g, 950, 970);
        drawPalmTree(g,  90, 280);
        for (int i = 0; i < engine.getObstacles().size(); i++) {
            engine.getObstacles().get(i).draw(g);
        }
        g.setColor(LIGHT_ORANGE);
        g.fillOval(820, 100, 160, 80);
        g.setColor(BROWN);
        g.fillOval(835, 110, 130, 60);
        g.setColor(DARK_GREEN);
        int[] gx = {705, 735, 765, 795};
        int[] gy = {230, 215, 230, 215};
        g.drawPolyline(gx, gy, gx.length);
    }

    private void drawTunnel(Graphics g) {
        int totalFrames = 150;
        int t = engine.getTunnelTimer();
        g.setColor(new Color(20, 20, 30));
        g.fillRect(0, 0, getWidth(), getHeight());
        int[] xs = {  50, 250, 400, 600, 750, 900 };
        int[] ys = { 150, 350, 200, 600, 400, 800 };
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(70, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(80, 80, 100));
        for (int i = 0; i < xs.length - 1; i++) g2.drawLine(xs[i], ys[i], xs[i+1], ys[i+1]);
        g2.setStroke(new BasicStroke(50, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(40, 40, 60));
        for (int i = 0; i < xs.length - 1; i++) g2.drawLine(xs[i], ys[i], xs[i+1], ys[i+1]);
        double progress = (double) t / totalFrames;
        if (progress > 1) progress = 1;
        int segments = xs.length - 1;
        double segProgress = progress * segments;
        int segIdx = (int) segProgress;
        if (segIdx >= segments) segIdx = segments - 1;
        double segFrac = segProgress - segIdx;
        int ballX = (int)(xs[segIdx] + (xs[segIdx+1] - xs[segIdx]) * segFrac);
        int ballY = (int)(ys[segIdx] + (ys[segIdx+1] - ys[segIdx]) * segFrac);
        g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.WHITE);
        g2.fillOval(ballX - 14, ballY - 14, 28, 28);
        g2.setColor(Color.BLACK);
        g2.drawOval(ballX - 14, ballY - 14, 28, 28);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.drawString("Through the tunnel...", 380, 950);
    }

    private void drawHUD(Graphics g) {
        drawPlayHealthBar(g);
        drawStrokeCount(g);
        drawLevelBadge(g);
    }

    private void drawPlayHealthBar(Graphics g) {
        int h = engine.getEgg().getHealth();
        if (h < 0) h = 0;
        if (h > Egg.MAX_HEALTH) h = Egg.MAX_HEALTH;
        g.setColor(new Color(40, 40, 40));
        g.fillRoundRect(20, 20, 240, 28, 8, 8);
        Color barColor;
        if (h > 60)      barColor = new Color(60, 180, 60);
        else if (h > 30) barColor = new Color(220, 180, 0);
        else             barColor = new Color(200, 40, 40);
        g.setColor(barColor);
        g.fillRoundRect(24, 24, (int)((h / (double) Egg.MAX_HEALTH) * 232), 20, 6, 6);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("HP " + h + " / " + Egg.MAX_HEALTH, 32, 39);
    }

    private void drawStrokeCount(Graphics g) {
        int map = engine.getCurrentMap();
        int strokes = engine.getStrokesForMap(map);
        int par = engine.getParForMap(map);
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(20, 58, 240, 32, 10, 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Strokes: " + strokes + "    Par: " + par, 32, 80);
    }

    private void drawLevelBadge(Graphics g) {
        String text = "Level " + engine.getLevelNumber() + " — " + engine.getDifficulty();
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(getWidth() - 250, 20, 230, 38, 12, 12);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString(text, getWidth() - 232, 46);
    }

    private void drawUI(Graphics g){

    }

    // Draws the red arrow that shows the shot preview
    private void drawShotPreview(Graphics g){
        // If we don't konw where the mouse position is or if they haven't started dragging
        // Do not do anything
            if (dragStart == null || currentMousePos == null) return;

            // Otherwise set color to red
            g.setColor(Color.RED);

            // These are the coordinates for beginning of mouse beign dragged and end
            int x1 = dragStart.x;
            int y1 = dragStart.y;
            int x2 = currentMousePos.x;
            int y2 = currentMousePos.y;

            // main line
            g.drawLine(x1, y1, x2, y2);

            // arrow head
            int dx = x2 - x1;
            int dy = y2 - y1;

            // This is the angle which we are shooting the egg
            double angle = Math.atan2(dy, dx);
            int size = 10;

            // Creates the v shape at the tiip of the line to make it an arrow
            int xA = (int)(x2 - size * Math.cos(angle - Math.PI / 6));
            int yA = (int)(y2 - size * Math.sin(angle - Math.PI / 6));

            int xB = (int)(x2 - size * Math.cos(angle + Math.PI / 6));
            int yB = (int)(y2 - size * Math.sin(angle + Math.PI / 6));

            g.drawLine(x2, y2, xA, yA);
            g.drawLine(x2, y2, xB, yB);


    }
    @Override
    public void mousePressed(MouseEvent e) {
        // Check which game state we're in to handle mouse clicks appropriately
        if (engine.getGameState() == GameEngine.STATE_MENU) {

            // If the player clicked the "Watch Tutorial" button, start the tutorial
            if (watchButton.contains(e.getPoint())) {
                engine.startTutorial();
            }

            // If the player clicked the "Skip" button, jump straight to the opening animation
            if (skipButton.contains(e.getPoint())) {
                engine.skipToOpening();
            }
        }

        // If the player clicks anywhere during the tutorial, skip it and go to the opening
        else if (engine.getGameState() == GameEngine.STATE_TUTORIAL) {
            engine.skipToOpening(); // allow skipping mid-animation
        }

        else if (engine.getGameState() == GameEngine.STATE_ENDING) {
            if (playAgainButton.contains(e.getPoint())) {
                engine.resetGame();
            }
        }

        else if (engine.getGameState() == GameEngine.STATE_PLAYING) {
            // If the shot direction has already been set and we're waiting for the player
            // to click to lock the shot meter, handle the meter result
            if (waitingForMeter) {
                // Lock the meter and get which zone the bar stopped in (green//yellow/red)
                String zone = meter.lockAndGetZone();
                double vx = pendingVX;
                double vy = pendingVY;

                // If the user clicks onto the yellow
                // Slightly reduce speed and add a small random angle deviation
                if (zone.equals("yellow")) {
                    double angle = (Math.random() - 0.5) * 0.4;
                    double speed = Math.sqrt(vx * vx + vy * vy) * (0.7 + Math.random() * 0.4);
                    double baseAngle = Math.atan2(vy, vx) + angle;
                    vx = Math.cos(baseAngle) * speed;
                    vy = Math.sin(baseAngle) * speed;
                }
                // Red zone: heavily reduce speed and apply a large random angle deviation
                else if (zone.equals("red")) {
                    double angle = (Math.random() - 0.5) * 1.2;
                    double speed = Math.sqrt(vx * vx + vy * vy) * (0.3 + Math.random() * 0.5);
                    double baseAngle = Math.atan2(vy, vx) + angle;
                    vx = Math.cos(baseAngle) * speed;
                    vy = Math.sin(baseAngle) * speed;
                }
                // Fire the egg with the final velocity values
                engine.processShotDirect(vx, vy);
                // Reset the meter and mark that we're no longer waiting for a meter click
                meter.reset();
                waitingForMeter = false;
            } else {
                // Only allow dragging if the egg has already stopped moving
                if (!engine.getEgg().isMoving()) {
                    dragStart = e.getPoint();
                    currentMousePos = e.getPoint();
                    isDragging = true;
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e){
        currentMousePos = e.getPoint();
    }

    private void drawFakeMouse(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillOval(fakeMouse.x - 5, fakeMouse.y - 5, 10, 10);
    }

    public void render() {
        BufferStrategy bf = getBufferStrategy();
        if (bf == null) {
            createBufferStrategy(2);
            return;
        }

        Graphics g = bf.getDrawGraphics();
        try {
            // Handle ALL drawing here
            if (engine.getGameState() == GameEngine.STATE_MENU) {
                drawMenu(g);
            }
            else if (engine.getGameState() == GameEngine.STATE_TUTORIAL) {
                drawTutorial(g);
            }
            else if (engine.getGameState() == GameEngine.STATE_ENDING) {
                drawEndScreen(g);
            }
            else {
                myPaint(g);
            }

        } finally {
            g.dispose();
        }

        bf.show();
        Toolkit.getDefaultToolkit().sync();
    }

    private void drawMenu(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("Egg Shot!", 260, 200);

        g.setColor(Color.GRAY);
        g.fillRect(250, 300, 200, 50);
        g.fillRect(250, 400, 200, 50);

        g.setColor(Color.WHITE);
        g.drawString("Watch Tutorial", 255, 335);
        g.drawString("Skip", 320, 435);
    }

    private void drawTutorial(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;

        // background
        g.setColor(new Color(120, 200, 120));
        g.fillRect(0, 0, getWidth(), getHeight());

        // obstacle block
        g.setColor(Color.DARK_GRAY);
        g.fillRect(450, 450, 50, 50);

        // draw nest
        drawNest(g);

        drawFakeMouse(g);

        drawTutorialHealthBar(g);

        // draw egg
        engine.getEgg().draw(g);

        int t = engine.getTutorialTimer();

// START ON EGG
        if (t < 60) {
            fakeMouse.x = 300;
            fakeMouse.y = 500;
        }

// WAIT (no movement)
        else if (t < 120) {
            fakeMouse.x = 300;
            fakeMouse.y = 500;
        }

// DRAG BACK (smooth pull)
        // FIRST DRAG
        else if (t < 240) {
            double progress = (t - 120) / 120.0;

            fakeMouse.x = (int)(300 - 120 * progress);
            fakeMouse.y = (int)(500 - 120 * progress);
        }

// HOLD AFTER FIRST SHOT
        else if (t < 360) {
            fakeMouse.x = 180;
            fakeMouse.y = 380;
        }

// RESET FOR SECOND SHOT
        else if (t < 420) {
            fakeMouse.x = 300;
            fakeMouse.y = 500;
        }

// SECOND DRAG (BAD AIM)
        else if (t < 480) {
            double progress = (t - 420) / 60.0;

            fakeMouse.x = (int)(300 - 100 * progress);
            fakeMouse.y = (int)(500 + 140 * progress); // slight angle difference
        }

// HOLD SECOND RELEASE
        else {
            fakeMouse.x = 200;
            fakeMouse.y = 450;
        }
        // text instructions
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 18));

        if (t < 120) {
            g.drawString("Goal: Get the egg into the nest!", 200, 100);
        }
        else if (t < 240 || (t >= 420 && t < 480)) {
            g.drawString("Click and drag backwards to aim", 180, 100);
            drawArrow(g2);
        }
        else if (t < 300) {
            g.drawString("Release to shoot!", 260, 100);
        }
        else if (t < 360) {
            g.drawString("Perfect shot!", 280, 100);
        }
        else if (t < 420) {
            g.drawString("But watch out for obstacles...", 200, 100);
        }
        else if (t < 480) {
            g.drawString("Bad aim can cause collisions!", 180, 100);
        }
        else {
            g.drawString("Avoid obstacles to protect your egg!", 150, 100);

            // impact feedback
            if (t > 500 && t < 540) {
                g.setColor(new Color(255, 0, 0, 120));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    private void drawArrow(Graphics2D g2) {

        int startX = 300;
        int startY = 500;

        int endX = fakeMouse.x;
        int endY = fakeMouse.y;

        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(3));
        g2.drawLine(startX, startY, endX, endY);
    }

    private void drawTutorialHealthBar(Graphics g) {
        g.setColor(Color.GRAY);
        g.fillRect(20, 20, 200, 20);
        int t = engine.getTutorialTimer();
        int health = 100;
        if (t > 360) health = 60;
        g.setColor(Color.RED);
        g.fillRect(20, 20, health * 2, 20);
    }


    @Override
    public void mouseReleased(MouseEvent e){
        if (isDragging) {
            Point release = e.getPoint();

            double dx = release.x - dragStart.x;
            double dy = release.y - dragStart.y;

            // Store shot but don't fire yet
            pendingVX = -dx * 0.1;
            pendingVY = -dy * 0.1;
            isDragging = false;
            dragStart = null;
            currentMousePos = null;

            // Activate the meter — egg fires only after user clicks to lock it
            meter.activate();
            waitingForMeter = true;
        }
    }

    private Rectangle playAgainButton = new Rectangle(400, 880, 200, 60);

    private void drawEndScreen(Graphics g){
        int time = engine.getEndingTimer();
        int strokes = engine.getStrokeCount();
        int totalPar = engine.getTotalPar();
        boolean lost = engine.didPlayerLose();

        g.setColor(lost ? new Color(255, 200, 200) : LIGHT_PURPLE);
        g.fillRect(0, 0, getWidth(), getHeight());

        int midX = getWidth()/2;
        int midY = getHeight()/2;

        // For the first frames, the egg is intact
        if (time < 60) {
            drawEndEgg(g, midX, midY, false, false);
        }

        // Cracks start to show up in this frame
        else if (time < 120) {
            drawEndEgg(g, midX, midY, true, false);
        }

        //Egg splits in this phase
        else {
            drawEndEgg(g, midX, midY, true, true);

            // "You Win!" text fades in
            g.setColor(DARK_GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 64));
            g.drawString("You Win!", midX - 150, midY - 120);
        }
        // Rating
        // This is going to check the amount of strokes the user took
        // Depending on if they had less than 4 strokes
        // Or less than 8 strokesw
        // or more, they get a different mesage at the end
        String rating;
        Color ratingColor;
        if (strokes <= 4) {
            rating = "★ Magical! ★";
            ratingColor = new Color(180, 0, 200);
        } else if (strokes <= 8) {
            rating = "Exceptional Effort!";
            ratingColor = new Color(0, 100, 180);
        } else {
            rating = "Average";
            ratingColor = new Color(180, 100, 0);
        }
        g.setColor(ratingColor);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        int ratingW = g.getFontMetrics().stringWidth(rating);
        g.drawString(rating, midX - ratingW / 2, midY+130);

        g.setColor(new Color(60, 130, 200));
        g.fillRoundRect(playAgainButton.x, playAgainButton.y,
                playAgainButton.width, playAgainButton.height, 16, 16);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        String play = "Play Again";
        int playW = g.getFontMetrics().stringWidth(play);
        g.drawString(play,
                playAgainButton.x + (playAgainButton.width - playW) / 2,
                playAgainButton.y + 38);
    }

    private void drawEndStats(Graphics g, int midX, int midY, int totalStrokes, int totalPar) {
        int x = midX - 200;
        int y = midY + 170;
        g.setColor(new Color(255, 255, 255, 200));
        g.fillRoundRect(x, y, 400, 180, 14, 14);
        g.setColor(Color.BLACK);
        g.drawRoundRect(x, y, 400, 180, 14, 14);

        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Level 1 (Easy):   " + engine.getStrokesForMap(1)
                + " / par " + engine.getParForMap(1), x + 20, y + 30);
        g.drawString("Level 2 (Medium): " + engine.getStrokesForMap(2)
                + " / par " + engine.getParForMap(2), x + 20, y + 60);
        int lvl3Strokes = engine.getStrokesForMap(3) + engine.getStrokesForMap(4);
        int lvl3Par     = engine.getParForMap(3)     + engine.getParForMap(4);
        g.drawString("Level 3 (Hard):   " + lvl3Strokes
                + " / par " + lvl3Par, x + 20, y + 90);
        g.setColor(new Color(80, 0, 0));
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("Total:  " + totalStrokes + " strokes  /  par " + totalPar,
                x + 20, y + 140);
    }

    private void drawEndEgg(Graphics g, int midX, int midY, boolean cracked, boolean open){
        if (!open) {
            // Draws the whole intact egg at the start
            g.setColor(Color.WHITE);
            g.fillOval(midX - 20, midY - 28, 40, 55);
            //Draws the outline of the egg
            g.setColor(Color.BLACK);
            g.drawOval(midX - 20, midY - 28, 40, 55);

            if (cracked) {
                // Draw cracks spreading across egg
                g.setColor(Color.BLACK);
                g.drawLine(midX - 2, midY - 28, midX - 8, midY);
                g.drawLine(midX + 2, midY - 24, midX + 8, midY + 5);
                g.drawLine(midX - 5, midY - 10, midX + 5, midY);
            }
        } else {
            // Left half of egg splits away from the rest of the egg
            g.setColor(Color.WHITE);
            g.fillArc(midX - 30, midY - 28, 40, 55, 90, 180);
            g.setColor(Color.BLACK);
            g.drawArc(midX - 30, midY - 28, 40, 55, 90, 180);

            // Right half splits away
            g.setColor(Color.WHITE);
            g.fillArc(midX + 10, midY - 28, 40, 55, 270, 180);
            g.setColor(Color.BLACK);
            g.drawArc(midX + 10, midY - 28, 40, 55, 270, 180);

            // Little chick peek in the middle of the egg
            g.setColor(new Color(255, 220, 50)); // yellow
            g.fillOval(midX - 12, midY - 15, 24, 24); // chick head
            g.setColor(Color.BLACK);
            g.drawOval(midX - 12, midY - 15, 24, 24);

            // Eyes for chick
            g.fillOval(midX - 6, midY - 10, 4, 4);
            g.fillOval(midX + 2, midY - 10, 4, 4);

            // Beak for chick
            g.setColor(new Color(255, 160, 0));
            int[] bx = {midX - 2, midX + 2, midX};
            int[] by = {midY - 5, midY - 5, midY - 1};
            //Creates polygon for the beak
            g.fillPolygon(bx, by, 3);
        }
    }



    @Override
    public void mouseClicked(MouseEvent e){}

    @Override
    public void mouseEntered(MouseEvent e){}

    @Override
    public void mouseExited(MouseEvent e){}

    @Override
    public void mouseMoved(MouseEvent e){}



}
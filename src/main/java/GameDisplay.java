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
        // Draw white screen each time so that old arrows are not remaining
        // If we are in opening stages of the game, draw the begining animation
        if (engine.getGameState() == GameEngine.STATE_OPENING) {
            drawOpening(g);

        }
        else if(engine.getGameState() == GameEngine.STATE_PLAYING){
            if (engine.getCurrentMap() == 1) {
                drawMap1(g);
            } else {
                drawMap2(g);
            }
            meter.drawMeter(g);
            engine.getEgg().draw(g);
        }

        // Only draw the egg when it is not moving and if the user is dragging
        if (isDragging && !engine.getEgg().isMoving()) {
            drawShotPreview(g);
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

        // TODO MM move to backend after Ryans code
        engine.addLevel1Obstacles();

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
        engine.addLevel2Obstacles();
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

        // In mousePressed(MouseEvent e):
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
                    double speed = Math.sqrt(vx*vx + vy*vy) * (0.7 + Math.random() * 0.4);
                    double baseAngle = Math.atan2(vy, vx) + angle;
                    vx = Math.cos(baseAngle) * speed;
                    vy = Math.sin(baseAngle) * speed;
                }
                // Red zone: heavily reduce speed and apply a large random angle deviation
                else if (zone.equals("red")) {
                    double angle = (Math.random() - 0.5) * 1.2;
                    double speed = Math.sqrt(vx*vx + vy*vy) * (0.3 + Math.random() * 0.5);
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
                // No shot is pending yet — the player is starting a new drag to aim
                dragStart = e.getPoint();
                currentMousePos = e.getPoint();
                isDragging = true;
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

        drawHealthBar(g);

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

    private void drawHealthBar(Graphics g) {
        g.setColor(Color.GRAY);
        g.fillRect(20, 20, 200, 20);

        int t = engine.getTutorialTimer();

        int health = 100;

        if (t > 360) {
            health = 60; // show damage
        }

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

    private void drawEndScreen(Graphics g){
        int time = engine.getEndingTimer();

        //Sets background to light purple
        g.setColor(LIGHT_PURPLE);
        g.fillRect(0,0, getWidth(), getHeight());

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
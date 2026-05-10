import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;

public class GameDisplay extends JFrame implements MouseListener, MouseMotionListener {
    private GameEngine engine;
    private ShotMeter meter;

    private Point dragStart;
    private Point currentMousePos;
    private boolean isDragging;

    private Point fakeMouse = new Point(300, 500);

    public static final int WINDOW_WIDTH = 1000;
    public static final int WINDOW_HEIGHT = 1000;

    public static final Color GRASS_2 = new Color(85, 155, 70);
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

    // Tropical theme colors for Level 3 maps.
    private static final Color SAND = new Color(240, 220, 160);
    private static final Color DARK_SAND = new Color(210, 185, 130);
    private static final Color WATER = new Color(110, 190, 220);
    private static final Color WATER_DARK = new Color(80, 160, 200);
    private static final Color PALM_TRUNK = new Color(100, 65, 35);
    private static final Color PALM_FRONDS = new Color(40, 130, 50);
    private static final Color PALM_SHADOW = new Color(20, 90, 30);
    private static final Color SUN_COLOR = new Color(255, 230, 100);

    private double pendingVX = 0;
    private double pendingVY = 0;
    private boolean waitingForMeter = false;

    Rectangle watchButton = new Rectangle(250, 300, 200, 50);
    Rectangle skipButton = new Rectangle(250, 400, 200, 50);

    public GameDisplay(GameEngine engine, ShotMeter meter) {
        this.engine = engine;
        this.meter = meter;
        addMouseListener(this);
        addMouseMotionListener(this);

        this.setTitle("GameDisplay");
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int side = Math.min(Math.min(screen.width, screen.height) - 120, WINDOW_HEIGHT);
        this.setSize(side, side);
        this.setResizable(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        createBufferStrategy(2);
    }

    private double scaleX() {
        return (double) getWidth() / WINDOW_WIDTH;
    }

    private double scaleY() {
        return (double) getHeight() / WINDOW_HEIGHT;
    }

    private Point worldPoint(MouseEvent e) {
        double sx = scaleX();
        double sy = scaleY();
        if (sx <= 0) sx = 1;
        if (sy <= 0) sy = 1;
        return new Point((int)(e.getX() / sx), (int)(e.getY() / sy));
    }

    public void paint(Graphics g){
        // No-op. Rendering is driven by GameEngine.run() calling render()
        // on the main thread. If the EDT also tried to render here, both
        // threads would touch the obstacles list and race.
    }

    private void myPaint(Graphics g) {
        if (engine.getGameState() == GameEngine.STATE_OPENING) {
            drawOpening(g);
        }
        else if (engine.getGameState() == GameEngine.STATE_PLAYING) {
            // Maps 1/2 are the team's existing levels; 3 and 4 are both Level 3.
            if (engine.getCurrentMap() == 1) {
                drawMap1(g);
            } else if (engine.getCurrentMap() == 2) {
                drawMap2(g);
            } else if (engine.getCurrentMap() == 3) {
                drawMap3(g);
            } else {
                drawMap4(g);
            }
            meter.drawMeter(g);
            engine.getEgg().draw(g);
            drawLevelInfo(g);
        }

        if (isDragging && !engine.getEgg().isMoving()) {
            drawShotPreview(g);
        }

        if (engine.getEgg().getState() == 5) {
            engine.getEgg().drawCrack(g, (int)engine.getEgg().getX(), (int)engine.getEgg().getY() - 10);
        }
    }

    private void drawEgg(Graphics g) {}

    private void drawBranch(Graphics g) {
        g.setColor(BROWN);
        g.fillRect(540, 400, 200, 20);
    }

    public void drawOpening(Graphics g) {
        drawBackground(g);
        drawTree(g);
        drawBranch(g);
        drawNest(g);
        engine.getEgg().draw(g);
    }

    private void drawBackground(Graphics g) {
        g.setColor(TOP_SKY);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        g.setColor(MID_SKY);
        g.fillRect(0, 0, WINDOW_WIDTH, 500);
        g.setColor(HORIZON_SKY);
        g.fillRect(0, 0, WINDOW_WIDTH, 250);
        g.setColor(GRASS_2);
        g.fillRect(0, 650, WINDOW_WIDTH, 350);
        g.setColor(GRASS_3);
        for (int i = 0; i < WINDOW_WIDTH; i += 40) {
            g.fillRect(i, 650 + (i % 3) * 3, 20, 100);
        }
        g.setColor(GRASS_H);
        g.drawLine(0, 650, WINDOW_WIDTH, 650);
    }

    private void drawTree(Graphics g) {
        g.setColor(TREE_BASE);
        g.fillRect(450, 250, 100, 450);
        g.setColor(Color.BLACK);
        g.drawRect(450, 250, 100, 450);
        g.setColor(TREE_LEAF);
        g.fillOval(350, 80, 300, 250);
        g.setColor(TREE_LEAF2);
        g.fillOval(300, 150, 200, 180);
        g.fillOval(450, 120, 220, 200);
        g.setColor(Color.BLACK);
        g.drawOval(350, 80, 300, 250);
    }

    private void drawNest(Graphics g) {
        g.setColor(BROWN);
        g.fillOval(560, 350, 120, 50);
        g.setColor(LIGHT_ORANGE);
        g.fillOval(570, 350, 100, 40);
        g.setColor(Color.BLACK);
        g.drawOval(560, 350, 120, 50);
    }

    private void drawMap(Graphics g) {}

    private void drawMap1(Graphics g) {
        g.setColor(GRASS);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        engine.addLevel1Obstacles();
        for (int i = 0; i < engine.getObstacles().size(); i++) {
            engine.getObstacles().get(i).draw(g);
        }

        g.setColor(LIGHT_ORANGE);
        g.fillOval(736, 66, 200, 100);
        g.setColor(BROWN);
        g.fillOval(750, 80, 160, 50);

        g.setColor(DARK_GREEN);
        int[] x1 = {90, 120, 150, 180, 210, 240, 270};
        int[] y1 = {370, 330, 370, 330, 370, 330, 370};
        g.drawPolyline(x1, y1, x1.length);

        int[] x2 = {620, 650, 680, 710, 740, 770, 800};
        int[] y2 = {290, 250, 290, 250, 290, 250, 290};
        g.drawPolyline(x2, y2, x2.length);

        int[] x3 = {680, 710, 740, 770, 800, 830, 860};
        int[] y3 = {710, 670, 710, 670, 710, 670, 710};
        g.drawPolyline(x3, y3, x3.length);
    }

    private void drawMap2(Graphics g) {
        g.setColor(GRASS);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        engine.addLevel2Obstacles();
        for (int i = 0; i < engine.getObstacles().size(); i++) {
            engine.getObstacles().get(i).draw(g);
        }

        g.setColor(LIGHT_ORANGE);
        g.fillOval(750, 650, 200, 100);
        g.setColor(BROWN);
        g.fillOval(760, 660, 160, 50);

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

    // Draws the sandy tropical background shared by Level 3 maps.
    private void drawTropicalBackground(Graphics g) {
        g.setColor(SAND);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        g.setColor(WATER);
        g.fillRect(0, 0, WINDOW_WIDTH, 60);
        g.setColor(WATER_DARK);
        for (int x = 0; x < WINDOW_WIDTH; x += 40) {
            g.drawArc(x, 40, 40, 20, 0, 180);
        }
        g.setColor(DARK_SAND);
        int[][] patches = {
                {120, 200, 80, 40}, {820, 350, 70, 35}, {400, 740, 90, 45},
                {660, 180, 60, 30}, {300, 880, 100, 35}, {850, 720, 65, 30}
        };
        for (int[] p : patches) {
            g.fillOval(p[0], p[1], p[2], p[3]);
        }
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
            double angle = i * Math.PI / 4;
            int x1 = (int)(cx + Math.cos(angle) * 42);
            int y1 = (int)(cy + Math.sin(angle) * 42);
            int x2 = (int)(cx + Math.cos(angle) * 62);
            int y2 = (int)(cy + Math.sin(angle) * 62);
            g2.drawLine(x1, y1, x2, y2);
        }
        g2.setStroke(new BasicStroke(1));
    }

    private void drawPalmTree(Graphics g, int baseX, int baseY) {
        g.setColor(PALM_TRUNK);
        g.fillRect(baseX - 8, baseY - 90, 16, 90);
        g.setColor(BROWN);
        g.drawRect(baseX - 8, baseY - 90, 16, 90);
        int topX = baseX;
        int topY = baseY - 90;
        g.setColor(PALM_FRONDS);
        g.fillOval(topX - 55, topY - 25, 110, 40);
        g.fillOval(topX - 35, topY - 50, 70, 60);
        g.fillOval(topX - 65, topY - 5, 55, 30);
        g.fillOval(topX + 10, topY - 5, 55, 30);
        g.setColor(PALM_SHADOW);
        g.drawOval(topX - 55, topY - 25, 110, 40);
        g.drawOval(topX - 35, topY - 50, 70, 60);
    }

    // Draws the moving barrier in front of Map 3's gate.
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

        drawPalmTree(g, 90, 950);
        drawPalmTree(g, 90, 240);
        drawPalmTree(g, 600, 980);

        engine.addLevel3aObstacles();
        for (int i = 0; i < engine.getObstacles().size(); i++) {
            engine.getObstacles().get(i).draw(g);
        }

        // Dark "hole" behind the gate, hinting at the tunnel beyond.
        g.setColor(new Color(20, 20, 30));
        g.fillRect(GameEngine.GATE_X, GameEngine.GATE_Y_TOP,
                WINDOW_WIDTH - GameEngine.GATE_X, GameEngine.GATE_Y_BOT - GameEngine.GATE_Y_TOP);

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

        drawPalmTree(g, 90, 970);
        drawPalmTree(g, 950, 970);
        drawPalmTree(g, 90, 280);

        engine.addLevel3bObstacles();
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

    private void drawLevelInfo(Graphics g) {
        int level = engine.getLevelNumber();
        String difficulty = engine.getDifficulty();
        String text = "Level " + level + " — " + difficulty;

        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(750, 18, 230, 38, 12, 12);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString(text, 768, 44);
    }

    private void drawTunnel(Graphics g) {
        int totalFrames = 150;
        int t = engine.getTunnelTimer();

        g.setColor(new Color(20, 20, 30));
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        int[] xs = {  50, 250, 400, 600, 750, 900 };
        int[] ys = { 150, 350, 200, 600, 400, 800 };

        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(new BasicStroke(70, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(80, 80, 100));
        for (int i = 0; i < xs.length - 1; i++) {
            g2.drawLine(xs[i], ys[i], xs[i+1], ys[i+1]);
        }

        g2.setStroke(new BasicStroke(50, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(40, 40, 60));
        for (int i = 0; i < xs.length - 1; i++) {
            g2.drawLine(xs[i], ys[i], xs[i+1], ys[i+1]);
        }

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

    private void drawUI(Graphics g) {}

    private void drawShotPreview(Graphics g) {
        if (dragStart == null || currentMousePos == null) return;
        g.setColor(Color.RED);
        int x1 = dragStart.x;
        int y1 = dragStart.y;
        int x2 = currentMousePos.x;
        int y2 = currentMousePos.y;
        g.drawLine(x1, y1, x2, y2);
        int dx = x2 - x1;
        int dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int size = 10;
        int xA = (int)(x2 - size * Math.cos(angle - Math.PI / 6));
        int yA = (int)(y2 - size * Math.sin(angle - Math.PI / 6));
        int xB = (int)(x2 - size * Math.cos(angle + Math.PI / 6));
        int yB = (int)(y2 - size * Math.sin(angle + Math.PI / 6));
        g.drawLine(x2, y2, xA, yA);
        g.drawLine(x2, y2, xB, yB);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point click = worldPoint(e);

        if (engine.getGameState() == GameEngine.STATE_MENU) {
            if (watchButton.contains(click)) {
                engine.startTutorial();
            }
            if (skipButton.contains(click)) {
                engine.skipToOpening();
            }
        }
        else if (engine.getGameState() == GameEngine.STATE_TUTORIAL) {
            engine.skipToOpening();
        }
        else if (engine.getGameState() == GameEngine.STATE_PLAYING) {
            if (waitingForMeter) {
                String zone = meter.lockAndGetZone();
                double vx = pendingVX;
                double vy = pendingVY;
                if (zone.equals("yellow")) {
                    double angle = (Math.random() - 0.5) * 0.4;
                    double speed = Math.sqrt(vx*vx + vy*vy) * (0.7 + Math.random() * 0.4);
                    double baseAngle = Math.atan2(vy, vx) + angle;
                    vx = Math.cos(baseAngle) * speed;
                    vy = Math.sin(baseAngle) * speed;
                }
                else if (zone.equals("red")) {
                    double angle = (Math.random() - 0.5) * 1.2;
                    double speed = Math.sqrt(vx*vx + vy*vy) * (0.3 + Math.random() * 0.5);
                    double baseAngle = Math.atan2(vy, vx) + angle;
                    vx = Math.cos(baseAngle) * speed;
                    vy = Math.sin(baseAngle) * speed;
                }
                engine.processShotDirect(vx, vy);
                meter.reset();
                waitingForMeter = false;
            } else {
                dragStart = click;
                currentMousePos = click;
                isDragging = true;
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        currentMousePos = worldPoint(e);
    }

    private void drawFakeMouse(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillOval(fakeMouse.x - 5, fakeMouse.y - 5, 10, 10);
    }

    public void render() {
        BufferStrategy bf = getBufferStrategy();
        if (bf == null) {
            try { createBufferStrategy(2); } catch (Exception ignored) {}
            return;
        }

        Graphics g;
        try {
            g = bf.getDrawGraphics();
        } catch (IllegalStateException e) {
            try { createBufferStrategy(2); } catch (Exception ignored) {}
            return;
        }
        try {
            Graphics2D scaled = (Graphics2D) g;
            double sx = scaleX();
            double sy = scaleY();
            if (sx > 0 && sy > 0) scaled.scale(sx, sy);

            if (engine.getGameState() == GameEngine.STATE_MENU) {
                drawMenu(scaled);
            }
            else if (engine.getGameState() == GameEngine.STATE_TUTORIAL) {
                drawTutorial(scaled);
            }
            else if (engine.getGameState() == GameEngine.STATE_TUNNEL) {
                drawTunnel(scaled);
            }
            else {
                myPaint(scaled);
            }
        } finally {
            g.dispose();
        }

        try {
            bf.show();
            Toolkit.getDefaultToolkit().sync();
        } catch (IllegalStateException e) {
            try { createBufferStrategy(2); } catch (Exception ignored) {}
        }
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
        g.setColor(new Color(120, 200, 120));
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        g.setColor(Color.DARK_GRAY);
        g.fillRect(450, 450, 50, 50);
        drawNest(g);
        drawFakeMouse(g);
        drawHealthBar(g);
        engine.getEgg().draw(g);

        int t = engine.getTutorialTimer();
        if (t < 60) { fakeMouse.x = 300; fakeMouse.y = 500; }
        else if (t < 120) { fakeMouse.x = 300; fakeMouse.y = 500; }
        else if (t < 240) {
            double progress = (t - 120) / 120.0;
            fakeMouse.x = (int)(300 - 120 * progress);
            fakeMouse.y = (int)(500 - 120 * progress);
        }
        else if (t < 360) { fakeMouse.x = 180; fakeMouse.y = 380; }
        else if (t < 420) { fakeMouse.x = 300; fakeMouse.y = 500; }
        else if (t < 480) {
            double progress = (t - 420) / 60.0;
            fakeMouse.x = (int)(300 - 100 * progress);
            fakeMouse.y = (int)(500 + 140 * progress);
        }
        else { fakeMouse.x = 200; fakeMouse.y = 450; }

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
            if (t > 500 && t < 540) {
                g.setColor(new Color(255, 0, 0, 120));
                g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
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
        if (t > 360) health = 60;
        g.setColor(Color.RED);
        g.fillRect(20, 20, health * 2, 20);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isDragging) {
            Point release = worldPoint(e);
            double dx = release.x - dragStart.x;
            double dy = release.y - dragStart.y;
            pendingVX = -dx * 0.1;
            pendingVY = -dy * 0.1;
            isDragging = false;
            dragStart = null;
            currentMousePos = null;
            meter.activate();
            waitingForMeter = true;
        }
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
}
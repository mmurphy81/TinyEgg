import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.Timer;

public class GameEngine {
    private GameDisplay window;
    private Egg activeEgg;
    private Level currentLevel;
    private int gameState;
    private double FRICTION;
    private int score;
    private ShotMeter meter;
    private ArrayList<Obstacle> obstacles;
    private int tutorialTimer;
    private int currentMap = 1;

    // Tunnel cutscene between Level 3 part 1 and Level 3 part 2.
    private int tunnelTimer = 0;
    private static final int TUNNEL_DURATION = 150;

    // Vibrating barrier in front of the gate on Map 3.
    private double barrierY = 450;
    private double barrierVelY = 3;
    private static final double BARRIER_MIN_Y = 380;
    private static final double BARRIER_MAX_Y = 560;
    public static final int BARRIER_X = 875;
    public static final int BARRIER_W = 30;
    public static final int BARRIER_H = 110;
    // Gate position: x > GATE_X with y in [GATE_Y_TOP, GATE_Y_BOT] = tunnel.
    public static final int GATE_X = 950;
    public static final int GATE_Y_TOP = 410;
    public static final int GATE_Y_BOT = 590;

    public static final int STATE_MENU = 0;
    public static final int STATE_TUTORIAL = 1;
    public static final int STATE_OPENING = 2;
    public static final int STATE_PLAYING = 3;
    public static final int STATE_TUNNEL = 4;

    // Map number → level number. Extend this if you add more levels.
    public int getLevelNumber() {
        if (currentMap == 1) return 1;
        if (currentMap == 2) return 2;
        return 3; // currentMap 3 and 4 are both Level 3
    }

    public String getDifficulty() {
        switch (getLevelNumber()) {
            case 1: return "Easy";
            case 2: return "Medium";
            case 3: return "Hard";
            default: return "?";
        }
    }

    public double getBarrierY() { return barrierY; }

    public GameEngine(){
        gameState = STATE_MENU;
        tutorialTimer = 0;
        activeEgg = new Egg(600,345);
        meter = new ShotMeter();
        obstacles = new ArrayList<>();
        window = new GameDisplay(this, meter);
    }

    public ArrayList<Obstacle> getObstacles() { return obstacles; }

    public void update() {
        if (gameState == STATE_MENU) {
        }
        else if (gameState == STATE_TUTORIAL) {
            tutorialTimer++;
            runTutorial();
        }
        else if (gameState == STATE_OPENING) {
            activeEgg.updateOpening();
            if (activeEgg.getState() == Egg.STATE_LANDED) {
                gameState = STATE_PLAYING;
            }
        }
        else if (gameState == STATE_PLAYING) {
            meter.update();
            activeEgg.move();
            checkCollision();
            // Map 3 has its own moving barrier in front of the tunnel gate.
            if (currentMap == 3) {
                updateBarrier();
                checkBarrierCollision();
            }
            checkNestEntry();
        }
        else if (gameState == STATE_TUNNEL) {
            tunnelTimer++;
            if (tunnelTimer >= TUNNEL_DURATION) {
                currentMap = 4;
                activeEgg = new Egg(120, 850);
                addLevel3bObstacles();
                gameState = STATE_PLAYING;
            }
        }
    }

    // Oscillate the barrier between BARRIER_MIN_Y and BARRIER_MAX_Y.
    private void updateBarrier() {
        barrierY += barrierVelY;
        if (barrierY < BARRIER_MIN_Y) {
            barrierY = BARRIER_MIN_Y;
            barrierVelY = -barrierVelY;
        }
        if (barrierY > BARRIER_MAX_Y) {
            barrierY = BARRIER_MAX_Y;
            barrierVelY = -barrierVelY;
        }
    }

    // Egg vs moving barrier — barrier acts like a vertical wall.
    private void checkBarrierCollision() {
        double ex = activeEgg.getX();
        double ey = activeEgg.getY();
        int bx = BARRIER_X, by = (int) barrierY, bw = BARRIER_W, bh = BARRIER_H;
        boolean overlapping = ex + Egg.WIDTH > bx && ex < bx + bw
                && ey + Egg.HEIGHT > by && ey < by + bh;
        if (!overlapping) return;
        double vx = activeEgg.getVelX();
        double vy = activeEgg.getVelY();
        double overlapL = (ex + Egg.WIDTH) - bx;
        double overlapR = (bx + bw) - ex;
        double newVX = vx;
        if (overlapL < overlapR && vx > 0) newVX = -vx * 0.6;
        else if (overlapL >= overlapR && vx < 0) newVX = -vx * 0.6;
        activeEgg.applyImpulsive(newVX, vy * 0.95);
    }

    public void processShotDirect(double vx, double vy) {
        activeEgg.applyImpulsive(vx, vy);
    }

    private void runTutorial() {
        if (tutorialTimer < 120) {}
        else if (tutorialTimer < 240) {}
        else if (tutorialTimer == 240) {
            double targetX = 760;
            double targetY = 660;
            double dx = targetX - activeEgg.getX();
            double dy = targetY - activeEgg.getY();
            double length = Math.sqrt(dx * dx + dy * dy);
            dx /= length;
            dy /= length;
            activeEgg.applyImpulsive(dx * 10, dy * 10);
        }
        else if (tutorialTimer < 360) {
            activeEgg.move();
        }
        else if (tutorialTimer == 360) {
            activeEgg = new Egg(300, 500);
        }
        else if (tutorialTimer < 420) {}
        else if (tutorialTimer < 480) {}
        else if (tutorialTimer == 480) {
            activeEgg.applyImpulsive(6, -4);
        }
        else if (tutorialTimer < 600) {
            activeEgg.move();
            double ex = activeEgg.getX();
            double ey = activeEgg.getY();
            if (ex + 40 > 450 && ex < 500 && ey + 55 > 450 && ey < 500) {
                activeEgg.applyImpulsive(-5, -6);
                tutorialTimer = 560;
            }
        }
        else if (tutorialTimer < 780) {}
        else {
            activeEgg = new Egg(600, 350);
            gameState = STATE_OPENING;
        }
    }

    public Egg getEgg() { return activeEgg; }

    public void processShot(double deltaX, double deltaY, double multiplier){
        double vx = deltaX * multiplier;
        double vy = deltaY * multiplier;
        activeEgg.applyImpulsive(vx, vy);
    }

    public int getGameState() { return gameState; }

    public void startTutorial() {
        tutorialTimer = 0;
        gameState = STATE_TUTORIAL;
    }

    public void skipToOpening() { gameState = STATE_OPENING; }

    public void checkCollision(){
        for (Obstacle obs : obstacles) {
            if (obs.hasCollided(activeEgg)) {
                obs.respondToCollision(activeEgg);
            }
        }
    }

    public void checkNestEntry(){
        // Map 3 uses a gate, not a nest. Cross GATE_X within y window → tunnel.
        if (currentMap == 3) {
            double ex = activeEgg.getX();
            double ey = activeEgg.getY();
            if (ex >= GATE_X
                    && ey >= GATE_Y_TOP && ey + Egg.HEIGHT <= GATE_Y_BOT) {
                tunnelTimer = 0;
                gameState = STATE_TUNNEL;
            }
            return;
        }
        Rectangle nestBounds;
        if (currentMap == 1) {
            nestBounds = new Rectangle(736, 66, 200, 100);
        } else if (currentMap == 2) {
            nestBounds = new Rectangle(750, 650, 200, 100);
        } else { // currentMap == 4
            nestBounds = new Rectangle(820, 100, 160, 80);
        }
        Rectangle eggBounds = new Rectangle((int)activeEgg.getX(), (int)activeEgg.getY(), 40, 55);
        if (nestBounds.intersects(eggBounds) && !activeEgg.isMoving()) {
            if (currentMap == 1) {
                currentMap = 2;
                activeEgg = new Egg(300, 645);
            } else if (currentMap == 2) {
                // Map 2 cleared — advance to Map 3 (Level 3 part 1).
                currentMap = 3;
                activeEgg = new Egg(120, 850);
                addLevel3aObstacles();
            }
            // currentMap == 4: player finished. Scoreboard hooks in here.
        }
    }

    // Map 3 (Level 3 part 1, pre-tunnel, tropical).
    public void addLevel3aObstacles() {
        obstacles.clear();
        obstacles.add(new Wall(940,  40,  20, GATE_Y_TOP - 40));
        obstacles.add(new Wall(940, GATE_Y_BOT, 20, 960 - GATE_Y_BOT));
        obstacles.add(new Wall(250, 200, 25, 350));
        obstacles.add(new Wall(500, 100, 25, 300));
        obstacles.add(new Wall(500, 550, 25, 300));
        obstacles.add(new Wall(700, 300, 25, 350));
        obstacles.add(new Ice(280, 420, 200, 80));
        obstacles.add(new GrassPatch(770, 420, 100, 60));
        obstacles.add(new GrassPatch(770, 540, 100, 60));
    }

    // Map 4 (Level 3 part 2, post-tunnel, tropical).
    public void addLevel3bObstacles() {
        obstacles.clear();
        obstacles.add(new Wall(300, 600,  25, 200));
        obstacles.add(new Wall(600, 300,  25, 200));
        obstacles.add(new Wall(450, 100, 300,  25));
        obstacles.add(new Wall(100, 400, 200,  25));
        obstacles.add(new Wall(750, 600,  25, 250));
        obstacles.add(new Ice( 50, 100, 250,  80));
        obstacles.add(new Ice(350, 350, 200, 100));
        obstacles.add(new GrassPatch(700, 200, 110,  70));
    }

    public void addLevel3Obstacles() { addLevel3bObstacles(); }

    public int getTunnelTimer() { return tunnelTimer; }

    public int getCurrentMap() { return currentMap; }

    public String getFinalResult(){ return null; }

    public void run() {
        while (true) {
            update();
            window.render();
            try { Thread.sleep(16); } catch (InterruptedException e) {}
        }
    }

    public int getTutorialTimer() { return tutorialTimer; }

    public void addLevel1Obstacles(){
        obstacles.clear();
        addLevel1Ice();
        addLevel1Walls();
        addLevel1GrassP();
    }
    public void addLevel2Obstacles(){
        obstacles.clear();
        addLevel2Ice();
        addLevel2Walls();
        addLevel2GrassP();
    }
    public void addLevel1Walls(){
        obstacles.add(new Wall(519, 79, 38, 316));
        obstacles.add(new Wall(245, 632, 38, 290));
    }
    public void addLevel2Walls(){
        obstacles.clear();
        obstacles.add(new Wall(200, 0, 25, 320));
        obstacles.add(new Wall(250, 500, 25, 300));
        obstacles.add((new Wall(475, 0, 25, 300)));
        obstacles.add((new Wall(700, 0, 25, 400)));
        obstacles.add((new Wall(475, 470, 25, 300)));
        obstacles.add(new Wall(900, 0, 25, 600));
    }
    public void addLevel1Ice(){
        obstacles.add((new Ice(113, 105, 245, 197)));
        obstacles.add((new Ice(396, 487, 283, 237)));
    }
    public void addLevel2Ice(){
        obstacles.add((new Ice(0, 200, 200, 120)));
        obstacles.add((new Ice(275, 600, 200, 120)));
        obstacles.add((new Ice(500, 260, 200, 120)));
    }
    public void addLevel1GrassP(){
        obstacles.add(new GrassPatch(620, 190, 180, 100));
        obstacles.add(new GrassPatch(620, 190, 180, 100));
        obstacles.add(new GrassPatch(680, 710, 180, 100));
    }
    public void addLevel2GrassP(){
        obstacles.add(new GrassPatch(390, 430, 125, 100));
        obstacles.add(new GrassPatch(720,530, 128, 100));
        obstacles.add(new GrassPatch(560, 720, 140, 100));
    }

    public static void main(String[] args) {
        GameEngine g = new GameEngine();
        g.run();
    }
}
import java.awt.*;
import java.util.ArrayList;

/**
 * GameEngine drives the game loop, manages game state transitions,
 * handles collision detection, and owns all game objects.
 */
public class GameEngine {

    // ── Core objects ───────────────────────────────────────────────────────────
    private GameDisplay window;
    private Egg activeEgg;
    private ShotMeter meter;
    private ArrayList<Obstacle> obstacles;

    // ── State tracking ─────────────────────────────────────────────────────────
    private int gameState;
    private int currentMap   = 1;
    private int tutorialTimer;
    private int endingTimer;
    private int strokeCount  = 0;  // total shots fired across all maps
    private int[] strokesPerMap = new int[5];
    private int tunnelTimer = 0;
    private boolean playerLost = false;

    private double barrierY = 450;
    private double barrierVelY = 3;

    // ── Game state constants ───────────────────────────────────────────────────
    public static final int STATE_MENU     = 0;
    public static final int STATE_TUTORIAL = 1;
    public static final int STATE_OPENING  = 2;
    public static final int STATE_PLAYING  = 3;
    public static final int STATE_ENDING   = 4;
    public static final int STATE_TUNNEL   = 5;

    // ── Map constants ──────────────────────────────────────────────────────────
    private static final int MAP_1 = 1;
    private static final int MAP_2 = 2;
    private static final int MAP_3 = 3;
    private static final int MAP_4 = 4;

    // Starting positions — chosen to avoid all obstacles on each map
    private static final int MAP1_START_X = 80;
    private static final int MAP1_START_Y = 645;
    private static final int MAP2_START_X = 80;
    private static final int MAP2_START_Y = 645;
    private static final int MAP3_START_X = 100;
    private static final int MAP3_START_Y = 870;
    private static final int MAP4_START_X = 120;
    private static final int MAP4_START_Y = 850;

    // Opening animation egg position (on the tree branch)
    private static final int OPENING_EGG_X = 600;
    private static final int OPENING_EGG_Y = 345;

    // Nest bounds for nest-entry detection
    private static final Rectangle MAP1_NEST = new Rectangle(736,  66, 200, 100);
    private static final Rectangle MAP2_NEST = new Rectangle(780, 660, 140,  50);
    private static final Rectangle MAP4_NEST = new Rectangle(820, 100, 160,  80);

    public static final int BARRIER_X = 875;
    public static final int BARRIER_W = 30;
    public static final int BARRIER_H = 110;
    private static final double BARRIER_MIN_Y = 380;
    private static final double BARRIER_MAX_Y = 560;
    public static final int GATE_X     = 950;
    public static final int GATE_Y_TOP = 410;
    public static final int GATE_Y_BOT = 590;

    private static final int TUNNEL_DURATION = 150;
    private static final int HEALTH_REGEN_PER_LEVEL = 20;
    private static final int HEALTH_COST_PER_SHOT = 2;
    private static final int[] PAR_PER_MAP = { 0, 3, 4, 3, 4 };

    public int getLevelNumber() {
        if (currentMap == MAP_1) return 1;
        if (currentMap == MAP_2) return 2;
        return 3;
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
    public int    getTunnelTimer() { return tunnelTimer; }
    public boolean didPlayerLose() { return playerLost; }
    public int  getStrokesForMap(int m) { return strokesPerMap[m]; }
    public int  getParForMap(int m)     { return PAR_PER_MAP[m]; }
    public int  getTotalPar() {
        return PAR_PER_MAP[1] + PAR_PER_MAP[2] + PAR_PER_MAP[3] + PAR_PER_MAP[4];
    }

    // Tutorial timing constants (frames)
    private static final int TUTORIAL_FIRE_FRAME  = 240;
    private static final int TUTORIAL_RESET_FRAME = 360;
    private static final int TUTORIAL_BAD_SHOT    = 480;
    private static final int TUTORIAL_END_FRAME   = 780;

    // Target nest position used in tutorial perfect shot
    private static final double TUTORIAL_NEST_X = 760;
    private static final double TUTORIAL_NEST_Y = 660;
    private static final double TUTORIAL_SHOT_POWER = 10;

    // Tutorial obstacle block position (used for collision detection in tutorial)
    private static final int TUTORIAL_BLOCK_X = 450;
    private static final int TUTORIAL_BLOCK_Y = 450;
    private static final int TUTORIAL_BLOCK_W = 50;
    private static final int TUTORIAL_BLOCK_H = 50;

    // Frame range where tutorial egg is moving after bad shot
    private static final int TUTORIAL_MOVE_START = 480;
    private static final int TUTORIAL_MOVE_END   = 600;
    private static final int TUTORIAL_BOUNCE_SKIP = 560;

    // Tick rate
    private static final int FRAME_DELAY_MS = 16; // ~60 FPS

    public GameEngine() {
        gameState  = STATE_MENU;
        tutorialTimer = 0;
        activeEgg  = new Egg(MAP1_START_X, MAP1_START_Y);
        meter      = new ShotMeter();
        obstacles  = new ArrayList<>();
        window     = new GameDisplay(this, meter);
        addLevel1Obstacles(); // load map 1 obstacles once at startup
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public ArrayList<Obstacle> getObstacles()  { return obstacles; }
    public int  getEndingTimer()               { return endingTimer; }
    public int  getStrokeCount()               { return strokeCount; }
    public Egg  getEgg()                       { return activeEgg; }
    public int  getGameState()                 { return gameState; }
    public int  getCurrentMap()                { return currentMap; }
    public int  getTutorialTimer()             { return tutorialTimer; }

    // ── State transitions ──────────────────────────────────────────────────────

    public void startTutorial() {
        tutorialTimer = 0;
        gameState     = STATE_TUTORIAL;
    }

    /** Resets the egg to the opening animation position and starts the animation. */
    public void skipToOpening() {
        activeEgg = new Egg(OPENING_EGG_X, OPENING_EGG_Y);
        gameState = STATE_OPENING;
    }

    // ── Main update loop ───────────────────────────────────────────────────────

    /** Called once per frame; routes to the appropriate state handler. */
    public void update() {
        switch (gameState) {
            case STATE_MENU:     break; // wait for input
            case STATE_TUTORIAL: updateTutorial();  break;
            case STATE_OPENING:  updateOpening();   break;
            case STATE_PLAYING:  updatePlaying();   break;
            case STATE_TUNNEL:   updateTunnel();    break;
            case STATE_ENDING:   endingTimer++;     break;
        }
    }

    private void updateTunnel() {
        tunnelTimer++;
        if (tunnelTimer >= TUNNEL_DURATION) {
            advanceToMap4();
        }
    }

    /** Advances the tutorial timer and runs scripted tutorial logic. */
    private void updateTutorial() {
        tutorialTimer++;
        runTutorial();
    }

    /**
     * Advances the opening animation.
     * When the egg lands, resets it to the map 1 play position
     * and transitions to STATE_PLAYING.
     */
    private void updateOpening() {
        activeEgg.updateOpening();
        if (activeEgg.getState() == Egg.STATE_LANDED) {
            gameState = STATE_PLAYING;
            activeEgg = new Egg(MAP1_START_X, MAP1_START_Y);
        }
    }

    /** Runs one frame of gameplay: move egg, check collisions, check nest. */
    private void updatePlaying() {
        meter.update();
        activeEgg.move();
        checkCollision();
        if (currentMap == MAP_3) {
            updateBarrier();
            checkBarrierCollision();
        }
        checkNestEntry();
        if (activeEgg.isCracked()) {
            playerLost = true;
            endingTimer = 0;
            gameState = STATE_ENDING;
        }
    }

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

    private void checkBarrierCollision() {
        double ex = activeEgg.getX();
        double ey = activeEgg.getY();
        int by = (int) barrierY;
        boolean overlapping = ex + Egg.WIDTH > BARRIER_X
                           && ex < BARRIER_X + BARRIER_W
                           && ey + Egg.HEIGHT > by
                           && ey < by + BARRIER_H;
        if (!overlapping) return;
        double vx = activeEgg.getVelX();
        double vy = activeEgg.getVelY();
        double overlapL = (ex + Egg.WIDTH) - BARRIER_X;
        double overlapR = (BARRIER_X + BARRIER_W) - ex;
        double newVX = vx;
        if (overlapL < overlapR && vx > 0)      newVX = -vx * 0.6;
        else if (overlapL >= overlapR && vx < 0) newVX = -vx * 0.6;
        activeEgg.reduceHealth(Egg.impactDamage(Math.sqrt(vx*vx + vy*vy)) / 2);
        activeEgg.applyImpulsive(newVX, vy * 0.95);
    }

    // ── Shot processing ────────────────────────────────────────────────────────

    /**
     * Fires the egg with pre-adjusted velocity from the shot meter.
     * Increments the stroke counter each time a real shot is taken.
     */
    public void processShotDirect(double vx, double vy) {
        strokeCount++;
        strokesPerMap[currentMap]++;
        activeEgg.reduceHealth(HEALTH_COST_PER_SHOT);
        activeEgg.applyImpulsive(vx, vy);
    }

    // ── Collision ──────────────────────────────────────────────────────────────

    /** Checks every obstacle and calls its collision response if the egg overlaps it. */
    public void checkCollision() {
        for (Obstacle obs : obstacles) {
            if (obs.hasCollided(activeEgg)) {
                obs.respondToCollision(activeEgg);
            }
        }
    }

    /**
     * Checks whether the egg has stopped inside the nest.
     * On map 1: advances to map 2 and resets the egg.
     * On map 2: triggers the ending screen.
     */
    public void checkNestEntry() {
        if (currentMap == MAP_3) {
            double ex = activeEgg.getX();
            double ey = activeEgg.getY();
            if (ex >= GATE_X
                    && ey >= GATE_Y_TOP && ey + Egg.HEIGHT <= GATE_Y_BOT) {
                startTunnelCutscene();
            }
            return;
        }

        Rectangle nestBounds;
        if      (currentMap == MAP_1) nestBounds = MAP1_NEST;
        else if (currentMap == MAP_2) nestBounds = MAP2_NEST;
        else                          nestBounds = MAP4_NEST;

        Rectangle eggBounds = new Rectangle(
                (int) activeEgg.getX(), (int) activeEgg.getY(),
                Egg.WIDTH, Egg.HEIGHT);

        if (nestBounds.intersects(eggBounds) && !activeEgg.isMoving()) {
            if (currentMap == MAP_1)      advanceToMap2();
            else if (currentMap == MAP_2) advanceToMap3();
            else {
                playerLost = false;
                endingTimer = 0;
                gameState = STATE_ENDING;
            }
        }
    }

    private void advanceToMap2() {
        currentMap = MAP_2;
        obstacles.clear();
        addLevel2Obstacles();
        activeEgg = new Egg(MAP2_START_X, MAP2_START_Y);
        activeEgg.addHealth(HEALTH_REGEN_PER_LEVEL);
    }

    private void advanceToMap3() {
        currentMap = MAP_3;
        obstacles.clear();
        addLevel3aObstacles();
        activeEgg = new Egg(MAP3_START_X, MAP3_START_Y);
        activeEgg.addHealth(HEALTH_REGEN_PER_LEVEL);
    }

    private void startTunnelCutscene() {
        tunnelTimer = 0;
        gameState   = STATE_TUNNEL;
    }

    private void advanceToMap4() {
        currentMap = MAP_4;
        obstacles.clear();
        addLevel3bObstacles();
        activeEgg = new Egg(MAP4_START_X, MAP4_START_Y);
        gameState = STATE_PLAYING;
    }

    public void resetGame() {
        strokeCount = 0;
        for (int i = 0; i < strokesPerMap.length; i++) strokesPerMap[i] = 0;
        tunnelTimer = 0;
        endingTimer = 0;
        playerLost = false;
        currentMap = MAP_1;
        obstacles.clear();
        addLevel1Obstacles();
        activeEgg = new Egg(OPENING_EGG_X, OPENING_EGG_Y);
        gameState = STATE_OPENING;
    }

    // ── Tutorial scripting ─────────────────────────────────────────────────────

    /**
     * Scripted tutorial sequence driven by tutorialTimer.
     * Visual/text elements are handled in GameDisplay; this controls egg movement.
     */
    private void runTutorial() {
        if      (tutorialTimer < 120)              { /* explain goal — visuals only */ }
        else if (tutorialTimer < TUTORIAL_FIRE_FRAME) { /* show pull-back — visuals only */ }
        else if (tutorialTimer == TUTORIAL_FIRE_FRAME) fireTutorialPerfectShot();
        else if (tutorialTimer < TUTORIAL_RESET_FRAME) activeEgg.move();
        else if (tutorialTimer == TUTORIAL_RESET_FRAME) resetTutorialEggForBadExample();
        else if (tutorialTimer < TUTORIAL_BAD_SHOT)    { /* pause for obstacle text */ }
        else if (tutorialTimer == TUTORIAL_BAD_SHOT)   fireTutorialBadShot();
        else if (tutorialTimer < TUTORIAL_MOVE_END)    updateTutorialBadShotMovement();
        else if (tutorialTimer < TUTORIAL_END_FRAME)   { /* pause after second shot */ }
        else                                           endTutorial();
    }

    /** Fires the egg directly at the nest for the tutorial's perfect-shot demo. */
    private void fireTutorialPerfectShot() {
        double dx = TUTORIAL_NEST_X - activeEgg.getX();
        double dy = TUTORIAL_NEST_Y - activeEgg.getY();
        double length = Math.sqrt(dx * dx + dy * dy);
        activeEgg.applyImpulsive(
                (dx / length) * TUTORIAL_SHOT_POWER,
                (dy / length) * TUTORIAL_SHOT_POWER);
    }

    /** Resets the egg to the tutorial position for the bad-aim demonstration. */
    private void resetTutorialEggForBadExample() {
        activeEgg = new Egg(300, 500);
    }

    /** Fires the egg at a bad angle so it hits the obstacle block. */
    private void fireTutorialBadShot() {
        activeEgg.applyImpulsive(6, -4);
    }

    /**
     * Moves the egg and checks for collision with the tutorial obstacle block.
     * Bounces back once on contact and skips ahead to prevent repeated bouncing.
     */
    private void updateTutorialBadShotMovement() {
        activeEgg.move();
        double ex = activeEgg.getX();
        double ey = activeEgg.getY();
        boolean hitsBlock =
                ex + Egg.WIDTH  > TUTORIAL_BLOCK_X &&
                        ex              < TUTORIAL_BLOCK_X + TUTORIAL_BLOCK_W &&
                        ey + Egg.HEIGHT > TUTORIAL_BLOCK_Y &&
                        ey              < TUTORIAL_BLOCK_Y + TUTORIAL_BLOCK_H;

        if (hitsBlock) {
            activeEgg.applyImpulsive(-5, -6);
            tutorialTimer = TUTORIAL_BOUNCE_SKIP; // skip ahead to prevent re-triggering
        }
    }

    /** Resets the egg to the opening animation start and transitions out of tutorial. */
    private void endTutorial() {
        activeEgg = new Egg(OPENING_EGG_X, OPENING_EGG_Y);
        gameState = STATE_OPENING;
    }

    // ── Obstacle builders ──────────────────────────────────────────────────────
    // Each method is called once per map load, not every frame.

    public void addLevel1Obstacles() {
        obstacles.clear();
        addLevel1Walls();
        addLevel1Ice();
        addLevel1GrassP();
    }

    public void addLevel2Obstacles() {
        obstacles.clear();
        addLevel2Walls();
        addLevel2Ice();
        addLevel2GrassP();
    }

    public void addLevel1Walls() {
        obstacles.add(new Wall(519,  79, 38, 316));
        obstacles.add(new Wall(245, 632, 38, 290));
    }

    public void addLevel2Walls() {
        obstacles.add(new Wall(200,   0, 25, 320));
        obstacles.add(new Wall(250, 500, 25, 300));
        obstacles.add(new Wall(475,   0, 25, 300));
        obstacles.add(new Wall(700,   0, 25, 200));
        obstacles.add(new Wall(475, 530, 25, 300));
        obstacles.add(new Wall(900,   0, 25, 600));
    }

    public void addLevel1Ice() {
        obstacles.add(new Ice(113, 105, 245, 197));
        obstacles.add(new Ice(396, 487, 283, 237));
    }

    public void addLevel2Ice() {
        obstacles.add(new Ice(  0, 200, 200, 120));
        obstacles.add(new Ice(275, 600, 200, 120));
        obstacles.add(new Ice(500, 260, 200, 120));
    }

    public void addLevel1GrassP() {
        obstacles.add(new GrassPatch( 90, 370, 180, 100));
        obstacles.add(new GrassPatch(620, 190, 180, 100));
        obstacles.add(new GrassPatch(680, 710, 180, 100));
    }

    public void addLevel2GrassP() {
        obstacles.add(new GrassPatch(390, 430, 125, 100));
        obstacles.add(new GrassPatch(720, 530, 128, 100));
        obstacles.add(new GrassPatch(560, 720, 140, 100));
    }

    public void addLevel3aObstacles() {
        obstacles.clear();
        obstacles.add(new Wall(940,  40, 20, GATE_Y_TOP - 40));
        obstacles.add(new Wall(940, GATE_Y_BOT, 20, 960 - GATE_Y_BOT));
        obstacles.add(new Wall(180, 200, 25, 500));
        obstacles.add(new Wall(180, 750, 200, 25));
        obstacles.add(new Wall(280, 100, 25, 300));
        obstacles.add(new Wall(450, 100, 25, 350));
        obstacles.add(new Wall(450, 550, 25, 350));
        obstacles.add(new Wall(450, 850, 350, 25));
        obstacles.add(new Wall(650, 250, 25, 300));
        obstacles.add(new Wall(650, 650, 25, 250));
        obstacles.add(new Wall(800, 350, 25, 100));
        obstacles.add(new Wall(800, 600, 25, 100));
        obstacles.add(new Ice( 60, 250, 100, 80));
        obstacles.add(new Ice(550, 750, 100, 80));
        obstacles.add(new Ice(700, 400, 80,  80));
        obstacles.add(new GrassPatch(770, 420, 100, 60));
        obstacles.add(new GrassPatch(770, 540, 100, 60));
        obstacles.add(new GrassPatch(350, 800,  80, 50));
    }

    public void addLevel3bObstacles() {
        obstacles.clear();
        obstacles.add(new Wall(300, 600,  25, 350));
        obstacles.add(new Wall(600, 300,  25, 350));
        obstacles.add(new Wall(450,  80, 350,  25));
        obstacles.add(new Wall(100, 400, 250,  25));
        obstacles.add(new Wall(750, 600,  25, 300));
        obstacles.add(new Wall(200, 200,  25, 200));
        obstacles.add(new Wall(450, 750,  25, 200));
        obstacles.add(new Wall(600, 870, 250,  25));
        obstacles.add(new Wall(820, 200,  25,  90));
        obstacles.add(new Ice( 50, 100, 250,  80));
        obstacles.add(new Ice(350, 350, 200, 100));
        obstacles.add(new Ice(550, 850, 200,  70));
        obstacles.add(new GrassPatch(700, 200, 110, 70));
    }

    // ── Game loop ──────────────────────────────────────────────────────────────

    /** Runs the game loop at ~60 FPS until the program exits. */
    public void run() {
        while (true) {
            update();
            window.render();
            try {
                Thread.sleep(FRAME_DELAY_MS);
            } catch (InterruptedException e) { /* ignore */ }
        }
    }

    public static void main(String[] args) {
        GameEngine g = new GameEngine();
        g.run();
    }
}
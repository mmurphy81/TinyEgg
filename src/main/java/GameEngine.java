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
    private int strokeCount  = 0;  // total shots fired across both maps

    // ── Game state constants ───────────────────────────────────────────────────
    public static final int STATE_MENU     = 0;
    public static final int STATE_TUTORIAL = 1;
    public static final int STATE_OPENING  = 2;
    public static final int STATE_PLAYING  = 3;
    public static final int STATE_ENDING   = 4;

    // ── Map constants ──────────────────────────────────────────────────────────
    private static final int MAP_1 = 1;
    private static final int MAP_2 = 2;

    // Starting positions — chosen to avoid all obstacles on each map
    private static final int MAP1_START_X = 80;
    private static final int MAP1_START_Y = 645;
    private static final int MAP2_START_X = 80;
    private static final int MAP2_START_Y = 645;

    // Opening animation egg position (on the tree branch)
    private static final int OPENING_EGG_X = 600;
    private static final int OPENING_EGG_Y = 345;

    // Nest bounds for nest-entry detection
    private static final Rectangle MAP1_NEST = new Rectangle(736,  66, 200, 100);
    private static final Rectangle MAP2_NEST = new Rectangle(780, 660, 140,  50);

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
            case STATE_ENDING:   endingTimer++;     break;
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
        checkNestEntry();
    }

    // ── Shot processing ────────────────────────────────────────────────────────

    /**
     * Fires the egg with pre-adjusted velocity from the shot meter.
     * Increments the stroke counter each time a real shot is taken.
     */
    public void processShotDirect(double vx, double vy) {
        strokeCount++;
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
        Rectangle nestBounds = (currentMap == MAP_1) ? MAP1_NEST : MAP2_NEST;
        Rectangle eggBounds  = new Rectangle(
                (int) activeEgg.getX(), (int) activeEgg.getY(), Egg.WIDTH, Egg.HEIGHT);

        if (nestBounds.intersects(eggBounds) && !activeEgg.isMoving()) {
            if (currentMap == MAP_2) {
                gameState = STATE_ENDING;
            } else {
                advanceToMap2();
            }
        }
    }

    /** Clears map 1 obstacles, loads map 2, and repositions the egg. */
    private void advanceToMap2() {
        currentMap = MAP_2;
        obstacles.clear();
        addLevel2Obstacles();
        activeEgg = new Egg(MAP2_START_X, MAP2_START_Y);
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
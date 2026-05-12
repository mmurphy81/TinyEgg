// Programmers: Ryan Weinswig, Mera Murphy, Alberto Perez-Jacome
// Date: May 11, 2025
// Project: Tiny Egg
// Description: Drives the game loop, owns all game objects, manages state
//              transitions, collision detection, nest-entry detection, and
//              level loading. GameDisplay has no direct access to game logic —
//              all state is exposed through getters only.

import java.awt.*;
import java.util.ArrayList;

public class GameEngine {

    // ── Game state constants ───────────────────────────────────────────────────
    // These constants define the five screens/phases the game can be in
    public static final int STATE_MENU     = 0; // main menu with two buttons
    public static final int STATE_TUTORIAL = 1; // scripted tutorial animation
    public static final int STATE_OPENING  = 2; // egg wobbling and falling from branch
    public static final int STATE_PLAYING  = 3; // live player-controlled gameplay
    public static final int STATE_ENDING   = 4; // win screen with chick animation

    // ── Map identifiers ────────────────────────────────────────────────────────
    private static final int MAP_1 = 1; // first level (nest in top-right corner)
    private static final int MAP_2 = 2; // second level (nest in bottom-right area)

    // ── Egg starting positions per map ─────────────────────────────────────────
    // Y values moved up 25px from 880 so the egg is fully visible at game start
    private static final int MAP1_START_X = 80;  // left edge of egg on map 1
    private static final int MAP1_START_Y = 855; // top edge of egg on map 1 (adjusted up 25px)
    private static final int MAP2_START_X = 80;  // left edge of egg on map 2
    private static final int MAP2_START_Y = 855; // top edge of egg on map 2 (adjusted up 25px)

    // ── Opening animation egg position (on the tree branch) ───────────────────
    private static final int OPENING_EGG_X = 600; // left edge of egg resting on the branch
    private static final int OPENING_EGG_Y = 345; // top edge of egg resting on the branch

    // ── Nest bounding boxes (used for nest-entry detection) ───────────────────
    // These rectangles define the target area; the egg must be inside AND stopped to score
    private static final Rectangle MAP1_NEST = new Rectangle(736,  66, 200, 100); // top-right nest
    private static final Rectangle MAP2_NEST = new Rectangle(780, 660, 140,  50); // bottom-right nest

    // ── Tutorial timing (frames at which each event fires) ────────────────────
    private static final int TUTORIAL_FIRE_FRAME   = 240; // frame on which the perfect shot fires
    private static final int TUTORIAL_RESET_FRAME  = 360; // frame on which the egg resets for bad demo
    private static final int TUTORIAL_BAD_SHOT     = 480; // frame on which the bad shot fires
    private static final int TUTORIAL_MOVE_END     = 650; // frame after which the bad-shot egg stops moving
    private static final int TUTORIAL_BOUNCE_SKIP  = 610; // timer jumps here after the block hit to prevent re-triggering
    private static final int TUTORIAL_END_FRAME    = 780; // frame on which the tutorial transitions to opening

    // ── Tutorial egg start position ───────────────────────────────────────────
    private static final int TUTORIAL_EGG_X = 150; // left edge of the egg at tutorial start
    private static final int TUTORIAL_EGG_Y = 700; // top edge of the egg at tutorial start

    // ── Tutorial nest target (where the perfect shot aims) ────────────────────
    // Must match the nest drawn in GameDisplay.drawTutorial
    private static final double TUTORIAL_NEST_X    = 720;  // x-center used for direction calculation
    private static final double TUTORIAL_NEST_Y    = 760;  // y-center used for direction calculation
    private static final double TUTORIAL_SHOT_POWER = 10;  // pixels per frame; normalized so speed is constant

    // ── Tutorial obstacle block (the bad shot hits this) ──────────────────────
    // Must match the block drawn in GameDisplay.drawTutorial
    private static final int TUTORIAL_BLOCK_X = 450; // left edge of the block
    private static final int TUTORIAL_BLOCK_Y = 450; // top edge of the block
    private static final int TUTORIAL_BLOCK_W = 50;  // block width in pixels
    private static final int TUTORIAL_BLOCK_H = 50;  // block height in pixels

    // ── Tutorial bad-shot velocities ──────────────────────────────────────────
    private static final double TUTORIAL_BAD_VX    =  9;  // initial horizontal speed of the bad shot (rightward)
    private static final double TUTORIAL_BAD_VY    = -6;  // initial vertical speed of the bad shot (upward)
    private static final double TUTORIAL_BOUNCE_VX = -5;  // horizontal rebound speed after hitting the block
    private static final double TUTORIAL_BOUNCE_VY = -6;  // vertical rebound speed after hitting the block

    // ── Frame rate ────────────────────────────────────────────────────────────
    private static final int FRAME_DELAY_MS = 16; // ~60 FPS; sleep time between updates

    // ── Core objects ──────────────────────────────────────────────────────────
    private GameDisplay          window;    // renders every frame and handles mouse input
    private Egg                  activeEgg; // the single egg the player controls
    private ShotMeter            meter;     // accuracy bar shown between drag release and fire
    private ArrayList<Obstacle>  obstacles; // all walls, ice patches, and grass patches for the current map

    // ── State tracking ─────────────────────────────────────────────────────────
    private int     gameState;             // one of the STATE_* constants above
    private int     currentMap    = MAP_1; // which level the player is currently on
    private int     tutorialTimer = 0;     // frame counter driving the tutorial script
    private int     endingTimer   = 0;     // frame counter driving the win-screen animation
    private int     strokeCount   = 0;     // total shots fired across both maps
    private boolean tutorialHit   = false; // true once the bad-shot egg hits the obstacle block

    // ── Constructor ────────────────────────────────────────────────────────────

    /**
     * Initializes all game objects, loads map 1 obstacles, and opens the window.
     */
    public GameEngine() {
        gameState = STATE_MENU;                     // always start at the main menu
        activeEgg = new Egg(MAP1_START_X, MAP1_START_Y); // egg at map 1 starting position
        meter     = new ShotMeter();                // accuracy meter starts hidden
        obstacles = new ArrayList<>();              // empty list; filled by addLevel1Obstacles()
        window    = new GameDisplay(this, meter);   // open the window (must come after objects exist)
        addLevel1Obstacles();                       // populate the obstacle list for map 1
    }

    // ── Getters (GameDisplay reads these — no direct field access) ─────────────

    public ArrayList<Obstacle> getObstacles()  { return obstacles; }    // obstacle list for current map
    public Egg                 getEgg()        { return activeEgg; }    // current egg object
    public int                 getGameState()  { return gameState; }    // current STATE_* value
    public int                 getCurrentMap() { return currentMap; }   // MAP_1 or MAP_2
    public int                 getTutorialTimer() { return tutorialTimer; } // tutorial frame counter
    public int                 getEndingTimer()   { return endingTimer; }   // win-screen frame counter
    public int                 getStrokeCount()   { return strokeCount; }   // total shots fired
    public boolean             getTutorialHit()   { return tutorialHit; }   // true after block impact

    // ── State transitions ──────────────────────────────────────────────────────

    /**
     * Starts the tutorial from the beginning.
     * Resets the egg to the tutorial start position and clears the hit flag.
     */
    public void startTutorial() {
        tutorialTimer = 0;                                // restart the scripted sequence
        tutorialHit   = false;                            // clear any previous hit state
        activeEgg     = new Egg(TUTORIAL_EGG_X, TUTORIAL_EGG_Y); // position egg for tutorial
        gameState     = STATE_TUTORIAL;                   // switch to tutorial rendering and updates
    }

    /**
     * Skips to the opening animation.
     * Positions the egg on the tree branch and starts the fall sequence.
     */
    public void skipToOpening() {
        activeEgg = new Egg(OPENING_EGG_X, OPENING_EGG_Y); // egg on the branch, ready to wobble and fall
        gameState = STATE_OPENING;                           // switch to opening animation state
    }

    // ── Main update loop ───────────────────────────────────────────────────────

    /**
     * Called once per frame. Routes to the correct update method
     * based on the current game state.
     */
    public void update() {
        switch (gameState) {
            case STATE_MENU:     break;                  // menu is purely input-driven; no per-frame updates
            case STATE_TUTORIAL: updateTutorial(); break; // advance tutorial script one frame
            case STATE_OPENING:  updateOpening();  break; // advance opening animation one frame
            case STATE_PLAYING:  updatePlaying();  break; // run physics, collisions, and nest check
            case STATE_ENDING:   endingTimer++;    break; // tick the win-screen animation timer
        }
    }

    /** Increments the tutorial timer and runs the scripted sequence. */
    private void updateTutorial() {
        tutorialTimer++;          // advance the frame counter
        runTutorialScript();      // fire events based on the current frame count
    }

    /**
     * Advances the opening animation one frame.
     * When the egg lands, transitions to STATE_PLAYING and resets the egg.
     */
    private void updateOpening() {
        activeEgg.updateOpening(); // drive the wobble → shake → crack → fall state machine
        if (activeEgg.getState() == Egg.STATE_LANDED) {
            // Egg has hit the floor — transition to live gameplay
            gameState = STATE_PLAYING;
            activeEgg = new Egg(MAP1_START_X, MAP1_START_Y); // reset egg to map 1 start
        }
    }

    /**
     * Runs one frame of live gameplay:
     * updates the meter, moves the egg, checks collisions, checks nest entry.
     */
    private void updatePlaying() {
        meter.update();      // move the accuracy bar one step if it is active
        activeEgg.move();    // apply velocity, friction, and boundary bouncing
        checkCollision();    // test every obstacle and apply physics responses
        checkNestEntry();    // test if the egg is inside the nest and at rest
    }

    // ── Shot processing ────────────────────────────────────────────────────────

    /**
     * Fires the egg with the given velocity (already adjusted by the shot meter).
     * Increments the stroke count each time this is called.
     *
     * @param vx horizontal velocity to apply
     * @param vy vertical velocity to apply
     */
    public void processShotDirect(double vx, double vy) {
        strokeCount++;                   // count this as one shot regardless of direction or power
        activeEgg.applyImpulsive(vx, vy); // replace current velocity with the new shot velocity
    }

    // ── Collision ──────────────────────────────────────────────────────────────

    /**
     * Tests every active obstacle against the egg and triggers collision
     * responses on any that overlap.
     */
    private void checkCollision() {
        for (Obstacle obs : obstacles) {
            if (obs.hasCollided(activeEgg)) {
                obs.respondToCollision(activeEgg); // bounce, speed boost, or slow down
            }
        }
    }

    /**
     * Checks whether the egg has come to rest inside the current map's nest.
     * Map 1: advances to map 2. Map 2: triggers the ending screen.
     */
    private void checkNestEntry() {
        // Select the correct nest rectangle for the current level
        Rectangle nestBounds = (currentMap == MAP_1) ? MAP1_NEST : MAP2_NEST;

        // Build a bounding box around the egg using its current position
        Rectangle eggBounds  = new Rectangle(
                (int) activeEgg.getX(), (int) activeEgg.getY(),
                Egg.WIDTH, Egg.HEIGHT);

        // Only register a nest entry when the egg is fully stopped inside the nest
        if (nestBounds.intersects(eggBounds) && !activeEgg.isMoving()) {
            if (currentMap == MAP_2) {
                gameState = STATE_ENDING; // both levels complete — show the win screen
            } else {
                advanceToMap2();          // first nest reached — load map 2
            }
        }
    }

    /**
     * Clears map 1 obstacles, loads map 2, and repositions the egg.
     */
    private void advanceToMap2() {
        currentMap = MAP_2;       // switch the map identifier so drawing uses map 2 layout
        obstacles.clear();        // remove all map 1 obstacles
        addLevel2Obstacles();     // populate with map 2 obstacles
        activeEgg = new Egg(MAP2_START_X, MAP2_START_Y); // reset egg to map 2 starting position
    }

    // ── Tutorial script ────────────────────────────────────────────────────────

    /**
     * Scripted tutorial sequence driven by tutorialTimer.
     * Visual elements (fake mouse, text) are drawn in GameDisplay.
     * This method only controls egg movement and state changes.
     */
    private void runTutorialScript() {
        if      (tutorialTimer < 120)                   { /* Phase 1: goal text displayed by GameDisplay */ }
        else if (tutorialTimer < TUTORIAL_FIRE_FRAME)   { /* Phase 2: drag animation displayed by GameDisplay */ }
        else if (tutorialTimer == TUTORIAL_FIRE_FRAME)  fireTutorialPerfectShot();      // launch toward nest
        else if (tutorialTimer < TUTORIAL_RESET_FRAME)  activeEgg.move();               // egg travels to nest
        else if (tutorialTimer == TUTORIAL_RESET_FRAME) resetTutorialEgg();             // reposition for bad demo
        else if (tutorialTimer < TUTORIAL_BAD_SHOT)     { /* Phase 4: obstacle warning text displayed */ }
        else if (tutorialTimer == TUTORIAL_BAD_SHOT)    fireTutorialBadShot();          // launch toward block
        else if (tutorialTimer < TUTORIAL_MOVE_END)     updateTutorialBadShotMovement(); // egg moves, checks hit
        else if (tutorialTimer < TUTORIAL_END_FRAME)    { /* Phase 6: pause after impact */ }
        else                                            endTutorial();                  // transition to opening
    }

    /**
     * Fires the egg directly toward the tutorial nest for the perfect-shot demo.
     * Direction is normalized so speed is always exactly TUTORIAL_SHOT_POWER.
     */
    private void fireTutorialPerfectShot() {
        double dx     = TUTORIAL_NEST_X - activeEgg.getX(); // horizontal distance to nest center
        double dy     = TUTORIAL_NEST_Y - activeEgg.getY(); // vertical distance to nest center
        double length = Math.sqrt(dx * dx + dy * dy);        // total distance (for normalization)

        // Normalize direction vector then scale to the desired shot power
        activeEgg.applyImpulsive(
                (dx / length) * TUTORIAL_SHOT_POWER,
                (dy / length) * TUTORIAL_SHOT_POWER);
    }

    /** Resets the egg to the tutorial start position for the bad-aim demo. */
    private void resetTutorialEgg() {
        activeEgg = new Egg(TUTORIAL_EGG_X, TUTORIAL_EGG_Y); // fresh egg at tutorial origin
    }

    /**
     * Fires the egg at an angle that will hit the obstacle block,
     * demonstrating what happens with bad aim.
     */
    private void fireTutorialBadShot() {
        activeEgg.applyImpulsive(TUTORIAL_BAD_VX, TUTORIAL_BAD_VY); // rightward + upward — aimed to collide with the block
    }

    /**
     * Moves the egg one frame and checks for a hit on the tutorial block.
     * On contact, bounces the egg back and jumps the timer past the hit frame
     * to prevent the collision from triggering repeatedly.
     */
    private void updateTutorialBadShotMovement() {
        activeEgg.move(); // apply velocity, friction, and boundary physics

        // AABB check: test whether any part of the egg overlaps the block rectangle
        boolean hitsBlock =
                activeEgg.getX() + Egg.WIDTH  > TUTORIAL_BLOCK_X &&               // egg right > block left
                        activeEgg.getX()              < TUTORIAL_BLOCK_X + TUTORIAL_BLOCK_W && // egg left < block right
                        activeEgg.getY() + Egg.HEIGHT > TUTORIAL_BLOCK_Y &&               // egg bottom > block top
                        activeEgg.getY()              < TUTORIAL_BLOCK_Y + TUTORIAL_BLOCK_H;  // egg top < block bottom

        if (hitsBlock) {
            tutorialHit = true;                                          // latch: GameDisplay will show the red flash
            activeEgg.applyImpulsive(TUTORIAL_BOUNCE_VX, TUTORIAL_BOUNCE_VY); // rebound: reverse direction with reduced speed
            tutorialTimer = TUTORIAL_BOUNCE_SKIP;                        // jump timer to skip past the hit frame
        }
    }

    /**
     * Ends the tutorial by placing the egg on the branch and
     * transitioning to the opening animation.
     */
    private void endTutorial() {
        activeEgg = new Egg(OPENING_EGG_X, OPENING_EGG_Y); // position egg on the tree branch
        gameState = STATE_OPENING;                           // start the opening animation
    }

    // ── Level loading ──────────────────────────────────────────────────────────

    /** Clears obstacles and loads all map 1 obstacles. */
    public void addLevel1Obstacles() {
        obstacles.clear();          // ensure the list is empty before adding new obstacles
        addLevel1Walls();           // solid bouncing walls
        addLevel1Ice();             // speed-boosting ice patches
        addLevel1GrassPatches();    // friction-increasing grass patches
    }

    /** Clears obstacles and loads all map 2 obstacles. */
    public void addLevel2Obstacles() {
        obstacles.clear();          // ensure the list is empty before adding new obstacles
        addLevel2Walls();           // more walls than map 1 for higher difficulty
        addLevel2Ice();             // ice patches in new positions
        addLevel2GrassPatches();    // grass patches in new positions
    }

    private void addLevel1Walls() {
        obstacles.add(new Wall(519,  79, 38, 316)); // vertical wall near the right side, upper area
        obstacles.add(new Wall(245, 632, 38, 290)); // vertical wall near the left side, lower area
    }

    private void addLevel2Walls() {
        obstacles.add(new Wall(200,   0, 25, 320)); // left vertical wall, top portion
        obstacles.add(new Wall(250, 500, 25, 300)); // left vertical wall, bottom portion
        obstacles.add(new Wall(475,   0, 25, 300)); // center-left vertical wall, top portion
        obstacles.add(new Wall(700,   0, 25, 200)); // center-right vertical wall, top portion
        obstacles.add(new Wall(475, 530, 25, 300)); // center-left vertical wall, bottom portion
        obstacles.add(new Wall(900,   0, 25, 600)); // far-right vertical wall
    }

    private void addLevel1Ice() {
        obstacles.add(new Ice(113, 105, 245, 197)); // large ice patch in the upper-left area
        obstacles.add(new Ice(396, 487, 283, 237)); // large ice patch in the center-right area
    }

    private void addLevel2Ice() {
        obstacles.add(new Ice(  0, 200, 200, 120)); // ice patch along the left edge
        obstacles.add(new Ice(275, 600, 200, 120)); // ice patch in the lower-center area
        obstacles.add(new Ice(500, 260, 200, 120)); // ice patch in the upper-center area
    }

    private void addLevel1GrassPatches() {
        obstacles.add(new GrassPatch( 90, 370, 180, 100)); // grass near the left wall
        obstacles.add(new GrassPatch(620, 190, 180, 100)); // grass in the upper-right area
        obstacles.add(new GrassPatch(680, 710, 180, 100)); // grass near the lower-right corner
    }

    private void addLevel2GrassPatches() {
        obstacles.add(new GrassPatch(390, 430, 125, 100)); // grass in the center area
        obstacles.add(new GrassPatch(720, 530, 128, 100)); // grass near the right wall
        obstacles.add(new GrassPatch(560, 720, 140, 100)); // grass near the bottom-center
    }

    // ── Game loop ──────────────────────────────────────────────────────────────

    /**
     * Main game loop. Runs at ~60 FPS until the program exits.
     * Each iteration updates game state then renders a frame.
     */
    public void run() {
        while (true) {
            update();         // advance all game objects and state machines by one frame
            window.render();  // draw the current frame to the screen
            try {
                Thread.sleep(FRAME_DELAY_MS); // pause to cap the frame rate at ~60 FPS
            } catch (InterruptedException e) { /* ignore — sleep interruption is non-critical */ }
        }
    }

    /** Entry point. */
    public static void main(String[] args) {
        new GameEngine().run(); // create the engine (which opens the window) and start the loop
    }
}
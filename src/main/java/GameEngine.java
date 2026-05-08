import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.Timer;

// Implement actionlistener to create animatinos
public class GameEngine {
    // Instance variables
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


    // Constants for state
    public static final int STATE_MENU = 0;
    public static final int STATE_TUTORIAL = 1;
    public static final int STATE_OPENING = 2;
    public static final int STATE_PLAYING = 3;

    // Constructor that gives access to everything
    public GameEngine(){
        gameState = STATE_PLAYING;
        gameState = STATE_MENU;
        tutorialTimer = 0;
        activeEgg = new Egg(600,345);
        meter = new ShotMeter();
        obstacles = new ArrayList<>();
        window = new GameDisplay(this, meter);
    }

    public ArrayList<Obstacle> getObstacles() {
        return obstacles;
    }

    // Updates the gamestate depending on egg
    public void update() {

            if (gameState == STATE_MENU) {
                // do nothing, just wait for input
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
                checkNestEntry();
            }
        }
    public void processShotDirect(double vx, double vy) {
        // Passes the pre-calculated velocity (already adjusted by the shot meter zone
        // in GameDisplay) directly to the egg with no additional scaling or modification,
        // immediately setting it in motion at the given speed and direction
        activeEgg.applyImpulsive(vx, vy);
    }

    private void runTutorial() {

        // 0–120 → Explain goal
        if (tutorialTimer < 120) {
            // just visuals
        }

        // 120–240 → show pulling back
        else if (tutorialTimer < 240) {
            // handled visually
        }

        // 240 → FIRE PERFECT SHOT
        else if (tutorialTimer == 240) {

            double targetX = 760; // nest position
            double targetY = 660;

            double dx = targetX - activeEgg.getX();
            double dy = targetY - activeEgg.getY();

            double length = Math.sqrt(dx * dx + dy * dy);

            dx /= length;
            dy /= length;

            // Controlled power so it lands nicely
            activeEgg.applyImpulsive(dx * 10, dy * 10);
        }

        // 240–360 → let it travel
        else if (tutorialTimer < 360) {
            activeEgg.move();
        }

        // 360 → RESET for bad example
        else if (tutorialTimer == 360) {
            activeEgg = new Egg(300, 500);
        }

        // 360–420 → explain obstacles
        else if (tutorialTimer < 420) {
            // pause for text
        }

        // 420–480 → drag again (bad angle)
        else if (tutorialTimer < 480) {
            // fake mouse handles this
        }

        // 480 → BAD SHOT INTO WALL
        else if (tutorialTimer == 480) {
            activeEgg.applyImpulsive(6, -4); // angled into obstacle
        }

        // 480–600 → movement + collision
        else if (tutorialTimer < 600) {
            activeEgg.move();

            double ex = activeEgg.getX();
            double ey = activeEgg.getY();

            // collision with your block at (450,450)
            if (ex + 40 > 450 && ex < 500 && ey + 55 > 450 && ey < 500) {
                activeEgg.applyImpulsive(-5, -6); // bounce back
                tutorialTimer = 560; // prevent spam bouncing
            }
        }

        // END
        // 600–780 → PAUSE AFTER SECOND SHOT
        else if (tutorialTimer < 780) {
            // do nothing, just let player see result
        }

// AFTER PAUSE → GO TO OPENING
        else {
            activeEgg = new Egg(600, 350);
            gameState = STATE_OPENING;
        }
    }

    public Egg getEgg() {
        return activeEgg;
    }

    public void processShot(double deltaX, double deltaY, double multiplier){
        // Multiplies the speed of velocity by the multiplier
        double vx = deltaX * multiplier;
        double vy = deltaY * multiplier;

        // Sets this new speed equal to the speed of the x and y speed of the egg
        activeEgg.applyImpulsive(vx, vy);
    }

    public int getGameState() {
        return gameState;
    }

    public void startTutorial() {
        tutorialTimer = 0;
        gameState = STATE_TUTORIAL;
    }

    public void skipToOpening() {
        gameState = STATE_OPENING;
    }

    public void checkCollision(){

    }
    public void checkNestEntry(){
        Rectangle nestBounds;
        if (currentMap == 1) {
            nestBounds = new Rectangle(736, 66, 200, 100); // map1 nest
        } else {
            nestBounds = new Rectangle(750, 650, 200, 100); // map2 nest
        }

        Rectangle eggBounds = new Rectangle((int)activeEgg.getX(), (int)activeEgg.getY(), 40, 55);

        if (nestBounds.intersects(eggBounds) && !activeEgg.isMoving()) {
            currentMap = currentMap == 1 ? 2 : 1; // toggle maps
            activeEgg = new Egg(300, 645); // reset egg to ground level
        }
    }


    // Add getter:
    public int getCurrentMap() {
        return currentMap;
    }

    public String getFinalResult(){
        return null;
    }

    // Timer for animation
    public void run() {
        // 90 milliseconds of delay
        while (true) {
            update();
            window.render();
            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {}
        }

    }

    public int getTutorialTimer() {
        return tutorialTimer;
    }

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

        //Right side walls
        obstacles.add(new Wall(200, 0, 25, 320));
        obstacles.add(new Wall(250, 500, 25, 300));
        obstacles.add((new Wall(475, 0, 25, 300)));

        //Left side walls
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private int tutorialTimer;

    // Constants for state
    public static final int STATE_MENU = 0;
    public static final int STATE_TUTORIAL = 1;
    public static final int STATE_OPENING = 2;
    public static final int STATE_PLAYING = 3;

    // Constructor that gives access to everything
    public GameEngine(){
        gameState = STATE_MENU;
        tutorialTimer = 0;
        activeEgg = new Egg(600,345);
        meter = new ShotMeter();
        window = new GameDisplay(this, meter);
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
            }
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

    }

    public String getFinalResult(){
        return null;
    }

    // Timer for animation
    public void run() {
        // 90 milliseconds of delay
        while (true) {
            update();
            window.render();   // 👈 custom render method
            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {}
        }

    }

    public int getTutorialTimer() {
        return tutorialTimer;
    }


    public static void main(String[] args) {
        GameEngine g = new GameEngine();
        g.run();
    }
}
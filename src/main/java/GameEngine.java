import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

// Implement actionlistener to create animatinos
public class GameEngine implements ActionListener{
    // Instance variables
    private GameDisplay window;
    private Egg activeEgg;
    private Level currentLevel;
    private int gameState;
    private double FRICTION;
    private int score;
    private ShotMeter meter;

    // Constants for state
    public static final int STATE_OPENING = 0;
    public static final int STATE_PLAYING = 1;

    // Constructor that gives access to everything
    public GameEngine(){
        gameState = STATE_OPENING;
        activeEgg = new Egg(600,345);
        meter = new ShotMeter();
        window = new GameDisplay(this, meter);
    }

    // Updates the gamestate depending on egg
    public void update() {
        if (gameState == STATE_OPENING) {
            activeEgg.updateOpening();

            if (activeEgg.getState() == Egg.STATE_LANDED) {
                gameState = STATE_PLAYING;
            }
        }
        else if( gameState == STATE_PLAYING){
            meter.update();
        }

        window.repaint();
    }

    public Egg getEgg() {
        return activeEgg;
    }

    public void processShot(double deltaX, double deltaY, double multiplier){

    }

    public int getGameState() {
        return gameState;
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
        Timer timer = new Timer(90, this);
        timer.start();

    }

    public void actionPerformed(ActionEvent e){

        update();
    }

    public static void main(String[] args) {
        GameEngine g = new GameEngine();
        g.run();
    }
}
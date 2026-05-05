import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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
    private ArrayList<Obstacle> obstacles;

    // Constants for state
    public static final int STATE_OPENING = 0;
    public static final int STATE_PLAYING = 1;

    // Constructor that gives access to everything
    public GameEngine(){
        gameState = STATE_PLAYING;
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
        // 90 milliseconds of delay
        Timer timer = new Timer(90, this);
        timer.start();

    }

    public void actionPerformed(ActionEvent e){

        update();
    }

    public void addLevel1Walls(){
        obstacles.clear();
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

    public static void main(String[] args) {
        GameEngine g = new GameEngine();
        g.run();
    }
}
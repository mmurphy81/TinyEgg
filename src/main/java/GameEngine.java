public class GameEngine {
    private GameDisplay window;
    private Egg activeEgg;
    private Level currentLevel;
    private int gameState;
    private double FRICTION;
    private int score;

    public GameEngine(){
        window = new GameDisplay(this);
    }

    public void update(){

    }

    public void processShot(double deltaX, double deltaY, double multiplier){

    }

    public void checkCollision(){

    }
    public void checkNestEntry(){

    }

    public String getFinalResult(){
        return null;
    }
    public void run() {

    }
    public static void main(String[] args) {
        GameEngine g = new GameEngine();
        g.run();
    }
}

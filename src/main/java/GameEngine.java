public class GameEngine {
    private GameDisplay window;
    private Egg activeEgg;
    private Level currentLevel;
    private int gameState;
    private double friction;
    private int score;

    public GameEngine() {
        currentLevel = new Level(3);
        activeEgg = new Egg(currentLevel.getStartPoint().x, currentLevel.getStartPoint().y);
        window = new GameDisplay(this);
    }

    public Egg getEgg() {
        return activeEgg;
    }

    public Level getLevel() {
        return currentLevel;
    }

    public void update() {

    }

    public void processShot(double deltaX, double deltaY, double multiplier) {

    }

    public void checkCollision() {

    }

    public void checkNestEntry() {

    }

    public String getFinalResult() {
        return null;
    }

    public void run() {

    }

    public static void main(String[] args) {
        GameEngine g = new GameEngine();
        g.run();
    }
}
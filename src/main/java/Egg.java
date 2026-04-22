public class Egg {
    private double x;
    private double y;
    private double velX;
    private double velY;
    private int health;
    private int strokes;
    public static final int RADIUS = 15;

    public Egg(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.velX = 0;
        this.velY = 0;
        this.health = 100;
        this.strokes = 0;
    }

    public void move() {

    }

    public void applyImpulse(double vx, double vy) {

    }

    public void reduceHealth(int amount) {

    }

    public boolean isCracked() {
        return health <= 0;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getVelX() {
        return velX;
    }

    public double getVelY() {
        return velY;
    }

    public int getHealth() {
        return health;
    }

    public int getStrokes() {
        return strokes;
    }
}

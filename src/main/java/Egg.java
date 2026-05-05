import java.awt.*;

public class Egg {
    // Instance variables
    private double x;
    private double y;
    private double velX;
    private double velY;
    private int health;
    private int strokes;
    private static final int RADIUS = 15;
    private boolean isFalling;
    private int state;
    private Obstacle obstacle;


    // Constants for the dimensinos of the egg and of the ground and gravity
    public static final int WIDTH = 40;
    public static final int HEIGHT = 55;
    private static final int GRAVITY = 1;
    private static final int FLOOR = 700;

    // Game states
    public static final int STATE_IDLE = 0;
    public static final int STATE_WOBBLE = 1;
    public static final int STATE_SHAKE = 2;
    public static final int STATE_CRACK = 3;
    public static final int STATE_FALL = 4;
    public static final int STATE_LANDED = 5;

    // Timer needed for animation
    private int animationTimer;

    // Constructor
    public Egg(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.velY = 0;
        this.state = STATE_WOBBLE;
    }

    // Updates the opening for the egg and makes it fall
    public void updateOpening() {
        // Add one to the animation timer
        animationTimer += 1;

        // Go through each state
        switch(state) {
            // If wobbling, first state
            case STATE_WOBBLE:

                // Wait, and then switch shake and restart timer
                if (animationTimer > 60) {
                    state = STATE_SHAKE;
                    animationTimer = 0;
                }
                break;

            // Next stage
            case STATE_SHAKE:

                // Wait a little bit longer and switch state
                if(animationTimer > 100){
                    state = STATE_CRACK;
                    animationTimer = 0;
                }
                break;

            // Get everything ready to fall
            case STATE_CRACK:
                if(animationTimer > 30){
                    state = STATE_FALL;
                    velY = 0;
                }
                break;

            // Sets it so gravity makes the egg accelerate as it falls to the ground
            case STATE_FALL:
                velY += GRAVITY;
                y += velY;

                // If the egg has hit the ground, then we set it onto the ground so that it is not below
                // Set velocity to 0, and then set it to landed
                if(y + HEIGHT >= FLOOR){
                    y = FLOOR - HEIGHT;
                    velY = 0;
                    state = STATE_LANDED;
                }

                break;
        }
    }

    public void draw(Graphics g) {
        int drawX = (int)(x);

        // Draws the egg
        g.setColor(Color.WHITE);
        g.fillOval(drawX, (int)y, WIDTH, HEIGHT);

        g.setColor(Color.black);
        g.drawOval(drawX, (int)y, WIDTH, HEIGHT);

        // draw crack during crack + fall
        if(state == STATE_LANDED){
            drawCrack(g, drawX, (int)y);

        }
    }

    public void drawCrack(Graphics g, int x, int y){
        // Draws the crack on the egg
        g.setColor(Color.BLACK);
        g.drawLine(x + 18, y + 5, x + 12, y + 30);
        g.drawLine(x + 22, y + 8, x + 28, y + 35);
        g.drawLine(x + 15, y + 15, x + 25, y + 25);
    }

    public int getState(){
        return state;
    }

    public void move(){

    }

    public void applyImpulsive(double vx, double vy){

    }

    public void reduceHealth(int amount){

    }

    public boolean isCracked(){
        return false;
    }

    public double getX(){
        return x;
    }

    public double getY() {
        return y;
    }

    public int getHealth() {
        return health;
    }

    public void whenCollided(){
        if (obstacle.hasCollided(this)){
            if (obstacle instanceof Wall){
                // TODO fix this
                x*=-1;
                y*=-1;
            }
            if(obstacle instanceof Ice){
                velX*=10;
                velY*=10;
            }
        }
    }

}
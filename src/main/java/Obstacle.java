import java.awt.*;

public abstract class Obstacle {
    private int x;
    private int y;
    private int width;
    private int height;

    public Obstacle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Rectangle getBounds() {
        return null;
    }

    //Checks to see if the egg has made contact with any part of the obstacle
    public boolean hasCollided(Egg egg) {
        double ex1 = egg.getX();
        double ex2 = egg.getX() + egg.WIDTH;
        double ey1 = egg.getY();
        double ey2 = egg.getY() + egg.HEIGHT;

        int wallx2 = x + width;
        int wally2 = y + height;
        //Checks if the coordinates of the egg are colliding with the coordinates of the wall
        if (ex1 < wallx2 && ey1 < wally2 && ex2 > x && ey2 > y) {
            return true;
        }
        return false;
    }
    public abstract void respondToCollision(Egg egg);

    //Only gets called if you know the egg has collided
    public boolean hitTopOrBottom(Egg egg){
        return false;
    }

    public void draw(Graphics g) {

    }

}
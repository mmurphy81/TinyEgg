import java.awt.*;

public class Wall extends Obstacle{
    public Wall(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public boolean hasCollided(Egg egg) {
        return super.hasCollided(egg);
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(GameDisplay.CORAL);
        g.fillRect(getX(), getY(), getWidth(), getHeight());
    }

    public void respondToCollision(Egg egg) {
        double vx = egg.getVelX();
        double vy = egg.getVelY();
        double ex = egg.getX();
        double ey = egg.getY();

        double overlapLeft   = (ex + Egg.WIDTH)        - getX();
        double overlapRight  = (getX() + getWidth())   - ex;
        double overlapTop    = (ey + Egg.HEIGHT)        - getY();
        double overlapBottom = (getY() + getHeight())  - ey;

        double minOverlap = Math.min(Math.min(overlapLeft, overlapRight),
                Math.min(overlapTop, overlapBottom));

        if (minOverlap == overlapLeft || minOverlap == overlapRight) {
            egg.applyImpulsive(-vx * 0.6, vy * 0.6);
        } else {
            egg.applyImpulsive(vx * 0.6, -vy * 0.6);
        }
}
}

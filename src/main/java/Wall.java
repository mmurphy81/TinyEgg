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

        // How far the egg has poked into each side of this wall.
        double overlapLeft   = (ex + Egg.WIDTH)       - getX();
        double overlapRight  = (getX() + getWidth())  - ex;
        double overlapTop    = (ey + Egg.HEIGHT)      - getY();
        double overlapBottom = (getY() + getHeight()) - ey;

        // Whichever overlap is smallest tells us which face the egg most
        // recently entered through.
        double minH = Math.min(overlapLeft, overlapRight);
        double minV = Math.min(overlapTop, overlapBottom);

        double newVX = vx;
        double newVY = vy;

        if (minH < minV) {
            // Side hit. Flip X velocity, BUT only if the egg is actually
            // moving INTO the wall. Without this direction check the old
            // code would re-flip every frame and the egg oscillated in
            // place, never escaping.
            if (overlapLeft < overlapRight && vx > 0) {
                newVX = -vx * 0.6;
            } else if (overlapLeft >= overlapRight && vx < 0) {
                newVX = -vx * 0.6;
            }
            newVY = vy * 0.95;
        } else {
            // Top or bottom hit.
            if (overlapTop < overlapBottom && vy > 0) {
                newVY = -vy * 0.6;
            } else if (overlapTop >= overlapBottom && vy < 0) {
                newVY = -vy * 0.6;
            }
            newVX = vx * 0.95;
        }

        egg.applyImpulsive(newVX, newVY);
    }
}

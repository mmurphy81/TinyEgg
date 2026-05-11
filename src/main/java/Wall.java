import java.awt.*;

public class Wall extends Obstacle{
    private boolean eggInside = false;
    public Wall(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public boolean hasCollided(Egg egg) {
        boolean colliding = super.hasCollided(egg);
        if (!colliding) {
            eggInside = false;
        }
        return colliding;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(GameDisplay.CORAL);
        g.fillRect(getX(), getY(), getWidth(), getHeight());
    }

    public void respondToCollision(Egg egg) {
        if (eggInside) return;
        eggInside = true;
        double evx = egg.getVelX();
        double evy = egg.getVelY();
        double ex = egg.getX();
        double ey = egg.getY();

        double impactSpeed = Math.sqrt(evx * evx + evy * evy);
        egg.reduceHealth(Egg.impactDamage(impactSpeed));

        double overlapLeft   = (ex + Egg.WIDTH)       - getX();
        double overlapRight  = (getX() + getWidth())  - ex;
        double overlapTop    = (ey + Egg.HEIGHT)      - getY();
        double overlapBottom = (getY() + getHeight()) - ey;

        double minOverlap = Math.min(Math.min(overlapLeft, overlapRight),
                Math.min(overlapTop,  overlapBottom));

        if (minOverlap == overlapLeft || minOverlap == overlapRight) {
            // Hit left or right side — reverse X, heavily damp Y
            egg.applyImpulsive(-evx * 0.1, evy * 0.1);
            if (minOverlap == overlapLeft) {
                egg.setX(getX() - Egg.WIDTH - 1);
            } else {
                egg.setX(getX() + getWidth() + 1);
            }
        } else {
            // Hit top or bottom — reverse Y, heavily damp X
            egg.applyImpulsive(evx * 0.1, -evy * 0.1);
            if (minOverlap == overlapTop) {
                egg.setY(getY() - Egg.HEIGHT - 1);
            } else {
                egg.setY(getY() + getHeight() + 1);
            }
        }
}
}

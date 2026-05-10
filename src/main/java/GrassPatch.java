import java.awt.*;

public class GrassPatch extends Obstacle {
    public GrassPatch(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public boolean hasCollided(Egg egg) {
        return super.hasCollided(egg);
    }

    @Override
    //Draws the grass
    public void draw(Graphics g) {
        g.setColor(GameDisplay.GRASS);
        g.fillRect(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void respondToCollision(Egg egg) {
        // Grass slows the egg by scaling its velocity, not by subtracting.
        // The original "velX-2, velY-2" actually ACCELERATED the egg in the
        // negative direction when it was moving leftward or upward
        // (e.g., velX = -1, minus 2 = -3, faster the wrong way).
        // Scaling by 0.85 cuts speed by ~15% per frame on grass.
        egg.applyImpulsive(egg.getVelX() * 0.85, egg.getVelY() * 0.85);
    }
}
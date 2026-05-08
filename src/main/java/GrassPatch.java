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
        egg.applyImpulsive(egg.getVelX() -2, egg.getVelY() -2);
    }

}

import java.awt.*;

public class GrassPatch extends Obstacle {
    private boolean eggInside = false;

    public GrassPatch(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public boolean hasCollided(Egg egg) {
        if (!super.hasCollided(egg)) {
            eggInside = false; // reset when egg leaves
            return false;
        }
        return true;
    }

    @Override
    //Draws the grass
    public void draw(Graphics g) {
        g.setColor(GameDisplay.GRASS);
        g.fillRect(getX(), getY(), getWidth(), getHeight());
    }
    @Override
    public void respondToCollision(Egg egg) {
        if (!eggInside) {
            egg.slowDown(0.9); // only applied once on entry
            eggInside = true;
        }
    }
}



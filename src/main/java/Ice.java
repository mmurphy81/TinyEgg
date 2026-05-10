import java.awt.*;

public class Ice extends Obstacle {
    public Ice(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public boolean hasCollided(Egg egg) {
        return super.hasCollided(egg);
    }

    @Override
    //Draws the ice
    public void draw(Graphics g) {
        g.setColor(GameDisplay.ICE);
        g.fillRect(getX(), getY(), getWidth(), getHeight());
    }
    @Override
    public void respondToCollision(Egg egg) {
        // Ice doesn't actively push the egg. The original code added +2 to
        // velX and DOUBLED velY every frame the egg was on ice — that
        // caused exponential acceleration and shot the egg off-screen.
        // For a correct slippery feel, ice should reduce friction (not add
        // force). Easiest version: just preserve current velocity here.
    }
}
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
    public void draw(Graphics g) {
        super.draw(g);
    }
}
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
}

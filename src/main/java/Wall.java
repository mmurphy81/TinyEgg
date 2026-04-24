import java.awt.*;

public class Wall extends Obstacle {

    public Wall(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void onCollision(Egg egg) {

    }

    @Override
    public void draw(Graphics g) {
        g.setColor(new Color(90, 90, 95));
        g.fillRect(x, y, width, height);
        g.setColor(new Color(50, 50, 55));
        g.drawRect(x, y, width, height);
    }
}
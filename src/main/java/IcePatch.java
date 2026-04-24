import java.awt.*;

public class IcePatch extends Obstacle {

    public IcePatch(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void onCollision(Egg egg) {

    }

    @Override
    public void draw(Graphics g) {
        g.setColor(new Color(180, 220, 245));
        g.fillRect(x, y, width, height);
        g.setColor(new Color(140, 180, 215));
        g.drawRect(x, y, width, height);
    }
}
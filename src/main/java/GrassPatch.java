import java.awt.*;

public class GrassPatch extends Obstacle {

    public GrassPatch(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void onCollision(Egg egg) {

    }

    @Override
    public void draw(Graphics g) {
        g.setColor(new Color(30, 90, 40));
        g.fillRect(x, y, width, height);
        g.setColor(new Color(20, 65, 28));
        for (int i = x + 6; i < x + width - 2; i += 10) {
            g.drawLine(i, y + 6, i, y + height - 4);
        }
    }
}
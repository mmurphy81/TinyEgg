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
        // Ice reduces friction — egg maintains more of its speed each frame.
        // We do NOT use applyImpulsive here because calling it every frame
        // the egg overlaps the ice compounds unpredictably and causes bouncing.
        // Instead, we counteract the egg's natural friction by slightly boosting
        // its current velocity, capped so it can't accelerate beyond a safe speed.
        double vx = egg.getVelX();
        double vy = egg.getVelY();
        double speed = Math.sqrt(vx * vx + vy * vy);

        if (speed > 0.1 && speed < 12) {
            // Nudge speed up by 2% to simulate low friction, preserving direction
            egg.setVelX(vx * 1.02);
            egg.setVelY(vy * 1.02);
        }
    }
}
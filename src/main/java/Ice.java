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
        // Calculate the egg's current speed from both velocity components.
        // We use Pythagorean theorem here because velX and velY are at right angles —
        // speed is the length of the diagonal, not just velX + velY
        double speed = Math.sqrt(egg.getVelX() * egg.getVelX() + egg.getVelY() * egg.getVelY());
        // Only apply the ice boost if the egg is moving slowly.
        // Without this check, the boost would apply every frame the egg overlaps
        // the ice, compounding each time and eventually launching it off screen
        if (speed < 8) {
            // Multiply velocity by 1.05 (a 5% speed increase) rather than adding
            // a flat amount. Adding flat values like +1 ignores direction — it would
            // push the egg diagonally even if it was only moving horizontally.
            // Multiplying preserves the existing direction and scales proportionally
            egg.applyImpulsive(egg.getVelX() * 1.05, egg.getVelY() * 1.05);
        }
    }
}
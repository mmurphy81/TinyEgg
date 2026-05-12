// Programmers: Ryan Weinswig, Mera Murphy, Alberto Perez-Jacome
// Date: May 11, 2025
// Project: Tiny Egg
// Description: A grass patch obstacle. Applies extra friction to the egg each
//              frame it overlaps, making it slow down faster than on bare ground.

import java.awt.*;

public class GrassPatch extends Obstacle {

    // Extra friction multiplier applied each frame while the egg is on grass.
    // Combined with the egg's natural 0.98 per-frame friction: 0.98 × 0.96 ≈ 0.94 net
    // This makes the egg decelerate noticeably faster than on open ground
    private static final double GRASS_FRICTION = 0.96;

    // Grass fill color — matches the map background so patches blend in visually
    private static final Color GRASS_COLOR = new Color(34, 139, 34);

    // ── Constructor ────────────────────────────────────────────────────────────

    /**
     * @param x      left edge
     * @param y      top edge
     * @param width  horizontal size
     * @param height vertical size
     */
    public GrassPatch(int x, int y, int width, int height) {
        super(x, y, width, height); // delegate position/size storage to the Obstacle base class
    }

    // ── Collision ──────────────────────────────────────────────────────────────

    /**
     * Slows the egg by multiplying its velocity by GRASS_FRICTION each frame.
     * Direction is preserved; only speed is reduced.
     *
     * @param egg the egg moving over this grass patch
     */
    @Override
    public void respondToCollision(Egg egg) {
        egg.slowDown(GRASS_FRICTION); // apply the extra friction on top of the egg's own 0.98 friction
    }

    // ── Drawing ────────────────────────────────────────────────────────────────

    /**
     * Draws the grass patch as a solid green rectangle.
     *
     * @param g graphics context
     */
    @Override
    public void draw(Graphics g) {
        g.setColor(GRASS_COLOR);
        g.fillRect(getX(), getY(), getWidth(), getHeight()); // filled rectangle at the patch's stored position
    }
}
// Programmers: Ryan Weinswig, Mera Murphy, Alberto Perez-Jacome
// Date: May 11, 2025
// Project: Tiny Egg
// Description: An ice patch obstacle. Counteracts the egg's natural friction
//              each frame it overlaps, making the egg slide faster and farther
//              than it would on normal ground.

import java.awt.*;

public class Ice extends Obstacle {

    // Speed multiplier applied each frame the egg overlaps ice.
    // 1.02 nudges speed up by 2% per frame, which counteracts the egg's built-in 0.98 friction
    // and produces a net acceleration effect — the egg slides instead of slowing down
    private static final double SPEED_BOOST     = 1.02;

    // Upper speed limit prevents the compounding boost from accelerating the egg to extreme values
    private static final double MAX_SPEED       = 12.0;

    // Minimum speed threshold — no boost is applied if the egg is barely drifting
    // This prevents ice from launching a nearly-stopped egg
    private static final double MIN_SPEED       = 0.1;

    // Ice fill color — bright sky blue so players can easily identify it
    private static final Color ICE_COLOR = new Color(0, 191, 255);

    // ── Constructor ────────────────────────────────────────────────────────────

    /**
     * @param x      left edge
     * @param y      top edge
     * @param width  horizontal size
     * @param height vertical size
     */
    public Ice(int x, int y, int width, int height) {
        super(x, y, width, height); // delegate position/size storage to the Obstacle base class
    }

    // ── Collision ──────────────────────────────────────────────────────────────

    /**
     * Nudges the egg's speed up slightly each frame it overlaps the ice.
     * Multiplies velocity by SPEED_BOOST, preserving direction.
     * Only applies when the egg is within the MIN_SPEED–MAX_SPEED range
     * to prevent runaway acceleration.
     *
     * @param egg the egg sliding over this ice patch
     */
    @Override
    public void respondToCollision(Egg egg) {
        double vx    = egg.getVelX();
        double vy    = egg.getVelY();
        double speed = Math.sqrt(vx * vx + vy * vy); // compute total speed from components

        // Only boost if the egg is moving fast enough to feel it, but not already at max
        if (speed > MIN_SPEED && speed < MAX_SPEED) {
            egg.setVelX(vx * SPEED_BOOST); // scale horizontal component up by boost factor
            egg.setVelY(vy * SPEED_BOOST); // scale vertical component up by boost factor
        }
    }

    // ── Drawing ────────────────────────────────────────────────────────────────

    /**
     * Draws the ice patch as a solid light-blue rectangle.
     *
     * @param g graphics context
     */
    @Override
    public void draw(Graphics g) {
        g.setColor(ICE_COLOR);
        g.fillRect(getX(), getY(), getWidth(), getHeight()); // filled rectangle at the patch's stored position
    }
}
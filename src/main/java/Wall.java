// Programmers: Ryan Weinswig, Mera Murphy, Alberto Perez-Jacome
// Date: May 11, 2025
// Project: Tiny Egg
// Description: A solid wall obstacle. Determines which face the egg hit by
//              finding the smallest overlap on each axis, then bounces the egg
//              off that face with heavy damping so it stops quickly.

import java.awt.*;

public class Wall extends Obstacle {

    // Velocity multiplier applied after bouncing — keeps the egg from rebounding far
    // At 0.1, the egg retains only 10% of its speed, coming to rest in a few frames
    private static final double BOUNCE_DAMP = 0.1;

    // Wall fill color — coral red so walls are visually distinct from other obstacles
    private static final Color WALL_COLOR = new Color(220, 80, 80);

    // Tracks whether the egg is currently inside this wall to prevent
    // respondToCollision from firing multiple times in one overlap event
    private boolean eggInside = false;

    // ── Constructor ────────────────────────────────────────────────────────────

    /**
     * @param x      left edge
     * @param y      top edge
     * @param width  horizontal size
     * @param height vertical size
     */
    public Wall(int x, int y, int width, int height) {
        super(x, y, width, height); // delegate position/size storage to the Obstacle base class
    }

    // ── Collision ──────────────────────────────────────────────────────────────

    /**
     * Extends the base check to also reset eggInside when the egg leaves,
     * so the next entry triggers a fresh collision response.
     */
    @Override
    public boolean hasCollided(Egg egg) {
        boolean colliding = super.hasCollided(egg); // use AABB test from Obstacle
        if (!colliding) eggInside = false;           // egg has exited — allow next entry to trigger a response
        return colliding;
    }

    /**
     * Bounces the egg off the face it hit.
     * The hit face is determined by the smallest overlap between the egg and wall
     * on each of the four sides. Velocity is reversed on the collision axis and
     * damped heavily. The egg is nudged 1 pixel outside the wall to prevent
     * it from tunneling through on the next frame.
     *
     * @param egg the egg that hit this wall
     */
    @Override
    public void respondToCollision(Egg egg) {
        if (eggInside) return; // already processing this overlap — skip to avoid double-bounce
        eggInside = true;      // latch: mark that the egg is inside so we don't bounce it again

        // Cache current velocity and position before modifying anything
        double evx = egg.getVelX();
        double evy = egg.getVelY();
        double ex  = egg.getX();
        double ey  = egg.getY();

        // Compute how far the egg has penetrated each of the four faces
        double overlapLeft   = (ex + Egg.WIDTH)        - getX();          // egg right edge past wall left face
        double overlapRight  = (getX() + getWidth())   - ex;              // wall right face past egg left edge
        double overlapTop    = (ey + Egg.HEIGHT)       - getY();          // egg bottom edge past wall top face
        double overlapBottom = (getY() + getHeight())  - ey;              // wall bottom face past egg top edge

        // The face with the smallest overlap is the one the egg entered through
        double minOverlap = Math.min(
                Math.min(overlapLeft, overlapRight),
                Math.min(overlapTop,  overlapBottom));

        // Bounce off whichever face has the smallest penetration depth
        if (minOverlap == overlapLeft) {
            // Egg came from the left — reverse horizontal velocity and push egg left of the wall
            egg.applyImpulsive(-evx * BOUNCE_DAMP, evy * BOUNCE_DAMP);
            egg.setX(getX() - Egg.WIDTH - 1); // nudge 1px away to prevent tunneling next frame
        } else if (minOverlap == overlapRight) {
            // Egg came from the right — reverse horizontal velocity and push egg right of the wall
            egg.applyImpulsive(-evx * BOUNCE_DAMP, evy * BOUNCE_DAMP);
            egg.setX(getX() + getWidth() + 1); // nudge 1px away to prevent tunneling next frame
        } else if (minOverlap == overlapTop) {
            // Egg came from above — reverse vertical velocity and push egg above the wall
            egg.applyImpulsive(evx * BOUNCE_DAMP, -evy * BOUNCE_DAMP);
            egg.setY(getY() - Egg.HEIGHT - 1); // nudge 1px away to prevent tunneling next frame
        } else {
            // Egg came from below — reverse vertical velocity and push egg below the wall
            egg.applyImpulsive(evx * BOUNCE_DAMP, -evy * BOUNCE_DAMP);
            egg.setY(getY() + getHeight() + 1); // nudge 1px away to prevent tunneling next frame
        }
    }

    // ── Drawing ────────────────────────────────────────────────────────────────

    /**
     * Draws the wall as a solid coral rectangle.
     *
     * @param g graphics context
     */
    @Override
    public void draw(Graphics g) {
        g.setColor(WALL_COLOR);
        g.fillRect(getX(), getY(), getWidth(), getHeight()); // filled rectangle at the wall's stored position
    }
}
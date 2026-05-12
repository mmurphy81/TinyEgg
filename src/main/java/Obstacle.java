// Programmers: Ryan Weinswig, Mera Murphy, Alberto Perez-Jacome
// Date: May 11, 2025
// Project: Tiny Egg
// Description: Abstract base class for all obstacles (Wall, Ice, GrassPatch).
//              Provides shared position/size storage, a generic AABB collision
//              check, and abstract methods each subclass must implement.

import java.awt.*;

public abstract class Obstacle {

    // ── Position and size ──────────────────────────────────────────────────────
    // All four fields are stored here so every subclass can share them
    // without duplicating code. Subclasses read them through the getters below.
    private int x;      // left edge of the obstacle in screen coordinates
    private int y;      // top edge of the obstacle in screen coordinates
    private int width;  // horizontal size in pixels
    private int height; // vertical size in pixels

    // ── Constructor ────────────────────────────────────────────────────────────

    /**
     * @param x      left edge of the obstacle
     * @param y      top edge of the obstacle
     * @param width  horizontal size in pixels
     * @param height vertical size in pixels
     */
    public Obstacle(int x, int y, int width, int height) {
        this.x      = x;      // store left edge
        this.y      = y;      // store top edge
        this.width  = width;  // store width
        this.height = height; // store height
    }

    // ── Collision detection ────────────────────────────────────────────────────

    /**
     * Returns true if the egg's bounding box overlaps this obstacle.
     * Uses axis-aligned bounding box (AABB) intersection — the simplest and most
     * efficient test for rectangular objects that don't rotate.
     * Two rectangles overlap if and only if neither separates on either axis.
     *
     * @param egg the egg to test
     * @return true if overlapping
     */
    public boolean hasCollided(Egg egg) {
        double eggLeft   = egg.getX();              // left edge of the egg
        double eggRight  = egg.getX() + Egg.WIDTH;  // right edge of the egg
        double eggTop    = egg.getY();              // top edge of the egg
        double eggBottom = egg.getY() + Egg.HEIGHT; // bottom edge of the egg

        int obsRight  = x + width;  // right edge of this obstacle
        int obsBottom = y + height; // bottom edge of this obstacle

        // Returns true only when the egg overlaps on both the horizontal and vertical axes
        return eggLeft < obsRight && eggRight > x
                && eggTop  < obsBottom && eggBottom > y;
    }

    // ── Abstract interface ─────────────────────────────────────────────────────

    /**
     * Called when the egg has collided with this obstacle.
     * Each subclass defines its own physical response:
     *   Wall      — bounces the egg off the hit face with heavy damping
     *   Ice       — boosts the egg's speed to counteract friction
     *   GrassPatch — slows the egg with extra friction
     *
     * @param egg the egg that collided
     */
    public abstract void respondToCollision(Egg egg);

    /**
     * Draws this obstacle to the screen.
     * Each subclass provides its own visual style and color.
     *
     * @param g graphics context
     */
    public abstract void draw(Graphics g);

    // ── Getters ────────────────────────────────────────────────────────────────

    public int getX()      { return x; }      // left edge of the obstacle
    public int getY()      { return y; }      // top edge of the obstacle
    public int getWidth()  { return width; }  // horizontal size
    public int getHeight() { return height; } // vertical size
}
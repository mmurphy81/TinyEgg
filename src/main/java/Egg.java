// Programmers: Ryan Weinswig, Mera Murphy, Alberto Perez-Jacome
// Date: May 11, 2025
// Project: Tiny Egg
// Description: Represents the egg the player shoots. Manages position, velocity,
//              friction, boundary collisions, drawing, and the opening animation
//              state machine (wobble → shake → crack → fall → landed).

import java.awt.*;

public class Egg {

    // ── Physics constants ──────────────────────────────────────────────────────
    private static final double FRICTION     = 0.98; // velocity multiplier applied every frame — slows the egg gradually
    private static final int    GRAVITY      = 1;    // pixels added to velY each frame during the fall phase
    private static final int    FLOOR        = 945;  // y-coordinate at which the egg is considered to have hit the ground
    private static final int    WALL_LEFT    = 0;    // left screen boundary
    private static final int    WALL_RIGHT   = 1000; // right screen boundary
    private static final int    WALL_TOP     = 0;    // top screen boundary

    // Velocity is multiplied by this value when the egg bounces off a screen edge
    // Low value (0.2) means the egg loses 80% of its speed on contact — stops quickly
    private static final double BOUNCE_DAMP  = 0.2;

    // Velocity components below this value are snapped to zero to stop micro-sliding
    private static final double STOP_THRESH  = 0.05;

    // ── Egg dimensions (public so obstacles and engine can read them) ──────────
    public static final int WIDTH  = 40; // egg bounding box width in pixels
    public static final int HEIGHT = 55; // egg bounding box height in pixels

    // ── Opening animation state constants ──────────────────────────────────────
    // These states drive the egg's behavior during the opening cinematic
    public static final int STATE_IDLE   = 0; // not currently animating
    public static final int STATE_WOBBLE = 1; // egg rocks gently on the branch
    public static final int STATE_SHAKE  = 2; // egg shakes harder before cracking
    public static final int STATE_CRACK  = 3; // crack lines appear on the egg
    public static final int STATE_FALL   = 4; // egg falls downward with gravity
    public static final int STATE_LANDED = 5; // egg has hit the floor and stopped

    // Duration of each opening animation phase in frames (~60 FPS)
    private static final int WOBBLE_FRAMES = 60;  // ~1 second of gentle rocking
    private static final int SHAKE_FRAMES  = 100; // ~1.7 seconds of harder shaking
    private static final int CRACK_FRAMES  = 30;  // ~0.5 seconds of crack display

    // ── Drawing colors ─────────────────────────────────────────────────────────
    private static final Color EGG_FILL    = Color.WHITE; // egg body fill
    private static final Color EGG_OUTLINE = Color.BLACK; // egg body outline
    private static final Color CRACK_COLOR = Color.BLACK; // crack lines drawn on the egg

    // ── Instance state ─────────────────────────────────────────────────────────
    private double x;              // left edge of the egg in screen coordinates
    private double y;              // top edge of the egg in screen coordinates
    private double velX;           // horizontal velocity in pixels per frame (positive = right)
    private double velY;           // vertical velocity in pixels per frame (positive = down)
    private int    state;          // current animation/physics state (one of the STATE_* constants)
    private int    animationTimer; // frame counter for the opening animation phases

    // ── Constructor ────────────────────────────────────────────────────────────

    /**
     * Creates an egg at the given position with zero velocity.
     * Starts in STATE_WOBBLE so the opening animation can begin immediately.
     *
     * @param startX left edge of the egg
     * @param startY top edge of the egg
     */
    public Egg(double startX, double startY) {
        this.x     = startX;       // set initial horizontal position
        this.y     = startY;       // set initial vertical position
        this.velX  = 0;            // egg starts at rest
        this.velY  = 0;            // no vertical motion until fired or fallen
        this.state = STATE_WOBBLE; // ready to begin the opening animation
    }

    // ── Opening animation ──────────────────────────────────────────────────────

    /**
     * Advances the opening animation one frame.
     * State machine: WOBBLE → SHAKE → CRACK → FALL → LANDED.
     * Called once per frame during STATE_OPENING.
     */
    public void updateOpening() {
        animationTimer++; // increment the per-phase frame counter

        switch (state) {
            case STATE_WOBBLE:
                // Wait for the wobble duration to expire before advancing
                if (animationTimer > WOBBLE_FRAMES) {
                    state = STATE_SHAKE;  // transition: wobble → shake
                    animationTimer = 0;   // reset counter for the next phase
                }
                break;

            case STATE_SHAKE:
                // Wait for the shake duration to expire before advancing
                if (animationTimer > SHAKE_FRAMES) {
                    state = STATE_CRACK;  // transition: shake → crack
                    animationTimer = 0;   // reset counter for the next phase
                }
                break;

            case STATE_CRACK:
                // Wait for the crack display duration before the egg actually falls
                if (animationTimer > CRACK_FRAMES) {
                    state = STATE_FALL;   // transition: crack → fall
                    velY  = 0;            // ensure vertical velocity starts at zero
                }
                break;

            case STATE_FALL:
                applyGravityAndFall(); // apply gravity each frame until landing
                break;
        }
    }

    /**
     * Applies gravity each frame during the fall phase and detects landing.
     * When the egg hits the floor it snaps to the surface and stops.
     */
    private void applyGravityAndFall() {
        velY += GRAVITY; // accelerate downward each frame
        y    += velY;    // move the egg by its current velocity

        // Check if the bottom of the egg has reached or passed the floor
        if (y + HEIGHT >= FLOOR) {
            y     = FLOOR - HEIGHT; // snap top edge so the bottom sits exactly on the floor
            velY  = 0;              // stop vertical movement
            state = STATE_LANDED;   // signal that the opening animation is complete
        }
    }

    // ── Gameplay movement ──────────────────────────────────────────────────────

    /**
     * Moves the egg one frame during gameplay.
     * Applies velocity, applies friction, snaps near-zero velocity to zero,
     * and bounces off all four screen boundaries with heavy damping.
     */
    public void move() {
        x += velX; // advance horizontal position by current velocity
        y += velY; // advance vertical position by current velocity

        applyFriction();         // reduce speed slightly every frame
        snapStoppedVelocity();   // zero out components that are nearly stopped
        enforceBoundaries();     // keep the egg within the screen and bounce off edges
    }

    /** Multiplies both velocity components by FRICTION each frame. */
    private void applyFriction() {
        velX *= FRICTION; // slow down horizontal movement
        velY *= FRICTION; // slow down vertical movement
    }

    /** Zeroes out velocity components that have fallen below the stop threshold. */
    private void snapStoppedVelocity() {
        if (Math.abs(velX) < STOP_THRESH) velX = 0; // prevent micro-sliding left/right
        if (Math.abs(velY) < STOP_THRESH) velY = 0; // prevent micro-sliding up/down
    }

    /**
     * Keeps the egg inside the screen. On contact with any edge, the egg is
     * repositioned flush with that edge and its velocity is reversed and damped
     * so it rolls to a stop quickly rather than bouncing repeatedly.
     */
    private void enforceBoundaries() {
        // Left wall: egg left edge goes past x=0
        if (x < WALL_LEFT) {
            x    = WALL_LEFT;                    // snap back to the left edge
            velX = Math.abs(velX) * BOUNCE_DAMP; // reverse and damp horizontal speed
            velY *= BOUNCE_DAMP;                 // damp vertical speed on side hit
        }
        // Right wall: egg right edge goes past x=1000
        if (x + WIDTH > WALL_RIGHT) {
            x    = WALL_RIGHT - WIDTH;            // snap so right edge is flush with the wall
            velX = -Math.abs(velX) * BOUNCE_DAMP; // reverse and damp horizontal speed
            velY *= BOUNCE_DAMP;                  // damp vertical speed on side hit
        }
        // Top wall: egg top edge goes above y=0
        if (y < WALL_TOP) {
            y    = WALL_TOP;                     // snap back to the top edge
            velY = Math.abs(velY) * BOUNCE_DAMP; // reverse and damp vertical speed
            velX *= BOUNCE_DAMP;                 // damp horizontal speed on top hit
        }
        // Floor: egg bottom edge goes below y=945
        if (y + HEIGHT > FLOOR) {
            y    = FLOOR - HEIGHT;                // snap so bottom edge is flush with the floor
            velY = -Math.abs(velY) * BOUNCE_DAMP; // reverse and damp vertical speed
            velX *= BOUNCE_DAMP;                  // damp horizontal speed on floor hit
        }
    }

    // ── Velocity control ───────────────────────────────────────────────────────

    /**
     * Sets the egg's velocity directly (used by the slingshot mechanic).
     * Replaces any existing velocity — does not add to it.
     *
     * @param vx new horizontal velocity (positive = right)
     * @param vy new vertical velocity  (positive = down)
     */
    public void applyImpulsive(double vx, double vy) {
        this.velX = vx; // overwrite — a shot always sets a fresh direction and speed
        this.velY = vy;
    }

    /**
     * Multiplies both velocity components by factor.
     * Used by GrassPatch to simulate extra drag. Factor should be less than 1.
     *
     * @param factor multiplier applied to velX and velY (e.g. 0.96)
     */
    public void slowDown(double factor) {
        velX *= factor; // reduce speed while preserving direction
        velY *= factor;
    }

    // ── Drawing ────────────────────────────────────────────────────────────────

    /**
     * Draws the egg as a filled white oval with a black outline.
     * Also draws crack lines if the egg has landed.
     *
     * @param g graphics context
     */
    public void draw(Graphics g) {
        int drawX = (int) x; // truncate to int for pixel-aligned rendering
        int drawY = (int) y;

        g.setColor(EGG_FILL);
        g.fillOval(drawX, drawY, WIDTH, HEIGHT); // white filled egg body

        g.setColor(EGG_OUTLINE);
        g.drawOval(drawX, drawY, WIDTH, HEIGHT); // black outline

        // Draw crack lines once the egg has come to rest on the floor
        if (state == STATE_LANDED) {
            drawCrack(g, drawX, drawY);
        }
    }

    /**
     * Draws three crack lines radiating across the egg surface.
     *
     * @param g graphics context
     * @param x left edge of the egg
     * @param y top edge of the egg
     */
    public void drawCrack(Graphics g, int x, int y) {
        g.setColor(CRACK_COLOR);
        g.drawLine(x + 18, y + 5,  x + 12, y + 30); // left-leaning crack from near the top
        g.drawLine(x + 22, y + 8,  x + 28, y + 35); // right-leaning crack from near the top
        g.drawLine(x + 15, y + 15, x + 25, y + 25); // short horizontal connecting crack
    }

    // ── Queries ────────────────────────────────────────────────────────────────

    /**
     * Returns true if either velocity component exceeds the stop threshold.
     * Used by the engine and display to determine when the egg has come to rest.
     */
    public boolean isMoving() {
        // Both components must be at or below the threshold for the egg to count as stopped
        return Math.abs(velX) > STOP_THRESH || Math.abs(velY) > STOP_THRESH;
    }

    // ── Getters and setters ────────────────────────────────────────────────────

    public double getX()     { return x; }     // left edge of the egg
    public double getY()     { return y; }     // top edge of the egg
    public double getVelX()  { return velX; }  // horizontal velocity (px/frame)
    public double getVelY()  { return velY; }  // vertical velocity (px/frame)
    public int    getState() { return state; } // current animation/physics state

    public void setX(double x)      { this.x    = x; }   // used by Wall to push egg out of collision
    public void setY(double y)      { this.y    = y; }   // used by Wall to push egg out of collision
    public void setVelX(double vx)  { this.velX = vx; }  // used by Ice to directly set boosted speed
    public void setVelY(double vy)  { this.velY = vy; }  // used by Ice to directly set boosted speed
}
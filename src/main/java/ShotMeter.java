// Programmers: Ryan Weinswig, Mera Murphy, Alberto Perez-Jacome
// Date: May 11, 2025
// Project: Tiny Egg
// Description: The shot accuracy meter shown after the player drags to aim.
//              A vertical bar bounces across three color zones (red, yellow, green).
//              The player clicks to freeze the bar; the zone determines how
//              accurately the shot is applied in GameDisplay.

import java.awt.*;

public class ShotMeter {

    // ── Zone boundary x-coordinates ───────────────────────────────────────────
    // The meter spans x=60 to x=290. Zones are laid out left-to-right:
    //   Red:    x=60  to x=170  (bad accuracy — heavy angle error + speed loss)
    //   Yellow: x=170 to x=250  (medium accuracy — slight angle error + speed loss)
    //   Green:  x=250 to x=290  (perfect accuracy — no penalty applied)
    private static final int METER_LEFT        = 60;  // leftmost x-coordinate of the meter
    private static final int METER_RIGHT       = 290; // rightmost x-coordinate of the meter
    private static final int YELLOW_START      = 170; // x where yellow zone begins (red zone ends)
    private static final int GREEN_START       = 250; // x where green zone begins (yellow zone ends)

    // Vertical position and height of the colored zone rectangles
    private static final int METER_TOP         = 50;  // y-coordinate of the top of the meter
    private static final int METER_HEIGHT      = 50;  // pixel height of the colored zone bars

    // Pixels the bar moves per frame — higher values make it harder to land on green
    private static final int BAR_SPEED         = 15;

    // Thickness of the moving indicator bar in pixels
    private static final int BAR_STROKE        = 6;

    // ── Zone colors ────────────────────────────────────────────────────────────
    private static final Color COLOR_RED    = Color.RED;    // leftmost zone — worst accuracy
    private static final Color COLOR_YELLOW = Color.YELLOW; // middle zone — medium accuracy
    private static final Color COLOR_GREEN  = Color.GREEN;  // rightmost zone — best accuracy
    private static final Color COLOR_BAR    = Color.BLACK;  // moving indicator bar

    // ── State ──────────────────────────────────────────────────────────────────
    private int     barX;       // current x position of the center of the moving bar
    private int     direction;  // +1 = moving right, -1 = moving left
    private boolean isVisible;  // true while the meter should be drawn
    private boolean isLocked;   // true after the player clicks to freeze the bar
    private boolean isMoving;   // true while the bar is bouncing; false when locked or hidden

    // ── Constructor ────────────────────────────────────────────────────────────

    /**
     * Creates the meter in a hidden, reset state.
     * Call activate() to show it.
     */
    public ShotMeter() {
        reset(); // initialize all state fields to their default hidden values
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    /**
     * Makes the meter visible and starts the bar bouncing from the left edge.
     * Called by GameDisplay when the player releases a drag.
     */
    public void activate() {
        isVisible = true;       // show the meter on screen
        isLocked  = false;      // bar has not been frozen yet
        isMoving  = true;       // bar starts moving immediately
        barX      = METER_LEFT; // always begin at the left edge so the bounce is predictable
        direction = 1;          // initial direction is right
    }

    /**
     * Hides the meter and resets all state.
     * Called by GameDisplay after the shot has been fired.
     */
    public void reset() {
        isVisible = false;      // hide the meter until the next drag-release
        isLocked  = false;      // clear the locked state
        isMoving  = true;       // ready to move again when activated
        barX      = METER_LEFT; // reset bar to the left edge
        direction = 1;          // reset direction to rightward
    }

    // ── Per-frame update ───────────────────────────────────────────────────────

    /**
     * Moves the bar one step and reverses direction at the edges.
     * Only runs when the meter is visible and the bar is not yet locked.
     */
    public void update() {
        if (!isVisible || !isMoving || isLocked) return; // skip if meter is hidden or frozen

        barX += direction * BAR_SPEED; // advance the bar by one speed increment

        // Reverse direction when the bar reaches either edge (bounce behavior)
        if (barX <= METER_LEFT) {
            barX      = METER_LEFT; // clamp to left boundary to prevent overshooting
            direction = 1;          // now moving right
        } else if (barX >= METER_RIGHT) {
            barX      = METER_RIGHT; // clamp to right boundary to prevent overshooting
            direction = -1;          // now moving left
        }
    }

    // ── Locking ────────────────────────────────────────────────────────────────

    /**
     * Freezes the bar at its current position and returns the zone name.
     * Green (barX >= GREEN_START):  perfect — no accuracy penalty.
     * Yellow (barX >= YELLOW_START): slight speed reduction + small angle error.
     * Red (barX < YELLOW_START):    heavy speed reduction + large angle error.
     *
     * @return "green", "yellow", or "red"
     */
    public String lockAndGetZone() {
        isLocked = true;   // freeze the bar so it stops moving
        isMoving = false;  // prevent update() from advancing it further

        // Determine and return the zone based on the bar's current x position
        if (barX >= GREEN_START)  return "green";  // best accuracy — no penalty
        if (barX >= YELLOW_START) return "yellow"; // medium accuracy — slight penalty
        return "red";                               // worst accuracy — heavy penalty
    }

    // ── Drawing ────────────────────────────────────────────────────────────────

    /**
     * Draws the three color zones and the moving indicator bar.
     * Does nothing if the meter is not currently visible.
     *
     * @param g graphics context
     */
    public void drawMeter(Graphics g) {
        if (!isVisible) return; // meter is hidden — skip all drawing

        drawZones(g); // colored background rectangles (red, yellow, green)
        drawBar(g);   // black vertical indicator bar on top of the zones
    }

    /**
     * Fills the three colored zone rectangles (red, yellow, green) left to right.
     *
     * @param g graphics context
     */
    private void drawZones(Graphics g) {
        // Red zone: from the left edge to the yellow zone start
        g.setColor(COLOR_RED);
        g.fillRect(METER_LEFT,   METER_TOP, YELLOW_START - METER_LEFT,  METER_HEIGHT);

        // Yellow zone: from the yellow start to the green zone start
        g.setColor(COLOR_YELLOW);
        g.fillRect(YELLOW_START, METER_TOP, GREEN_START  - YELLOW_START, METER_HEIGHT);

        // Green zone: from the green start to the right edge
        g.setColor(COLOR_GREEN);
        g.fillRect(GREEN_START,  METER_TOP, METER_RIGHT  - GREEN_START,  METER_HEIGHT);
    }

    /**
     * Draws the vertical black indicator bar at the current barX position.
     *
     * @param g graphics context
     */
    private void drawBar(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;        // cast to Graphics2D to set stroke width
        g2.setColor(COLOR_BAR);
        g2.setStroke(new BasicStroke(BAR_STROKE));                              // thick line for visibility
        g2.drawLine(barX, METER_TOP, barX, METER_TOP + METER_HEIGHT);           // vertical line spanning the full meter height
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public boolean isVisible() { return isVisible; } // true while the meter should be rendered
    public boolean isLocked()  { return isLocked; }  // true after the player has frozen the bar
}
// Programmers: Ryan Weinswig, Mera Murphy, Alberto Perez-Jacome
// Date: May 11, 2025
// Project: Tiny Egg
// Description: Handles all rendering and mouse input. Reads game state through
//              GameEngine getters — it does not modify any engine state directly
//              except by calling the two public methods startTutorial(),
//              skipToOpening(), and processShotDirect(). ShotMeter is accessed
//              only to activate, reset, lock, and draw it.

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;

public class GameDisplay extends JFrame implements MouseListener, MouseMotionListener {

    // ── Window ─────────────────────────────────────────────────────────────────
    public static final int WINDOW_WIDTH  = 1000; // total pixel width of the game window
    public static final int WINDOW_HEIGHT = 1000; // total pixel height of the game window

    // ── Color palette ──────────────────────────────────────────────────────────
    // Sky gradient colors — drawn top-to-bottom during the opening scene
    private static final Color TOP_SKY     = new Color(135, 206, 235); // deep sky blue at the top
    private static final Color MID_SKY     = new Color(176, 226, 255); // lighter mid-sky band
    private static final Color HORIZON_SKY = new Color(200, 240, 255); // palest near the horizon

    // Ground and grass shading colors
    private static final Color GRASS       = new Color( 34, 139,  34); // base map fill color
    private static final Color GRASS_2     = new Color( 85, 155,  70); // lighter stripe on ground
    private static final Color GRASS_3     = new Color( 70, 140,  60); // alternating stripe on ground
    private static final Color GRASS_H     = new Color( 60, 120,  55); // horizon line color
    private static final Color DARK_GREEN  = new Color(  0, 100,   0); // used for zigzag grass blades

    // Wood and nest colors
    private static final Color TREE_BASE   = new Color(101,  67,  33); // tree trunk brown
    private static final Color TREE_LEAF   = new Color( 34, 139,  34); // main foliage oval
    private static final Color TREE_LEAF2  = new Color( 30, 120,  30); // secondary foliage ovals
    private static final Color BROWN       = new Color(122,  75,  29); // branch and nest outer ring
    private static final Color LIGHT_ORANGE= new Color(205, 133,  63); // inner nest highlight

    // UI and effect colors
    private static final Color LIGHT_PURPLE= new Color(210, 180, 255); // end screen background
    private static final Color ARROW_COLOR = Color.RED;                 // aiming arrow and tutorial arrow
    private static final Color MENU_BG     = Color.BLACK;              // main menu background
    private static final Color MENU_BTN    = Color.GRAY;               // menu button fill
    private static final Color MENU_TEXT   = Color.WHITE;              // menu title and button labels
    private static final Color TUTORIAL_BG = new Color(120, 200, 120); // tutorial scene background
    private static final Color OBSTACLE_COLOR = Color.DARK_GRAY;       // tutorial obstacle block
    private static final Color IMPACT_FLASH   = new Color(255, 0, 0, 120); // semi-transparent red overlay on hit

    // Baby chick colors used in the win screen egg-open animation
    private static final Color CHICK_YELLOW = new Color(255, 220,  50); // chick body
    private static final Color CHICK_BEAK   = new Color(255, 160,   0); // chick beak triangle

    // ── Tutorial constants (must match GameEngine) ─────────────────────────────
    // These positions are fixed so the fake mouse, arrow, and egg line up correctly
    private static final int TUTORIAL_NEST_X  = 700; // left edge of the drawn tutorial nest
    private static final int TUTORIAL_NEST_Y  = 750; // top edge of the drawn tutorial nest
    private static final int TUTORIAL_EGG_X   = 150; // egg start x in tutorial (matches GameEngine)
    private static final int TUTORIAL_EGG_Y   = 700; // egg start y in tutorial (matches GameEngine)

    // ── Tutorial block bounds (must match GameEngine TUTORIAL_BLOCK_* constants) ──
    private static final int TUTORIAL_BLOCK_X = 450; // left edge of the obstacle block
    private static final int TUTORIAL_BLOCK_Y = 450; // top edge of the obstacle block
    private static final int TUTORIAL_BLOCK_W = 50;  // width of the obstacle block
    private static final int TUTORIAL_BLOCK_H = 50;  // height of the obstacle block

    // ── End-screen egg geometry ────────────────────────────────────────────────
    // All drawEndEgg, drawSplitEggHalves, and drawChick methods use these values
    // so the egg, crack lines, halves, and chick all stay consistently proportioned
    private static final int END_EGG_W        = 40;  // egg oval width
    private static final int END_EGG_H        = 55;  // egg oval height
    private static final int END_EGG_OFFSET_X = 20;  // half-width: centers the oval on midX
    private static final int END_EGG_OFFSET_Y = 28;  // centers the oval vertically on midY

    // ── Menu button bounds ─────────────────────────────────────────────────────
    // Rectangles used both to draw and to hit-test mouse clicks on the menu
    private static final Rectangle WATCH_BUTTON = new Rectangle(250, 300, 200, 50); // "Watch Tutorial" button
    private static final Rectangle SKIP_BUTTON  = new Rectangle(250, 400, 200, 50); // "Skip" button

    // ── Back-end references ────────────────────────────────────────────────────
    private final GameEngine engine; // supplies all game state via getters
    private final ShotMeter  meter;  // drawn by this class; activated/reset here

    // ── Mouse / shot state ─────────────────────────────────────────────────────
    private Point   dragStart;       // screen point where the drag began (null when not dragging)
    private Point   currentMousePos; // updated every frame while dragging (null when not dragging)
    private boolean isDragging;      // true between mousePressed and mouseReleased
    private double  pendingVX;       // computed shot X velocity, held until meter is clicked
    private double  pendingVY;       // computed shot Y velocity, held until meter is clicked
    private boolean waitingForMeter; // true after drag release, before meter click fires the shot

    // ── Tutorial visual state ──────────────────────────────────────────────────
    private Point   fakeMouse          = new Point(TUTORIAL_EGG_X, TUTORIAL_EGG_Y); // scripted cursor position
    private boolean tutorialHitOccurred = false; // latches true once the egg hits the obstacle block

    // ── Constructor ────────────────────────────────────────────────────────────

    /**
     * Creates and shows the game window.
     *
     * @param engine the GameEngine driving the game
     * @param meter  the ShotMeter to draw and interact with
     */
    public GameDisplay(GameEngine engine, ShotMeter meter) {
        this.engine = engine; // store reference so render() can read game state
        this.meter  = meter;  // store reference so we can draw and control the meter

        addMouseListener(this);       // receive press/release/click events
        addMouseMotionListener(this); // receive drag/move events

        setTitle("Egg Shot!");                              // window title bar text
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);               // set window dimensions
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);    // close button exits the app
        setVisible(true);                                   // make the window appear
        createBufferStrategy(2);                            // double-buffer to prevent flicker
    }

    // ── Render entry point ─────────────────────────────────────────────────────

    /**
     * Called once per frame by GameEngine.
     * Routes to the correct draw method based on game state.
     */
    public void render() {
        BufferStrategy bf = getBufferStrategy();
        if (bf == null) { createBufferStrategy(2); return; } // safety: recreate if lost

        Graphics g = bf.getDrawGraphics(); // get the back-buffer graphics context
        try {
            // Dispatch to the correct scene based on current game state
            switch (engine.getGameState()) {
                case GameEngine.STATE_MENU:     drawMenu(g);      break; // main menu with two buttons
                case GameEngine.STATE_TUTORIAL: drawTutorial(g);  break; // animated tutorial scene
                case GameEngine.STATE_OPENING:  drawOpening(g);   break; // tree/branch opening animation
                case GameEngine.STATE_PLAYING:  drawPlaying(g);   break; // live gameplay
                case GameEngine.STATE_ENDING:   drawEndScreen(g); break; // win screen with chick reveal
            }
        } finally {
            g.dispose(); // always release the graphics context to free resources
        }

        bf.show();                             // flip the back buffer to the screen
        Toolkit.getDefaultToolkit().sync();    // flush display pipeline (fixes tearing on Linux)
    }

    // ── Menu ───────────────────────────────────────────────────────────────────

    /**
     * Draws the main menu: title and two buttons (Watch Tutorial, Skip).
     */
    private void drawMenu(Graphics g) {
        g.setColor(MENU_BG);          // fill entire window with black
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw the game title centered near the top
        g.setColor(MENU_TEXT);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("Egg Shot!", 350, 200);

        // Draw button backgrounds as gray rectangles
        g.setColor(MENU_BTN);
        g.fillRect(WATCH_BUTTON.x, WATCH_BUTTON.y, WATCH_BUTTON.width, WATCH_BUTTON.height);
        g.fillRect(SKIP_BUTTON.x,  SKIP_BUTTON.y,  SKIP_BUTTON.width,  SKIP_BUTTON.height);

        // Draw button labels on top of the rectangles
        g.setColor(MENU_TEXT);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Watch Tutorial", 270, 333); // label for the top button
        g.drawString("Skip",           340, 433); // label for the bottom button
    }

    // ── Opening animation ──────────────────────────────────────────────────────

    /**
     * Draws the opening scene: sky/ground background, tree, branch, nest, egg.
     */
    private void drawOpening(Graphics g) {
        drawBackground(g);          // layered sky-to-grass gradient
        drawTree(g);                // trunk + foliage ovals
        drawBranch(g);              // horizontal branch the egg sits on
        drawNestAt(g, 560, 350);    // nest positioned at the end of the branch
        engine.getEgg().draw(g);    // egg drawn on top of everything else
    }

    // ── Gameplay ───────────────────────────────────────────────────────────────

    /**
     * Draws the active gameplay screen: map, obstacles, shot meter, egg,
     * aiming arrow (while dragging), and landing crack.
     */
    private void drawPlaying(Graphics g) {
        // Draw the correct map layout depending on which level the player is on
        if (engine.getCurrentMap() == 1) {
            drawMap1(g); // green field with top-right nest
        } else {
            drawMap2(g); // green field with bottom-right nest
        }

        meter.drawMeter(g);      // draw the shot accuracy bar if it is currently visible
        engine.getEgg().draw(g); // draw the egg on top of the map and obstacles

        // Only show the aiming arrow while the player is actively dragging
        if (isDragging && !engine.getEgg().isMoving()) {
            drawShotPreview(g); // red arrow from drag start to current mouse position
        }

        // Show crack lines after the egg has come to rest
        if (engine.getEgg().getState() == Egg.STATE_LANDED) {
            engine.getEgg().drawCrack(g,
                    (int) engine.getEgg().getX(),
                    (int) engine.getEgg().getY() - 10); // offset up slightly so cracks appear on egg body
        }
    }

    // ── Tutorial ───────────────────────────────────────────────────────────────

    /**
     * Draws the tutorial scene: background, obstacle block, nest, egg,
     * animated fake mouse, instructional text, and impact flash.
     */
    private void drawTutorial(Graphics g) {
        Graphics2D g2 = (Graphics2D) g; // needed for stroke width on the arrow line

        // Solid green background — simpler than the full sky/grass opening scene
        g.setColor(TUTORIAL_BG);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw the obstacle block the bad shot will hit
        g.setColor(OBSTACLE_COLOR);
        g.fillRect(TUTORIAL_BLOCK_X, TUTORIAL_BLOCK_Y, TUTORIAL_BLOCK_W, TUTORIAL_BLOCK_H); // fixed position; matches GameEngine's TUTORIAL_BLOCK constants

        drawNestAt(g, TUTORIAL_NEST_X, TUTORIAL_NEST_Y); // the target the player should aim for
        engine.getEgg().draw(g);                          // the egg whose movement is scripted by GameEngine

        // Advance and draw the scripted fake mouse cursor
        updateFakeMouse();
        drawFakeMouse(g);

        drawTutorialArrowIfNeeded(g2); // red line from egg to fake mouse during drag phases
        drawTutorialText(g);           // step-by-step instruction text at the top of the screen
        drawImpactFlashIfNeeded(g);    // red overlay that appears when the bad shot hits the block
    }

    /**
     * Moves the fake mouse to its scripted position for the current tutorial frame.
     * Drags left+up for the good shot (slingshot fires right+down toward nest),
     * and left+down for the bad shot (fires right+up toward the block).
     */
    private void updateFakeMouse() {
        int t = engine.getTutorialTimer(); // current frame count within the tutorial sequence

        if (t < 120) {
            // Phase 1: cursor rests at the egg — player reads the goal text
            fakeMouse.x = TUTORIAL_EGG_X;
            fakeMouse.y = TUTORIAL_EGG_Y;
        } else if (t < 240) {
            // Phase 2: cursor slides left and up — demonstrating how to drag for a good shot
            double p    = (t - 120) / 120.0;          // interpolation factor 0→1
            fakeMouse.x = (int)(TUTORIAL_EGG_X - 120 * p); // moves 120px left
            fakeMouse.y = (int)(TUTORIAL_EGG_Y - 100 * p); // moves 100px up
        } else if (t < 360) {
            // Phase 3: cursor holds still after the shot fires and the egg travels
            fakeMouse.x = TUTORIAL_EGG_X - 120;
            fakeMouse.y = TUTORIAL_EGG_Y - 100;
        } else if (t < 420) {
            // Phase 4: egg resets; cursor snaps back to the egg start position
            fakeMouse.x = TUTORIAL_EGG_X;
            fakeMouse.y = TUTORIAL_EGG_Y;
        } else if (t < 480) {
            // Phase 5: cursor slides left and down — demonstrating bad aim toward the block
            double p    = (t - 420) / 60.0;           // interpolation factor 0→1
            fakeMouse.x = (int)(TUTORIAL_EGG_X - 100 * p); // moves 100px left
            fakeMouse.y = (int)(TUTORIAL_EGG_Y + 140 * p); // moves 140px down
        } else {
            // Phase 6: cursor holds at the bad-shot drag position until the tutorial ends
            fakeMouse.x = TUTORIAL_EGG_X - 100;
            fakeMouse.y = TUTORIAL_EGG_Y + 140;
        }
    }

    /**
     * Draws the red aiming arrow from the egg to the fake mouse,
     * but only during the drag-demonstration phases.
     */
    private void drawTutorialArrowIfNeeded(Graphics2D g2) {
        int t = engine.getTutorialTimer();
        // Only show the arrow while the cursor is actively moving (phases 2 and 5)
        boolean isDragPhase = (t >= 120 && t < 240) || (t >= 420 && t < 480);
        if (!isDragPhase) return; // skip drawing if not in a drag phase

        g2.setColor(ARROW_COLOR);
        g2.setStroke(new BasicStroke(3));                                   // 3-pixel thick line
        g2.drawLine(TUTORIAL_EGG_X, TUTORIAL_EGG_Y, fakeMouse.x, fakeMouse.y); // line from egg to cursor
    }

    /**
     * Draws the step-by-step instructional text for the current tutorial phase.
     */
    private void drawTutorialText(Graphics g) {
        int t = engine.getTutorialTimer(); // determines which instruction to show

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 18));

        // Each branch matches a phase of the tutorial sequence
        if      (t < 120) g.drawString("Goal: Get the egg into the nest!", 200, 100); // introduce goal
        else if (t < 240) g.drawString("Click and drag backwards to aim",  180, 100); // drag demo
        else if (t < 300) g.drawString("Release to shoot!",                260, 100); // shot fires
        else if (t < 360) g.drawString("Perfect shot!",                    280, 100); // egg in nest
        else if (t < 420) g.drawString("But watch out for obstacles...",   200, 100); // reset for bad demo
        else if (t < 480) g.drawString("Obstacles have their own powers: speed, slowing, and reflecting.",    180, 100); // bad drag demo
        else              g.drawString("Use the obstacles how you please to help you win!", 150, 100); // impact warning
    }

    /**
     * Shows a red screen flash once the tutorial egg has hit the obstacle block.
     * The flash stays on until the tutorial ends.
     */
    private void drawImpactFlashIfNeeded(Graphics g) {
        if (engine.getTutorialHit()) tutorialHitOccurred = true; // latch: once hit, always flash
        if (tutorialHitOccurred) {
            g.setColor(IMPACT_FLASH);                         // semi-transparent red (alpha=120)
            g.fillRect(0, 0, getWidth(), getHeight());        // overlay covers the whole screen
        }
    }

    // ── Map drawing ────────────────────────────────────────────────────────────

    /**
     * Draws map 1: green background, obstacles, top-right nest,
     * and decorative grass zigzag lines.
     */
    private void drawMap1(Graphics g) {
        g.setColor(GRASS);
        g.fillRect(0, 0, getWidth(), getHeight()); // solid green base coat

        for (Obstacle obs : engine.getObstacles()) obs.draw(g); // walls, ice, grass patches

        drawNestAt(g, 736, 66); // nest is positioned in the top-right corner of map 1

        // Decorative zigzag grass lines — visual flair only, no gameplay effect
        drawGrassZigzag(g,
                new int[]{90,  120, 150, 180, 210, 240, 270},
                new int[]{370, 330, 370, 330, 370, 330, 370}); // left-side grass cluster
        drawGrassZigzag(g,
                new int[]{620, 650, 680, 710, 740, 770, 800},
                new int[]{290, 250, 290, 250, 290, 250, 290}); // top-center grass cluster
        drawGrassZigzag(g,
                new int[]{680, 710, 740, 770, 800, 830, 860},
                new int[]{710, 670, 710, 670, 710, 670, 710}); // bottom-right grass cluster
    }

    /**
     * Draws map 2: green background, obstacles, bottom-right nest,
     * and decorative grass zigzag lines.
     */
    private void drawMap2(Graphics g) {
        g.setColor(GRASS);
        g.fillRect(0, 0, getWidth(), getHeight()); // solid green base coat

        for (Obstacle obs : engine.getObstacles()) obs.draw(g); // walls, ice, grass patches

        drawNestAt(g, 750, 650); // nest is positioned in the bottom-right area of map 2

        // Decorative zigzag grass lines — positions differ from map 1 to match the layout
        drawGrassZigzag(g,
                new int[]{390, 415, 440, 465, 490, 515},
                new int[]{430, 400, 430, 400, 430, 400}); // mid-left grass cluster
        drawGrassZigzag(g,
                new int[]{720, 748, 776, 804, 832, 860, 888},
                new int[]{530, 500, 530, 500, 530, 500, 530}); // right-side grass cluster
        drawGrassZigzag(g,
                new int[]{560, 588, 616, 644, 672, 700},
                new int[]{720, 690, 720, 690, 720, 690}); // bottom-center grass cluster
    }

    // ── Scene component helpers ────────────────────────────────────────────────

    /**
     * Draws the sky-to-ground background using horizontal color bands.
     */
    private void drawBackground(Graphics g) {
        // Paint the sky in three horizontal bands from top (dark) to horizon (pale)
        g.setColor(TOP_SKY);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT); // full-screen dark base

        g.setColor(MID_SKY);
        g.fillRect(0, 0, WINDOW_WIDTH, 500); // lighter band covering upper half

        g.setColor(HORIZON_SKY);
        g.fillRect(0, 0, WINDOW_WIDTH, 250); // palest band just above the ground line

        // Ground: solid green with alternating stripe texture
        g.setColor(GRASS_2);
        g.fillRect(0, 650, WINDOW_WIDTH, 350); // base ground fill

        g.setColor(GRASS_3);
        for (int i = 0; i < WINDOW_WIDTH; i += 40) {
            // Alternate stripe height by 0, 3, or 6 pixels for subtle texture
            g.fillRect(i, 650 + (i % 3) * 3, 20, 100);
        }

        g.setColor(GRASS_H);
        g.drawLine(0, 650, WINDOW_WIDTH, 650); // single-pixel horizon line
    }

    /**
     * Draws the decorative tree with trunk, main canopy, and two foliage blobs.
     */
    private void drawTree(Graphics g) {
        // Trunk: tall brown rectangle with black outline
        g.setColor(TREE_BASE);
        g.fillRect(450, 250, 100, 450);
        g.setColor(Color.BLACK);
        g.drawRect(450, 250, 100, 450); // outline gives the trunk a solid look

        // Main canopy: large oval over the trunk top
        g.setColor(TREE_LEAF);
        g.fillOval(350, 80, 300, 250);

        // Two secondary foliage blobs for a fuller, layered appearance
        g.setColor(TREE_LEAF2);
        g.fillOval(300, 150, 200, 180); // left cluster, slightly darker green
        g.fillOval(450, 120, 220, 200); // right cluster, overlaps the main canopy

        g.setColor(Color.BLACK);
        g.drawOval(350, 80, 300, 250); // outline the main canopy oval
    }

    /**
     * Draws the horizontal branch the egg rests on during the opening animation.
     */
    private void drawBranch(Graphics g) {
        g.setColor(BROWN);
        g.fillRect(540, 400, 200, 20); // wide flat rectangle extending right from the trunk
    }

    /**
     * Draws a bird's nest at the given position.
     * Used for the opening animation, tutorial, and both map nests.
     *
     * @param g graphics context
     * @param x left edge of the nest oval
     * @param y top edge of the nest oval
     */
    private void drawNestAt(Graphics g, int x, int y) {
        // Outer ring: brown oval representing the woven nest base
        g.setColor(BROWN);
        g.fillOval(x, y, 120, 50);

        // Inner highlight: smaller lighter oval giving the nest a bowl shape
        g.setColor(LIGHT_ORANGE);
        g.fillOval(x + 10, y, 100, 40);

        g.setColor(Color.BLACK);
        g.drawOval(x, y, 120, 50); // outline the nest for crisp edges
    }

    /**
     * Draws a zigzag polyline in DARK_GREEN to represent decorative grass blades.
     *
     * @param g  graphics context
     * @param xs x-coordinates of the zigzag points
     * @param ys y-coordinates of the zigzag points
     */
    private void drawGrassZigzag(Graphics g, int[] xs, int[] ys) {
        g.setColor(DARK_GREEN);
        g.drawPolyline(xs, ys, xs.length); // connect all points with straight line segments
    }

    /**
     * Draws the red aiming arrow from drag start to current mouse position,
     * including a small arrowhead at the mouse end.
     */
    private void drawShotPreview(Graphics g) {
        if (dragStart == null || currentMousePos == null) return; // nothing to draw if not dragging

        int x1 = dragStart.x;      // arrow tail — where the player pressed down
        int y1 = dragStart.y;
        int x2 = currentMousePos.x; // arrow tip — where the mouse currently is
        int y2 = currentMousePos.y;

        g.setColor(ARROW_COLOR);
        g.drawLine(x1, y1, x2, y2); // main shaft of the aiming arrow

        // Compute the arrow tip angle and draw two short lines forming the arrowhead
        double angle = Math.atan2(y2 - y1, x2 - x1); // direction of the arrow
        int    size  = 10;                             // arrowhead arm length in pixels

        // Left arm of the arrowhead
        int xA = (int)(x2 - size * Math.cos(angle - Math.PI / 6));
        int yA = (int)(y2 - size * Math.sin(angle - Math.PI / 6));

        // Right arm of the arrowhead
        int xB = (int)(x2 - size * Math.cos(angle + Math.PI / 6));
        int yB = (int)(y2 - size * Math.sin(angle + Math.PI / 6));

        g.drawLine(x2, y2, xA, yA); // left arrowhead arm
        g.drawLine(x2, y2, xB, yB); // right arrowhead arm
    }

    /**
     * Draws a small black dot representing the scripted fake mouse cursor.
     */
    private void drawFakeMouse(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillOval(fakeMouse.x - 5, fakeMouse.y - 5, 10, 10); // 10px dot centered on fakeMouse
    }

    // ── End screen ─────────────────────────────────────────────────────────────

    /**
     * Draws the win screen: purple background, egg cracking open to reveal a chick,
     * "You Win!" text, and a stroke-count rating.
     */
    private void drawEndScreen(Graphics g) {
        int time    = engine.getEndingTimer();  // frames elapsed since the win state began
        int strokes = engine.getStrokeCount(); // total shots taken — used to compute rating

        g.setColor(LIGHT_PURPLE);
        g.fillRect(0, 0, getWidth(), getHeight()); // light purple background for the win screen

        int midX = getWidth()  / 2; // horizontal center of the window
        int midY = getHeight() / 2; // vertical center of the window

        // Three-phase animation: intact egg → cracked egg → open egg with chick
        if      (time < 60)  drawEndEgg(g, midX, midY, false, false); // intact egg
        else if (time < 120) drawEndEgg(g, midX, midY, true,  false); // cracked but closed
        else {
            drawEndEgg(g, midX, midY, true, true);                     // split open with chick
            g.setColor(DARK_GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 64));
            g.drawString("You Win!", midX - 150, midY - 120);          // victory text above the egg
        }

        drawRating(g, midX, midY, strokes); // show the performance rating below the egg
    }

    /**
     * Draws the stroke-count rating centered below the egg.
     * ≤4 strokes: Magical. ≤8: Exceptional Effort. 9+: Average.
     *
     * @param g       graphics context
     * @param midX    horizontal center
     * @param midY    vertical center
     * @param strokes total shots taken
     */
    private void drawRating(Graphics g, int midX, int midY, int strokes) {
        String rating;      // label text shown to the player
        Color  ratingColor; // color chosen to match performance level

        // Select rating tier based on how many strokes it took to finish the game
        if (strokes <= 6) {
            rating      = "★ Magical! ★";
            ratingColor = new Color(180, 0, 200); // purple — best tier
        } else if (strokes <= 8) {
            rating      = "Exceptional Effort!";
            ratingColor = new Color(0, 100, 180); // blue — middle tier
        } else {
            rating      = "Average";
            ratingColor = new Color(180, 100, 0); // orange — lowest tier
        }

        g.setColor(ratingColor);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        int w = g.getFontMetrics().stringWidth(rating); // measure string so we can center it
        g.drawString(rating, midX - w / 2, midY + 130); // position below the egg graphic
    }

    /**
     * Draws the end-screen egg in one of three states:
     * intact, cracked (crack lines visible), or open (split halves + chick).
     *
     * @param g       graphics context
     * @param midX    horizontal center
     * @param midY    vertical center
     * @param cracked whether to draw crack lines
     * @param open    whether to split the egg and show the chick
     */
    private void drawEndEgg(Graphics g, int midX, int midY, boolean cracked, boolean open) {
        if (!open) {
            // Draw the egg as a whole oval
            g.setColor(Color.WHITE);
            g.fillOval(midX - END_EGG_OFFSET_X, midY - END_EGG_OFFSET_Y, END_EGG_W, END_EGG_H); // egg body
            g.setColor(Color.BLACK);
            g.drawOval(midX - END_EGG_OFFSET_X, midY - END_EGG_OFFSET_Y, END_EGG_W, END_EGG_H); // egg outline
            if (cracked) {
                // Three jagged lines radiating from the top of the egg to suggest cracking
                g.setColor(Color.BLACK);
                g.drawLine(midX - 2, midY - END_EGG_OFFSET_Y, midX - 8, midY);     // left crack
                g.drawLine(midX + 2, midY - 24, midX + 8, midY + 5); // right crack
                g.drawLine(midX - 5, midY - 10, midX + 5, midY);     // middle crack
            }
        } else {
            // Egg has fully opened — draw the two halves and the chick inside
            drawSplitEggHalves(g, midX, midY); // left and right shell arcs
            drawChick(g, midX, midY);           // baby chick peeking out from the middle
        }
    }

    /**
     * Draws the two split egg halves for the open egg animation.
     */
    private void drawSplitEggHalves(Graphics g, int midX, int midY) {
        // Left half: a 180-degree arc (right-facing semicircle) offset to the left
        g.setColor(Color.WHITE);
        g.fillArc(midX - 30, midY - END_EGG_OFFSET_Y, END_EGG_W, END_EGG_H, 90,  180);
        g.setColor(Color.BLACK);
        g.drawArc(midX - 30, midY - END_EGG_OFFSET_Y, END_EGG_W, END_EGG_H, 90,  180);

        // Right half: a 180-degree arc (left-facing semicircle) offset to the right
        g.setColor(Color.WHITE);
        g.fillArc(midX + 10, midY - END_EGG_OFFSET_Y, END_EGG_W, END_EGG_H, 270, 180);
        g.setColor(Color.BLACK);
        g.drawArc(midX + 10, midY - END_EGG_OFFSET_Y, END_EGG_W, END_EGG_H, 270, 180);
    }

    /**
     * Draws the baby chick (head, eyes, beak) peeking out of the cracked egg.
     */
    private void drawChick(Graphics g, int midX, int midY) {
        // Chick head: yellow filled oval with black outline
        g.setColor(CHICK_YELLOW);
        g.fillOval(midX - 12, midY - 15, 24, 24);
        g.setColor(Color.BLACK);
        g.drawOval(midX - 12, midY - 15, 24, 24);

        // Eyes: two small filled ovals
        g.fillOval(midX - 6,  midY - 10,  4,  4); // left eye
        g.fillOval(midX + 2,  midY - 10,  4,  4); // right eye

        // Beak: small orange downward-pointing triangle
        g.setColor(CHICK_BEAK);
        int[] bx = {midX - 2, midX + 2, midX};     // x-coordinates of triangle vertices
        int[] by = {midY - 5, midY - 5, midY - 1}; // y-coordinates of triangle vertices
        g.fillPolygon(bx, by, 3);
    }

    // ── Mouse input ────────────────────────────────────────────────────────────

    /**
     * Routes mouse presses to the correct handler for the current game state.
     * Menu: checks which button was clicked.
     * Tutorial: skips to the opening.
     * Playing: either locks the meter and fires, or starts a drag.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        switch (engine.getGameState()) {
            case GameEngine.STATE_MENU:
                handleMenuClick(e.getPoint()); // check if a menu button was hit
                break;
            case GameEngine.STATE_TUTORIAL:
                engine.skipToOpening();        // any click during tutorial skips to opening
                break;
            case GameEngine.STATE_PLAYING:
                handlePlayingClick(e.getPoint()); // fire or start dragging
                break;
        }
    }

    /**
     * Checks which menu button was clicked and triggers the appropriate action.
     */
    private void handleMenuClick(Point p) {
        if (WATCH_BUTTON.contains(p)) engine.startTutorial(); // go to tutorial sequence
        if (SKIP_BUTTON.contains(p))  engine.skipToOpening(); // go straight to opening animation
    }

    /**
     * During gameplay, either locks the meter and fires the egg (if waiting for
     * meter input) or starts an aiming drag (if the egg has stopped moving).
     */
    private void handlePlayingClick(Point p) {
        if (waitingForMeter) {
            fireFromMeter();                     // player clicked to lock the shot meter
        } else if (!engine.getEgg().isMoving()) {
            // Begin a new drag only if the egg is at rest
            dragStart       = p;    // record where the drag started
            currentMousePos = p;    // initialize current position to the same point
            isDragging      = true; // flag that a drag is in progress
        }
    }

    /**
     * Locks the shot meter, applies the zone penalty to the pending velocity,
     * and fires the egg via GameEngine.
     * Green: no change. Yellow: slight random angle + speed reduction.
     * Red: heavy random angle + heavy speed reduction.
     */
    private void fireFromMeter() {
        String zone = meter.lockAndGetZone(); // freeze bar and get the zone name ("green"/"yellow"/"red")
        double vx   = pendingVX; // start with the ideal velocity computed from the drag
        double vy   = pendingVY;

        if (zone.equals("yellow")) {
            // Medium penalty: small random angle error + moderate speed reduction
            double angle     = (Math.random() - 0.5) * 0.4;               // ±0.2 rad wobble
            double speed     = Math.sqrt(vx * vx + vy * vy) * (0.7 + Math.random() * 0.4); // 70–110% speed
            double baseAngle = Math.atan2(vy, vx) + angle;                 // original angle + wobble
            vx = Math.cos(baseAngle) * speed;                              // recompute components
            vy = Math.sin(baseAngle) * speed;
        } else if (zone.equals("red")) {
            // Heavy penalty: large random angle error + significant speed reduction
            double angle     = (Math.random() - 0.5) * 1.2;               // ±0.6 rad wobble
            double speed     = Math.sqrt(vx * vx + vy * vy) * (0.3 + Math.random() * 0.5); // 30–80% speed
            double baseAngle = Math.atan2(vy, vx) + angle;
            vx = Math.cos(baseAngle) * speed;
            vy = Math.sin(baseAngle) * speed;
        }
        // Green zone: no modification — vx and vy remain at their ideal values

        engine.processShotDirect(vx, vy); // hand velocity to engine; increments stroke count
        meter.reset();          // hide the meter and reset for the next shot
        waitingForMeter = false; // allow the next drag to begin
    }

    /**
     * On drag release, computes pending shot velocity from the drag vector
     * (negated — slingshot mechanic), activates the meter, and waits for a click.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        if (!isDragging) return; // ignore releases that weren't preceded by a drag

        // Compute the drag displacement from start to release point
        double dx = e.getPoint().x - dragStart.x; // positive = dragged right
        double dy = e.getPoint().y - dragStart.y; // positive = dragged down

        // Negate and scale: dragging right launches the egg left, etc. (slingshot)
        pendingVX = -dx * 0.1;
        pendingVY = -dy * 0.1;

        // Clear drag state
        isDragging      = false;
        dragStart       = null;
        currentMousePos = null;

        meter.activate();       // start the bouncing accuracy bar
        waitingForMeter = true; // next click will lock the meter and fire
    }

    /** Tracks the mouse position continuously while dragging. */
    @Override
    public void mouseDragged(MouseEvent e) {
        currentMousePos = e.getPoint(); // update so drawShotPreview shows a live arrow
    }

    // Required but unused interface methods
    @Override public void mouseClicked(MouseEvent e)  {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
    @Override public void mouseMoved(MouseEvent e)    {}
}
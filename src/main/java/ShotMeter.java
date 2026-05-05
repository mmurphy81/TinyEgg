import java.awt.*;

public class ShotMeter {
    private double accuracy;
    private boolean isMoving;
    private int direction;
    private int barX;
    private int barY;
    private int speed;
    private boolean isVisible = false;
    private boolean isLocked = false;
    private int lockedX = -1;


    //Constructor for ShotMeter
    public ShotMeter() {
        barX = 60;     // start at left edge of meter
        barY = 50;
        direction = 1;  // start moving right
        isMoving = true;
    }

    public void update() {
        if (!isMoving || !isVisible || isLocked) return;

        // 15 = speed of movement
        barX += direction * 15;

        // Bounce off edges (red start to green end)
        if (barX <= 100) {
            barX = 100;
            direction = 1;
        } else if (barX >= 290) { // right edge (adjust if needed)
            barX = 290;
            direction = -1;
        }
    }

    public void startMeter() {

    }

    public double stopMeter() {

        return 0;
    }

    public int getSpeed() {
        return speed;
    }

    public void activate() {
        isVisible = true;
        isLocked = false;
        barX = 100;
        direction = 1;
    }

    public boolean isVisible() { return isVisible; }
    public boolean isLocked() { return isLocked; }

    // Returns "green", "yellow", or "red" based on where bar stopped
    public String lockAndGetZone() {
        isLocked = true;
        lockedX = barX;
        isMoving = false;

        if (lockedX >= 250) return "green";
        else if (lockedX >= 170) return "yellow";
        else return "red";
    }

    public void reset() {
        isVisible = false;
        isLocked = false;
        isMoving = true;
        barX = 100;
    }

    public void redRect(Graphics g) {
        g.setColor(Color.red);
        g.fillRect(60, 50, 110,50);
        speed = 10;
    }
    public void yellowRect(Graphics g) {
        g.setColor(Color.yellow);
        g.fillRect(170, 50, 90,50);
        speed = 30;
    }
    public void greenRect(Graphics g){
        g.setColor(Color.green);
        g.fillRect(250, 50, 40,50);
        speed = 50;
    }

    public void drawMeter(Graphics g){
        if (!isVisible) return;  // ← must be FIRST

        redRect(g);
        yellowRect(g);
        greenRect(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.black);
        g2.setStroke(new BasicStroke(6));
        g2.drawLine(barX, barY, barX, barY + 50);
    }

}

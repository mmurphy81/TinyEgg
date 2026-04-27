import java.awt.*;

public class ShotMeter {
    private double accuracy;
    private boolean isMoving;
    private int direction;
    private int barX;
    private int barY;
    private int speed;

    public void update() {

    }

    public void startMeter() {

    }

    public double stopMeter() {

        return 0;
    }

    public int getSpeed() {
        return speed;
    }

    public void redRect(Graphics g) {
        g.setColor(Color.red);
        g.drawRect(100, 50, 100,100);
        speed = 10;
    }
    public void yellowRect(Graphics g) {
        g.setColor(Color.yellow);
        g.drawRect(180, 50, 80,100);
        speed = 30;
    }
    public void greenRect(Graphics g){
        g.setColor(Color.green);
        g.drawRect(230, 50, 60,100);
        speed = 50;
    }

}

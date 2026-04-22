import java.awt.*;
import java.util.ArrayList;

public class Level {
    private ArrayList<Obstacle> obstacles;
    private int nestX;
    private int nestY;
    private int nestWidth;
    private int nestHeight;
    private int startX;
    private int startY;
    private int levelID;

    public Level(int id) {
        this.levelID = id;
        this.obstacles = new ArrayList<>();
        this.startX = 500;
        this.startY = 500;
        this.nestX = 500;
        this.nestY = 500;
        this.nestWidth = 80;
        this.nestHeight = 50;
    }

    public ArrayList<Obstacle> getObstacles() {
        return obstacles;
    }

    public Rectangle getNestBounds() {
        return new Rectangle(nestX - nestWidth / 2, nestY - nestHeight / 2, nestWidth, nestHeight);
    }

    public Point getStartPoint() {
        return new Point(startX, startY);
    }

    public int getLevelID() {
        return levelID;
    }
}

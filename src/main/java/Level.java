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
        this.nestWidth = 80;
        this.nestHeight = 50;

        if (id == 3) {
            buildLevel3();
        } else {
            this.startX = 500;
            this.startY = 500;
            this.nestX = 500;
            this.nestY = 500;
        }
    }

    private void buildLevel3() {
        startX = 130;
        startY = 820;
        nestX = 880;
        nestY = 840;

        obstacles.add(new Wall(120,  40, 20, 200));
        obstacles.add(new Wall(340,  40, 20, 120));
        obstacles.add(new Wall(340, 300, 20, 320));
        obstacles.add(new Wall(660,  40, 20, 340));
        obstacles.add(new Wall(820,  40, 20, 200));
        obstacles.add(new Wall(720, 500, 20, 220));
        obstacles.add(new Wall(500, 640, 20, 320));
        obstacles.add(new Wall(720, 780, 200, 20));

        obstacles.add(new IcePatch( 40,  80, 180, 70));
        obstacles.add(new IcePatch(380, 260, 240, 70));
        obstacles.add(new IcePatch(680, 280, 180, 70));

        obstacles.add(new GrassPatch(440, 380, 120, 60));
        obstacles.add(new GrassPatch(820, 500, 140, 60));
        obstacles.add(new GrassPatch(740, 820, 180, 60));
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
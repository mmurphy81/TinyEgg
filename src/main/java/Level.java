import java.awt.*;
import java.util.ArrayList;

public class Level {
    private ArrayList<Obstacle> obstacles;
    private int nestX;
    private int nestY;
    private int startX;
    private int startY;
    private int levelID;

    public int Level(int id){
        return 0;
    }

    public ArrayList<Obstacle> getObstacles() {
        return obstacles;
    }

    public Rectangle getNestBounds(){
        return null;
    }

    public Point getStartPoint(){
        return null;
    }
}

import java.awt.*;

abstract class Obstacle {
    private int x;
    private int y;
    private int width;
    private int height;

    public Rectangle getBounds(){
        return null;
    }

    public void onCollision(Egg egg){

    }

    public void draw(Graphics g){

    }

    public class SwingDoor extends Obstacle{

        @Override
        public void onCollision(Egg egg) {
            super.onCollision(egg);
        }
        @Override
        public void draw(Graphics g) {
            super.draw(g);
        }

        public void update(){

        }
    }

    public class GrassPatch extends Obstacle{
        @Override
        public void onCollision(Egg egg) {
            super.onCollision(egg);
        }
        @Override
        public void draw(Graphics g) {
            super.draw(g);
        }

    }
    public class Wall extends Obstacle{
        @Override
        public void onCollision(Egg egg) {
            super.onCollision(egg);
        }

        @Override
        public void draw(Graphics g) {
            super.draw(g);
        }
    }
}

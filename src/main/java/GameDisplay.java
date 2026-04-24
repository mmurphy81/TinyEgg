import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameDisplay extends JFrame {
    private GameEngine engine;
    private GamePanel panel;
    private ShotMeter meter;
    private Point dragStart;
    private Point currentMousePos;
    private boolean isDragging;
    private boolean isCharging;

    public static final int WORLD_WIDTH = 1000;
    public static final int WORLD_HEIGHT = 1000;

    public GameDisplay(GameEngine engine) {
        this.engine = engine;
        this.setTitle("Tiny Egg");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(true);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int startSize = Math.min(screen.width, screen.height) - 120;
        if (startSize > WORLD_WIDTH) startSize = WORLD_WIDTH;

        panel = new GamePanel();
        panel.setPreferredSize(new Dimension(startSize, startSize));
        this.add(panel);

        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public GamePanel getPanel() {
        return panel;
    }

    public class GamePanel extends JPanel {

        public GamePanel() {
            setDoubleBuffered(true);
            setBackground(new Color(70, 160, 70));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double sx = (double) getWidth() / WORLD_WIDTH;
            double sy = (double) getHeight() / WORLD_HEIGHT;
            g2.scale(sx, sy);
            drawMap(g2);
            drawEgg(g2);
            drawShotPreview(g2);
            drawUI(g2);
            g2.dispose();
        }

        private void drawEgg(Graphics g) {
            Egg egg = engine.getEgg();
            if (egg == null) return;
            int r = Egg.RADIUS;
            int ex = (int) egg.getX();
            int ey = (int) egg.getY();
            g.setColor(new Color(250, 245, 225));
            g.fillOval(ex - r, ey - r - 2, r * 2, r * 2 + 4);
            g.setColor(new Color(150, 130, 100));
            g.drawOval(ex - r, ey - r - 2, r * 2, r * 2 + 4);
        }

        private void drawMap(Graphics g) {
            g.setColor(new Color(110, 75, 50));
            g.fillRect(0, 0, WORLD_WIDTH, 40);
            g.fillRect(0, WORLD_HEIGHT - 40, WORLD_WIDTH, 40);
            g.fillRect(0, 0, 40, WORLD_HEIGHT);
            g.fillRect(WORLD_WIDTH - 40, 0, 40, WORLD_HEIGHT);

            Level level = engine.getLevel();
            if (level == null) return;

            for (Obstacle o : level.getObstacles()) {
                o.draw(g);
            }

            Rectangle nest = level.getNestBounds();
            g.setColor(new Color(180, 140, 90));
            g.fillOval(nest.x, nest.y, nest.width, nest.height);
            g.setColor(new Color(100, 70, 40));
            g.drawOval(nest.x, nest.y, nest.width, nest.height);
        }

        private void drawUI(Graphics g) {

        }

        private void drawShotPreview(Graphics g) {

        }
    }

    public void mousePressed(MouseEvent e) {

    }

    public void mouseDragged(MouseEvent e) {

    }

    public void mouseReleased(MouseEvent e) {

    }
}
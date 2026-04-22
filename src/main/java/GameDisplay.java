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
    public static final int WINDOW_WIDTH = 1000;
    public static final int WINDOW_HEIGHT = 1000;

    public GameDisplay(GameEngine engine) {
        this.engine = engine;
        this.setTitle("Tiny Egg");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        panel = new GamePanel();
        panel.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
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
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawMap(g2);
            drawEgg(g2);
            drawShotPreview(g2);
            drawUI(g2);
        }

        private void drawEgg(Graphics g) {

        }

        private void drawMap(Graphics g) {

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

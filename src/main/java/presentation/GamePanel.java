package presentation;

import domain.DopoSoccer;
import domain.Field;
import domain.Match;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Game screen: HUD on top and the court below. Owns the loop and the keyboard controller. */
public class GamePanel extends JPanel {
    private final DopoSoccerGUI gui;
    private final Match match;
    private final Renderer renderer;
    private final HUDPanel hud;
    private final CourtPanel court = new CourtPanel();
    private final GameLoop loop;
    private final KeyboardController keys;

    public GamePanel(DopoSoccerGUI gui, DopoSoccer game, Dimension courtSize) {
        this.gui = gui;
        this.match = game.getMatch();
        this.renderer = new Renderer(loadBackground(courtSize.width));
        setLayout(new BorderLayout());
        hud = new HUDPanel(match, courtSize.width);
        court.setPreferredSize(courtSize);
        court.setFocusable(true);
        keys = new KeyboardController(match, this);
        court.addKeyListener(keys);
        add(hud, BorderLayout.NORTH);
        add(court, BorderLayout.CENTER);
        loop = new GameLoop(match, this::repaintAll, () -> SwingUtilities.invokeLater(gui::onMatchFinished));
    }

    /**
     * One background image per resolution, named after the court width (e.g. match_background_1024.jpg).
     * If the exact one is missing, the closest available resolution is used instead.
     */
    private static BufferedImage loadBackground(int courtWidth) {
        Integer[] widths = Arrays.stream(DopoSoccerGUI.RESOLUTIONS).map(r -> r[0]).toArray(Integer[]::new);
        Arrays.sort(widths, Comparator.comparingInt(w -> Math.abs(w - courtWidth)));
        for (int w : widths) {
            BufferedImage image = SpriteLoader.load("match_background_" + w + ".jpg");
            if (image != null) return image;
        }
        return null;   // no image at all: the Renderer draws its generated background
    }

    public void start() {
        loop.start();
        court.requestFocusInWindow();
    }

    public void stop() { loop.stop(); }

    private void repaintAll() {
        hud.repaint();
        court.repaint();
    }

    public void togglePause() {
        synchronized (match) {
            if (match.isFinished()) return;
            match.pause();
        }
        keys.releaseAll();
        hud.repaint();
        boolean resume = PauseDialog.show(this);
        if (resume) {
            synchronized (match) { match.resume(); }
            court.requestFocusInWindow();
        } else {
            gui.onEndRequested();
        }
    }

    /** Draws the field scaled to the panel while keeping the 16:9 ratio. */
    private class CourtPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            double s = Math.min(getWidth() / (double) Field.WIDTH, getHeight() / (double) Field.HEIGHT);
            g.translate((getWidth() - Field.WIDTH * s) / 2, (getHeight() - Field.HEIGHT * s) / 2);
            g.scale(s, s);
            synchronized (match) { renderer.render(g, match); }
            g.dispose();
        }
    }
}
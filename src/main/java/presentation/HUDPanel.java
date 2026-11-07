package presentation;

import domain.Character;
import domain.GoalsMatch;
import domain.Match;
import domain.TimeMatch;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/** Always-visible score, time and per-player life and power bars. Read-only. */
public class HUDPanel extends JPanel {
    public static final int HEIGHT = 84;
    private static final int BAR_W = 250;

    private final Match match;

    public HUDPanel(Match match, int width) {
        this.match = match;
        setPreferredSize(new Dimension(width, HEIGHT));
        setBackground(Theme.BG);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        synchronized (match) {
            drawPlayer(g, 0, 12);
            drawPlayer(g, 1, getWidth() - 12 - BAR_W);
            drawCenter(g);
        }
        g.dispose();
    }

    private void drawPlayer(Graphics2D g, int slot, int x) {
        Character c = match.getCharacterOf(slot);
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.setColor(Theme.ownerColor(slot));
        g.drawString("P" + (slot + 1) + "  " + c.getCountryName(), x, 20);

        double lifeFrac = c.getLife() / c.getMaxLife();
        g.setColor(new Color(0x444444));
        g.fillRoundRect(x, 28, BAR_W, 16, 8, 8);
        g.setColor(lifeFrac > 0.5 ? new Color(0x3FBF5F) : lifeFrac > 0.25 ? new Color(0xF2A900) : new Color(0xE03C3C));
        g.fillRoundRect(x, 28, (int) (BAR_W * lifeFrac), 16, 8, 8);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("LIFE " + (int) Math.ceil(c.getLife()) + " / " + (int) c.getMaxLife(), x + 6, 41);

        g.setColor(new Color(0x444444));
        g.fillRoundRect(x, 50, BAR_W, 12, 8, 8);
        if (c.isPowerActive()) {
            g.setColor(Theme.ACCENT);
            g.fillRoundRect(x, 50, BAR_W, 12, 8, 8);
            g.setColor(Theme.BG);
            g.drawString(c.getPower().getName().toUpperCase() + " ACTIVE", x + 6, 60);
        } else {
            g.setColor(new Color(0x2D8CFF));
            g.fillRoundRect(x, 50, (int) (BAR_W * c.getPowerBar()), 12, 8, 8);
            g.setColor(Color.WHITE);
            g.drawString(c.getPowerBar() >= 1.0 ? "POWER READY" : "POWER", x + 6, 60);
        }

        String status = c.isStunned() ? "STUNNED" : c.getEffect() == null ? "" : c.getEffect().getClass().getSimpleName().toUpperCase();
        g.setColor(new Color(0xFFD700));
        g.drawString(status, x, 78);
    }

    private void drawCenter(Graphics2D g) {
        int cx = getWidth() / 2;
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 38));
        String score = match.getScoreboard().getGoals(0) + " - " + match.getScoreboard().getGoals(1);
        center(g, score, cx, 40);
        g.setFont(new Font("SansSerif", Font.BOLD, 17));
        String time;
        if (match instanceof TimeMatch) {
            TimeMatch tm = (TimeMatch) match;
            time = tm.isSuddenDeath() ? "SUDDEN DEATH" : format(tm.getRemainingTime());
        } else if (match instanceof GoalsMatch) {
            time = "First to " + ((GoalsMatch) match).getTargetGoals() + "  |  " + format(match.getScoreboard().getElapsedTime());
        } else {
            time = format(match.getScoreboard().getElapsedTime());
        }
        g.setColor(match.isPaused() ? Theme.ACCENT : Color.LIGHT_GRAY);
        center(g, match.isPaused() ? "PAUSED" : time, cx, 66);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(Color.GRAY);
        center(g, "Esc: pause", cx, 80);
    }

    private static void center(Graphics2D g, String s, int cx, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, cx - fm.stringWidth(s) / 2, y);
    }

    private static String format(double seconds) {
        int s = (int) Math.ceil(seconds);
        return String.format("%02d:%02d", s / 60, s % 60);
    }
}

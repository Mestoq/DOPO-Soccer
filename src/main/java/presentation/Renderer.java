package presentation;

import domain.Ball;
import domain.Character;
import domain.Confusion;
import domain.Effect;
import domain.Electrocuted;
import domain.Field;
import domain.Goal;
import domain.Match;
import domain.Push;
import domain.Unconscious;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/** Draws the domain state in logical field coordinates (1280x720). Read-only. */
public class Renderer {
    private static final Map<String, Color> COUNTRY = new HashMap<>();
    private static final Map<String, Color> AURA = new HashMap<>();
    static {
        COUNTRY.put("South Korea", new Color(0xD7263D));
        COUNTRY.put("Cameroon", new Color(0xF2C300));
        COUNTRY.put("Nigeria", new Color(0x1F9D55));
        COUNTRY.put("USA", new Color(0x2B59C3));
        AURA.put("Blue Aura", new Color(0x2D8CFF));
        AURA.put("Lightning", new Color(0xFFE23B));
        AURA.put("Tornado", new Color(0xB0B7C3));
        AURA.put("Illusion", new Color(0xD946EF));
    }

    /** Draws the visual of one effect on the affected character. */
    @FunctionalInterface
    private interface EffectPainter {
        void paint(Graphics2D g, Character c);
    }

    // One painter per effect class. A new Effect only needs a painter registered here.
    private final Map<Class<? extends Effect>, EffectPainter> effectPainters = new HashMap<>();
    private final EffectPainter stunPainter = this::drawStars;

    public Renderer() {
        effectPainters.put(Electrocuted.class, this::drawLightning);
        effectPainters.put(Push.class, this::drawPushArrows);
        effectPainters.put(Confusion.class, this::drawQuestionMark);
        effectPainters.put(Unconscious.class, stunPainter);
    }

    public void render(Graphics2D g, Match match) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Field field = match.getField();
        drawBackground(g, field);
        drawGoals(g, field);
        for (int i = 0; i < 2; i++) drawCharacter(g, match.getCharacterOf(i));
        for (Ball b : match.getBalls()) drawBall(g, b);
    }

    private void drawBackground(Graphics2D g, Field f) {
        int ground = (int) f.getGroundY();
        g.setPaint(new GradientPaint(0, 0, new Color(0x6EC1F0), 0, ground, new Color(0xD6F0FF)));
        g.fillRect(0, 0, f.getWidth(), ground);
        g.setColor(new Color(0x3FA34D));
        g.fillRect(0, ground, f.getWidth(), f.getHeight() - ground);
        g.setColor(new Color(0x368F43));
        g.fillRect(0, ground + 20, f.getWidth(), 20);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(4));
        g.drawLine(0, ground, f.getWidth(), ground);
        g.drawLine(f.getWidth() / 2, ground, f.getWidth() / 2, f.getHeight());
    }

    private void drawGoals(Graphics2D g, Field f) {
        for (int slot = 0; slot < 2; slot++) {
            Goal gl = f.getGoalOf(slot);
            int x = (int) gl.getX(), y = (int) gl.getY(), w = (int) gl.getWidth(), h = (int) gl.getHeight();
            g.setColor(new Color(255, 255, 255, 45));
            g.fillRect(x, y, w, h);
            g.setColor(new Color(255, 255, 255, 110));
            g.setStroke(new BasicStroke(1));
            for (int i = 0; i <= w; i += 15) g.drawLine(x + i, y, x + i, y + h);
            for (int j = 0; j <= h; j += 15) g.drawLine(x, y + j, x + w, y + j);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(7));
            int post = slot == 0 ? x + w : x;
            g.drawLine(post, y, post, y + h);
            g.drawLine(x, y, x + w, y);
        }
    }

    private void drawCharacter(Graphics2D g, Character c) {
        double x = c.getX(), y = c.getY(), w = c.getWidth(), h = c.getHeight();
        Color body = COUNTRY.getOrDefault(c.getCountryName(), Color.GRAY);
        Color owner = Theme.ownerColor(c.getOwnerSlot());
        int face = c.getFacing();

        if (c.isPowerActive()) drawAura(g, c);

        // Big head (as wide as the hitbox) on top of a narrow body.
        int hd = (int) w;
        int bodyW = 34;
        int bodyX = (int) (x + (w - bodyW) / 2);
        int bodyTop = (int) y + hd - 12;
        int bodyH = (int) (y + h) - bodyTop;

        g.setColor(body);
        g.fillRoundRect(bodyX, bodyTop, bodyW, bodyH, 14, 14);
        g.setColor(owner);
        g.setStroke(new BasicStroke(4));
        g.drawRoundRect(bodyX, bodyTop, bodyW, bodyH, 14, 14);

        int hx = (int) x, hy = (int) y;
        g.setColor(new Color(0xF1C27D));
        g.fillOval(hx, hy, hd, hd);
        g.setColor(Color.DARK_GRAY);
        g.setStroke(new BasicStroke(2));
        g.drawOval(hx, hy, hd, hd);
        int eyeX = hx + hd / 2 + face * 16;
        g.setColor(Color.BLACK);
        g.fillOval(eyeX - 13, hy + 30, 10, 10);
        g.fillOval(eyeX + 3, hy + 30, 10, 10);

        // Feet: a block covering the whole bottom of the body. While kicking it stretches forward
        // until the end of the kick hitbox.
        int feetH = 22;
        int feetX = bodyX - 10;
        int feetW = bodyW + 20;
        int feetY = (int) (y + h) - feetH;
        int from = feetX, to = feetX + feetW;
        if (c.getKickTime() > 0) {
            if (face > 0) to = (int) (x + w + Character.KICK_REACH);
            else from = (int) (x - Character.KICK_REACH);
        }
        g.setColor(new Color(0x222222));
        g.fillRoundRect(from, feetY, to - from, feetH, 14, 14);

        drawEffect(g, c);
        drawLabel(g, c, owner);
    }

    private void drawAura(Graphics2D g, Character c) {
        Color base = AURA.getOrDefault(c.getPower().getName(), Color.WHITE);
        double pulse = 0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 140.0);
        for (int k = 0; k < 3; k++) {
            int grow = (int) (8 + k * 9 + pulse * 6);
            g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 90 - k * 25));
            g.fillOval((int) c.getX() - grow, (int) c.getY() - grow,
                    (int) c.getWidth() + 2 * grow, (int) c.getHeight() + 2 * grow);
        }
    }

    private void drawEffect(Graphics2D g, Character c) {
        Stroke old = g.getStroke();
        Effect e = c.getEffect();
        EffectPainter painter = e == null ? null : effectPainters.get(e.getClass());
        if (painter != null) painter.paint(g, c);
        if (c.isStunned() && painter != stunPainter) drawStars(g, c);
        g.setStroke(old);
    }

    private void drawLightning(Graphics2D g, Character c) {
        Random r = new Random(System.currentTimeMillis() / 70 + c.getOwnerSlot());
        g.setColor(new Color(0xFFE23B));
        g.setStroke(new BasicStroke(3));
        for (int i = 0; i < 6; i++) {
            int px = (int) (c.getX() - 10 + r.nextDouble() * (c.getWidth() + 20));
            int py = (int) (c.getY() + r.nextDouble() * c.getHeight());
            for (int s = 0; s < 4; s++) {
                int nx = px + r.nextInt(25) - 12, ny = py + r.nextInt(25) - 12;
                g.drawLine(px, py, nx, ny);
                px = nx;
                py = ny;
            }
        }
    }

    private void drawPushArrows(Graphics2D g, Character c) {
        int dir = c.getOwnerSlot() == 0 ? -1 : 1;
        g.setColor(new Color(0x2D8CFF));
        g.setStroke(new BasicStroke(5));
        for (int k = 0; k < 3; k++) {
            int px = (int) (c.getX() + c.getWidth() / 2 - dir * (c.getWidth() / 2 + 20 + k * 24));
            int py = (int) (c.getY() + c.getHeight() / 2);
            g.drawLine(px, py - 16, px + dir * 14, py);
            g.drawLine(px + dir * 14, py, px, py + 16);
        }
    }

    private void drawQuestionMark(Graphics2D g, Character c) {
        g.setColor(new Color(0xD946EF));
        g.setFont(new Font("SansSerif", Font.BOLD, 44));
        g.drawString("?", (int) (c.getX() + c.getWidth() / 2 + 34), (int) (c.getY() + 30));
    }

    private void drawStars(Graphics2D g, Character c) {
        double t = System.currentTimeMillis() / 250.0;
        g.setColor(new Color(0xFFD700));
        for (int k = 0; k < 3; k++) {
            double a = t + k * 2 * Math.PI / 3;
            g.fillOval((int) (c.getX() + c.getWidth() / 2 + Math.cos(a) * 34 - 6), (int) (c.getY() - 4 + Math.sin(a) * 9 - 6), 12, 12);
        }
    }

    private void drawLabel(Graphics2D g, Character c, Color owner) {
        String text = "P" + (c.getOwnerSlot() + 1);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        FontMetrics fm = g.getFontMetrics();
        int bw = fm.stringWidth(text) + 14;
        int bx = (int) (c.getX() + c.getWidth() / 2 - bw / 2.0);
        int by = (int) c.getY() - 34;
        g.setColor(owner);
        g.fillRoundRect(bx, by, bw, 24, 10, 10);
        g.fillPolygon(new int[] {bx + bw / 2 - 6, bx + bw / 2 + 6, bx + bw / 2}, new int[] {by + 24, by + 24, by + 32}, 3);
        g.setColor(Color.WHITE);
        g.drawString(text, bx + 7, by + 18);
    }

    private void drawBall(Graphics2D g, Ball b) {
        double r = b.getWidth() / 2;
        double cx = b.getCenterX(), cy = b.getCenterY();
        if (b.isPowerShot()) {
            Color base = AURA.getOrDefault(b.getShooter().getPower().getName(), Color.WHITE);
            for (int k = 0; k < 3; k++) {
                int grow = 6 + k * 8;
                g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 120 - k * 35));
                g.fillOval((int) (cx - r - grow), (int) (cy - r - grow), (int) (2 * (r + grow)), (int) (2 * (r + grow)));
            }
        }
        g.setColor(Color.WHITE);
        g.fillOval((int) (cx - r), (int) (cy - r), (int) (2 * r), (int) (2 * r));
        g.setColor(new Color(0x222222));
        g.setStroke(new BasicStroke(2));
        g.drawOval((int) (cx - r), (int) (cy - r), (int) (2 * r), (int) (2 * r));
        double ang = b.getX() / r;
        int[] px = new int[5], py = new int[5];
        for (int i = 0; i < 5; i++) {
            px[i] = (int) (cx + Math.cos(ang + i * 2 * Math.PI / 5) * r * 0.5);
            py[i] = (int) (cy + Math.sin(ang + i * 2 * Math.PI / 5) * r * 0.5);
        }
        g.fillPolygon(px, py, 5);
    }
}
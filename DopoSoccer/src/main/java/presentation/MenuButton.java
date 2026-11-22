package presentation;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Botón de menú con estilo propio. Se reutiliza en todos los paneles de configuración.
 */
public class MenuButton extends JButton {

    public static final int BUTTON_WIDTH = 640;
    public static final int BUTTON_HEIGHT = 80;
    private static final float FONT_SIZE = 24f;
    private static final int ARC = 24;

    private static final Color NORMAL_COLOR = new Color(0, 0, 0, 150);
    private static final Color HOVER_COLOR = new Color(255, 255, 255, 60);
    private static final Color BORDER_COLOR = new Color(255, 255, 255, 200);
    private static final Color DISABLED_TEXT_COLOR = new Color(255, 255, 255, 90);
    private static final Color DISABLED_BORDER_COLOR = new Color(255, 255, 255, 70);

    private boolean hovered;

    public MenuButton(String text, Runnable onClick) {
        super(text);
        this.hovered = false;

        setFont(FontLoader.load(FONT_SIZE));
        setForeground(Color.WHITE);
        setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
        addActionListener(e -> onClick.run());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            hovered = false;
        }
        setCursor(Cursor.getPredefinedCursor(enabled ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        boolean enabled = isEnabled();

        // Fondo y borde (el hover solo aplica si está habilitado)
        g2.setColor(NORMAL_COLOR);
        g2.fillRoundRect(0, 0, w, h, ARC, ARC);
        if (hovered && enabled) {
            g2.setColor(HOVER_COLOR);
            g2.fillRoundRect(0, 0, w, h, ARC, ARC);
        }
        g2.setColor(enabled ? BORDER_COLOR : DISABLED_BORDER_COLOR);
        g2.drawRoundRect(1, 1, w - 3, h - 3, ARC, ARC);

        // Texto centrado (gris y opaco si está deshabilitado)
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int textX = (w - fm.stringWidth(getText())) / 2;
        int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(enabled ? getForeground() : DISABLED_TEXT_COLOR);
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }
}
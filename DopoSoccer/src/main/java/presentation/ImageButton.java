package presentation;

import javax.swing.JButton;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * Botón cuadrado dibujado a partir de una imagen. Se aclara al pasar el mouse.
 */
public class ImageButton extends JButton {

    private static final float NORMAL_ALPHA = 0.85f;

    private final BufferedImage image;
    private boolean hovered;

    public ImageButton(BufferedImage image, int size, Runnable onClick) {
        this.image = image;
        this.hovered = false;

        setPreferredSize(new Dimension(size, size));
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
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, hovered ? 1.0f : NORMAL_ALPHA));

        if (image != null) {
            g2.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        } else {
            // Respaldo si la imagen no cargó
            g2.setColor(Color.GRAY);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        g2.dispose();
    }
}
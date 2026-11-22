package presentation;

import domain.ErrorLog;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class StartPanel extends JPanel {

    private static final String BACKGROUND_PATH = "/images/start/background.png";
    private static final String TITLE_PATH = "/images/start/title.png";
    private static final String CLICK_SOUND = "/audio/click.wav";

    private static final int TITLE_SIZE = 800;
    private static final int BLINK_DELAY_MS = 600;
    private static final String PROMPT_TEXT = "PRESS ANY KEY TO CONTINUE";

    private final PanelRotation rotation;
    private final AudioPlay audio;

    private final BufferedImage background;
    private final BufferedImage title;
    private final Font promptFont;

    private boolean promptVisible;
    private final Timer blinkTimer;

    public StartPanel(PanelRotation rotation, AudioPlay audio) {
        this.rotation = rotation;
        this.audio = audio;
        this.background = loadImage(BACKGROUND_PATH);
        this.title = loadImage(TITLE_PATH);
        this.promptFont = FontLoader.load(28f);
        this.promptVisible = true;

        setPreferredSize(new Dimension(DopoSoccerGUI.MENU_WIDTH, DopoSoccerGUI.MENU_HEIGHT));
        setFocusable(true);

        this.blinkTimer = new Timer(BLINK_DELAY_MS, e -> {
            if (isShowing()) {
                promptVisible = !promptVisible;
                repaint();
            }
        });
        this.blinkTimer.start();

        prepareKeyBindings();
    }

    private BufferedImage loadImage(String path) {
        URL url = getClass().getResource(path);
        if (url == null) {
            ErrorLog.warn("Imagen no encontrada: " + path);
            return null;
        }
        try {
            return ImageIO.read(url);
        } catch (IOException e) {
            ErrorLog.error("No se pudo leer la imagen: " + path, e);
            return null;
        }
    }

    /**
     * Registra cada tecla del teclado para que cualquiera avance al siguiente panel.
     */
    private void prepareKeyBindings() {
        AbstractAction continueAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Los bindings de la ventana siguen activos aunque el panel esté oculto
                if (!isShowing()) {
                    return;
                }
                audio.playSound(CLICK_SOUND);
                rotation.next();
            }
        };

        for (int keyCode = KeyEvent.VK_ENTER; keyCode <= KeyEvent.VK_CONTEXT_MENU; keyCode++) {
            KeyStroke stroke = KeyStroke.getKeyStroke(keyCode, 0);
            String actionKey = "continue-" + keyCode;
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, actionKey);
            getActionMap().put(actionKey, continueAction);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();

        // Fondo por default
        if (background != null) {
            g2.drawImage(background, 0, 0, w, h, null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, w, h);
        }

        // Logo centrado
        if (title != null) {
            int x = (w - TITLE_SIZE) / 2;
            int y = (h - TITLE_SIZE) / 2;
            g2.drawImage(title, x, y, TITLE_SIZE, TITLE_SIZE, null);
        }

        // Texto parpadeante debajo del logo
        if (promptVisible) {
            g2.setFont(promptFont);
            FontMetrics fm = g2.getFontMetrics();
            int textX = (w - fm.stringWidth(PROMPT_TEXT)) / 2;
            int textY = h - 30;

            g2.setColor(new Color(0, 0, 0, 160)); // sombra para dar legibilidad
            g2.drawString(PROMPT_TEXT, textX + 2, textY + 2);
            g2.setColor(Color.WHITE);
            g2.drawString(PROMPT_TEXT, textX, textY);
        }
        g2.dispose();
    }
}
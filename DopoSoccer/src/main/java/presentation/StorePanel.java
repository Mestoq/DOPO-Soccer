package presentation;

import domain.DopoSoccer;
import domain.DopoSoccerException;
import domain.ErrorLog;
import domain.player.Player;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class StorePanel extends JPanel {

    private static final String[] STATS = {
            Player.STAT_SPEED, Player.STAT_JUMP, Player.STAT_KICK, Player.STAT_SURVIVAL
    };

    private static final String BACKGROUND_PATH = "/images/start/background.png";
    private static final String BACK_PATH = "/images/whovswho/back.png";
    private static final String CLICK_SOUND = "/audio/click.wav";

    private static final String[] PROFILES = {DopoSoccer.PLAYER_1, DopoSoccer.PLAYER_2};
    private static final String[] TITLES = {"JUGADOR 1", "JUGADOR 2"};
    private static final String[] STAT_LABELS = {"VELOCIDAD", "SALTO", "PATADA", "VIDA"};

    private static final int BACK_SIZE = 100;
    private static final int BACK_MARGIN = 40;

    private static final int TITLE_Y = 190;
    private static final int POINTS_Y = 250;
    private static final int ROWS_TOP = 300;
    private static final int ROW_HEIGHT = 80;
    private static final int ROW_GAP = 40;
    private static final int ROW_X = 30;          // margen de la fila dentro de la columna
    private static final int ROW_WIDTH = 740;
    private static final int LABEL_X = 60;
    private static final int LEVEL_X = 330;
    private static final int BUTTON_X = 470;
    private static final int BUTTON_WIDTH = 280;

    private static final float TITLE_FONT_SIZE = 40f;
    private static final float TEXT_FONT_SIZE = 20f;

    private final PanelRotation rotation;
    private final AudioPlay audio;
    private final DopoSoccer dopo;

    private final BufferedImage background;
    private final Font titleFont;
    private final Font textFont;

    private final MenuButton[][] upgradeButtons = new MenuButton[PROFILES.length][STATS.length];

    public StorePanel(PanelRotation rotation, AudioPlay audio, DopoSoccer dopo) {
        this.rotation = rotation;
        this.audio = audio;
        this.dopo = dopo;
        this.background = loadImage(BACKGROUND_PATH);
        this.titleFont = FontLoader.load(TITLE_FONT_SIZE);
        this.textFont = FontLoader.load(TEXT_FONT_SIZE);

        setPreferredSize(new Dimension(DopoSoccerGUI.MENU_WIDTH, DopoSoccerGUI.MENU_HEIGHT));
        setLayout(null); // tamaño fijo 1600x900: los botones se ubican con setBounds
        prepareElements();

        // Los puntos cambian al jugar o cargar un perfil: se actualiza cada vez que se muestra
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refresh();
            }
        });
        refresh();
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

    private int columnX(int column) {
        return column * (DopoSoccerGUI.MENU_WIDTH / PROFILES.length);
    }

    private int rowY(int row) {
        return ROWS_TOP + row * (ROW_HEIGHT + ROW_GAP);
    }

    private void prepareElements() {
        ImageButton backButton = new ImageButton(loadImage(BACK_PATH), BACK_SIZE, this::onBack);
        backButton.setBounds(BACK_MARGIN, BACK_MARGIN, BACK_SIZE, BACK_SIZE);
        add(backButton);

        for (int column = 0; column < PROFILES.length; column++) {
            for (int row = 0; row < STATS.length; row++) {
                final String profile = PROFILES[column];
                final String stat = STATS[row];
                MenuButton button = new MenuButton("", () -> onUpgrade(profile, stat));
                button.setBounds(columnX(column) + BUTTON_X, rowY(row), BUTTON_WIDTH, ROW_HEIGHT);
                upgradeButtons[column][row] = button;
                add(button);
            }
        }
    }

    // Acciones

    private void onBack() {
        audio.playSound(CLICK_SOUND);
        rotation.goTo(DopoSoccerGUI.MENU_CONFIG_PANEL);
    }

    private void onUpgrade(String profile, String stat) {
        audio.playSound(CLICK_SOUND);
        try {
            dopo.upgradeStat(profile, stat);
        } catch (DopoSoccerException e) {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), e.getMessage(),
                    "No se pudo mejorar", JOptionPane.ERROR_MESSAGE);
        }
        refresh();
    }

    /**
     * Actualiza el texto y el estado de cada botón (costo, o MAX en nivel 10) y repinta.
     */
    private void refresh() {
        for (int column = 0; column < PROFILES.length; column++) {
            for (int row = 0; row < STATS.length; row++) {
                MenuButton button = upgradeButtons[column][row];
                int cost = dopo.getUpgradeCost(PROFILES[column], STATS[row]);
                if (cost < 0) {
                    button.setText("MAX");
                    button.setEnabled(false);
                } else {
                    button.setText(cost + " PTS");
                    button.setEnabled(true);
                }
            }
        }
        repaint();
    }

    // Dibujo

    private void drawText(Graphics2D g2, String text, int x, int y) {
        g2.setColor(new Color(0, 0, 0, 160)); // sombra para legibilidad
        g2.drawString(text, x + 2, y + 2);
        g2.setColor(Color.WHITE);
        g2.drawString(text, x, y);
    }

    private void drawCentered(Graphics2D g2, String text, int centerX, int y) {
        FontMetrics fm = g2.getFontMetrics();
        drawText(g2, text, centerX - fm.stringWidth(text) / 2, y);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (background != null) {
            g2.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        for (int column = 0; column < PROFILES.length; column++) {
            int colX = columnX(column);
            int centerX = colX + (getWidth() / PROFILES.length) / 2;

            g2.setFont(titleFont);
            drawCentered(g2, TITLES[column], centerX, TITLE_Y);

            g2.setFont(textFont);
            drawCentered(g2, "PUNTOS: " + dopo.getPoints(PROFILES[column]), centerX, POINTS_Y);

            for (int row = 0; row < STATS.length; row++) {
                int y = rowY(row);

                // Fondo de la fila
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRoundRect(colX + ROW_X, y, ROW_WIDTH, ROW_HEIGHT, 24, 24);

                // Rótulo y nivel actual, centrados verticalmente en la fila
                g2.setFont(textFont);
                FontMetrics fm = g2.getFontMetrics();
                int baseline = y + (ROW_HEIGHT - fm.getHeight()) / 2 + fm.getAscent();
                drawText(g2, STAT_LABELS[row], colX + LABEL_X, baseline);
                int level = dopo.getStatLevel(PROFILES[column], STATS[row]);
                drawText(g2, level + "/" + Player.MAX_STAT_LEVEL, colX + LEVEL_X, baseline);
            }
        }
        g2.dispose();
    }
}
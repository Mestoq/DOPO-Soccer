package presentation;

import domain.ErrorLog;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class MatchModePanel extends JPanel {

    public enum Mode { NONE, TIME, GOALS }

    private static final String LEFT_PATH = "/images/matchmode/left.png";
    private static final String MIDDLE_PATH = "/images/matchmode/middle.png";
    private static final String RIGHT_PATH = "/images/matchmode/right.png";
    private static final String BACK_PATH = "/images/matchmode/back.png";
    private static final String CLICK_SOUND = "/audio/click.wav";

    private static final int BACK_SIZE = 100;
    private static final int BACK_Y = 40;
    private static final int CONTINUE_BOTTOM_MARGIN = 20;

    // Texto con el valor elegido, sobre el botón CONTINUAR

    private static final float VALUE_FONT_SIZE = 28f;
    private static final int VALUE_GAP= 60;

    // Opciones del diálogo: 1..MAX
    private static final int MAX_MINUTES = 7;
    private static final int MAX_GOALS = 10;
    private static final int DEFAULT_MINUTES = 3;
    private static final int DEFAULT_GOALS = 5;

    private static final float ALPHA_FULL = 1.0f;
    private static final float ALPHA_IDLE = 0.85f;
    private static final float ALPHA_DIMMED = 0.35f;

    private final PanelRotation rotation;
    private final AudioPlay audio;

    private final BufferedImage left;
    private final BufferedImage middle;
    private final BufferedImage right;
    private final Font valueFont;

    private Mode selected;
    private Mode hovered;
    private int minutes;
    private int goals;

    private MenuButton continueButton;

    public MatchModePanel(PanelRotation rotation, AudioPlay audio) {
        this.rotation = rotation;
        this.audio = audio;
        this.left = loadImage(LEFT_PATH);
        this.middle = loadImage(MIDDLE_PATH);
        this.right = loadImage(RIGHT_PATH);
        this.valueFont = FontLoader.load(VALUE_FONT_SIZE);
        this.selected = Mode.NONE;
        this.hovered = Mode.NONE;
        this.minutes = DEFAULT_MINUTES;
        this.goals = DEFAULT_GOALS;

        setPreferredSize(new Dimension(DopoSoccerGUI.MENU_WIDTH, DopoSoccerGUI.MENU_HEIGHT));
        setLayout(null);
        prepareElements();
        prepareMouse();
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

    private void prepareElements() {
        int w = DopoSoccerGUI.MENU_WIDTH;
        int h = DopoSoccerGUI.MENU_HEIGHT;

        // Botón volver: cuadrado, sobre la imagen central
        ImageButton backButton = new ImageButton(loadImage(BACK_PATH), BACK_SIZE, this::onBack);
        backButton.setBounds((w - BACK_SIZE) / 2, BACK_Y, BACK_SIZE, BACK_SIZE);
        add(backButton);

        // Botón continuar
        continueButton = new MenuButton("CONTINUAR", this::onContinue);
        continueButton.setEnabled(false);
        continueButton.setBounds((w - MenuButton.BUTTON_WIDTH) / 2,
                h - MenuButton.BUTTON_HEIGHT - CONTINUE_BOTTOM_MARGIN,
                MenuButton.BUTTON_WIDTH, MenuButton.BUTTON_HEIGHT);
        add(continueButton);
    }

    /**
     * Un solo listener decide el lado por la coordenada X: la mitad izquierda es
     * POR TIEMPO y la derecha POR GOLES. Los botones consumen sus propios eventos.
     */
    private void prepareMouse() {
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateHover(sideAt(e.getX()));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                updateHover(Mode.NONE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                select(sideAt(e.getX()));
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    private Mode sideAt(int x) {
        return x < getWidth() / 2 ? Mode.TIME : Mode.GOALS;
    }

    private void updateHover(Mode side) {
        if (hovered != side) {
            hovered = side;
            repaint();
        }
    }

    /**
     * Abre el diálogo de valor para la mitad pulsada. Si se cierra sin elegir,
     * no cambia ni el modo ni el valor anteriores.
     */
    private void select(Mode mode) {
        audio.playSound(CLICK_SOUND);
        int value = askValue(mode);
        if (value < 0) {
            return;
        }
        if (mode == Mode.TIME) {
            minutes = value;
        } else {
            goals = value;
        }
        selected = mode;
        continueButton.setEnabled(true);
        repaint();
    }

    /**
     * Muestra el diálogo con las opciones permitidas (1..máximo).
     * @return el valor elegido, o -1 si se cerró el diálogo sin elegir
     */
    private int askValue(Mode mode) {
        boolean time = (mode == Mode.TIME);
        int max = time ? MAX_MINUTES : MAX_GOALS;
        int current = time ? minutes : goals;

        String[] options = new String[max];
        for (int i = 0; i < max; i++) {
            int n = i + 1;
            options[i] = time ? n + " MIN" : n + (n == 1 ? " GOL" : " GOLES");
        }

        int choice = JOptionPane.showOptionDialog(
                SwingUtilities.getWindowAncestor(this),
                time ? "Selecciona la duración del partido" : "Selecciona los goles para ganar",
                time ? "Partido por tiempo" : "Partido por goles",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[current - 1]);

        return choice < 0 ? -1 : choice + 1;
    }

    // Acciones de los botones

    private void onBack() {
        audio.playSound(CLICK_SOUND);
        rotation.back();
    }

    private void onContinue() {
        audio.playSound(CLICK_SOUND);
        rotation.next();
    }

    // Datos para DopoSoccer.preFixMatch(type, field, limitValue)

    public Mode getMode() { return selected; }

    /** @return "TIME", "GOALS" o null si aún no se ha elegido */
    public String getMatchType() {
        if (selected == Mode.TIME) return "TIME";
        if (selected == Mode.GOALS) return "GOALS";
        return null;
    }

    /** @return segundos si es por tiempo, número de goles si es por goles, 0 si no hay elección */
    public double getLimitValue() {
        if (selected == Mode.TIME) return minutes * 60.0;
        if (selected == Mode.GOALS) return goals;
        return 0.0;
    }

    // Dibujo

    private float alphaFor(Mode side) {
        if (selected != Mode.NONE) {
            return selected == side ? ALPHA_FULL : ALPHA_DIMMED;
        }
        return hovered == side ? ALPHA_FULL : ALPHA_IDLE;
    }

    private void drawHalf(Graphics2D g2, BufferedImage image, int x, int w, int h, Mode side) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaFor(side)));
        if (image != null) {
            g2.drawImage(image, x, 0, w, h, null);
        } else {
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(x, 0, w, h);
        }
    }


    /* Esto Lo dejo para reautilizar en caso de algo
    private void drawValue(Graphics2D g2) {
        String text = (selected == Mode.TIME) ? minutes + " MIN" : goals + (goals == 1 ? " GOL" : " GOLES");
        int halfCenterX = (selected == Mode.TIME) ? getWidth() / 4 : (getWidth() * 3) / 4;

        g2.setComposite(AlphaComposite.SrcOver);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(valueFont);
        FontMetrics fm = g2.getFontMetrics();
        int textX = halfCenterX - fm.stringWidth(text) / 2;
        int textY = getHeight() - MenuButton.BUTTON_HEIGHT - CONTINUE_BOTTOM_MARGIN - VALUE_GAP;

        g2.setColor(new Color(0, 0, 0, 160)); // sombra para legibilidad
        g2.drawString(text, textX + 2, textY + 2);
        g2.setColor(Color.WHITE);
        g2.drawString(text, textX, textY);
    }

     */

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();

        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, w, h);

        drawHalf(g2, left, 0, w / 2, h, Mode.TIME);
        drawHalf(g2, right, w / 2, w / 2, h, Mode.GOALS);

        // Divisor central: alto completo, proporción conservada, centrado
        if (middle != null) {
            g2.setComposite(AlphaComposite.SrcOver);
            int mw = middle.getWidth() * h / middle.getHeight();
            g2.drawImage(middle, (w - mw) / 2, 0, mw, h, null);
        }

        // Valor elegido (solo con un modo seleccionado)
        /*
        if (selected != Mode.NONE) {
            drawValue(g2);
        }
        g2.dispose();

         */
    }
}
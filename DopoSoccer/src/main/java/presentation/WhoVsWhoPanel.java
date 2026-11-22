package presentation;

import domain.ErrorLog;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class WhoVsWhoPanel extends JPanel {

    public enum Mode { NONE, HUMAN_VS_HUMAN, HUMAN_VS_CPU }

    private static final String LEFT_PATH = "/images/whovswho/left.png";
    private static final String MIDDLE_PATH = "/images/whovswho/middle.png";
    private static final String RIGHT_PATH = "/images/whovswho/right.png";
    private static final String BACK_PATH = "/images/whovswho/back.png";
    private static final String CLICK_SOUND = "/audio/click.wav";


    private static final int BACK_SIZE = 100;
    private static final int BACK_Y = 40;
    private static final int CONTINUE_BOTTOM_MARGIN = 40;

    private static final float ALPHA_FULL = 1.0f;
    private static final float ALPHA_IDLE = 0.85f;
    private static final float ALPHA_DIMMED = 0.35f;

    private final PanelRotation rotation;
    private final AudioPlay audio;

    private final BufferedImage left;
    private final BufferedImage middle;
    private final BufferedImage right;

    private Mode selected;
    private Mode hovered;
    private MenuButton continueButton;

    public WhoVsWhoPanel(PanelRotation rotation, AudioPlay audio) {
        this.rotation = rotation;
        this.audio = audio;
        this.left = loadImage(LEFT_PATH);
        this.middle = loadImage(MIDDLE_PATH);
        this.right = loadImage(RIGHT_PATH);
        this.selected = Mode.NONE;
        this.hovered = Mode.NONE;

        setPreferredSize(new Dimension(DopoSoccerGUI.MENU_WIDTH, DopoSoccerGUI.MENU_HEIGHT));
        setLayout(null); // tamaño fijo 1600x900: los botones se ubican con setBounds
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

        // Botón continuar: abajo, sobre todo lo demás, deshabilitado hasta elegir un modo
        continueButton = new MenuButton("CONTINUAR", this::onContinue);
        continueButton.setEnabled(false);
        continueButton.setBounds((w - MenuButton.BUTTON_WIDTH) / 2,
                h - MenuButton.BUTTON_HEIGHT - CONTINUE_BOTTOM_MARGIN,
                MenuButton.BUTTON_WIDTH, MenuButton.BUTTON_HEIGHT);
        add(continueButton);
    }

    /**
     * Un solo listener decide el lado por la coordenada X: la mitad izquierda es
     * Humano vs Humano y la derecha Humano vs CPU. Los botones consumen sus propios eventos.
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
        return x < getWidth() / 2 ? Mode.HUMAN_VS_HUMAN : Mode.HUMAN_VS_CPU;
    }

    private void updateHover(Mode side) {
        if (hovered != side) {
            hovered = side;
            repaint();
        }
    }

    private void select(Mode mode) {
        if (selected != mode) {
            audio.playSound(CLICK_SOUND);
            selected = mode;
            continueButton.setEnabled(true);
            repaint();
        }
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

    public Mode getMode() { return selected; }

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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();

        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, w, h);

        drawHalf(g2, left, 0, w / 2, h, Mode.HUMAN_VS_HUMAN);
        drawHalf(g2, right, w / 2, w / 2, h, Mode.HUMAN_VS_CPU);

        // Divisor central: alto completo, proporción conservada, centrado
        if (middle != null) {
            g2.setComposite(AlphaComposite.SrcOver);
            int mw = middle.getWidth() * h / middle.getHeight();
            g2.drawImage(middle, (w - mw) / 2, 0, mw, h, null);
        }
        g2.dispose();
    }
}
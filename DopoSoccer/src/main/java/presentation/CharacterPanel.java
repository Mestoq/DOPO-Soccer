package presentation;

import domain.ErrorLog;
import domain.character.Character_;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
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

public class CharacterPanel extends JPanel {

    private static final float LABEL_FONT_SIZE = 16f;
    private static final int LABEL_HEIGHT = 40;

    private static final String[] CHARACTERS = {
            Character_.KOREA, Character_.CAMEROON, Character_.NIGERIA, Character_.USA
    };
    private static final String[] IMAGE_PATHS = {
            "/images/character/korea.png",
            "/images/character/cameroon.png",
            "/images/character/nigeria.png",
            "/images/character/usa.png"
    };

    private static final String BACKGROUND_PATH = "/images/character/background.png";
    private static final String BACK_PATH = "/images/whovswho/back.png";
    private static final String CLICK_SOUND = "/audio/click.wav";

    private static final int COLUMNS = 2;       // jugadores
    private static final int GRID_COLS = 2;     // cuadrícula 2x2 por columna
    private static final int GRID_ROWS = 2;

    private static final int BACK_SIZE = 100;
    private static final int BACK_Y = 40;
    private static final int CONTINUE_BOTTOM_MARGIN = 40;

    private static final int TITLE_Y = 190;
    private static final float TITLE_FONT_SIZE = 40f;
    private static final int GRID_TOP = 240;
    private static final int GRID_SIDE_MARGIN = 60;
    private static final int CELL_PADDING = 20;

    private static final float ALPHA_FULL = 1.0f;
    private static final float ALPHA_IDLE = 0.85f;
    private static final float ALPHA_DIMMED = 0.35f;

    private static final int NONE = -1;

    private final PanelRotation rotation;
    private final AudioPlay audio;

    private final BufferedImage background;
    private final BufferedImage[] images;
    private final Font titleFont;

    // Personaje elegido por columna (índice en CHARACTERS) y celda bajo el mouse
    private final int[] selected = {NONE, NONE};
    private final Font labelFont;
    private int hoveredColumn = NONE;
    private int hoveredCell = NONE;

    private MenuButton continueButton;

    public CharacterPanel(PanelRotation rotation, AudioPlay audio) {
        this.rotation = rotation;
        this.audio = audio;
        this.background = loadImage(BACKGROUND_PATH);
        this.titleFont = FontLoader.load(TITLE_FONT_SIZE);
        this.labelFont = FontLoader.load(LABEL_FONT_SIZE);

        this.images = new BufferedImage[IMAGE_PATHS.length];
        for (int i = 0; i < IMAGE_PATHS.length; i++) {
            this.images[i] = loadImage(IMAGE_PATHS[i]);
        }

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

        ImageButton backButton = new ImageButton(loadImage(BACK_PATH), BACK_SIZE, this::onBack);
        backButton.setBounds((w - BACK_SIZE) / 2, BACK_Y, BACK_SIZE, BACK_SIZE);
        add(backButton);

        // Deshabilitado hasta que ambos jugadores tengan personaje
        continueButton = new MenuButton("CONTINUAR", this::onContinue);
        continueButton.setEnabled(false);
        continueButton.setBounds((w - MenuButton.BUTTON_WIDTH) / 2,
                h - MenuButton.BUTTON_HEIGHT - CONTINUE_BOTTOM_MARGIN,
                MenuButton.BUTTON_WIDTH, MenuButton.BUTTON_HEIGHT);
        add(continueButton);
    }

    // Geometría de la cuadrícula

    private int columnAt(int x) {
        return x < getWidth() / COLUMNS ? 0 : 1;
    }

    private int gridBottom() {
        return getHeight() - MenuButton.BUTTON_HEIGHT - CONTINUE_BOTTOM_MARGIN - 30;
    }

    private int cellWidth() {
        return (getWidth() / COLUMNS - 2 * GRID_SIDE_MARGIN) / GRID_COLS;
    }

    private int cellHeight() {
        return (gridBottom() - GRID_TOP) / GRID_ROWS;
    }

    private int cellX(int column, int cellIndex) {
        return column * (getWidth() / COLUMNS) + GRID_SIDE_MARGIN + (cellIndex % GRID_COLS) * cellWidth();
    }

    private int cellY(int cellIndex) {
        return GRID_TOP + (cellIndex / GRID_COLS) * cellHeight();
    }

    /**
     * @return índice de la celda bajo el punto (x, y) dentro de su columna, o NONE
     */
    private int cellAt(int x, int y) {
        int column = columnAt(x);
        int localX = x - (column * (getWidth() / COLUMNS) + GRID_SIDE_MARGIN);
        int localY = y - GRID_TOP;
        if (localX < 0 || localY < 0
                || localX >= cellWidth() * GRID_COLS || localY >= cellHeight() * GRID_ROWS) {
            return NONE;
        }
        int index = (localY / cellHeight()) * GRID_COLS + (localX / cellWidth());
        return index < CHARACTERS.length ? index : NONE;
    }

    // Interacción

    private void prepareMouse() {
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateHover(columnAt(e.getX()), cellAt(e.getX(), e.getY()));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                updateHover(NONE, NONE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int cell = cellAt(e.getX(), e.getY());
                if (cell != NONE) {
                    select(columnAt(e.getX()), cell);
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    private void updateHover(int column, int cell) {
        if (hoveredColumn != column || hoveredCell != cell) {
            hoveredColumn = column;
            hoveredCell = cell;
            repaint();
        }
    }

    private void select(int column, int cell) {
        if (selected[column] != cell) {
            audio.playSound(CLICK_SOUND);
            selected[column] = cell;
            continueButton.setEnabled(selected[0] != NONE && selected[1] != NONE);
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

    // Datos para DopoSoccer.preFixCharacters(p1CharacterName, p2CharacterName)

    /** @return nombre del personaje del jugador 1 o null si aún no eligió */
    public String getPlayer1Character() {
        return selected[0] == NONE ? null : CHARACTERS[selected[0]];
    }

    /** @return nombre del personaje del jugador 2 o null si aún no eligió */
    public String getPlayer2Character() {
        return selected[1] == NONE ? null : CHARACTERS[selected[1]];
    }

    // Dibujo

    private float alphaFor(int column, int cell) {
        if (selected[column] != NONE) {
            return selected[column] == cell ? ALPHA_FULL : ALPHA_DIMMED;
        }
        return (hoveredColumn == column && hoveredCell == cell) ? ALPHA_FULL : ALPHA_IDLE;
    }

    private void drawTitle(Graphics2D g2, String text, int column) {
        g2.setComposite(AlphaComposite.SrcOver);
        g2.setFont(titleFont);
        FontMetrics fm = g2.getFontMetrics();
        int centerX = column * (getWidth() / COLUMNS) + (getWidth() / COLUMNS) / 2;
        int textX = centerX - fm.stringWidth(text) / 2;

        g2.setColor(new Color(0, 0, 0, 160)); // sombra para legibilidad
        g2.drawString(text, textX + 3, TITLE_Y + 3);
        g2.setColor(Color.WHITE);
        g2.drawString(text, textX, TITLE_Y);
    }

    /**
     * Dibuja la imagen dentro de su celda conservando la proporción y centrada.
     */
    private void drawCharacter(Graphics2D g2, int column, int cell) {
            BufferedImage image = images[cell];
            int boxX = cellX(column, cell) + CELL_PADDING;
            int boxY = cellY(cell) + CELL_PADDING;
            int boxW = cellWidth() - 2 * CELL_PADDING;
            int boxH = cellHeight() - 2 * CELL_PADDING - LABEL_HEIGHT;

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaFor(column, cell)));
            if (image == null) {
                g2.setColor(Color.DARK_GRAY);
                g2.fillRect(boxX, boxY, boxW, boxH);
            } else {
                double scale = Math.min((double) boxW / image.getWidth(), (double) boxH / image.getHeight());
                int drawW = (int) (image.getWidth() * scale);
                int drawH = (int) (image.getHeight() * scale);
                g2.drawImage(image, boxX + (boxW - drawW) / 2, boxY + (boxH - drawH) / 2, drawW, drawH, null);
            }

            // Rótulo con el país, con la misma opacidad que la imagen
            g2.setFont(labelFont);
            FontMetrics fm = g2.getFontMetrics();
            String label = CHARACTERS[cell];
            int textX = boxX + (boxW - fm.stringWidth(label)) / 2;
            int textY = boxY + boxH + fm.getAscent() + 8;
            g2.setColor(new Color(0, 0, 0, 160));
            g2.drawString(label, textX + 2, textY + 2);
            g2.setColor(Color.WHITE);
            g2.drawString(label, textX, textY);
        }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (background != null) {
            g2.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        drawTitle(g2, "JUGADOR 1", 0);
        drawTitle(g2, "JUGADOR 2", 1);

        for (int column = 0; column < COLUMNS; column++) {
            for (int cell = 0; cell < CHARACTERS.length; cell++) {
                drawCharacter(g2, column, cell);
            }
        }
        g2.dispose();
    }
}
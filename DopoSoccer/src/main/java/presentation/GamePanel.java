package presentation;

import domain.DopoSoccer;
import domain.DopoSoccerException;
import domain.ErrorLog;
import domain.character.Character_;
import domain.match.InGameInfo;
import domain.match.Match;
import domain.match.MatchSnapshot;
import domain.physics.Ball;
import domain.physics.Field;
import domain.physics.Goal;
import domain.player.Cpu;
import domain.player.HumanPlayer;
import domain.player.Player;
import domain.power.effect.Effect;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GamePanel extends JPanel {

    // Sprites generales
    private static final String BACKGROUND_PATH = "/images/game/background.png";
    private static final String GOAL_LEFT_PATH = "/images/game/goal_left.png";
    private static final String GOAL_RIGHT_PATH = "/images/game/goal_right.png";
    private static final String BALL_PATH = "/images/game/ball.png";
    private static final String STUN_PATH = "/images/game/effects/stun.png";

    // HUD
    private static final String BAR_FRAME_PATH = "/images/game/hud/bar_frame.png";
    private static final String HEALTH_FILL_PATH = "/images/game/hud/health_fill.png";
    private static final String POWER_FILL_PATH = "/images/game/hud/power_fill.png";
    private static final String[][] FLAG_SPRITES = {
            {"COREA DEL SUR", "/images/game/flags/korea.png"},
            {"CAMERUN", "/images/game/flags/cameroon.png"},
            {"NIGERIA", "/images/game/flags/nigeria.png"},
            {"ESTADOS UNIDOS", "/images/game/flags/usa.png"}
    };

    // Personaje: {país (clave de Character_.create), sprite normal, sprite de patada, aura del poder}
    private static final String[][] CHARACTER_SPRITES = {
            {"COREA DEL SUR", "/images/character/korea.png",
                    "/images/game/characters/korea_kick.png", "/images/game/aura/korea.png"},
            {"CAMERUN", "/images/character/cameroon.png",
                    "/images/game/characters/cameroon_kick.png", "/images/game/aura/cameroon.png"},
            {"NIGERIA", "/images/character/nigeria.png",
                    "/images/game/character/nigeria_kick.png", "/images/game/aura/nigeria.png"},
            {"ESTADOS UNIDOS", "/images/character/usa.png",
                    "/images/game/characters/usa_kick.png", "/images/game/aura/usa.png"}
    };
    // Icono del efecto negativo sobre la cabeza (clave = Effect.getName())
    private static final String[][] EFFECT_SPRITES = {
            {"Empuje", "/images/game/effects/push.png"},
            {"Electrocutado", "/images/game/effects/electrocuted.png"},
            {"Inconsciente", "/images/game/effects/unconscious.png"},
            {"Confusión", "/images/game/effects/confusion.png"}
    };
    // Sprite sobre el balón con tiro de poder en vuelo (clave = Power.getPowerName()).
    // Ilusión no tiene: si el balón real se distinguiera de los falsos, el poder no serviría.
    private static final String[][] BALL_POWER_SPRITES = {
            {"Aura Azul", "/images/game/ballpower/blue_aura.png"},
            {"Rayo", "/images/game/ballpower/lightning.png"},
            {"Tornado", "/images/game/ballpower/tornado.png"}
    };

    private static final String CLICK_SOUND = "/audio/click.wav";
    private static final String SAVE_EXTENSION = "dat";
    private static final FileNameExtensionFilter SAVE_FILTER =
            new FileNameExtensionFilter("Partido DOPOSoccer (*." + SAVE_EXTENSION + ")", SAVE_EXTENSION);

    // Loop
    private static final int FRAME_MS = 16;
    private static final double MAX_DT = 1.0 / 30.0;

    // Escalas de dibujo
    private static final double AURA_SCALE = 1.6;
    private static final double BALL_POWER_SCALE = 2.2;
    private static final double ICON_SCALE = 0.8;        // tamaño del icono respecto al ancho del personaje
    private static final double ICON_FOLLOW_SPEED = 8.0; // mayor = el icono sigue más rápido

    // Menú de pausa (medidas de referencia a 900 px de alto)
    private static final int PAUSE_BUTTON_COUNT = 4;
    private static final int PAUSE_BUTTON_HEIGHT = 80;
    private static final int PAUSE_BUTTON_GAP = 20;
    private static final int PAUSE_TITLE_SPACE = 110;
    private static final int EXIT_DELAY_MS = 250;

    // Dependencias
    private final PanelRotation rotation;
    private final AudioPlay audio;
    private final ResolutionSetting resolution;
    private final DopoSoccer dopo;
    private final WhoVsWhoPanel whoVsWho;
    private final MatchModePanel matchMode;
    private final CharacterPanel characterPanel;
    private MatchSnapshot pendingSnapshot;
    private boolean fromSnapshot;
    private String matchType;
    private double limitValue;

    // Imágenes
    private final BufferedImage background;
    private final BufferedImage goalLeft;
    private final BufferedImage goalRight;
    private final BufferedImage ball;
    private final BufferedImage stun;
    private final BufferedImage barFrame;
    private final BufferedImage healthFill;
    private final BufferedImage powerFill;
    private final Map<String, BufferedImage> characterSprites = new HashMap<>();
    private final Map<String, BufferedImage> kickSprites = new HashMap<>();
    private final Map<String, BufferedImage> auraSprites = new HashMap<>();
    private final Map<String, BufferedImage> effectSprites = new HashMap<>();
    private final Map<String, BufferedImage> ballPowerSprites = new HashMap<>();

    // Estado del partido
    private Match match;
    private HumanPlayer human1;
    private HumanPlayer human2; // null si el rival es la CPU
    private final Timer loop;
    private long lastNanos;

    // Posición suavizada de los iconos de estado sobre cada personaje
    private final Map<Player, double[]> iconPositions = new HashMap<>();

    // Fuentes escaladas a la resolución del partido
    private Font scoreFont;
    private Font infoFont;
    private Font pauseFont;

    // Botones del menú de pausa
    private MenuButton[] pauseButtons;

    public GamePanel(PanelRotation rotation, AudioPlay audio, ResolutionSetting resolution, DopoSoccer dopo,
                     WhoVsWhoPanel whoVsWho, MatchModePanel matchMode, CharacterPanel characterPanel) {
        this.rotation = rotation;
        this.audio = audio;
        this.resolution = resolution;
        this.dopo = dopo;
        this.whoVsWho = whoVsWho;
        this.matchMode = matchMode;
        this.characterPanel = characterPanel;

        this.background = loadImage(BACKGROUND_PATH);
        this.goalLeft = loadImage(GOAL_LEFT_PATH);
        this.goalRight = loadImage(GOAL_RIGHT_PATH);
        this.ball = loadImage(BALL_PATH);
        this.stun = loadImage(STUN_PATH);
        this.barFrame = loadImage(BAR_FRAME_PATH);
        this.healthFill = loadImage(HEALTH_FILL_PATH);
        this.powerFill = loadImage(POWER_FILL_PATH);
        for (String[] entry : CHARACTER_SPRITES) {
            characterSprites.put(entry[0], loadImage(entry[1]));
            kickSprites.put(entry[0], loadImage(entry[2]));
            auraSprites.put(entry[0], loadImage(entry[3]));
        }
        for (String[] entry : EFFECT_SPRITES) {
            effectSprites.put(entry[0], loadImage(entry[1]));
        }
        for (String[] entry : BALL_POWER_SPRITES) {
            ballPowerSprites.put(entry[0], loadImage(entry[1]));
        }

        this.loop = new Timer(FRAME_MS, e -> tick());

        setPreferredSize(new Dimension(DopoSoccerGUI.MENU_WIDTH, DopoSoccerGUI.MENU_HEIGHT));
        setLayout(null); // los botones de pausa se ubican con setBounds
        setFocusable(true);
        preparePauseMenu();
        prepareKeyBindings();
        prepareLifecycle();
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

    // Ciclo de vida: el partido existe solo mientras el panel está visible


    private void prepareLifecycle() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                startMatch();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                stopMatch();
            }
        });
    }

    /**
     * Usa las elecciones de los paneles anteriores para configurar el dominio y arrancar el partido.
     */
    private void startMatch() {
        try {
            MatchSnapshot snapshot = pendingSnapshot;
            pendingSnapshot = null;
            fromSnapshot = (snapshot != null);

            int width;
            int height;
            if (fromSnapshot) {
                // La cancha tiene la resolución con que se guardó el partido
                width = (int) snapshot.getFieldWidth();
                height = (int) snapshot.getFieldHeight();
                match = dopo.loadMatch(snapshot, new Field(width, height));
                matchType = snapshot.getMatchType();
                limitValue = snapshot.getLimitValue();
                human1 = (match.getPlayer1() instanceof HumanPlayer) ? (HumanPlayer) match.getPlayer1() : null;
                human2 = (match.getPlayer2() instanceof HumanPlayer) ? (HumanPlayer) match.getPlayer2() : null;
            } else {
                Dimension res = resolution.getGameResolution();
                width = res.width;
                height = res.height;
                Field field = new Field(width, height);

                boolean cpuMode = whoVsWho.getMode() == WhoVsWhoPanel.Mode.HUMAN_VS_CPU;
                human1 = new HumanPlayer("Player 1");
                Player second;
                if (cpuMode) {
                    human2 = null;
                    second = new Cpu("CPU", whoVsWho.getCpuProfile());
                } else {
                    human2 = new HumanPlayer("Player 2");
                    second = human2;
                }

                dopo.preFixPlayers(human1, second);
                dopo.preFixCharacters(characterPanel.getPlayer1Character(), characterPanel.getPlayer2Character());
                matchType = matchMode.getMatchType();
                limitValue = matchMode.getLimitValue();
                match = dopo.preFixMatch(matchType, field, limitValue);
            }

            double scale = width / 1600.0;
            scoreFont = FontLoader.load((float) (40 * scale));
            infoFont = FontLoader.load((float) Math.max(10, 18 * scale));
            pauseFont = FontLoader.load((float) Math.max(16, 36 * scale));
            layoutPauseMenu(width, height);
            setPauseMenuVisible(false);

            requestFocusInWindow();
            lastNanos = System.nanoTime();
            loop.start();
        } catch (DopoSoccerException e) {
            abortMatch(e.getMessage());
        } catch (IllegalStateException | IllegalArgumentException e) {
            ErrorLog.error("No se pudo iniciar el partido", e);
            abortMatch(e.getMessage());
        }
    }

    public void loadMatch(MatchSnapshot snapshot) {
        this.pendingSnapshot = snapshot;
    }

    private void abortMatch(String message) {
        stopMatch();
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), message,
                "No se pudo iniciar el partido", JOptionPane.ERROR_MESSAGE);
        // Un partido cargado no viene del flujo de selección: vuelve al menú
        if (fromSnapshot) {
            rotation.goTo(DopoSoccerGUI.MENU_CONFIG_PANEL);
        } else {
            rotation.back();
        }
    }

    private void stopMatch() {
        loop.stop();
        if (match != null) {
            match.disposeOldField();
            match = null;
        }
        human1 = null;
        human2 = null;
        iconPositions.clear();
        setPauseMenuVisible(false);
    }

    private void tick() {
        if (match == null) {
            return;
        }
        long now = System.nanoTime();
        double dt = Math.min((now - lastNanos) / 1_000_000_000.0, MAX_DT);
        lastNanos = now;

        match.update(dt); // no hace nada mientras está pausado
        if (!isPaused()) {
            updateIconFollow(match.getPlayer1(), dt);
            updateIconFollow(match.getPlayer2(), dt);
        }
        repaint();

        if (match.getGameInfo().isFinished()) {
            finishMatch();
        }
    }

    private void finishMatch() {
        loop.stop();
        String result;
        try {
            result = dopo.finishMatch(); // suma los puntos y actualiza el SaveFile en memoria
        } catch (DopoSoccerException e) {
            result = e.getMessage();
        }
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), result,
                "Fin del partido", JOptionPane.INFORMATION_MESSAGE);
        stopMatch();
        rotation.goTo(DopoSoccerGUI.MENU_CONFIG_PANEL);
    }

    // Pausa


    private void preparePauseMenu() {
        pauseButtons = new MenuButton[] {
                new MenuButton("REANUDAR", this::onResume),
                new MenuButton("GUARDAR PARTIDO", this::onSaveMatch),
                new MenuButton("SALIR SIN GUARDAR", this::onExitWithoutSaving),
                new MenuButton("SALIR DEL PARTIDO", this::onLeaveMatch)
        };
        for (MenuButton button : pauseButtons) {
            add(button);
        }
        setPauseMenuVisible(false);
    }

    // Medidas de la pestaña de pausa, escaladas con el alto de la cancha (900 = referencia)
    private int pauseButtonHeight(int h) { return (int) (PAUSE_BUTTON_HEIGHT * h / 900.0); }
    private int pauseGap(int h) { return (int) (PAUSE_BUTTON_GAP * h / 900.0); }
    private int pauseTitleSpace(int h) { return (int) (PAUSE_TITLE_SPACE * h / 900.0); }

    private int pauseTabHeight(int h) {
        return PAUSE_BUTTON_COUNT * pauseButtonHeight(h) + (PAUSE_BUTTON_COUNT - 1) * pauseGap(h)
                + pauseTitleSpace(h) + pauseGap(h);
    }

    private int pauseTabY(int h) {
        return (h - pauseTabHeight(h)) / 2;
    }

    /**
     * Ubica los botones dentro de la pestaña de pausa según la resolución del partido.
     */
    private void layoutPauseMenu(int w, int h) {
        int bw = Math.min(MenuButton.BUTTON_WIDTH, (int) (w * 0.6));
        int bh = pauseButtonHeight(h);
        int x = (w - bw) / 2;
        int y = pauseTabY(h) + pauseTitleSpace(h);
        for (int i = 0; i < pauseButtons.length; i++) {
            pauseButtons[i].setBounds(x, y + i * (bh + pauseGap(h)), bw, bh);
        }
    }

    private void setPauseMenuVisible(boolean visible) {
        for (MenuButton button : pauseButtons) {
            button.setVisible(visible);
        }
    }

    private boolean isPaused() {
        return match != null && match.getGameInfo().isPaused();
    }

    private void togglePause() {
        if (match == null || match.getGameInfo().isFinished()) {
            return;
        }
        boolean pause = !isPaused();
        match.getGameInfo().setPaused(pause);
        if (pause) {
            // Evita que una tecla mantenida deje al personaje moviéndose al reanudar
            if (human1 != null) human1.clearInputs();
            if (human2 != null) human2.clearInputs();
        }
        setPauseMenuVisible(pause);
        if (!pause) {
            requestFocusInWindow(); // el botón REANUDAR tenía el foco y se oculta
        }
        repaint();
    }

    private void onResume() {
        audio.playSound(CLICK_SOUND);
        togglePause();
    }

    private void onSaveMatch() {
        audio.playSound(CLICK_SOUND);
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar partido");
        chooser.setFileFilter(SAVE_FILTER);
        if (chooser.showSaveDialog(SwingUtilities.getWindowAncestor(this)) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = withExtension(chooser.getSelectedFile());
        try {
            MatchSnapshot.capture(match, matchType, limitValue).guardar(file);
            showMessage("Partido guardado", JOptionPane.INFORMATION_MESSAGE);
        } catch (DopoSoccerException e) {
            showMessage(e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Cierra la aplicación sin guardar nada (misma lógica que SALIR del menú principal). */
    private void onExitWithoutSaving() {
        audio.playSound(CLICK_SOUND);
        loop.stop();
    }

    private void onExit() {
        audio.playSound(CLICK_SOUND);
        // Pequeña espera para que el sonido alcance a oírse antes de cerrar
        Timer exitTimer = new Timer(EXIT_DELAY_MS, e -> System.exit(0));
        exitTimer.setRepeats(false);
        exitTimer.start();
    }

    /** Vuelve al menú sin guardar nada del progreso ni sumar puntos. */
    private void onLeaveMatch() {
        audio.playSound(CLICK_SOUND);
        stopMatch(); // no llama a dopo.finishMatch()
        rotation.goTo(DopoSoccerGUI.MENU_CONFIG_PANEL);
    }

    private File withExtension(File file) {
        if (file.getName().toLowerCase().endsWith("." + SAVE_EXTENSION)) {
            return file;
        }
        return new File(file.getParentFile(), file.getName() + "." + SAVE_EXTENSION);
    }

    private void showMessage(String message, int type) {
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), message, "DOPOSoccer", type);
    }

    // Controles: J1 (A/D/W, F patear, G poder), J2 (flechas, L patear, K poder), ESC pausa


    private void prepareKeyBindings() {
        bind(KeyEvent.VK_ESCAPE, true, this::togglePause);

        bind(KeyEvent.VK_A, true, () -> p1(p -> p.setMovingLeft(true)));
        bind(KeyEvent.VK_A, false, () -> p1(p -> p.setMovingLeft(false)));
        bind(KeyEvent.VK_D, true, () -> p1(p -> p.setMovingRight(true)));
        bind(KeyEvent.VK_D, false, () -> p1(p -> p.setMovingRight(false)));
        bind(KeyEvent.VK_W, true, () -> p1(HumanPlayer::requestJump));
        bind(KeyEvent.VK_F, true, () -> p1(HumanPlayer::requestKick));
        bind(KeyEvent.VK_G, true, () -> p1(HumanPlayer::requestPower));

        bind(KeyEvent.VK_LEFT, true, () -> p2(p -> p.setMovingLeft(true)));
        bind(KeyEvent.VK_LEFT, false, () -> p2(p -> p.setMovingLeft(false)));
        bind(KeyEvent.VK_RIGHT, true, () -> p2(p -> p.setMovingRight(true)));
        bind(KeyEvent.VK_RIGHT, false, () -> p2(p -> p.setMovingRight(false)));
        bind(KeyEvent.VK_UP, true, () -> p2(HumanPlayer::requestJump));
        bind(KeyEvent.VK_L, true, () -> p2(HumanPlayer::requestKick));
        bind(KeyEvent.VK_K, true, () -> p2(HumanPlayer::requestPower));
    }

    private void bind(int keyCode, boolean onPress, Runnable action) {
        KeyStroke stroke = KeyStroke.getKeyStroke(keyCode, 0, !onPress);
        String id = (onPress ? "press-" : "release-") + keyCode;
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, id);
        getActionMap().put(id, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Los bindings de la ventana siguen activos aunque el panel esté oculto
                if (isShowing() && match != null) {
                    action.run();
                }
            }
        });
    }

    private void p1(Consumer<HumanPlayer> action) {
        if (human1 != null && !isPaused()) action.accept(human1);
    }

    private void p2(Consumer<HumanPlayer> action) {
        if (human2 != null && !isPaused()) action.accept(human2);
    }


    // Dibujo


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (match == null) {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            return;
        }

        Field field = match.getField();
        int w = (int) field.getWidth();
        int h = (int) field.getHeight();

        if (background != null) {
            g2.drawImage(background, 0, 0, w, h, null);
        } else {
            g2.setColor(new Color(40, 120, 50));
            g2.fillRect(0, 0, w, h);
        }

        drawCharacter(g2, match.getPlayer1());
        drawCharacter(g2, match.getPlayer2());
        for (Ball b : match.getActiveBalls()) {
            drawBall(g2, b);
        }
        drawGoal(g2, field.getLeftGoal(), goalLeft);
        drawGoal(g2, field.getRightGoal(), goalRight);
        drawHud(g2, w);

        // Se pinta antes que los hijos (botones), así que queda por debajo de ellos
        if (isPaused()) {
            drawPauseOverlay(g2, w, h);
        }
        g2.dispose();
    }

    /**
     * Opaca todo el panel y dibuja la pestaña de pausa detrás de los botones.
     */
    private void drawPauseOverlay(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(0, 0, w, h);

        int tabW = Math.min(MenuButton.BUTTON_WIDTH, (int) (w * 0.6)) + 80;
        int tabH = pauseTabHeight(h);
        int tabX = (w - tabW) / 2;
        int tabY = pauseTabY(h);

        g2.setColor(new Color(20, 20, 30, 235));
        g2.fillRoundRect(tabX, tabY, tabW, tabH, 32, 32);
        g2.setColor(new Color(255, 255, 255, 200));
        g2.drawRoundRect(tabX, tabY, tabW, tabH, 32, 32);

        g2.setFont(pauseFont);
        FontMetrics fm = g2.getFontMetrics();
        String title = "PAUSA";
        g2.setColor(Color.WHITE);
        g2.drawString(title, (w - fm.stringWidth(title)) / 2, tabY + pauseGap(h) + fm.getAscent());
    }

    private void drawGoal(Graphics2D g2, Goal goal, BufferedImage image) {
        int gw = (int) goal.getLogicalWidth();
        int gh = (int) goal.getLogicalHeight();
        int x = (int) (goal.getDrawX() - gw / 2.0);
        int y = (int) (goal.getDrawY() - gh / 2.0);
        if (image != null) {
            g2.drawImage(image, x, y, gw, gh, null);
        } else {
            g2.setColor(Color.WHITE);
            g2.drawRect(x, y, gw, gh);
        }
    }

    private void drawCharacter(Graphics2D g2, Player player) {
        Character_ c = player.getCharacter();
        if (c == null) {
            return;
        }
        int cw = (int) c.getLogicalWidth();
        int ch = (int) c.getLogicalHeight();
        int x = (int) (c.getX() - cw / 2.0);
        int y = (int) (c.getY() - ch / 2.0);

        // Aura del poder activo, detrás del personaje (una distinta por país)
        BufferedImage aura = auraSprites.get(c.getCountry());
        if (player.hasPowerAura() && aura != null) {
            int aw = (int) (cw * AURA_SCALE);
            int ah = (int) (ch * AURA_SCALE);
            g2.drawImage(aura, (int) (c.getX() - aw / 2.0), (int) (c.getY() - ah / 2.0), aw, ah, null);
        }

        // Sprite normal o de patada (dibujados mirando a la derecha; se voltean al mirar a la izquierda)
        BufferedImage sprite = c.isKicking() ? kickSprites.get(c.getCountry()) : characterSprites.get(c.getCountry());
        if (sprite == null) {
            sprite = characterSprites.get(c.getCountry()); // respaldo: sprite normal
        }
        drawFacing(g2, sprite, x, y, cw, ch, c.isFacingRight());

        // Aturdimiento y efectos negativos: iconos sobre la cabeza
        drawStatusIcons(g2, player, c);
    }

    private void drawFacing(Graphics2D g2, BufferedImage sprite, int x, int y, int w, int h, boolean facingRight) {
        if (sprite == null) {
            g2.setColor(Color.MAGENTA);
            g2.fillRect(x, y, w, h);
        } else if (facingRight) {
            g2.drawImage(sprite, x, y, w, h, null);
        } else {
            g2.drawImage(sprite, x + w, y, -w, h, null);
        }
    }

    /** Posición objetivo del icono: centrada sobre la cabeza del personaje. */
    private double[] iconTarget(Character_ c) {
        double size = c.getLogicalWidth() * ICON_SCALE;
        return new double[] {c.getX(), c.getY() - c.getLogicalHeight() / 2.0 - size / 2.0 - 6};
    }

    /** Acerca la posición del icono a la del personaje (seguimiento con retraso). */
    private void updateIconFollow(Player p, double dt) {
        Character_ c = p.getCharacter();
        if (c == null) {
            return;
        }
        double[] target = iconTarget(c);
        double[] pos = iconPositions.get(p);
        if (pos == null) {
            iconPositions.put(p, target);
            return;
        }
        double k = Math.min(1.0, ICON_FOLLOW_SPEED * dt);
        pos[0] += (target[0] - pos[0]) * k;
        pos[1] += (target[1] - pos[1]) * k;
    }

    private void drawStatusIcons(Graphics2D g2, Player player, Character_ c) {
        double[] pos = iconPositions.get(player);
        if (pos == null) {
            return;
        }
        List<BufferedImage> icons = new ArrayList<>();
        if (player.isStunned() && stun != null) {
            icons.add(stun);
        }
        Effect effect = c.getActiveEffect();
        if (effect != null) {
            BufferedImage effectIcon = effectSprites.get(effect.getName());
            if (effectIcon != null) {
                icons.add(effectIcon);
            }
        }
        int size = (int) (c.getLogicalWidth() * ICON_SCALE);
        int startX = (int) (pos[0] - icons.size() * size / 2.0);
        for (int i = 0; i < icons.size(); i++) {
            g2.drawImage(icons.get(i), startX + i * size, (int) (pos[1] - size / 2.0), size, size, null);
        }
    }

    private void drawBall(Graphics2D g2, Ball b) {
        int d = (int) (b.getRadius() * 2);
        int x = (int) (b.getX() - b.getRadius());
        int y = (int) (b.getY() - b.getRadius());

        // Sprite del poder en vuelo, detrás del balón (solo el real; los falsos no lo llevan)
        if (!b.isFake() && b.hasActivePower()) {
            BufferedImage powerSprite = ballPowerSprites.get(b.getActivePower().getPowerName());
            if (powerSprite != null) {
                int pd = (int) (d * BALL_POWER_SCALE);
                g2.drawImage(powerSprite, (int) (b.getX() - pd / 2.0), (int) (b.getY() - pd / 2.0), pd, pd, null);
            }
        }

        if (ball == null) {
            g2.setColor(Color.WHITE);
            g2.fillOval(x, y, d, d);
            return;
        }
        // Se rota alrededor de su centro según el ángulo del cuerpo físico
        Graphics2D rotated = (Graphics2D) g2.create();
        rotated.rotate(b.getAngleRadians(), b.getX(), b.getY());
        rotated.drawImage(ball, x, y, d, d, null);
        rotated.dispose();
    }


    // HUD


    private void drawHud(Graphics2D g2, int w) {
        double s = w / 1600.0;
        int margin = (int) (30 * s);
        int barW = (int) (420 * s);
        int barH = (int) (36 * s);
        int nameY = (int) (50 * s);
        int healthY = (int) (62 * s);
        int powerY = healthY + barH + (int) (8 * s);
        int powerH = (int) (22 * s);

        drawPlayerInfo(g2, match.getPlayer1(), "JUGADOR 1", margin, nameY, healthY, powerY, barW, barH, powerH, false);
        String name2 = match.getPlayer2().isHuman() ? "JUGADOR 2" : "CPU";
        drawPlayerInfo(g2, match.getPlayer2(), name2, w - margin - barW, nameY, healthY, powerY, barW, barH, powerH, true);

        drawScoreboard(g2, w, s);
    }

    private void drawPlayerInfo(Graphics2D g2, Player p, String name, int x, int nameY, int healthY, int powerY,
                                int barW, int barH, int powerH, boolean rightToLeft) {
        g2.setFont(infoFont);
        drawShadowedText(g2, name, x, nameY);

        double health = p.getMaxHealth() <= 0 ? 0 : p.getCurrentHealth() / p.getMaxHealth();
        drawBar(g2, x, healthY, barW, barH, health, healthFill, new Color(220, 50, 50), rightToLeft);
        drawBar(g2, x, powerY, barW, powerH, p.getPowerBar() / 100.0, powerFill, new Color(60, 140, 240), rightToLeft);
    }

    /**
     * Dibuja el relleno recortado según la proporción y encima el marco.
     * Se asume que fill y frame tienen las mismas proporciones.
     */
    private void drawBar(Graphics2D g2, int x, int y, int w, int h, double ratio, BufferedImage fill,
                         Color fallback, boolean rightToLeft) {
        int fillW = (int) (w * Math.max(0.0, Math.min(1.0, ratio)));
        int clipX = rightToLeft ? x + (w - fillW) : x;

        Shape oldClip = g2.getClip();
        g2.setClip(clipX, y, fillW, h);
        if (fill != null) {
            g2.drawImage(fill, x, y, w, h, null);
        } else {
            g2.setColor(fallback);
            g2.fillRect(x, y, w, h);
        }
        g2.setClip(oldClip);

        if (barFrame != null) {
            g2.drawImage(barFrame, x, y, w, h, null);
        } else {
            g2.setColor(Color.WHITE);
            g2.drawRect(x, y, w, h);
        }
    }

    private void drawScoreboard(Graphics2D g2, int w, double s) {
        InGameInfo info = match.getGameInfo();

        g2.setFont(scoreFont);
        String score = info.getPlayer1Goals() + " - " + info.getPlayer2Goals();
        drawCenteredText(g2, score, w / 2, (int) (60 * s));

        g2.setFont(infoFont);
        String detail;
        if (info.isTimeMode()) {
            if (info.isSuddenDeath()) {
                detail = "MUERTE SUBITA";
            } else {
                int seconds = (int) Math.ceil(info.getRemainingTime());
                detail = String.format("%d:%02d", seconds / 60, seconds % 60);
            }
        } else {
            detail = "A " + info.getTargetGoals() + (info.getTargetGoals() == 1 ? " GOL" : " GOLES");
        }
        drawCenteredText(g2, detail, w / 2, (int) (100 * s));
    }

    private void drawCenteredText(Graphics2D g2, String text, int centerX, int baselineY) {
        FontMetrics fm = g2.getFontMetrics();
        drawShadowedText(g2, text, centerX - fm.stringWidth(text) / 2, baselineY);
    }

    private void drawShadowedText(Graphics2D g2, String text, int x, int y) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.drawString(text, x + 2, y + 2);
        g2.setColor(Color.WHITE);
        g2.drawString(text, x, y);
    }
}
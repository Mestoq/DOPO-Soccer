package presentation;

import domain.DopoSoccer;
import domain.ErrorLog;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DopoSoccerGUI extends JFrame implements AudioPlay, PanelRotation, ResolutionSetting {

    // Resolución fija de los menús y pantallas de configuración
    public static final int MENU_WIDTH = 1600;
    public static final int MENU_HEIGHT = 900;

    // Nombres de los paneles del flujo (el orden de registro define next() y back())
    public static final String START_PANEL = "START";
    public static final String MENU_CONFIG_PANEL = "MENU_CONFIG";
    public static final String WHO_VS_WHO_PANEL = "WHO_VS_WHO";
    public static final String MATCH_MODE_PANEL = "MATCH_MODE";
    public static final String CHARACTER_PANEL = "CHARACTER";
    public static final String GAME_PANEL = "GAME";

    // Paneles auxiliares (fuera del flujo lineal, solo se alcanzan con goTo)
    public static final String STORE_PANEL = "STORE";

    private final CardLayout cardLayout;
    private final JPanel container;

    // Flujo lineal de paneles, paneles auxiliares y música asociada a cada uno
    private final List<String> panelOrder;
    private final Set<String> auxiliaryPanels;
    private final Map<String, String> panelMusic;
    private int currentIndex;

    // Tamaño de ventana por panel (si no está, se usa 1600x900)
    private final Map<String, Dimension> panelSize;

    // Estado de la música de fondo
    private Clip backgroundClip;
    private String backgroundPath;

    public DopoSoccerGUI() {
        super("DOPOSoccer");
        this.cardLayout = new CardLayout();
        this.container = new JPanel(cardLayout);
        this.panelOrder = new ArrayList<>();
        this.auxiliaryPanels = new HashSet<>();
        this.panelMusic = new HashMap<>();
        this.panelSize = new HashMap<>();
        this.currentIndex = 0;

        prepareElements();
    }

    private void prepareElements() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        container.setPreferredSize(new Dimension(MENU_WIDTH, MENU_HEIGHT));
        setContentPane(container);
        pack();
        setLocationRelativeTo(null);
    }

    // Registro de paneles

    /**
     * Registra un panel al final del flujo sin música propia
     * (al mostrarlo se detiene el sonido de fondo).
     */
    public void addPanel(String name, JPanel panel) {
        addPanel(name, panel, null);
    }

    /**
     * Registra un panel al final del flujo con la pista de fondo que debe sonar mientras esté visible.
     */
    public void addPanel(String name, JPanel panel, String musicPath) {
        if (name == null || panel == null) {
            ErrorLog.warn("addPanel recibió un nombre o panel nulo; se ignora el registro.");
            return;
        }
        container.add(panel, name);
        panelOrder.add(name);
        if (musicPath != null) {
            panelMusic.put(name, musicPath);
        }
    }

    /**
     * Registra un panel fuera del flujo lineal: no participa en next()/back(),
     * solo se alcanza con goTo(name).
     */
    public void addAuxiliaryPanel(String name, JPanel panel, String musicPath) {
        if (name == null || panel == null) {
            ErrorLog.warn("addAuxiliaryPanel recibió un nombre o panel nulo; se ignora el registro.");
            return;
        }
        container.add(panel, name);
        auxiliaryPanels.add(name);
        if (musicPath != null) {
            panelMusic.put(name, musicPath);
        }
    }

    // Tamaño de la ventana

    /**
     * Define el tamaño que debe tener la ventana mientras el panel indicado esté visible.
     */
    public void setPanelSize(String name, int width, int height) {
        panelSize.put(name, new Dimension(width, height));
    }

    /**
     * Define la resolución de la cancha: es el tamaño de la ventana al mostrar GamePanel.
     */
    @Override
    public void setGameResolution(int width, int height) {
        setPanelSize(GAME_PANEL, width, height);
    }

    private void applyWindowSize(String panelName) {
        Dimension size = panelSize.getOrDefault(panelName, new Dimension(MENU_WIDTH, MENU_HEIGHT));
        container.setPreferredSize(size);
        pack();
        setLocationRelativeTo(null);
    }

    // Mostrar paneles

    private void showCurrent() {
        showPanel(panelOrder.get(currentIndex));
    }

    private void showPanel(String name) {
        applyWindowSize(name);
        cardLayout.show(container, name);
        updateBackgroundMusic(name);
    }

    private void updateBackgroundMusic(String panelName) {
        String music = panelMusic.get(panelName);
        if (music == null) {
            stopBackground();
        } else {
            playBackground(music);
        }
    }

    // PanelRotation

    @Override
    public void next() {
        if (currentIndex < panelOrder.size() - 1) {
            currentIndex++;
            showCurrent();
        }
    }

    @Override
    public void back() {
        if (currentIndex > 0) {
            currentIndex--;
            showCurrent();
        }
    }

    @Override
    public void goTo(String panelName) {
        if (auxiliaryPanels.contains(panelName)) {
            showPanel(panelName); // no altera currentIndex
            return;
        }
        int index = panelOrder.indexOf(panelName);
        if (index < 0) {
            ErrorLog.warn("goTo: el panel no está registrado: " + panelName);
            return;
        }
        currentIndex = index;
        showCurrent();
    }

    // AudioPlay

    @Override
    public void playSound(String soundPath) {
        URL url = getClass().getResource(soundPath);
        if (url == null) {
            ErrorLog.warn("Sonido no encontrado: " + soundPath);
            return;
        }
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(url)) {
            Clip clip = AudioSystem.getClip();
            clip.addLineListener(event -> {
                // Libera el recurso cuando termina de sonar
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            clip.open(stream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            ErrorLog.error("No se pudo reproducir el sonido: " + soundPath, e);
        }
    }

    @Override
    public void playBackground(String musicPath) {
        // Misma pista que ya suena: no se reinicia
        if (musicPath.equals(backgroundPath) && backgroundClip != null && backgroundClip.isRunning()) {
            return;
        }
        stopBackground();

        URL url = getClass().getResource(musicPath);
        if (url == null) {
            ErrorLog.warn("Música de fondo no encontrada: " + musicPath);
            return;
        }
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(url)) {
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            this.backgroundClip = clip;
            this.backgroundPath = musicPath;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            ErrorLog.error("No se pudo reproducir la música de fondo: " + musicPath, e);
        }
    }

    @Override
    public void stopBackground() {
        if (backgroundClip != null) {
            backgroundClip.stop();
            backgroundClip.close();
            backgroundClip = null;
            backgroundPath = null;
        }
    }

    /**
     * Muestra el primer panel registrado y hace visible la ventana.
     */
    public void start() {
        if (!panelOrder.isEmpty()) {
            currentIndex = 0;
            showCurrent();
        }
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DopoSoccerGUI gui = new DopoSoccerGUI();
            DopoSoccer dopo = new DopoSoccer(null);

            gui.addPanel(START_PANEL, new StartPanel(gui, gui), "/audio/music/start.wav");
            gui.addPanel(MENU_CONFIG_PANEL, new MenuConfigPanel(gui, gui, gui, dopo), "/audio/music/menu.wav");
            gui.addPanel(WHO_VS_WHO_PANEL, new WhoVsWhoPanel(gui, gui), "/audio/music/menu.wav");
            // Pendientes (se habilitan al crear cada panel):
            // gui.addPanel(MATCH_MODE_PANEL, new MatchModePanel(gui, gui), "/audio/music/menu.wav");
            // gui.addPanel(CHARACTER_PANEL,  new CharacterPanel(gui, gui),  "/audio/music/character.wav");
            // gui.addPanel(GAME_PANEL,       new GamePanel(gui, gui),       "/audio/music/game.wav");
            // gui.addAuxiliaryPanel(STORE_PANEL, new StorePanel(gui, gui), "/audio/music/menu.wav");

            gui.start();
        });
    }
}
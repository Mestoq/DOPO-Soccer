package presentation;

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
import java.util.List;
import java.util.Map;

public class DopoSoccerGUI extends JFrame implements AudioPlay, PanelRotation {

    //
    public static final int MENU_WIDTH = 1600;
    public static final int MENU_HEIGHT = 900;

    //
    public static final String START_PANEL = "START";
    public static final String MENU_CONFIG_PANEL = "MENU_CONFIG";
    public static final String WHO_VS_WHO_PANEL = "WHO_VS_WHO";
    public static final String MATCH_MODE_PANEL = "MATCH_MODE";
    public static final String CHARACTER_PANEL = "CHARACTER";
    public static final String GAME_PANEL = "GAME";

    private final CardLayout cardLayout;
    private final JPanel container;

    // Flujo de paneles y música asociada a cada uno
    private final List<String> panelOrder;
    private final Map<String, String> panelMusic;
    private int currentIndex;

    //Musica
    private Clip backgroundClip;
    private String backgroundPath;

    public DopoSoccerGUI() {
        super("DOPOSoccer");
        this.cardLayout = new CardLayout();
        this.container = new JPanel(cardLayout);
        this.panelOrder = new ArrayList<>();
        this.panelMusic = new HashMap<>();
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
     * Registra un panel al final del flujo sin música propia (osea se detiene el sonido de fondo).
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

    private void showCurrent() {
        String name = panelOrder.get(currentIndex);
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

            // gui.addPanel(START_PANEL,       new StartPanel(gui, gui),       "/audio/music/start.wav");
            // gui.addPanel(MENU_CONFIG_PANEL, new MenuConfigPanel(gui, gui),  "/audio/music/menu.wav");
            // gui.addPanel(WHO_VS_WHO_PANEL,  new WhoVsWhoPanel(gui, gui),    "/audio/music/menu.wav");
            // gui.addPanel(MATCH_MODE_PANEL,  new MatchModePanel(gui, gui),   "/audio/music/menu.wav");
            // gui.addPanel(CHARACTER_PANEL,   new CharacterPanel(gui, gui),   "/audio/music/character.wav");
            // gui.addPanel(GAME_PANEL,        new GamePanel(gui, gui),        "/audio/music/game.wav");

            gui.start();
        });
    }
}
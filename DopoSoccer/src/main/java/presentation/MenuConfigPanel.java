package presentation;

import domain.DopoSoccer;
import domain.DopoSoccerException;
import domain.ErrorLog;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class MenuConfigPanel extends JPanel {

    private static final String BACKGROUND_PATH = "/images/start/background.png";
    private static final String CLICK_SOUND = "/audio/click.wav";
    private static final int EXIT_DELAY_MS = 250;
    private static final int BUTTON_GAP = 20;

    private static final String SAVE_EXTENSION = "dat";
    private static final FileNameExtensionFilter SAVE_FILTER =
            new FileNameExtensionFilter("Partida DOPOSoccer (*." + SAVE_EXTENSION + ")", SAVE_EXTENSION);

    // Resoluciones de la cancha (ancho, alto)
    private static final int[][] RESOLUTIONS = {{1024, 576}, {1280, 720}, {1600, 900}};
    private static final int DEFAULT_RESOLUTION_INDEX = 2;

    private final PanelRotation rotation;
    private final AudioPlay audio;
    private final ResolutionSetting resolution;
    private final DopoSoccer game;
    private final BufferedImage background;

    private MenuButton loadMatchButton;
    private int resolutionIndex;

    public MenuConfigPanel(PanelRotation rotation, AudioPlay audio, ResolutionSetting resolution, DopoSoccer game) {
        this.rotation = rotation;
        this.audio = audio;
        this.resolution = resolution;
        this.game = game;
        this.background = loadImage(BACKGROUND_PATH);
        this.resolutionIndex = DEFAULT_RESOLUTION_INDEX;

        setPreferredSize(new Dimension(DopoSoccerGUI.MENU_WIDTH, DopoSoccerGUI.MENU_HEIGHT));
        setLayout(new GridBagLayout());
        prepareElements();

        // Resolución por defecto hasta que el jugador elija otra
        resolution.setGameResolution(RESOLUTIONS[resolutionIndex][0], RESOLUTIONS[resolutionIndex][1]);
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
        loadMatchButton = new MenuButton("LOAD ONGOING MATCH", this::onLoadMatch);
        loadMatchButton.setEnabled(game.isSaveLoaded());

        addButton(new MenuButton("NEW MATCH", this::onNewGame), 0);
        addButton(new MenuButton("LOAD SAVE", this::onLoadGame), 1);
        addButton(loadMatchButton, 2);
        addButton(new MenuButton("SAVE PROGRESS", this::onSaveGame), 3);
        addButton(new MenuButton("STORE", this::onStore), 4);
        addButton(new MenuButton("SET RESOLUTION", this::onResolution), 5);
        addButton(new MenuButton("EXIT", this::onExit), 6);
    }

    private void addButton(MenuButton button, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.insets = new Insets(BUTTON_GAP / 2, 0, BUTTON_GAP / 2, 0);
        add(button, gbc);
    }

    // Acciones de los botones

    private void onNewGame() {
        audio.playSound(CLICK_SOUND);
        rotation.next();
    }

    private void onLoadGame() {
        audio.playSound(CLICK_SOUND);
        JFileChooser chooser = createChooser("Load a Save");
        if (chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            game.loadSave(chooser.getSelectedFile());
            loadMatchButton.setEnabled(true);
            showMessage("Save Loaded", JOptionPane.INFORMATION_MESSAGE);
        } catch (DopoSoccerException e) {
            showMessage(e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSaveGame() {
        audio.playSound(CLICK_SOUND);
        JFileChooser chooser = createChooser("Save Progress");
        if (chooser.showSaveDialog(SwingUtilities.getWindowAncestor(this)) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = withExtension(chooser.getSelectedFile());
        try {
            game.saveProgress(file);
            showMessage("Progress Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (DopoSoccerException e) {
            showMessage(e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onLoadMatch() {
        audio.playSound(CLICK_SOUND);
        // Pendiente: se conectará con la carga del partido (MatchSnapshot)
    }

    private void onStore() {
        audio.playSound(CLICK_SOUND);
        // Pendiente: se conectará con el panel de la tienda
    }

    private void onResolution() {
        audio.playSound(CLICK_SOUND);
        String[] options = new String[RESOLUTIONS.length];
        for (int i = 0; i < RESOLUTIONS.length; i++) {
            options[i] = RESOLUTIONS[i][0] + " x " + RESOLUTIONS[i][1];
        }
        int choice = JOptionPane.showOptionDialog(
                SwingUtilities.getWindowAncestor(this),
                "Select the match resolution",
                "Set Resolution",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[resolutionIndex]);

        if (choice < 0) { // se cerró el diálogo sin elegir
            return;
        }
        resolutionIndex = choice;
        resolution.setGameResolution(RESOLUTIONS[choice][0], RESOLUTIONS[choice][1]);
        showMessage("Resolution set for the match of: " + options[choice], JOptionPane.INFORMATION_MESSAGE);
    }

    private void onExit() {
        audio.playSound(CLICK_SOUND);
        // Pequeña espera para que el sonido alcance a oírse antes de cerrar
        Timer exitTimer = new Timer(EXIT_DELAY_MS, e -> System.exit(0));
        exitTimer.setRepeats(false);
        exitTimer.start();
    }

    // Utilidades

    private JFileChooser createChooser(String title) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setFileFilter(SAVE_FILTER);
        return chooser;
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
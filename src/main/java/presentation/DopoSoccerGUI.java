package presentation;

import domain.DopoSoccer;
import domain.DopoSoccerException;
import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Single JFrame with a CardLayout. The only class that knows the whole screen flow. */
public class DopoSoccerGUI extends JFrame {
    public static final int[][] RESOLUTIONS = {{800, 450}, {1024, 576}, {1280, 720}};
    private static final Dimension MENU_SIZE = new Dimension(820, 580);

    private final DopoSoccer game = new DopoSoccer();
    private final CardLayout cards = new CardLayout();
    private Dimension currentSize = MENU_SIZE;
    private final JPanel root = new JPanel(cards) {
        @Override
        public Dimension getPreferredSize() { return currentSize; }
    };
    private final ConfigPanel config;
    private final CharacterSelectPanel select;
    private GamePanel gamePanel;

    public DopoSoccerGUI() {
        super("DOPOSoccer 2026-2");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        config = new ConfigPanel(this);
        select = new CharacterSelectPanel(this, game.getCatalogIds());
        root.add(new MainMenuPanel(this), "MENU");
        root.add(config, "CONFIG");
        root.add(select, "SELECT");
        setContentPane(root);
        setResizable(false);
        showMenu();
    }

    public DopoSoccer getGame() { return game; }

    public void showMenu() {
        stopGame();
        show("MENU", MENU_SIZE);
    }

    public void showConfig() { show("CONFIG", MENU_SIZE); }

    /** Validates the mode early so an unavailable one is reported before choosing characters. */
    public void showCharacterSelect() {
        if (config.isVsMachine()) {
            try {
                game.setCPU(1, config.getCpuProfile());
            } catch (DopoSoccerException ex) {
                ErrorDialog.show(this, ex);
                return;
            }
        }
        select.setVsMachine(config.isVsMachine());
        show("SELECT", MENU_SIZE);
    }

    public void startMatch() {
        try {
            game.setHuman(0);
            if (config.isVsMachine()) game.setCPU(1, config.getCpuProfile());
            else game.setHuman(1);
            game.choose(0, select.getChoice(0));
            game.choose(1, select.getChoice(1));
            if (config.isByTime()) game.createTimeMatch(config.getValue());
            else game.createGoalsMatch(config.getValue());
        } catch (DopoSoccerException ex) {
            ErrorDialog.show(this, ex);
            return;
        }
        int[] res = RESOLUTIONS[config.getResolutionIndex()];
        gamePanel = new GamePanel(this, game, new Dimension(res[0], res[1]));
        root.add(gamePanel, "GAME");
        show("GAME", new Dimension(res[0], res[1] + HUDPanel.HEIGHT));
        gamePanel.start();
    }

    /** Called on the EDT when the match ends by itself. */
    public void onMatchFinished() {
        ResultDialog.show(this, game.getMatch());
        showMenu();
    }

    /** Called when the players end the game from the pause dialog. */
    public void onEndRequested() { showMenu(); }

    private void stopGame() {
        if (gamePanel == null) return;
        gamePanel.stop();
        root.remove(gamePanel);
        gamePanel = null;
    }

    private void show(String card, Dimension size) {
        currentSize = size;
        cards.show(root, card);
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new DopoSoccerGUI().setVisible(true));
    }
}

package presentation;

import java.awt.Component;
import javax.swing.JOptionPane;

public final class PauseDialog {
    private PauseDialog() { }

    /** @return true to resume, false to end the game. */
    public static boolean show(Component parent) {
        int choice = JOptionPane.showOptionDialog(parent, "Game paused", "Pause",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[] {"Resume", "End game"}, "Resume");
        return choice != 1;
    }
}

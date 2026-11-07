package presentation;

import domain.DopoSoccerException;
import java.awt.Component;
import javax.swing.JOptionPane;

public final class ErrorDialog {
    private ErrorDialog() { }

    public static void show(Component parent, DopoSoccerException ex) {
        JOptionPane.showMessageDialog(parent, ex.getMessage(), "DOPOSoccer - Error", JOptionPane.ERROR_MESSAGE);
    }
}

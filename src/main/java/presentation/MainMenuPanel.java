package presentation;

import domain.DopoSoccerException;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

public class MainMenuPanel extends JPanel {
    public MainMenuPanel(DopoSoccerGUI gui) {
        setBackground(Theme.BG);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(10, 0, 10, 0);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0;
        add(Theme.label("DOPOSoccer 2026-2", 44, true), c);
        c.gridy = 1;
        add(Theme.label("Head Soccer for the desktop", 18, false), c);

        JButton play = Theme.button("Play");
        play.addActionListener(e -> gui.showConfig());
        c.gridy = 2;
        add(play, c);

        JButton open = Theme.button("Open game");
        open.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
            try {
                gui.getGame().open(chooser.getSelectedFile().getPath());
            } catch (DopoSoccerException ex) {
                ErrorDialog.show(this, ex);
            }
        });
        c.gridy = 3;
        add(open, c);

        JButton exit = Theme.button("Exit");
        exit.addActionListener(e -> System.exit(0));
        c.gridy = 4;
        add(exit, c);
    }
}

package presentation;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/*
 *  Configuramos la modalidad y requisitos de los partidos
 * */
public class ConfigPanel extends JPanel {
    private JRadioButton pvp = radio("Player vs Player", true);
    private JRadioButton pvm = radio("Player vs Machine", false);
    private JComboBox<String> cpuProfile = new JComboBox<>(new String[] {"Dumb", "Aggressive", "Defensive", "Expert"});
    private JComboBox<String> resolution = new JComboBox<>(new String[] {"800 x 450", "1024 x 576", "1280 x 720"});
    private JRadioButton byTime = radio("By time (minutes)", true);
    private JRadioButton byGoals = radio("By goals", false);
    private JSpinner value = new JSpinner(new SpinnerNumberModel(2, 1, 20, 1));

    public ConfigPanel(DopoSoccerGUI gui) {
        setBackground(Theme.BG);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 12, 8, 12);
        c.anchor = GridBagConstraints.WEST;

        ButtonGroup mode = new ButtonGroup();
        mode.add(pvp);
        mode.add(pvm);
        ButtonGroup type = new ButtonGroup();
        type.add(byTime);
        type.add(byGoals);
        cpuProfile.setEnabled(false);
        resolution.setSelectedIndex(1);

        pvm.addActionListener(e -> cpuProfile.setEnabled(true));
        pvp.addActionListener(e -> cpuProfile.setEnabled(false));
        byTime.addActionListener(e -> value.setValue(2));
        byGoals.addActionListener(e -> value.setValue(3));

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        add(Theme.label("Game setup", 32, true), c);
        c.gridwidth = 1;
        row(c, 1, Theme.label("Mode", 18, true), pvp);
        row(c, 2, null, pvm);
        row(c, 3, Theme.label("Machine profile", 18, true), cpuProfile);
        row(c, 4, Theme.label("Field resolution", 18, true), resolution);
        row(c, 5, Theme.label("Match type", 18, true), byTime);
        row(c, 6, null, byGoals);
        row(c, 7, Theme.label("Minutes / goals", 18, true), value);

        JButton back = Theme.button("Back");
        back.addActionListener(e -> gui.showMenu());
        JButton next = Theme.button("Next");
        next.addActionListener(e -> gui.showCharacterSelect());
        c.gridx = 0; c.gridy = 8;
        add(back, c);
        c.gridx = 1;
        add(next, c);
    }

    private static JRadioButton radio(String text, boolean selected) {
        JRadioButton r = new JRadioButton(text, selected);
        r.setBackground(Theme.BG);
        r.setForeground(Theme.TEXT);
        r.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 16));
        return r;
    }

    private void row(GridBagConstraints c, int y, java.awt.Component left, java.awt.Component right) {
        c.gridy = y;
        c.gridx = 0;
        if (left != null) add(left, c);
        c.gridx = 1;
        add(right, c);
    }

    public boolean isVsMachine() { return pvm.isSelected(); }
    public String getCpuProfile() { return ((String) cpuProfile.getSelectedItem()).toUpperCase(); }
    public int getResolutionIndex() { return resolution.getSelectedIndex(); }
    public boolean isByTime() { return byTime.isSelected(); }
    public int getValue() { return (Integer) value.getValue(); }
}

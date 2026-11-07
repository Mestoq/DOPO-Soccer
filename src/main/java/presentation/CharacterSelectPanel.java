package presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/** Each player picks a country. Both may pick the same one. */
public class CharacterSelectPanel extends JPanel {
    private List<String> ids;
    private JToggleButton[][] buttons;
    private JLabel[] titles = new JLabel[2];

    public CharacterSelectPanel(DopoSoccerGUI gui, List<String> catalogIds) {
        this.ids = catalogIds;
        setBackground(Theme.BG);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(Theme.label("Choose your characters", 32, true), BorderLayout.NORTH);

        JPanel columns = new JPanel(new GridLayout(1, 2, 20, 0));
        columns.setOpaque(false);
        buttons = new JToggleButton[2][ids.size()];
        for (int slot = 0; slot < 2; slot++) columns.add(buildColumn(slot));
        add(columns, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        south.setOpaque(false);
        JButton back = Theme.button("Back");
        back.addActionListener(e -> gui.showConfig());
        JButton start = Theme.button("Start match");
        start.addActionListener(e -> gui.startMatch());
        south.add(back);
        south.add(start);
        add(south, BorderLayout.SOUTH);
    }

    private JPanel buildColumn(int slot) {
        JPanel col = new JPanel(new GridLayout(ids.size() + 2, 1, 6, 6));
        col.setBackground(Theme.PANEL);
        col.setBorder(BorderFactory.createLineBorder(Theme.ownerColor(slot), 3));
        titles[slot] = Theme.label("PLAYER " + (slot + 1), 22, true);
        titles[slot].setForeground(Theme.ownerColor(slot));
        titles[slot].setHorizontalAlignment(JLabel.CENTER);
        col.add(titles[slot]);
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < ids.size(); i++) {
            JToggleButton b = new JToggleButton(Theme.prettyName(ids.get(i)));
            b.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18));
            b.setFocusPainted(false);
            buttons[slot][i] = b;
            group.add(b);
            col.add(b);
        }
        buttons[slot][Math.min(slot, ids.size() - 1)].setSelected(true);
        JLabel keys = Theme.label(slot == 0 ? "A / D move, W jump, S kick, E power"
                                            : "Arrows move/jump, L kick, K power", 13, false);
        keys.setForeground(Color.LIGHT_GRAY);
        keys.setHorizontalAlignment(JLabel.CENTER);
        col.add(keys);
        return col;
    }

    public void setVsMachine(boolean vsMachine) {
        titles[1].setText(vsMachine ? "PLAYER 2 (MACHINE)" : "PLAYER 2");
    }

    public String getChoice(int slot) {
        for (int i = 0; i < ids.size(); i++) if (buttons[slot][i].isSelected()) return ids.get(i);
        return ids.get(0);
    }
}

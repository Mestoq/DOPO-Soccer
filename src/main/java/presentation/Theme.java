package presentation;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;

/** Colors, fonts and small widget factories shared by every screen. */
final class Theme {
    static final Color BG = new Color(0x14213D);
    static final Color PANEL = new Color(0x1F2F55);
    static final Color ACCENT = new Color(0xFCA311);
    static final Color TEXT = Color.WHITE;
    static final Color P1 = new Color(0xFF8C00);
    static final Color P2 = new Color(0x9B59B6);

    private Theme() { }

    static Color ownerColor(int slot) { return slot == 0 ? P1 : P2; }

    static JButton button(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 18));
        b.setBackground(ACCENT);
        b.setForeground(BG);
        b.setFocusPainted(false);
        b.setOpaque(true);
        return b;
    }

    static JLabel label(String text, int size, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, size));
        l.setForeground(TEXT);
        return l;
    }

    /** "SOUTH_KOREA" -> "South Korea", "USA" -> "USA". */
    static String prettyName(String id) {
        if (id == null) return "-";
        StringBuilder sb = new StringBuilder();
        for (String part : id.split("_")) {
            if (sb.length() > 0) sb.append(' ');
            if (part.length() <= 3) sb.append(part);
            else sb.append(part.charAt(0)).append(part.substring(1).toLowerCase());
        }
        return sb.toString();
    }
}

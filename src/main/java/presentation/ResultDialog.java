package presentation;

import domain.Match;
import domain.Player;
import java.awt.Component;
import javax.swing.JOptionPane;

public final class ResultDialog {
    private ResultDialog() { }

    public static void show(Component parent, Match match) {
        Player winner = match.getWinner();
        StringBuilder sb = new StringBuilder();
        sb.append(winner == null ? "No winner" : "Winner: " + describe(winner)).append("\n");
        sb.append("Score: ").append(match.getScoreboard().getGoals(0)).append(" - ")
          .append(match.getScoreboard().getGoals(1)).append("\n\nPoints earned:\n");
        for (int i = 0; i < 2; i++) {
            Player p = match.getPlayer(i);
            sb.append(describe(p)).append(": +").append(match.getPointsEarned(p)).append("\n");
        }
        JOptionPane.showMessageDialog(parent, sb.toString(), "Match finished", JOptionPane.INFORMATION_MESSAGE);
    }

    private static String describe(Player p) {
        return "Player " + (p.getSlot() + 1) + " (" + Theme.prettyName(p.getName()) + ")";
    }
}

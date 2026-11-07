package domain;

import java.util.Map;
import java.util.function.Function;

public class GoalsMatch extends Match {
    private int targetGoals;

    public GoalsMatch(Player p1, Player p2, Field field, Map<String, Function<Player, Character>> catalog,
                      int targetGoals) throws DopoSoccerException {
        super(p1, p2, field, catalog);
        if (targetGoals <= 0) throw new DopoSoccerException(DopoSoccerException.INVALID_PARAMETER + ": goals must be > 0");
        this.targetGoals = targetGoals;
    }

    @Override
    public boolean isOver() {
        return scoreboard.getGoals(0) >= targetGoals || scoreboard.getGoals(1) >= targetGoals;
    }

    @Override
    public Player getWinner() { return isOver() ? players[scoreboard.getLeader()] : null; }

    public int getTargetGoals() { return targetGoals; }
}

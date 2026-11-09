package domain;

import java.util.Map;
import java.util.function.Function;

public class TimeMatch extends Match {
    private double durationSeconds;

    public TimeMatch(Player p1, Player p2, Field field, Map<String, Function<Player, Character>> catalog,
                     int minutes) throws DopoSoccerException {
        super(p1, p2, field, catalog);
        if (minutes <= 0) throw new DopoSoccerException(DopoSoccerException.INVALID_PARAMETER + ": minutes must be > 0");
        this.durationSeconds = minutes * 60.0;
    }

    //Si no hay ganador cuando sea el over se juega a la muerte subita
    @Override
    public boolean isOver() {
        return scoreboard.getElapsedTime() >= durationSeconds && scoreboard.getLeader() != -1;
    }

    @Override
    public Player getWinner() { return isOver() ? players[scoreboard.getLeader()] : null; }

    public double getRemainingTime() { return Math.max(0, durationSeconds - scoreboard.getElapsedTime()); }
    public boolean isSuddenDeath() {
        return scoreboard.getElapsedTime() >= durationSeconds && scoreboard.getLeader() == -1;
    }
}

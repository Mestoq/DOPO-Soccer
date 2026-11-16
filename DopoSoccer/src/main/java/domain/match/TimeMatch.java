package domain.match;

import domain.physics.Field;
import domain.player.Player;

public class TimeMatch extends Match {

    public TimeMatch(Field field, Player player1, Player player2, double durationSeconds) {
        super(field, player1, player2, InGameInfo.createTimeMode(durationSeconds));
    }

    @Override
    protected void updateModeRules(double dt) {
        gameInfo.updateTimer(dt);
        if (gameInfo.getRemainingTime() <= 0.0) {
            gameInfo.finishMatch();
        }
    }

    @Override
    protected void checkEndCondition() {
    }
}
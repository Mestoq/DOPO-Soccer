package domain.match;

import domain.physics.Field;
import domain.player.Player;

public class GoalMatch extends Match {

    public GoalMatch(Field field, Player player1, Player player2, int targetGoals) {
        super(field, player1, player2, InGameInfo.createGoalMode(targetGoals));
    }

    @Override
    protected void updateModeRules(double dt) {
        gameInfo.elapsedTime(dt);
    }

    @Override
    protected void checkEndCondition() {
        if (gameInfo.hasReachedTargetGoals()) {
            gameInfo.finishMatch();
        }
    }
}
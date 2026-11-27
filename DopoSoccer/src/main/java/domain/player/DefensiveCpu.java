package domain.player;

import domain.match.Match;
import domain.physics.Ball;
import domain.physics.Field;

/**
 * Defensiva: su prioridad es defender el arco.
 */
public class DefensiveCpu extends Cpu {

    public DefensiveCpu(String name) {
        super(name, PROFILE_DEFENSIVE);
    }

    @Override
    protected void execute(double dt, Match match, Ball ball, Field field) {
        defend(ball, field);
    }
}
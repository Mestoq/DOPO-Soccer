package domain.player;

import domain.match.Match;
import domain.physics.Ball;
import domain.physics.Field;

/**
 * Ofensiva: su prioridad es anotar.
 */
public class OffensiveCpu extends Cpu {

    public OffensiveCpu(String name) {
        super(name, PROFILE_OFFENSIVE);
    }

    @Override
    protected void execute(double dt, Match match, Ball ball, Field field) {
        attack(ball);
    }
}
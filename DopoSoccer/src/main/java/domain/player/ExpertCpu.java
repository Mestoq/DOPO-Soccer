package domain.player;

import domain.match.Match;
import domain.physics.Ball;
import domain.physics.Field;

/**
 * Experta: decide la acción según el estado del partido.
 */
public class ExpertCpu extends Cpu {

    public ExpertCpu(String name) {
        super(name, PROFILE_EXPERT);
    }

    @Override
    protected void execute(double dt, Match match, Ball ball, Field field) {
        Player rival = match.getPlayer1();
        int goalDiff = this.goals - (rival != null ? rival.getGoals() : 0);

        // Si el rival está aturdido o va perdiendo, presiona al ataque
        if ((rival != null && rival.isStunned()) || goalDiff < 0) {
            attack(ball);
            return;
        }

        // Si va ganando y el balón está en campo rival, asegura la defensa
        if (goalDiff > 0 && ball.getX() < field.getWidth() * 0.45) {
            defend(ball, field);
            return;
        }

        // En empate o balón disputado: defiende si el balón amenaza su área, ataca si está libre
        if (ball.getX() > field.getWidth() * 0.60) {
            defend(ball, field);
        } else {
            attack(ball);
        }
    }
}
package domain.power;

import domain.character.Character_;
import domain.match.Match;
import domain.physics.Ball;
import domain.power.effect.Effect;

public class TornadoPower extends Power {

    public TornadoPower() {
        super("Tornado", 40.0);
    }

    @Override
    public void launchShot(Ball ball, Character_ shooter, Match match) {
        this.activeShotInAir = true;
        double dir = shooter.isFacingRight() ? 1.0 : -1.0;
        // Remolino a ras de suelo
        ball.launch(dir * 13.5, 2.5);
    }

    @Override
    public Effect createImpactEffect() {
        return new UnconsciousEffect();
    }
}
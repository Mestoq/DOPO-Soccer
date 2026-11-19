package domain.power;

import domain.character.Character_;
import domain.match.Match;
import domain.physics.Ball;
import domain.power.effect.Effect;
import domain.power.effect.UnconsciousEffect;

public class TornadoPower extends Power {

    public TornadoPower() {
        super("Tornado", 40.0);
    }

    @Override
    public void launchShot(Ball ball, Character_ shooter, Match match) {
        this.activeShotInAir = true;
        double dir = shooter.isFacingRight() ? 1.0 : -1.0;
        //pa que quede pegado al suelo
        ball.launch(dir * 13.5, 2.5);
    }
    @Override
    public double getGravityScale() { return 3.0; }

    @Override
    public boolean endsOnGroundContact() { return false; }

    @Override
    public Effect createImpactEffect() {
        return new UnconsciousEffect();
    }
}
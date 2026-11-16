package domain.power;

import domain.character.Character_;
import domain.match.Match;
import domain.physics.Ball;
import domain.power.effect.Effect;
import domain.power.effect.PushEffect;

public class BlueAuraPower extends Power {

    private boolean shotRight;

    public BlueAuraPower() {
        super("Aura Azul", 40.0);
        this.shotRight = true;
    }

    public void launchShot(Ball ball, Character_ shooter, Match match) {
        this.shotRight = shooter.isFacingRight();
        this.activeShotInAir = true;
        double dir = shotRight ? 1.0 : -1.0;
        // Tiro recto horizontal a muy alta velocidad
        ball.launch(dir * 16.5, -0.5);
    }



    @Override
    public Effect createImpactEffect() {
        return new PushEffect(this.shotRight);
    }
}
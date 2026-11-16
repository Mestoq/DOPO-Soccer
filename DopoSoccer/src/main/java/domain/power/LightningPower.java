package domain.power;

import domain.character.Character_;
import domain.match.Match;
import domain.physics.Ball;
import domain.power.effect.Effect;
import domain.power.effect.ElectrocutedEffect;

public class LightningPower extends Power {

    public LightningPower() {
        super("Rayo", 60.0);
    }

    @Override
    public void launchShot(Ball ball, Character_ shooter, Match match) {
        this.activeShotInAir = true;
        double dir = shooter.isFacingRight() ? 1.0 : -1.0;
        // Tiro recto potente cubierto de electricidad
        ball.launch(dir * 15.0, -1.2);
    }

    @Override
    public Effect createImpactEffect() {
        return new ElectrocutedEffect();
    }
}
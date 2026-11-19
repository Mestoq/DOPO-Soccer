package domain.power;

import domain.character.Character_;
import domain.match.Match;
import domain.physics.Ball;
import domain.power.effect.ConfusionEffect;
import domain.power.effect.Effect;

public class IllusionPower extends Power {

    public IllusionPower() {
        super("Ilusión", 30.0);
    }



    @Override
    public void launchShot(Ball ball, Character_ shooter, Match match) {
        this.activeShotInAir = true;
        double dir = shooter.isFacingRight() ? 1.0 : -1.0;

        ball.launch(dir * 12.5, -2.0);

        double[] verticalSpeeds = {-5.5, -4.0, -2.8, -1.0, 0.5, 1.8, 3.0};
        for (int i = 0; i < 7; i++) {

            Ball fakeBall = new Ball(ball.getRadius(), true, ConfusionEffect.DURATION);
            double vx = dir * (11.5 + (i % 3) * 0.8);
            match.spawnFakeBall(fakeBall, ball.getX(), ball.getY(), vx, verticalSpeeds[i]);
        }
    }

    @Override
    public Effect createImpactEffect() {
        return new ConfusionEffect();
    }
}
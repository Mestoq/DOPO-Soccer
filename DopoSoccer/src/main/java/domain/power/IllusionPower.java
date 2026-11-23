package domain.power;

import domain.character.Character_;
import domain.match.Match;
import domain.physics.Ball;
import domain.physics.Field;
import domain.power.effect.ConfusionEffect;
import domain.power.effect.Effect;

public class IllusionPower extends Power {

    // Lanzamiento del balón real y los falsos conservan este mismo ángulo
    private static final double REAL_VX = 12.5;
    private static final double REAL_VY = -2.0;

    // Un falso por posición (ninguno es 1.0 para no igualar la velocidad del real)
    private static final double[] FAKE_SPEED_FACTORS = {0.70, 1.25, 0.85, 1.40, 0.95, 1.15, 0.60};
    // Desplazamiento inicial en altura, en múltiplos del radio del balón (negativo = más arriba)
    private static final double[] FAKE_HEIGHT_OFFSETS = {-5.0, 2.0, -2.5, 4.0, -0.8, -4.0, 1.0};

    public IllusionPower() {
        super("Ilusión", 30.0);
    }

    @Override
    public void launchShot(Ball ball, Character_ shooter, Match match) {
        this.activeShotInAir = true;
        double dir = shooter.isFacingRight() ? 1.0 : -1.0;

        ball.launch(dir * REAL_VX, REAL_VY);

        Field field = match.getField();
        double minY = ball.getRadius();
        double maxY = field.getGroundLevelY() - ball.getRadius();

        for (int i = 0; i < FAKE_SPEED_FACTORS.length; i++) {
            double factor = FAKE_SPEED_FACTORS[i];
            double y = ball.getY() + FAKE_HEIGHT_OFFSETS[i] * ball.getRadius();
            y = Math.max(minY, Math.min(maxY, y)); // que no nazca fuera de la cancha

            Ball fakeBall = new Ball(ball.getRadius(), true, ConfusionEffect.DURATION);
            // Mismo ángulo que el real (vy/vx constante), distinta velocidad
            match.spawnFakeBall(fakeBall, ball.getX(), y, dir * REAL_VX * factor, REAL_VY * factor);
        }
    }

    @Override
    public Effect createImpactEffect() {
        return new ConfusionEffect();
    }
}
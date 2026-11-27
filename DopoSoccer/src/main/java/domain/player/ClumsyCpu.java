package domain.player;

import domain.match.Match;
import domain.physics.Ball;
import domain.physics.Field;

import java.util.Random;

/**
 * Torpe: escoge al azar sus movimientos.
 */
public class ClumsyCpu extends Cpu {

    private final Random random;
    private double decisionCooldown;
    private int currentAction; // 0: quieto, 1: izquierda, 2: derecha

    public ClumsyCpu(String name) {
        super(name, PROFILE_CLUMSY);
        this.random = new Random();
        this.decisionCooldown = 0.0;
        this.currentAction = 0;
    }

    @Override
    protected boolean activatesPowerAutomatically() {
        return false;
    }

    @Override
    protected void execute(double dt, Match match, Ball ball, Field field) {
        this.decisionCooldown -= dt;
        if (this.decisionCooldown <= 0.0) {
            this.decisionCooldown = 0.4 + random.nextDouble() * 0.6; // Cambia de idea cada 0.4 - 1.0s
            this.currentAction = random.nextInt(3);

            if (random.nextDouble() < 0.25) {
                this.character.jump(this.jump);
            }
            if (random.nextDouble() < 0.30) {
                this.character.kick(getNormalKickDamage());
            }
            if (random.nextDouble() < 0.20) {
                activatePower();
            }
        }

        if (currentAction == 1) {
            this.character.moveLeft(this.speed);
        } else if (currentAction == 2) {
            this.character.moveRight(this.speed);
        } else {
            this.character.stopHorizontal();
        }
    }
}
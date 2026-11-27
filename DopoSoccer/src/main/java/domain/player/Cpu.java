package domain.player;

import domain.DopoSoccerException;
import domain.ErrorLog;
import domain.character.Character_;
import domain.match.Match;
import domain.physics.Ball;
import domain.physics.Field;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class Cpu extends Player {

    public static final String PROFILE_CLUMSY = "TORPE";
    public static final String PROFILE_OFFENSIVE = "OFENSIVA";
    public static final String PROFILE_DEFENSIVE = "DEFENSIVA";
    public static final String PROFILE_EXPERT = "EXPERTA";

    private static final Map<String, Function<String, Cpu>> CPU_FACTORIES = new HashMap<>();

    static {
        CPU_FACTORIES.put(PROFILE_CLUMSY,    ClumsyCpu::new);
        CPU_FACTORIES.put(PROFILE_OFFENSIVE, OffensiveCpu::new);
        CPU_FACTORIES.put(PROFILE_DEFENSIVE, DefensiveCpu::new);
        CPU_FACTORIES.put(PROFILE_EXPERT,    ExpertCpu::new);
    }

    protected final String profile;

    protected Cpu(String name, String profile) {
        super(name, false);
        this.profile = profile;
    }

    /**
     * Crea la CPU del perfil indicado. Si el perfil no es válido se usa TORPE por defecto.
     */
    public static Cpu create(String name, String profile) {
        String key = (profile == null) ? null : profile.trim().toUpperCase();
        Function<String, Cpu> factory = (key == null) ? null : CPU_FACTORIES.get(key);
        if (factory == null) {
            ErrorLog.warn(DopoSoccerException.INVALID_PROFILE_DEFAULT_CLUMSY + profile);
            factory = CPU_FACTORIES.get(PROFILE_CLUMSY);
        }
        return factory.apply(name);
    }

    /**
     * Decisión propia de cada perfil en este fotograma.
     */
    protected abstract void execute(double dt, Match match, Ball ball, Field field);

    /**
     * Indica si activa el poder apenas se llena la barra (el Torpe lo decide al azar).
     */
    protected boolean activatesPowerAutomatically() {
        return true;
    }

    @Override
    public void decideAction(double dt, Match match) {
        if (this.character == null || match == null) {
            return;
        }

        Ball ball = match.getMainBall();
        Field field = match.getField();
        if (ball == null || field == null) {
            return;
        }

        if (activatesPowerAutomatically() && this.powerBar >= 100.0 && !this.powerAuraActive) {
            activatePower();
        }

        execute(dt, match, ball, field);
    }

    // Comportamientos compartidos entre perfiles

    /**
     * Prioridad: anotar. La CPU defiende el arco derecho y ataca hacia la izquierda.
     */
    protected void attack(Ball ball) {
        Character_ c = this.character;
        double charX = c.getX();
        double ballX = ball.getX();
        double ballY = ball.getY();

        // Se ubica ligeramente a la derecha del balón para empujarlo hacia el arco rival
        double targetX = ballX + (c.getLogicalWidth() * 0.45);
        moveTowards(charX, targetX);

        // Si el balón está cerca y en el aire, salta para cabecear
        if (Math.abs(ballX - charX) < 90.0 && ballY < c.getY() - 30.0) {
            c.jump(this.jump);
        }

        // Si tiene el balón a tiro frente a él, patea
        if (Math.abs(ballX - charX) < 75.0 && ballX <= charX) {
            c.kick(getNormalKickDamage());
        }
    }

    /**
     * Prioridad: defender su arco.
     */
    protected void defend(Ball ball, Field field) {
        Character_ c = this.character;
        double charX = c.getX();
        double ballX = ball.getX();
        double ballY = ball.getY();
        double fieldWidth = field.getWidth();

        double homeGuardX = fieldWidth * 0.82;

        if (ballX > fieldWidth * 0.50) {
            // Si el balón cruza a su mitad de cancha, sale a despejarlo
            moveTowards(charX, ballX + 20.0);

            if (Math.abs(ballX - charX) < 85.0 && ballY < c.getY() - 25.0) {
                c.jump(this.jump);
            }
            if (Math.abs(ballX - charX) < 80.0) {
                c.kick(getNormalKickDamage());
            }
        } else {
            // Si el balón está lejos, regresa a proteger su arco
            moveTowards(charX, homeGuardX);
        }
    }

    protected void moveTowards(double currentX, double targetX) {
        double tolerance = 12.0; // evita que se detenga en seco o tenga movimiento errático
        if (currentX > targetX + tolerance) {
            this.character.moveLeft(this.speed);
        } else if (currentX < targetX - tolerance) {
            this.character.moveRight(this.speed);
        } else {
            this.character.stopHorizontal();
        }
    }

    @Override
    public String getAiProfile() { return profile; }

    public String getProfile() { return profile; }
}
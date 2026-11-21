package domain.player;

import domain.match.Match;
import domain.physics.Ball;
import domain.physics.Field;
import domain.ErrorLog;
import domain.DopoSoccerException;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Cpu extends Player {

    public static final String PROFILE_CLUMSY = "TORPE";
    public static final String PROFILE_OFFENSIVE = "OFENSIVA";
    public static final String PROFILE_DEFENSIVE = "DEFENSIVA";
    public static final String PROFILE_EXPERT = "EXPERTA";

    private String profile;
    private final Random random;
    private double decisionCooldown;
    private int clumsyCurrentAction; // 0: quieto, 1: izquierda, 2: derecha

    private final Map<String, ProfileStrategy> strategies = new HashMap<>();

    @FunctionalInterface
    private interface ProfileStrategy {
        void execute(double dt, Match match, Ball ball, Field field);
    }

    public Cpu(String name, String profile) {
        super(name, false);
        this.random = new Random();
        this.decisionCooldown = 0.0;
        this.clumsyCurrentAction = 0;
        initStrategies();
        setProfile(profile);
    }

    private void initStrategies() {
        strategies.put(PROFILE_CLUMSY, this::executeClumsy);
        strategies.put(PROFILE_OFFENSIVE, this::executeOffensive);
        strategies.put(PROFILE_DEFENSIVE, this::executeDefensive);
        strategies.put(PROFILE_EXPERT, this::executeExpert);
    }

    public void setProfile(String profile) {
        String key = (profile == null) ? null : profile.trim().toUpperCase();
        if (key == null || !strategies.containsKey(key)) {
            ErrorLog.warn(DopoSoccerException.INVALID_PROFILE_DEFAULT_CLUMSY + profile);
            this.profile = PROFILE_CLUMSY;
        } else {
            this.profile = key;
        }
    }

    @Override
    public String getAiProfile() { return profile; }

    public String getProfile() {
        return profile;
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

        // Si tiene la barra llena, activa el poder automáticamente (excepto el torpe que lo hace al azar)
        if (!PROFILE_CLUMSY.equals(this.profile) && this.powerBar >= 100.0 && !this.powerAuraActive) {
            activatePower();
        }

        ProfileStrategy strategy = strategies.get(this.profile);
        if (strategy != null) {
            strategy.execute(dt, match, ball, field);
        }
    }

    //  torpe (Movimientos al azar)
    private void executeClumsy(double dt, Match match, Ball ball, Field field) {
        this.decisionCooldown -= dt;
        if (this.decisionCooldown <= 0.0) {
            this.decisionCooldown = 0.4 + random.nextDouble() * 0.6; // Cambia de idea cada 0.4 - 1.0s
            this.clumsyCurrentAction = random.nextInt(3);

            if (random.nextDouble() < 0.25) {
                this.character.jump(this.jump);
            }
            if (random.nextDouble() < 0.30) {
                this.character.kick(this.getNormalKickDamage());
            }
            if (random.nextDouble() < 0.20) {
                activatePower();
            }
        }

        if (clumsyCurrentAction == 1) {
            this.character.moveLeft(this.speed);
        } else if (clumsyCurrentAction == 2) {
            this.character.moveRight(this.speed);
        } else {
            this.character.stopHorizontal();
        }
    }

    // ofensiva (Prioridad: anotar)
    private void executeOffensive(double dt, Match match, Ball ball, Field field) {
        double charX = this.character.getX();
        double ballX = ball.getX();
        double ballY = ball.getY();

        // Vamos definir que la CPU defiende el arco derecho y ataca hacia la izquierda
        // Buscara ubicarse ligeramente a la derecha del balón para empujarlo hacia el arco rival
        double targetX = ballX + (this.character.getLogicalWidth() * 0.45);

        moveTowards(charX, targetX);

        // Si el balón está cerca y en el aire, salta para cabecear
        if (Math.abs(ballX - charX) < 90.0 && ballY < this.character.getY() - 30.0) {
            this.character.jump(this.jump);
        }

        // Si tiene el balón a tiro frente a él, patea
        if (Math.abs(ballX - charX) < 75.0 && ballX <= charX) {
            this.character.kick(this.getNormalKickDamage());
        }
    }

    // defensiva (Prioridad: defender su arco)
    private void executeDefensive(double dt, Match match, Ball ball, Field field) {
        double charX = this.character.getX();
        double ballX = ball.getX();
        double ballY = ball.getY();
        double fieldWidth = field.getWidth();

        // Zona de resguardo frente a su portería (entre el 72% y el 88% del ancho de la cancha)
        double homeGuardX = fieldWidth * 0.82;

        if (ballX > fieldWidth * 0.50) {
            // Si el balón cruza a su mitad de cancha, sale a despejarlo
            moveTowards(charX, ballX + 20.0);

            if (Math.abs(ballX - charX) < 85.0 && ballY < this.character.getY() - 25.0) {
                this.character.jump(this.jump);
            }
            if (Math.abs(ballX - charX) < 80.0) {
                this.character.kick(this.getNormalKickDamage());
            }
        } else {
            // Si el balón está lejos, regresa a proteger su arco
            moveTowards(charX, homeGuardX);
        }
    }

    // experta (Decide según el estado del partido)
    private void executeExpert(double dt, Match match, Ball ball, Field field) {
        Player rival = match.getPlayer1();
        int goalDiff = this.goals - (rival != null ? rival.getGoals() : 0);

        // Si el rival está aturdido o vamos perdiendo, presiona al ataque
        if ((rival != null && rival.isStunned()) || goalDiff < 0) {
            executeOffensive(dt, match, ball, field);
            return;
        }

        // Si va ganando y el balón está en campo rival, asegura la defensa
        if (goalDiff > 0 && ball.getX() < field.getWidth() * 0.45) {
            executeDefensive(dt, match, ball, field);
            return;
        }

        // En empate o balón disputado: defiende si el balón amenaza su área, ataca si está libre
        if (ball.getX() > field.getWidth() * 0.60) {
            executeDefensive(dt, match, ball, field);
        } else {
            executeOffensive(dt, match, ball, field);
        }
    }

    private void moveTowards(double currentX, double targetX) {
        double tolerance = 12.0; // para evitar que se detenga en seco o tenga movimiento erratico
        if (currentX > targetX + tolerance) {
            this.character.moveLeft(this.speed);
        } else if (currentX < targetX - tolerance) {
            this.character.moveRight(this.speed);
        } else {
            this.character.stopHorizontal();
        }
    }
}
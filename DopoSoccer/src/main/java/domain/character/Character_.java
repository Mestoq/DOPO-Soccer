package domain.character;

import domain.power.effect.Effect;
import domain.player.Player;
import domain.power.Power;
import domain.match.Match;
import domain.physics.Ball;
import domain.physics.GameObject;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;
import org.dyn4j.collision.CategoryFilter;

public abstract class Character_ extends GameObject {

    private static final double KICK_BALL_SPEED_X = 7.95;
    private static final double KICK_BALL_SPEED_Y = -4.2;
    private static final double KICK_REACH_FACTOR = 1.2;

    protected final String country;
    protected final Power specialPower;
    protected Effect activeEffect;

    // Orientación y dimensiones
    protected boolean facingRight;
    protected double groundLevelY;

    // Estado de patada y daño activo
    protected boolean kicking;
    protected double kickTimer;
    protected double currentKickDamage;
    protected boolean kickResolved;

    // Modificadores alterados por los Effect
    protected double speedMultiplier;
    protected boolean canJump;
    protected boolean incapacitated;
    protected boolean velocityLocked;
    protected boolean stunned;

    public static final String KOREA = "KOREA";
    public static final String CAMEROON = "CAMEROON";
    public static final String NIGERIA = "NIGERIA";
    public static final String USA = "USA";

    protected Character_(String country, Power specialPower) {
        super(75.0, 95.0);
        this.country = country;
        this.specialPower = specialPower;
        this.activeEffect = null;
        resetModifiers();
    }

    /**
     * Configura el cuerpo rígido del personaje en dyn4j según la resolución de Field.
     */
    public void initPhysics(World<Body> world, double startX, double startY, double width, double height, boolean facingRight, double groundLevelY) {
        this.logicalWidth = width;
        this.logicalHeight = height;
        this.facingRight = facingRight;
        this.groundLevelY = groundLevelY;

        this.body.removeAllFixtures();

        double widthMeters = width / PIXELS_PER_METER;
        double heightMeters = height / PIXELS_PER_METER;

        BodyFixture mainFixture = new BodyFixture(Geometry.createCapsule(widthMeters, heightMeters));
        mainFixture.setDensity(5.0);
        mainFixture.setFriction(0.0);
        mainFixture.setRestitution(0.1);
        mainFixture.setFilter(new CategoryFilter(CATEGORY_PLAYABLE, Long.MAX_VALUE));
        this.body.addFixture(mainFixture);

        this.body.setMass(MassType.FIXED_ANGULAR_VELOCITY);
        this.body.setAtRestDetectionEnabled(false);

        attachToWorld(world, startX, startY);
    }

    // logica de movimientos

    public void moveLeft(int speedStat) {
        if (cannotAct()) {
            stopHorizontal();
            return;
        }
        this.facingRight = false;
        double baseSpeedMeters = 3.2 * (1.0 + 0.10 * (speedStat - 1)) * this.speedMultiplier;
        Vector2 currentVel = this.body.getLinearVelocity();
        this.body.setLinearVelocity(-baseSpeedMeters, currentVel.y);
    }

    public void moveRight(int speedStat) {
        if (cannotAct()) {
            stopHorizontal();
            return;
        }
        this.facingRight = true; // [FIX 4]
        double baseSpeedMeters = 3.2 * (1.0 + 0.10 * (speedStat - 1)) * this.speedMultiplier;
        Vector2 currentVel = this.body.getLinearVelocity();
        this.body.setLinearVelocity(baseSpeedMeters, currentVel.y);
    }

    public void stopHorizontal() {
        if (this.velocityLocked) {
            return;
        }
        Vector2 currentVel = this.body.getLinearVelocity();
        this.body.setLinearVelocity(0.0, currentVel.y);
    }

    public void jump(int jumpStat) {
        if (cannotAct() || !this.canJump || !isOnGround()) { // [FIX 6]
            return;
        }
        double jumpImpulse = -6.5 * (1.0 + 0.10 * (jumpStat - 1));
        Vector2 currentVel = this.body.getLinearVelocity();
        this.body.setLinearVelocity(currentVel.x, jumpImpulse);
    }

    public void kick(double kickDamage) {
        if (cannotAct() || this.kicking) {
            return;
        }
        this.kicking = true;
        this.kickTimer = 0.22;
        this.currentKickDamage = kickDamage;

        this.kickResolved = false;
    }

    // los efectos y como se gestiona

    public void applyEffect(Effect newEffect) {
        if (this.activeEffect != null) {
            this.activeEffect.onExpire(this);
        }
        this.activeEffect = newEffect;
        if (this.activeEffect != null) {
            this.activeEffect.onApply(this);
        }
    }

    public void resetModifiers() {
        this.speedMultiplier = 1.0;
        this.canJump = true;
        this.incapacitated = false;
        this.kicking = false;
        this.kickTimer = 0.0;
        this.kickResolved = false;
        this.velocityLocked = false;
    }

    // el update para match

    @Override
    public void update(double dt) {
        if (this.kicking) {
            this.kickTimer -= dt;
            if (this.kickTimer <= 0.0) {
                this.kicking = false;
                this.kickTimer = 0.0;
            }
        }

        if (this.activeEffect != null) {
            this.activeEffect.update(dt, this);
            if (this.activeEffect.isExpired()) {
                this.activeEffect = null;
            }
        }
    }

    public boolean isOnGround() {
        double bottomY = getY() + (this.logicalHeight / 2.0);
        return bottomY >= (this.groundLevelY - 4.0) && Math.abs(this.body.getLinearVelocity().y) < 0.2;
    }

    /**
     * Ejecuta el tiro de poder específico del país cuando el jugador con aura toca el balón.
     */
    public abstract void executeSpecialShot(Ball ball, Match match);

    // lo de collidable

    @Override
    public void collideWithBall(Ball ball, BodyFixture myFixture, Match match) {
        if (ball.isFake()) {
            match.removeFakeBall(ball);
            return;
        }
        Player me = match.getOwnerOf(this);
        if (ball.hasActivePower() && ball.getPowerOwner() != null && ball.getPowerOwner() != me) {
            ball.getActivePower().onHitRival(ball.getPowerOwner(), me);
            ball.clearPowerShot();
        }
    }

    @Override
    public void postSolveWithBall(Ball ball, Match match) {
        if (ball.isFake()) {
            return;
        }
        Player me = match.getOwnerOf(this);
        if (me == null) {
            return;
        }

        if (me.isPowerReady()) {
            me.consumePower();
            executeSpecialShot(ball, match);
            ball.activatePowerShot(this.specialPower, me);
            // si el contacto fue un tiro de poder, esta patada ya no daña ni vuelve a impulsar
            this.kickResolved = true;
            return;
        }

        // validar cosas del powershot mas que todo respecto a donde se activa o interactua y ademas de aplicar el poder
        boolean carryingMyPowerShot = ball.hasActivePower() && ball.getPowerOwner() == me;
        if (this.kicking && !this.kickResolved && !carryingMyPowerShot) {
            double dirX = this.facingRight ? 1.0 : -1.0;
            boolean ballInFront = (ball.getX() - getX()) * dirX >= 0.0;
            if (ballInFront) {
                ball.launch(dirX * KICK_BALL_SPEED_X, KICK_BALL_SPEED_Y);
                this.kickResolved = true;
            }
        }
    }

    private boolean cannotAct() {
        return this.incapacitated || this.stunned;
    }

    public void setStunned(boolean stunned) {
        this.stunned = stunned;
        if (stunned) {
            this.kicking = false;
            this.kickTimer = 0.0;
            stopHorizontal();
        }
    }

    // patada en el aire, osea, que aun no define que hizo
    public boolean isKickDamagePending() {
        return this.kicking && !this.kickResolved;
    }

    public void markKickResolved() {
        this.kickResolved = true;
    }

    public boolean isInKickRange(Character_ rival) {
        double dir = this.facingRight ? 1.0 : -1.0;
        double dx = (rival.getX() - getX()) * dir;
        double dy = Math.abs(rival.getY() - getY());
        double reach = this.logicalWidth * KICK_REACH_FACTOR;
        return dx > 0.0 && dx <= reach && dy <= this.logicalHeight * 0.75;
    }

    // Para los efectos que controlan el efecto de empuje, osea, bloquean/desbloquean los atributos
    public void setVelocityLocked(boolean velocityLocked) { this.velocityLocked = velocityLocked; }

    // pa la persistencia

    public void setFacingRight(boolean facingRight) { this.facingRight = facingRight; }

    // getters y setters

    public String getCountry() { return country; }
    public Power getSpecialPower() { return specialPower; }
    public Effect getActiveEffect() { return activeEffect; }
    public boolean isFacingRight() { return facingRight; }
    public boolean isKicking() { return kicking; }
    public double getCurrentKickDamage() { return currentKickDamage; }

    public void setSpeedMultiplier(double speedMultiplier) { this.speedMultiplier = speedMultiplier; }
    public void setCanJump(boolean canJump) { this.canJump = canJump; }
    public void setIncapacitated(boolean incapacitated) { this.incapacitated = incapacitated; }
}
package domain.physics;

import domain.match.Match;
import domain.player.Player;
import domain.power.Power;
import org.dyn4j.collision.CategoryFilter;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;

public class Ball extends GameObject {

    private double radius;
    private final boolean fake;
    private double fakeRemainingTime;


    // Estado cuando el balón viaja con algun efecto de un tiro especial
    private Power activePower;
    private Player powerOwner;

    private static final double MIN_POWER_SHOT_SPEED = 2.0;

    /**
     * Constructor principal para el balón real del partido.
     */
    public Ball(double radius) {
        this(radius, false, 0.0);
    }

    /**
     * Constructor completo que ya permitiria crear los 7 balones falsos del poder Ilusión
     */
    public Ball(double radius, boolean fake, double fakeDuration) {
        super(radius * 2.0, radius * 2.0);
        this.radius = radius;
        this.fake = fake;
        this.fakeRemainingTime = fakeDuration;
        this.activePower = null;
        this.powerOwner = null;
    }

    /**
     * Configura la hitbox circular y las propiedades físicas del balón en dyn4j.
     */
    public void initPhysics(World<Body> world, double startX, double startY, double radius) {
        this.radius = radius;
        this.logicalWidth = radius * 2.0;
        this.logicalHeight = radius * 2.0;

        this.body.removeAllFixtures();

        double radiusMeters = radius / PIXELS_PER_METER;
        BodyFixture ballFixture = new BodyFixture(Geometry.createCircle(radiusMeters));
        ballFixture.setDensity(1.2);
        ballFixture.setFriction(0.15);
        ballFixture.setRestitution(0.78);


        if (this.fake) {
            ballFixture.setFilter(new CategoryFilter(CATEGORY_FAKE, CATEGORY_WORLD));
        } else {
            ballFixture.setFilter(new CategoryFilter(CATEGORY_PLAYABLE, Long.MAX_VALUE));
        }

        this.body.addFixture(ballFixture);
        this.body.setMass(MassType.NORMAL);
        this.body.setLinearDamping(0.08);
        this.body.setAngularDamping(0.20);

        attachToWorld(world, startX, startY);
    }



    /**
     * Aplica un impulso de patada o cabezazo en unidades de velocidad .
     */
    public void launch(double velocityXMeters, double velocityYMeters) {
        this.body.setLinearVelocity(new Vector2(velocityXMeters, velocityYMeters));
        // Aplica rotación visual proporcional al desplazamiento horizontal
        this.body.setAngularVelocity(velocityXMeters * 1.5);
    }

    /**
     * Carga el balón con un tiro de poder activo lanzado por un jugador.
     */
    public void activatePowerShot(Power power, Player owner) {
        this.activePower = power;
        this.powerOwner = owner;

        this.body.setGravityScale(power.getGravityScale());
    }

    /**
     * Limpia el estado de tiro de poder (por ejemplo, cuando toca el suelo o tras impactar al rival).
     */
    public void clearPowerShot() {
        if (this.activePower != null) {
            this.activePower.deactivateShot();
        }
        this.activePower = null;
        this.powerOwner = null;
        this.body.setGravityScale(1.0); // [FIX 12] el balón vuelve a la gravedad normal
    }

    /**
     * Reinicia el balón al centro de la cancha tras un gol.
     */
    public void resetToCenter(double centerX, double centerY) {
        clearPowerShot();
        resetPosition(centerX, centerY);
    }

    @Override
    public void update(double dt) {
        if (this.fake) {
            this.fakeRemainingTime -= dt;
        } else if (this.activePower != null
                && this.body.getLinearVelocity().getMagnitude() < MIN_POWER_SHOT_SPEED) {
            clearPowerShot();
        }
    }

    public boolean isExpiredFake() {
        return this.fake && this.fakeRemainingTime <= 0.0;
    }

    //para el collidable

    @Override
    public void onCollisionBegin(Collidable other, BodyFixture myFixture, BodyFixture otherFixture, Match match) {
        other.collideWithBall(this, otherFixture, match);
    }

    @Override
    public void onCollisionPostSolve(Collidable other, Match match) {
        other.postSolveWithBall(this, match);
    }



    // los getters de toda la vida

    public double getRadius() { return radius; }
    public boolean isFake() { return fake; }
    public boolean hasActivePower() { return activePower != null && activePower.isActiveShotInAir(); }
    public Power getActivePower() { return activePower; }
    public Player getPowerOwner() { return powerOwner; }
    public double getAngleRadians() { return this.body.getTransform().getRotationAngle(); }
}
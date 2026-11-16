package domain.physics;

import domain.match.Match;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.world.World;

public class Goal extends GameObject {

    private final boolean leftSide;
    private BodyFixture goalLineSensor;
    private BodyFixture crossbarFixture;

    /**
     * @param leftSide true para el arco izquierdo (defendido por P1), false para el derecho (P2).
     */
    public Goal(boolean leftSide, double width, double height) {
        super(width, height);
        this.leftSide = leftSide;
    }

    /**
     * Crea el sensor de gol y el travesaño sólido dentro del World de dyn4j.
     */
    public void initPhysics(World<Body> world, double centerX, double centerY, double width, double height) {
        this.logicalWidth = width;
        this.logicalHeight = height;

        this.body.removeAllFixtures();

        double wMeters = width / PIXELS_PER_METER;
        double hMeters = height / PIXELS_PER_METER;
        double crossbarThickness = 0.14; // 14 px lógicos de grosor de travesaño

        // 1. Travesaño superior (sólido, con buen rebote)
        Rectangle crossbarRect = Geometry.createRectangle(wMeters, crossbarThickness);
        crossbarRect.translate(0.0, -(hMeters / 2.0) + (crossbarThickness / 2.0));
        this.crossbarFixture = new BodyFixture(crossbarRect);
        this.crossbarFixture.setRestitution(0.85);
        this.crossbarFixture.setFriction(0.1);
        this.body.addFixture(this.crossbarFixture);

        // 2. Sensor interior de gol (desplazado ligeramente hacia adentro para que entre todo el balón)
        double sensorWidth = wMeters * 0.65;
        double sensorHeight = hMeters - crossbarThickness;
        Rectangle sensorRect = Geometry.createRectangle(sensorWidth, sensorHeight);
        double offsetX = leftSide ? -(wMeters * 0.15) : (wMeters * 0.15);
        sensorRect.translate(offsetX, crossbarThickness / 2.0);

        this.goalLineSensor = new BodyFixture(sensorRect);
        this.goalLineSensor.setSensor(true); // Detecta colisión sin detener el balón
        this.body.addFixture(this.goalLineSensor);

        this.body.setMass(MassType.INFINITE); // Estático, inamovible
        attachToWorld(world, centerX, centerY);
    }

    public boolean isGoalSensor(BodyFixture fixture) {
        return this.goalLineSensor != null && this.goalLineSensor.equals(fixture);
    }

    public boolean isLeftSide() {
        return leftSide;
    }

    // lo de colidable

    @Override
    public void collideWithBall(Ball ball, BodyFixture myFixture, Match match) {
        if (isGoalSensor(myFixture)) {
            if (ball.isFake()) {
                match.removeFakeBall(ball);
            } else {
                match.onGoalScored(!this.leftSide);
            }
        }
    }


    @Override
    public void update(double dt) {
        // Las porterías son estáticas en dyn4j
    }

}
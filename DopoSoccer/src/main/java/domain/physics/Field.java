package domain.physics;

import domain.match.Match;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;

public class Field implements Collidable {

    public static final double PIXELS_PER_METER = GameObject.PIXELS_PER_METER;

    private final double width;
    private final double height;
    private final double groundLevelY;

    private final World<Body> world;
    private final Body boundariesBody;
    private BodyFixture groundFixture;

    private final Goal leftGoal;
    private final Goal rightGoal;

    public Field(double width, double height) {
        this.width = width;
        this.height = height;
        // El césped queda al 85% de la altura total de la ventana
        this.groundLevelY = height * 0.85;

        // Inicialización del mundo físico dyn4j (gravedad positiva hacia abajo en eje Y de pantalla)
        this.world = new World<>();
        this.world.setGravity(new Vector2(0.0, 14.5));

        this.boundariesBody = new Body();
        this.boundariesBody.setUserData(this);
        buildBoundaries();

        // Dimensiones proporcionales de las porterías
        double goalWidth = width * 0.085;
        double goalHeight = height * 0.34;
        double goalCenterY = this.groundLevelY - (goalHeight / 2.0);

        this.leftGoal = new Goal(true, goalWidth, goalHeight);
        this.leftGoal.initPhysics(this.world, goalWidth / 2.0, goalCenterY, goalWidth, goalHeight);

        this.rightGoal = new Goal(false, goalWidth, goalHeight);
        this.rightGoal.initPhysics(this.world, width - (goalWidth / 2.0), goalCenterY, goalWidth, goalHeight);
    }

    private void buildBoundaries() {
        double wMeters = this.width / PIXELS_PER_METER;
        double hMeters = this.height / PIXELS_PER_METER;
        double groundMeters = this.groundLevelY / PIXELS_PER_METER;
        double wallThickness = 1.0; // 1 metro de grosor para evitar tunneling a altas velocidades

        // Suelo
        org.dyn4j.geometry.Rectangle groundRect = Geometry.createRectangle(wMeters + 2.0, wallThickness);
        groundRect.translate(wMeters / 2.0, groundMeters + (wallThickness / 2.0));
        this.groundFixture = new BodyFixture(groundRect);
        this.groundFixture.setFriction(0.35);
        this.groundFixture.setRestitution(0.75);
        this.boundariesBody.addFixture(this.groundFixture);

        // Techo
        org.dyn4j.geometry.Rectangle ceilingRect = Geometry.createRectangle(wMeters + 2.0, wallThickness);
        ceilingRect.translate(wMeters / 2.0, -(wallThickness / 2.0));
        BodyFixture ceilingFixture = new BodyFixture(ceilingRect);
        ceilingFixture.setRestitution(0.60);
        this.boundariesBody.addFixture(ceilingFixture);

        //  Pared izquierda
        org.dyn4j.geometry.Rectangle leftWall = Geometry.createRectangle(wallThickness, hMeters + 2.0);
        leftWall.translate(-(wallThickness / 2.0), hMeters / 2.0);
        BodyFixture leftFixture = new BodyFixture(leftWall);
        leftFixture.setRestitution(0.65);
        this.boundariesBody.addFixture(leftFixture);

        // Pared derecha
        org.dyn4j.geometry.Rectangle rightWall = Geometry.createRectangle(wallThickness, hMeters + 2.0);
        rightWall.translate(wMeters + (wallThickness / 2.0), hMeters / 2.0);
        BodyFixture rightFixture = new BodyFixture(rightWall);
        rightFixture.setRestitution(0.65);
        this.boundariesBody.addFixture(rightFixture);

        this.boundariesBody.setMass(MassType.INFINITE);
        this.world.addBody(this.boundariesBody);
    }

    /**
     * Avanza un paso la simulación física de dyn4j.
     */
    public void stepPhysics(double dt) {
        this.world.update(dt);
    }

    public boolean isGroundFixture(BodyFixture fixture) {
        return this.groundFixture != null && this.groundFixture.equals(fixture);
    }

    //lo de collidable

    @Override
    public void collideWithBall(Ball ball, BodyFixture myFixture, Match match) {
        if (isGroundFixture(myFixture)) {
            if (ball.isFake()) {
                match.removeFakeBall(ball);
            } else if (ball.hasActivePower()) {
                ball.clearPowerShot();
            }
        }
    }

    // los getters

    public World<Body> getWorld() { return world; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getGroundLevelY() { return groundLevelY; }
    public Goal getLeftGoal() { return leftGoal; }
    public Goal getRightGoal() { return rightGoal; }


}
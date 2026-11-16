package domain.physics;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;

public abstract class GameObject implements Update, Collidable {

    public static final double PIXELS_PER_METER = 100.0;

    protected final Body body; //Representacion del cuerpo logico de nuestro GameObject
    protected World<Body> world; //entorno o espacio donde van a estar nuestros GameObjects
    protected double logicalWidth;
    protected double logicalHeight;

    public GameObject(double logicalWidth, double logicalHeight) {
        this.logicalWidth = logicalWidth;
        this.logicalHeight = logicalHeight;
        this.body = new Body();
        this.body.setUserData(this); // Permite identificar qué GameObject chocó en los ContactListeners de dyn4j
    }

    /**
     * Registra el cuerpo físico en el World de dyn4j y lo ubica en la posición inicial (coordenadas lógicas).
     */
    public void attachToWorld(World<Body> world, double logicalX, double logicalY) {
        this.world = world;
        resetPosition(logicalX, logicalY);
        if (this.world != null && !this.world.containsBody(this.body)) {
            this.world.addBody(this.body);
        }
    }

    public void resetPosition(double logicalX, double logicalY) {
        this.body.setLinearVelocity(new Vector2(0.0, 0.0));
        this.body.setAngularVelocity(0.0);
        this.body.getTransform().setTranslation(
                logicalX / PIXELS_PER_METER,
                logicalY / PIXELS_PER_METER
        );
    }

    // Getters de posición en unidades lógicas (listos para la vista)
    public double getX() {
        return this.body.getWorldCenter().x * PIXELS_PER_METER;
    }

    public double getY() {
        return this.body.getWorldCenter().y * PIXELS_PER_METER;
    }

    public double getLogicalWidth() { return logicalWidth; }
    public double getLogicalHeight() { return logicalHeight; }
    public Body getBody() { return body; }

    @Override
    public abstract void update(double dt);

    //esto es del collidable pero esto es abstracto entonces las subclases tendran un cuerpo


}
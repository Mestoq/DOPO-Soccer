package domain;

/*
*obedece a physics y es cualquier objeto con movimientos, gravedad y/o input, osea, charecter y ball
 */
public abstract class GameObject {
    private double x, y, vx, vy, width, height, restitution;
    private boolean gravityEnabled = true;
    private boolean onGround;

    public GameObject(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void update(double dt) {
        x += vx * dt;
        y += vy * dt;
    }

    public boolean intersects(GameObject o) {
        return x < o.x + o.width && x + width > o.x && y < o.y + o.height && y + height > o.y;
    }

    public void resetTo(double x, double y) {
        this.x = x;
        this.y = y;
        this.vx = 0;
        this.vy = 0;
    }

    //Con lo que chequea en que parte esta en contacto el GameObject
    public void onBoundary(String surface) { }
    //getters y setters para acutalizar las posiciones y estados de los objetos
    public double getCenterX() { return x + width / 2; }
    public double getCenterY() { return y + height / 2; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getRestitution() { return restitution; }
    public boolean isGravityEnabled() { return gravityEnabled; }
    public boolean isOnGround() { return onGround; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setVx(double vx) { this.vx = vx; }
    public void setVy(double vy) { this.vy = vy; }
    public void setRestitution(double r) { this.restitution = r; }
    public void setGravityEnabled(boolean g) { this.gravityEnabled = g; }
    public void setOnGround(boolean g) { this.onGround = g; }
}

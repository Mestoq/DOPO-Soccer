package domain;

public class Ball extends GameObject {
    public static final double SIZE = 44;
    public static final double RESTITUTION = 0.8;

    private boolean real;
    private Character shooter;
    private String lastContact;

    public Ball(double x, double y, boolean real) {
        super(x, y, SIZE, SIZE);
        this.real = real;
        setRestitution(RESTITUTION);
    }

    public boolean isReal() { return real; }
    public boolean isPowerShot() { return shooter != null; }
    public Character getShooter() { return shooter; }
    public String getLastContact() { return lastContact; }
    public void clearContact() { lastContact = null; }

    public void launchAsPowerShot(Character shooter) {
        this.shooter = shooter;
        setGravityEnabled(false);
    }

    public void clearPowerShot() {
        shooter = null;
        setGravityEnabled(true);
        setRestitution(RESTITUTION);
    }

    @Override
    public void onBoundary(String surface) { lastContact = surface; }
}

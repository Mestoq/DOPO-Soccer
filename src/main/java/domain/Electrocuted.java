package domain;

public class Electrocuted extends Effect {
    public Electrocuted(double duration) { super(duration); }

    @Override public boolean canJump() { return false; }
    @Override public double getSpeedFactor() { return 0.5; }
}

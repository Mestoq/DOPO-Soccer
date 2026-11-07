package domain;

public abstract class Effect {
    protected double duration;
    private double remaining;

    protected Effect(double duration) { this.duration = duration; }

    public void begin(Character target, Match match) { remaining = duration; }
    public void update(double dt) { remaining -= dt; }
    public boolean isExpired() { return remaining <= 0; }
    public void end(Character target, Match match) { }
    public boolean canMove() { return true; }
    public boolean canJump() { return true; }
    public boolean canKick() { return true; }
    public double getSpeedFactor() { return 1.0; }
    public double getRemaining() { return remaining; }
}

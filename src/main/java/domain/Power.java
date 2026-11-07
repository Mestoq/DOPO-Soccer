package domain;

public abstract class Power {
    protected String name;
    protected double baseDamage;
    private Effect effect;

    protected Power(String name, double baseDamage, Effect effect) {
        this.name = name;
        this.baseDamage = baseDamage;
        this.effect = effect;
    }

    /** Turns the real ball into this power's shot. */
    public abstract void launch(Character shooter, Ball ball, Match match);

    /** Damage = baseDamage * kickMultiplier, then the effect replaces the rival's current one. */
    public void onHit(Character shooter, Character rival, Match match) {
        rival.receiveDamage(baseDamage * shooter.getKickMultiplier());
        match.applyEffect(rival, effect);
    }

    /** By default any contact ends the shot. Subclasses may ignore some surfaces. */
    public boolean endsOnContact(String surface) { return true; }

    /** Places the ball in front of the shooter and sends it straight, without gravity. */
    protected void fire(Character shooter, Ball ball, double speed) {
        int dir = shooter.getFacing();
        double x = dir > 0 ? shooter.getX() + shooter.getWidth() + 2 : shooter.getX() - ball.getWidth() - 2;
        double y = shooter.getY() + shooter.getHeight() * 0.4;
        ball.resetTo(x, y);
        ball.setVx(dir * speed);
        ball.launchAsPowerShot(shooter);
    }

    public String getName() { return name; }
    public double getBaseDamage() { return baseDamage; }
    public Effect getEffect() { return effect; }
}

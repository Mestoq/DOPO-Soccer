package domain;
/*
* Powers empleados por los charecters cpn sus efectos anclados teniendo sus metodos respecto a lo que hacen con los
* GameObjects
 */
public abstract class Power {
    protected String name;
    protected double baseDamage;
    private Effect effect;

    protected Power(String name, double baseDamage, Effect effect) {
        this.name = name;
        this.baseDamage = baseDamage;
        this.effect = effect;
    }

    //transforma la ball en el tiro especial
    public abstract void launch(Character shooter, Ball ball, Match match);

    //Calcula daño y despues aplicara el efecto correspondiente al otro jugador
    public void onHit(Character shooter, Character rival, Match match) {
        rival.receiveDamage(baseDamage * shooter.getKickMultiplier());
        match.applyEffect(rival, effect);
    }

    //Determina si el power termina al hacer contacto
    public boolean endsOnContact(String surface) { return true; }

    //Coloca el balon frente al jugador y deshabilita la gravedad durante
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

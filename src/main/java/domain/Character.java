package domain;

/**
 * El personaje escogido en base al ID escogido desde la GUI y el catalog
 * match iterara con este toda la partida y es lo que el player utiliza, por ende este charecter tendra el id al que corresponde ademas de otros atributos
 * */
public abstract class Character extends GameObject {
    public static final double WIDTH = 90, HEIGHT = 120;
    public static final double BASE_SPEED = 400, BASE_JUMP = 750;
    public static final double BASE_KICK_DAMAGE = 10, KICK_DURATION = 0.25, KICK_REACH = 50;
    public static final double POWER_CHARGE_SECONDS = 10, STUN_SECONDS = 3;

    private int ownerSlot;
    private String countryName;
    private double maxLife, life, powerBar;
    private double kickMultiplier, speedMultiplier, jumpMultiplier;
    private boolean powerActive;
    private double stunnedTime, kickTime;
    private boolean kickHandled;
    private int facing;
    private Power power;
    private Effect effect;

    protected Character(int ownerSlot, String countryName, double maxLife, double kickMultiplier,
                        double speedMultiplier, double jumpMultiplier, Power power) {
        super(0, 0, WIDTH, HEIGHT);
        this.ownerSlot = ownerSlot;
        this.countryName = countryName;
        this.maxLife = maxLife;
        this.life = maxLife;
        this.kickMultiplier = kickMultiplier;
        this.speedMultiplier = speedMultiplier;
        this.jumpMultiplier = jumpMultiplier;
        this.power = power;
        this.facing = ownerSlot == 0 ? 1 : -1;
        setRestitution(0);
    }
//Aqui estan todas las mecanicas logicas que pueden hacer en el field

    public void move(int direction) {
        if (!canMove()) return;
        setVx(direction * BASE_SPEED * speedMultiplier * getSpeedFactor());
        if (direction != 0) facing = direction;
    }

    public void jump() {
        if (!canJump() || !isOnGround()) return;
        setVy(-BASE_JUMP * Math.sqrt(jumpMultiplier));
        setOnGround(false);
    }

    public void kick() {
        if (!canKick() || kickTime > 0) return;
        kickTime = KICK_DURATION;
        kickHandled = false;
    }

    public void activatePower() {
        if (isStunned() || powerActive || powerBar < 1.0) return;
        powerActive = true;
        powerBar = 0;
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        powerBar = Math.min(1.0, powerBar + dt / POWER_CHARGE_SECONDS);
        if (kickTime > 0) kickTime = Math.max(0, kickTime - dt);
        if (stunnedTime > 0) {
            stunnedTime -= dt;
            if (stunnedTime <= 0) {
                stunnedTime = 0;
                life = maxLife;
            }
        }
        if (isOnGround() && !canMove()) setVx(getVx() * Math.max(0, 1 - 8 * dt));
    }

    public void receiveDamage(double amount) {
        if (isStunned()) return;
        life = Math.max(0, life - amount);
        if (life == 0) stunnedTime = STUN_SECONDS;
    }

    public void receiveGoal() { powerBar = Math.min(1.0, powerBar + 0.25); }
    public void consumePower() { powerActive = false; }
//Todos estos booleans van conrespecto a los powers y los efectos sobre los jugadores
    public boolean isStunned() { return stunnedTime > 0; }
    public boolean canMove() { return !isStunned() && (effect == null || effect.canMove()); }
    public boolean canJump() { return !isStunned() && (effect == null || effect.canJump()); }
    public boolean canKick() { return !isStunned() && (effect == null || effect.canKick()); }
    public double getSpeedFactor() { return effect == null ? 1.0 : effect.getSpeedFactor(); }
    public double getKickDamage() { return BASE_KICK_DAMAGE * kickMultiplier; }

    //Hitbox del pie para las pateadas, con un delay para evitar hayan bugs visuales y logicas
    public boolean isInKickRange(GameObject t) {
        if (kickTime <= 0) return false;
        double zx = facing > 0 ? getX() + getWidth() : getX() - KICK_REACH;
        double zy = getY() + getHeight() * 0.4;
        double zh = getHeight() * 0.6;
        return zx < t.getX() + t.getWidth() && zx + KICK_REACH > t.getX()
            && zy < t.getY() + t.getHeight() && zy + zh > t.getY();
    }
    public boolean isKickActive() { return kickTime > 0 && !kickHandled; }
    public void markKickHandled() { kickHandled = true; }

    public void setEffect(Effect e) { effect = e; }
    public Effect getEffect() { return effect; }
    public void clearEffect() { effect = null; }
//getters unicamente
    public int getOwnerSlot() { return ownerSlot; }
    public String getCountryName() { return countryName; }
    public double getLife() { return life; }
    public double getMaxLife() { return maxLife; }
    public double getPowerBar() { return powerBar; }
    public boolean isPowerActive() { return powerActive; }
    public double getStunnedTime() { return stunnedTime; }
    public double getKickTime() { return kickTime; }
    public double getKickMultiplier() { return kickMultiplier; }
    public int getFacing() { return facing; }
    public Power getPower() { return power; }
}

package domain.power.effect;

import domain.character.Character_;

public abstract class Effect {

    protected final String name;
    protected double remainingDuration;

    public static final String PUSH = "Empuje";
    public static final String ELECTROCUTED = "Electrocutado";
    public static final String UNCONSCIOUS = "Inconsciente";
    public static final String CONFUSION = "Confusión";

    protected Effect(String name, double durationSeconds) {
        this.name = name;
        this.remainingDuration = durationSeconds;
    }

    public void update(double dt, Character_ affectedCharacter) {
        this.remainingDuration -= dt;
        if (this.remainingDuration > 0.0) {
            onTick(dt, affectedCharacter);
        } else {
            onExpire(affectedCharacter);
        }
    }

    public boolean isExpired() {
        return this.remainingDuration <= 0.0;
    }

    public String getName() { return name; }
    public double getRemainingDuration() { return remainingDuration; }

    public abstract void onApply(Character_ target);
    public abstract void onTick(double dt, Character_ target);
    public abstract void onExpire(Character_ target);
}
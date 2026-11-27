package domain.power.effect;

import domain.character.Character_;

public class ConfusionEffect extends Effect {
    public static final double DURATION = 2.5;

    public ConfusionEffect() {
        super(CONFUSION, DURATION);
    }

    @Override
    public void onApply(Character_ target) {
        //no se aun que hacer en esto :p
    }





    @Override
    public void onTick(double dt, Character_ target) {
    }

    @Override
    public void onExpire(Character_ target) {
    }
}
package domain.power.effect;

import domain.character.Character_;

public class ElectrocutedEffect extends Effect {

    public ElectrocutedEffect() {
        super(ELECTROCUTED, 3.0);
    }

    @Override
    public void onApply(Character_ target) {
        target.setCanJump(false);
        target.setSpeedMultiplier(0.5);
    }

    @Override
    public void onTick(double dt, Character_ target) {
        target.setCanJump(false);
        target.setSpeedMultiplier(0.5);
    }

    @Override
    public void onExpire(Character_ target) {
        target.setCanJump(true);
        target.setSpeedMultiplier(1.0);
    }
}
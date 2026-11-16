package domain.power.effect;

import domain.character.Character_;

public class ConfusionEffect extends Effect {

    public ConfusionEffect() {
        super("Confusión", 2.5);
    }

    @Override
    public void onApply(Character_ target) {
        // Invierte brevemente la percepción o mantiene el estado visual de confusión en HUD
    }

    @Override
    public void onTick(double dt, Character_ target) {
    }

    @Override
    public void onExpire(Character_ target) {
    }
}
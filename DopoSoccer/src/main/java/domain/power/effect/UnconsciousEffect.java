package domain.power.effect;

import domain.character.Character_;
import org.dyn4j.geometry.Vector2;

public class UnconsciousEffect extends Effect {

    public UnconsciousEffect() {
        super("Inconsciente", 3.0);
    }

    @Override
    public void onApply(Character_ target) {
        target.setIncapacitated(true);
        target.setCanJump(false);
        // Lanza al rival
        target.getBody().setLinearVelocity(new Vector2(0.0, -8.5));
    }

    @Override
    public void onTick(double dt, Character_ target) {
        target.stopHorizontal();
    }

    @Override
    public void onExpire(Character_ target) {
        target.setIncapacitated(false);
        target.setCanJump(true);
    }
}
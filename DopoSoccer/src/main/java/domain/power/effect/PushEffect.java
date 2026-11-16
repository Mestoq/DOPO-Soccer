package domain.power.effect;

import domain.character.Character_;
import org.dyn4j.geometry.Vector2;

public class PushEffect extends Effect {

    private final boolean pushRight;

    public PushEffect(boolean pushRight) {
        super("Empuje", 1.5);
        this.pushRight = pushRight;
    }

    @Override
    public void onApply(Character_ target) {
        target.setIncapacitated(true);
        double pushVelocity = pushRight ? 11.0 : -11.0;
        target.getBody().setLinearVelocity(new Vector2(pushVelocity, -2.5));
    }

    @Override
    public void onTick(double dt, Character_ target) {
        // Mantiene el impulso de retroceso durante la ventana del efecto
    }

    @Override
    public void onExpire(Character_ target) {
        target.setIncapacitated(false);
        target.stopHorizontal();
    }
}
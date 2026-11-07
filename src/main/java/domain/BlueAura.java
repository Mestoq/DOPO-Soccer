package domain;

public class BlueAura extends Power {
    public BlueAura() { super("Blue Aura", 40, new Push(1.5)); }

    @Override
    public void launch(Character shooter, Ball ball, Match match) { fire(shooter, ball, 1800); }
}

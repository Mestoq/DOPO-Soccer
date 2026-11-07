package domain;

public class Unconscious extends Effect {
    public Unconscious(double duration) { super(duration); }

    @Override
    public void begin(Character target, Match match) {
        super.begin(target, match);
        target.setVx(0);
        target.setVy(-900);
        target.setOnGround(false);
    }

    @Override public boolean canMove() { return false; }
    @Override public boolean canJump() { return false; }
    @Override public boolean canKick() { return false; }
}

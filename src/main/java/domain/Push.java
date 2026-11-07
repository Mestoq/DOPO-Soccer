package domain;

/** The rival is thrown towards their own goal. */
public class Push extends Effect {
    private static final double PUSH_SPEED = 1600;

    public Push(double duration) { super(duration); }

    @Override
    public void begin(Character target, Match match) {
        super.begin(target, match);
        int towardsOwnGoal = target.getOwnerSlot() == 0 ? -1 : 1;
        target.setVx(towardsOwnGoal * PUSH_SPEED);
    }

    @Override public boolean canMove() { return false; }
}

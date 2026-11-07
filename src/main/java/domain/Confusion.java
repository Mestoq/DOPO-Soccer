package domain;

public class Confusion extends Effect {
    private static final int FAKE_BALLS = 7;

    public Confusion(double duration) { super(duration); }

    @Override
    public void begin(Character target, Match match) {
        super.begin(target, match);
        match.spawnFakeBalls(FAKE_BALLS);
    }

    @Override
    public void end(Character target, Match match) { match.removeFakeBalls(); }
}

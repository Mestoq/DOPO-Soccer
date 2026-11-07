package domain;

public class Lightning extends Power {
    public Lightning() { super("Lightning", 60, new Electrocuted(3)); }

    @Override
    public void launch(Character shooter, Ball ball, Match match) { fire(shooter, ball, 1500); }
}

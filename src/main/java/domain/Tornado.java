package domain;

/** Ground-level whirlwind that keeps bouncing; the floor does not end it. */
public class Tornado extends Power {
    public Tornado() { super("Tornado", 40, new Unconscious(3)); }

    @Override
    public void launch(Character shooter, Ball ball, Match match) {
        fire(shooter, ball, 1000);
        ball.setY(match.getField().getGroundY() - ball.getHeight());
        ball.setGravityEnabled(true);
        ball.setRestitution(1.0);
        ball.setVy(-400);
    }

    @Override
    public boolean endsOnContact(String surface) { return !"FLOOR".equals(surface); }
}

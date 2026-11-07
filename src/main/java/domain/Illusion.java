package domain;

public class Illusion extends Power {
    public Illusion() { super("Illusion", 30, new Confusion(2.5)); }

    @Override
    public void launch(Character shooter, Ball ball, Match match) { fire(shooter, ball, 1500); }
}

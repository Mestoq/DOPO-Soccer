package domain.physics;

import domain.match.Match;
import org.dyn4j.dynamics.BodyFixture;

public interface Collidable {

    default void onCollisionBegin(Collidable other, BodyFixture myFixture, BodyFixture otherFixture, Match match) {}
    default void onCollisionPostSolve(Collidable other, Match match) {}

    default void collideWithBall(Ball ball, BodyFixture myFixture, Match match) {}
    default void postSolveWithBall(Ball ball, Match match) {}
}
package domain.character;

import domain.power.Power;
import domain.match.Match;
import domain.physics.Ball;

public class NigeriaCharacter extends Character_ {

    public NigeriaCharacter() {
        super(NIGERIA, Power.create("TORNADO"));
    }

    @Override
    public void executeSpecialShot(Ball ball, Match match) {
        if (this.specialPower != null) {
            this.specialPower.launchShot(ball, this, match);
        }
    }
}
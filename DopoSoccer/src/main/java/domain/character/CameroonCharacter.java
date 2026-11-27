package domain.character;

import domain.power.Power;
import domain.match.Match;
import domain.physics.Ball;

public class CameroonCharacter extends Character_ {

    public CameroonCharacter() {
        super(CAMEROON, Power.create("RAYO"));
    }

    @Override
    public void executeSpecialShot(Ball ball, Match match) {
        if (this.specialPower != null) {
            this.specialPower.launchShot(ball, this, match);
        }
    }
}
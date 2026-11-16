package domain.character;

import domain.DopoSoccerException;
import domain.power.LightningPower;
import domain.power.Power;
import domain.match.Match;
import domain.physics.Ball;

public class CameroonCharacter extends Character_ {

    public CameroonCharacter() {
        super("CAMERUN", createPowerSafe("RAYO"));
    }

    private static Power createPowerSafe(String name) {
        try {
            return Power.create(name);
        } catch (DopoSoccerException e) {
            return new LightningPower();
        }
    }

    @Override
    public void executeSpecialShot(Ball ball, Match match) {
        if (this.specialPower != null) {
            this.specialPower.launchShot(ball, this, match);
        }
    }
}
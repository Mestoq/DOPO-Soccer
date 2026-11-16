package domain.character;

import domain.DopoSoccerException;
import domain.power.Power;
import domain.power.TornadoPower;
import domain.match.Match;
import domain.physics.Ball;

public class NigeriaCharacter extends Character_ {

    public NigeriaCharacter() {
        super("NIGERIA", createPowerSafe("TORNADO"));
    }

    private static Power createPowerSafe(String name) {
        try {
            return Power.create(name);
        } catch (DopoSoccerException e) {
            return new TornadoPower();
        }
    }

    @Override
    public void executeSpecialShot(Ball ball, Match match) {
        if (this.specialPower != null) {
            this.specialPower.launchShot(ball, this, match);
        }
    }
}
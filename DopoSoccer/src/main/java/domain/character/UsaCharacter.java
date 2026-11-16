package domain.character;

import domain.DopoSoccerException;
import domain.power.effect.IllusionPower;
import domain.power.Power;
import domain.match.Match;
import domain.physics.Ball;

public class UsaCharacter extends Character_ {

    public UsaCharacter() {
        super("ESTADOS UNIDOS", createPowerSafe("ILUSION"));
    }

    private static Power createPowerSafe(String name) {
        try {
            return Power.create(name);
        } catch (DopoSoccerException e) {
            return new IllusionPower();
        }
    }

    @Override
    public void executeSpecialShot(Ball ball, Match match) {
        if (this.specialPower != null) {
            this.specialPower.launchShot(ball, this, match);
        }
    }
}
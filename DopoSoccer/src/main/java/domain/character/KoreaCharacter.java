package domain.character;

import domain.power.BlueAuraPower;
import domain.DopoSoccerException;
import domain.power.Power;
import domain.match.Match;
import domain.physics.Ball;

public class KoreaCharacter extends Character_ {

    public KoreaCharacter() {
        super("COREA DEL SUR", createPowerSafe("AURA AZUL"));
    }

    private static Power createPowerSafe(String name) {
        try {
            return Power.create(name);
        } catch (DopoSoccerException e) {
            return new BlueAuraPower();
        }
    }

    @Override
    public void executeSpecialShot(Ball ball, Match match) {
        if (this.specialPower != null) {
            this.specialPower.launchShot(ball, this, match);
        }
    }
}
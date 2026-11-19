package domain.character;

import domain.power.Power;
import domain.match.Match;
import domain.physics.Ball;

public class KoreaCharacter extends Character_ {

    public KoreaCharacter() {
        super("COREA DEL SUR", Power.create("AURA AZUL"));
    }

    @Override
    public void executeSpecialShot(Ball ball, Match match) {
        if (this.specialPower != null) {
            this.specialPower.launchShot(ball, this, match);
        }
    }
}
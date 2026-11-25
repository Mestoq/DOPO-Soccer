package domain.character;


import domain.power.Power;
import domain.match.Match;
import domain.physics.Ball;

public class UsaCharacter extends Character_ {

    public UsaCharacter() {
        super("USA", Power.create("ILUSION"));
    }

    @Override
    public void executeSpecialShot(Ball ball, Match match) {
        if (this.specialPower != null) {
            this.specialPower.launchShot(ball, this, match);
        }
    }
}
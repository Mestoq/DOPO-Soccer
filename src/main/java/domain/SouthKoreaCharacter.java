package domain;

public class SouthKoreaCharacter extends Character {
    public SouthKoreaCharacter(Player owner) {
        super(owner.getSlot(), "South Korea", owner.getMaxLife(), owner.getKickMultiplier(),
              owner.getSpeedMultiplier(), owner.getJumpMultiplier(), new BlueAura());
    }
}

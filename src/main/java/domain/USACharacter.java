package domain;

public class USACharacter extends Character {
    public USACharacter(Player owner) {
        super(owner.getSlot(), "USA", owner.getMaxLife(), owner.getKickMultiplier(),
              owner.getSpeedMultiplier(), owner.getJumpMultiplier(), new Illusion());
    }
}

package domain;

public class CameroonCharacter extends Character {
    public CameroonCharacter(Player owner) {
        super(owner.getSlot(), "Cameroon", owner.getMaxLife(), owner.getKickMultiplier(),
              owner.getSpeedMultiplier(), owner.getJumpMultiplier(), new Lightning());
    }
}

package domain;

public class NigeriaCharacter extends Character {
    public NigeriaCharacter(Player owner) {
        super(owner.getSlot(), "Nigeria", owner.getMaxLife(), owner.getKickMultiplier(),
              owner.getSpeedMultiplier(), owner.getJumpMultiplier(), new Tornado());
    }
}

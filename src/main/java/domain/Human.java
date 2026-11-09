package domain;

import java.util.HashSet;
import java.util.Set;

public class Human extends Player {
    private int points;
    private Set<String> unlockedCharacters = new HashSet<>();
    private boolean leftPressed, rightPressed, jumpPressed, kickPressed, powerPressed;

    public Human(int slot) { super(slot, 1, 1, 1, 1); }

    /** Called by the GUI with the current keyboard state. */
    @Override
    public void setInput(boolean left, boolean right, boolean jump, boolean kick, boolean power) {
        leftPressed = left;
        rightPressed = right;
        jumpPressed = jump;
        kickPressed = kick;
        powerPressed = power;
    }

    @Override
    public boolean canChoose(String id) { return unlockedCharacters.contains(id); }

    @Override
    public void act(Match match) {
        Character c = getCharacter();
        if (c == null) return;
        c.move((rightPressed ? 1 : 0) - (leftPressed ? 1 : 0));
        if (jumpPressed) c.jump();
        if (kickPressed) c.kick();
        if (powerPressed) c.activatePower();
    }

    @Override public void addPoints(int n) { points += n; }
    public void unlock(String id) { unlockedCharacters.add(id); }
    public int getPoints() { return points; }

    public void buyUpgrade(String stat) throws DopoSoccerException {
        throw new DopoSoccerException(DopoSoccerException.NOT_IMPLEMENTED + ": upgrades (version 2)");
    }
}

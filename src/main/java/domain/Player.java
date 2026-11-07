package domain;

public abstract class Player {
    private int slot;                 // 0 = Player1, 1 = Player2
    private String characterChosen;
    private int speed, jump, kick, survival;   // 1..10
    private Character character;

    protected Player(int slot, int speed, int jump, int kick, int survival) {
        this.slot = slot;
        this.speed = speed;
        this.jump = jump;
        this.kick = kick;
        this.survival = survival;
    }

    public void chooseCharacter(String id) throws DopoSoccerException {
        if (!canChoose(id)) throw new DopoSoccerException(DopoSoccerException.CHARACTER_LOCKED + ": " + id);
        characterChosen = id;
    }

    public abstract boolean canChoose(String id);
    public abstract void act(Match match);

    public String getName() { return characterChosen != null ? characterChosen : "Player" + (slot + 1); }
    public void addPoints(int n) { }

    public double getKickMultiplier() { return 1 + 0.10 * (kick - 1); }
    public double getMaxLife() { return 100 * (1 + 0.10 * (survival - 1)); }
    public double getSpeedMultiplier() { return 1 + 0.10 * (speed - 1); }
    public double getJumpMultiplier() { return 1 + 0.10 * (jump - 1); }

    public int getSlot() { return slot; }
    public String getCharacterChosen() { return characterChosen; }
    public Character getCharacter() { return character; }
    public void setCharacter(Character c) { this.character = c; }
}

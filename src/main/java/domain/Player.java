package domain;
/*
*La clase Abstract Player se encarga de definir lo que seran los dos tipos de jugadores para un match
* su funcion principal es la de configurar el tipo de jugador (CPU/HUMAN) ademas de los inputs que envien o calculen segun el caso
* @param recibe que slot se le ubico y ademas las stats iniciales del jugador(Esta es la predeterminada por nosotros
* al aun no terminar de implementar la persistencia)
 */
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
        if (!canChoose(id)) {
            throw new DopoSoccerException(DopoSoccerException.CHARACTER_LOCKED + ": " + id);
        }
        characterChosen = id;
    }

    public abstract boolean canChoose(String id); //Puede el player escoger el jugador para jugar?? (polimorfismo)
    public abstract void act(Match match); //Como se usa el charecter (polimorfismo)

    public String getName() { return characterChosen != null ? characterChosen : "Player" + (slot + 1); }
    public void addPoints(int n) { }

    //estas son las stats
    public double getKickMultiplier() { return 1 + 0.10 * (kick - 1); }
    public double getMaxLife() { return 100 * (1 + 0.10 * (survival - 1)); }
    public double getSpeedMultiplier() { return 1 + 0.10 * (speed - 1); }
    public double getJumpMultiplier() { return 1 + 0.10 * (jump - 1); }

    public int getSlot() { return slot; }
    public String getCharacterChosen() { return characterChosen; }
    public Character getCharacter() { return character; }
    public void setCharacter(Character c) { this.character = c; }
}

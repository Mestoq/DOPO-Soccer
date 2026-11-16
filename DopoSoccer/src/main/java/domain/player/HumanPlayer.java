package domain.player;

import domain.match.Match;

public class HumanPlayer extends Player {

    private boolean movingLeft;
    private boolean movingRight;
    private boolean jumpRequested;
    private boolean kickRequested;
    private boolean powerRequested;

    public HumanPlayer(String name) {
        super(name, true);
        clearInputs();
    }

    public void clearInputs() {
        this.movingLeft = false;
        this.movingRight = false;
        this.jumpRequested = false;
        this.kickRequested = false;
        this.powerRequested = false;
    }

    @Override
    public void resetMatchState() {
        super.resetMatchState();
        clearInputs();
    }

    @Override
    public void decideAction(double dt, Match match) {
        if (this.character == null) {
            return;
        }

        // Movimiento horizontal según las teclas mantenidas
        if (movingLeft && !movingRight) {
            this.character.moveLeft(this.speed);
        } else if (movingRight && !movingLeft) {
            this.character.moveRight(this.speed);
        } else {
            this.character.stopHorizontal();
        }

        // Acciones instantáneas (se consumen al ejecutarse)
        if (jumpRequested) {
            this.character.jump(this.jump);
            this.jumpRequested = false;
        }

        if (kickRequested) {
            this.character.kick(this.getNormalKickDamage());
            this.kickRequested = false;
        }

        if (powerRequested) {
            activatePower();
            this.powerRequested = false;
        }
    }

    // Setters de intención invocados desde el controlador de teclado en la GUI
    public void setMovingLeft(boolean movingLeft) { this.movingLeft = movingLeft; }
    public void setMovingRight(boolean movingRight) { this.movingRight = movingRight; }
    public void requestJump() { this.jumpRequested = true; }
    public void requestKick() { this.kickRequested = true; }
    public void requestPower() { this.powerRequested = true; }
}
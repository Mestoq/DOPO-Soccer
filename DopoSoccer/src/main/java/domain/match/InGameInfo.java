package domain.match;

public class InGameInfo {

    private final boolean timeMode;
    private final double initialTimeSeconds;
    private final int targetGoals;

    private int player1Goals;
    private int player2Goals;

    private double remainingTime;
    private double elapsedTime;

    private boolean paused;
    private boolean finished;
    private boolean suddenDeath;

    public static final int POINTS_WINNER = 100;
    public static final int POINTS_LOSER = 25;
    public static final int POINTS_DRAW = 50;



    private InGameInfo(boolean timeMode, double durationSeconds, int targetGoals) {
        this.timeMode = timeMode;
        this.initialTimeSeconds = durationSeconds;
        this.targetGoals = targetGoals;

        this.player1Goals = 0;
        this.player2Goals = 0;
        this.remainingTime = durationSeconds;
        this.elapsedTime = 0.0;
        this.paused = false;
        this.finished = false;
    }

    // las fabricas

    public static InGameInfo createTimeMode(double durationSeconds) {
        return new InGameInfo(true, Math.max(1.0, durationSeconds), 0);
    }

    public static InGameInfo createGoalMode(int targetGoals) {
        return new InGameInfo(false, 0.0, Math.max(1, targetGoals));
    }

    // controla tiempo e info del match

    public void addGoalPlayer1() {
        if (!finished) {
            this.player1Goals++;
        }
    }

    public void addGoalPlayer2() {
        if (!finished) {
            this.player2Goals++;
        }
    }

    public void updateTimer(double dt) {
        if (paused || finished) {
            return;
        }
        this.elapsedTime += dt;
        if (this.timeMode) {
            this.remainingTime = Math.max(0.0, this.remainingTime - dt);
        }
    }

    public void elapsedTime(double dt) {
        if (!paused && !finished) {
            this.elapsedTime += dt;
        }
    }

    public boolean hasReachedTargetGoals() {
        return !this.timeMode && (this.player1Goals >= this.targetGoals || this.player2Goals >= this.targetGoals);
    }

    // estado del partido y puntos

    public void togglePause() {
        if (!finished) {
            this.paused = !this.paused;
        }
    }

    public void setPaused(boolean paused) {
        if (!finished) {
            this.paused = paused;
        }
    }

    public void finishMatch() {
        this.finished = true;
        this.paused = false;
    }

    /**
     * asigna puntos ganados por el Jugador 1 o 2 al finalizar el partido
     * (100 pts por gol anotado + 300 pts por victoria o 100 pts por empate).
     */

    public int getPointsFor(int playerNumber) {
        int winner = getWinnerNumber();
        if (winner == 0) {
            return POINTS_DRAW;
        }
        return (winner == playerNumber) ? POINTS_WINNER : POINTS_LOSER;
    }

    // Se conserva el nombre por compatibilidad con la GUI.
    public int calculatePlayer1EarnedPoints() {
        return getPointsFor(1);
    }

    public int getWinnerNumber() {
        if (player1Goals > player2Goals) return 1;
        if (player2Goals > player1Goals) return 2;
        return 0;
    }

    /**
     * Transfiere las estadísticas finales del encuentro al SaveFile.
     */
    public void recordResultToSaveFile(SaveFile saveFile) {
        if (saveFile == null || !finished) {
            return;
        }
        saveFile.recordMatchStats(this.player1Goals, this.player2Goals, this.player1Goals > this.player2Goals);
    }

    public void startSuddenDeath() { this.suddenDeath = true; }
    public boolean isSuddenDeath() { return suddenDeath; }

    //para la persistencia
    public void restoreState(int player1Goals, int player2Goals, double remainingTime, double elapsedTime) {
        this.player1Goals = Math.max(0, player1Goals);
        this.player2Goals = Math.max(0, player2Goals);
        this.remainingTime = Math.max(0.0, remainingTime);
        this.elapsedTime = Math.max(0.0, elapsedTime);
        this.finished = false;
    }

    // getters

    public boolean isTimeMode() { return timeMode; }
    public double getInitialTimeSeconds() { return initialTimeSeconds; }
    public int getTargetGoals() { return targetGoals; }
    public int getPlayer1Goals() { return player1Goals; }
    public int getPlayer2Goals() { return player2Goals; }
    public double getRemainingTime() { return remainingTime; }
    public double getElapsedTime() { return elapsedTime; }
    public boolean isPaused() { return paused; }
    public boolean isFinished() { return finished; }
}
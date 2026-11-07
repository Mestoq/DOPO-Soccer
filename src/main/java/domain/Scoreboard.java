package domain;

public class Scoreboard {
    private int[] goals = new int[2];
    private double elapsedTime;

    public void addGoal(int slot) { goals[slot]++; }
    public int getGoals(int slot) { return goals[slot]; }
    public void addTime(double dt) { elapsedTime += dt; }
    public double getElapsedTime() { return elapsedTime; }

    /** Slot with more goals, or -1 when tied. */
    public int getLeader() {
        if (goals[0] > goals[1]) return 0;
        if (goals[1] > goals[0]) return 1;
        return -1;
    }
}

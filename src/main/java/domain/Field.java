package domain;

/** Fixed logical field (1280x720). The GUI scales the drawing to the chosen resolution. */
public class Field {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    public static final int GROUND_MARGIN = 60;
    /** The only number to change to resize the goals; the width follows the sprite proportions (252 x 335). */
    public static final int GOAL_HEIGHT = 220;
    public static final int GOAL_WIDTH = (int) Math.round(GOAL_HEIGHT * 360.0 / 373.0);

    private int width;
    private int height;
    private Goal[] goals = new Goal[2];   // 0 = left (defended by slot 0), 1 = right

    public Field() {
        width = WIDTH;
        height = HEIGHT;
        double top = getGroundY() - GOAL_HEIGHT;
        goals[0] = new Goal(0, top, GOAL_WIDTH, GOAL_HEIGHT, 1);
        goals[1] = new Goal(width - GOAL_WIDTH, top, GOAL_WIDTH, GOAL_HEIGHT, -1);
    }

    public Goal getGoalOf(int slot) { return goals[slot]; }
    public double getGroundY() { return height - GROUND_MARGIN; }
    public double getCenterX() { return width / 2.0; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
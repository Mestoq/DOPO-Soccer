package domain;

/**
 * A goal. Its rectangle is exactly the rectangle where the goal sprite is drawn.
 * The mouth is the side that faces the field: right for the left goal, left for the right goal.
 */
public class Goal {
    // Crossbar proportions measured on the goal sprite (14 px thick and 9 px below the top when 240 px tall).
    private static final double CROSSBAR_THICKNESS_RATIO = 12.0 / 240;
    private static final double CROSSBAR_OFFSET_RATIO = 9.0 / 240;

    private double x, y, width, height;
    private int mouthDirection;   // +1: the mouth faces right, -1: it faces left

    public Goal(double x, double y, double width, double height, int mouthDirection) {
        int spriteMatchUp=0;
        if(mouthDirection==1){
            this.x = x-spriteMatchUp;
        }
        else{
            this.x = x+spriteMatchUp;
        }
        this.y = y;
        this.width = width;
        this.height = height;
        this.mouthDirection = mouthDirection;
    }

    /** A goal counts when the whole object is inside the goal rectangle (it crossed the front completely). */
    public boolean contains(GameObject o) {
        boolean insideX = o.getX() >= x && o.getX() + o.getWidth() <= x + width;
        boolean insideY = o.getCenterY() >= y && o.getCenterY() <= y + height;
        return insideX && insideY;
    }

    /** Top of the solid crossbar. It scales with the height of the goal. */
    public double getCrossbarY() { return y + height * CROSSBAR_OFFSET_RATIO; }

    public double getCrossbarThickness() { return height * CROSSBAR_THICKNESS_RATIO; }

    public int getMouthDirection() { return mouthDirection; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}
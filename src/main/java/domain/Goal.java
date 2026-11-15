package domain;

public class Goal {
    /** Thickness of the solid crossbar, centered on the top edge of the goal. */
    public static final double CROSSBAR_THICKNESS = 14;

    private double x, y, width, height;

    public Goal(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /** True when the center of the object is inside the goal area. */
    public boolean contains(GameObject o) {
        return o.getCenterX() >= x && o.getCenterX() <= x + width
                && o.getCenterY() >= y && o.getCenterY() <= y + height;
    }

    /** Top of the solid crossbar. */
    public double getCrossbarY() { return y - CROSSBAR_THICKNESS / 2; }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}
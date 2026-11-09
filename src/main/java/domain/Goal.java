package domain;

public class Goal {
    private double x, y, width, height;

    public Goal(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    //Revisa que el centro del objeto este dentro de este
    public boolean contains(GameObject o) {
        return o.getCenterX() >= x && o.getCenterX() <= x + width
            && o.getCenterY() >= y && o.getCenterY() <= y + height;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}

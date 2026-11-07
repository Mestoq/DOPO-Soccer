package domain;

import java.util.List;

public class Physics {
    private double gravity;

    public Physics(double gravity) { this.gravity = gravity; }

    public void step(List<GameObject> objects, Field field, double dt) {
        for (GameObject o : objects) {
            if (o.isGravityEnabled()) o.setVy(o.getVy() + gravity * dt);
            o.update(dt);
            resolveBounds(o, field);
        }
    }

    protected void resolveBounds(GameObject o, Field field) {
        o.setOnGround(false);
        double ground = field.getGroundY();
        if (o.getY() + o.getHeight() >= ground) {
            o.setY(ground - o.getHeight());
            double vy = -o.getVy() * o.getRestitution();
            if (o.getRestitution() < 1 && Math.abs(vy) < 40) vy = 0;
            o.setVy(vy);
            o.setOnGround(true);
            o.onBoundary("FLOOR");
        }
        if (o.getY() < 0) {
            o.setY(0);
            if (o.getVy() < 0) o.setVy(-o.getVy() * o.getRestitution());
            o.onBoundary("CEILING");
        }
        if (o.getX() < 0) {
            o.setX(0);
            if (o.getVx() < 0) o.setVx(-o.getVx() * o.getRestitution());
            o.onBoundary("WALL");
        } else if (o.getX() + o.getWidth() > field.getWidth()) {
            o.setX(field.getWidth() - o.getWidth());
            if (o.getVx() > 0) o.setVx(-o.getVx() * o.getRestitution());
            o.onBoundary("WALL");
        }
    }

    /** Two solid bodies block each other: separate along the axis of least overlap. */
    public void resolveBlock(GameObject a, GameObject b) {
        if (!a.intersects(b)) return;
        double ox = Math.min(a.getX() + a.getWidth(), b.getX() + b.getWidth()) - Math.max(a.getX(), b.getX());
        double oy = Math.min(a.getY() + a.getHeight(), b.getY() + b.getHeight()) - Math.max(a.getY(), b.getY());
        if (ox < oy) {
            double dir = a.getCenterX() < b.getCenterX() ? -1 : 1;
            a.setX(a.getX() + dir * ox / 2);
            b.setX(b.getX() - dir * ox / 2);
            a.setVx(0);
            b.setVx(0);
        } else {
            GameObject upper = a.getCenterY() < b.getCenterY() ? a : b;
            upper.setY(upper.getY() - oy);
            upper.setVy(0);
            upper.setOnGround(true);
        }
    }

    public void resolveBallVsCharacter(Ball ball, Character c) {
        if (!ball.intersects(c)) return;
        double ox = Math.min(ball.getX() + ball.getWidth(), c.getX() + c.getWidth()) - Math.max(ball.getX(), c.getX());
        double oy = Math.min(ball.getY() + ball.getHeight(), c.getY() + c.getHeight()) - Math.max(ball.getY(), c.getY());
        if (ox < oy) {
            int dir = ball.getCenterX() < c.getCenterX() ? -1 : 1;
            ball.setX(ball.getX() + dir * ox);
            ball.setVx(dir * Math.max(Math.abs(ball.getVx()) * 0.8, 300) + c.getVx() * 0.5);
        } else {
            int dir = ball.getCenterY() < c.getCenterY() ? -1 : 1;
            ball.setY(ball.getY() + dir * oy);
            ball.setVy(dir * Math.max(Math.abs(ball.getVy()) * 0.8, 400));
        }
    }
}

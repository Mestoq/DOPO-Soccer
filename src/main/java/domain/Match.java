package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class Match {
    public static final double GRAVITY = 2200;
    public static final double KICK_BALL_SPEED = 900, KICK_BALL_LIFT = 600;

    private boolean paused;
    private boolean finished;
    protected Player[] players = new Player[2];
    protected Character[] characters = new Character[2];
    protected List<Ball> balls = new ArrayList<>();
    protected Field field;
    protected Scoreboard scoreboard = new Scoreboard();
    protected Physics physics = new Physics(GRAVITY);

    public Match(Player p1, Player p2, Field field, Map<String, Function<Player, Character>> catalog)
            throws DopoSoccerException {
        players[0] = p1;
        players[1] = p2;
        this.field = field;
        for (int i = 0; i < 2; i++) {
            String id = players[i].getCharacterChosen();
            if (id == null || !catalog.containsKey(id))
                throw new DopoSoccerException(DopoSoccerException.UNKNOWN_CHARACTER + ": " + id);
            if (!players[i].canChoose(id))
                throw new DopoSoccerException(DopoSoccerException.CHARACTER_LOCKED + ": " + id);
            characters[i] = catalog.get(id).apply(players[i]);
            players[i].setCharacter(characters[i]);
        }
        balls.add(new Ball(0, 0, true));
        resetPositions();
    }

    /** One simulation step. The GUI only calls this and then repaints. */
    public void update(double dt) {
        if (paused || finished) return;
        for (Player p : players) p.act(this);
        for (Character c : characters) updateEffect(c, dt);
        for (Ball b : balls) b.clearContact();
        List<GameObject> objects = new ArrayList<>(balls);
        Collections.addAll(objects, characters);
        physics.step(objects, field, dt);
        if (checkGoals()) { scoreboard.addTime(dt); if (isOver()) finish(); return; }
        resolveContacts();
        scoreboard.addTime(dt);
        if (isOver()) finish();
    }

    private void updateEffect(Character c, double dt) {
        Effect e = c.getEffect();
        if (e == null) return;
        e.update(dt);
        if (e.isExpired()) {
            e.end(c, this);
            c.clearEffect();
        }
    }

    private boolean checkGoals() {
        for (Ball b : balls) {
            if (!b.isReal()) continue;
            for (int slot = 0; slot < 2; slot++) {
                if (field.getGoalOf(slot).contains(b)) {
                    onGoal(slot);
                    return true;
                }
            }
        }
        return false;
    }

    protected void resolveContacts() {
        physics.resolveBlock(characters[0], characters[1]);
        for (Ball ball : new ArrayList<>(balls)) {
            if (ball.isPowerShot()) { resolvePowerShot(ball); continue; }
            for (Character c : characters) {
                boolean kicked = c.isKickActive() && c.isInKickRange(ball);
                if (!kicked && !ball.intersects(c)) continue;
                if (!ball.isReal()) {                       // fake balls vanish when touched
                    balls.remove(ball);
                    if (kicked) c.markKickHandled();
                } else if (c.isPowerActive()) {             // touching the ball fires the power
                    c.getPower().launch(c, ball, this);
                    c.consumePower();
                } else if (kicked) {                        // a kick that touches the ball only pushes it
                    ball.setVx(c.getFacing() * KICK_BALL_SPEED);
                    ball.setVy(-KICK_BALL_LIFT);
                    c.markKickHandled();
                } else {
                    physics.resolveBallVsCharacter(ball, c);
                }
                break;
            }
        }
        for (Character c : characters) {                    // kick damage if the ball was not touched
            Character rival = characters[1 - c.getOwnerSlot()];
            if (c.isKickActive() && c.isInKickRange(rival)) {
                rival.receiveDamage(c.getKickDamage());
                c.markKickHandled();
            }
        }
    }

    private void resolvePowerShot(Ball ball) {
        Character shooter = ball.getShooter();
        Character rival = characters[1 - shooter.getOwnerSlot()];
        if (ball.intersects(rival)) {
            shooter.getPower().onHit(shooter, rival, this);
            ball.clearPowerShot();
            return;
        }
        String surface = ball.getLastContact();
        if (surface != null && shooter.getPower().endsOnContact(surface)) ball.clearPowerShot();
    }

    /** Regla 8: the new effect replaces the previous one. */
    public void applyEffect(Character target, Effect effect) {
        Effect old = target.getEffect();
        if (old != null) old.end(target, this);
        target.setEffect(effect);
        effect.begin(target, this);
    }

    public void spawnFakeBalls(int n) {
        Ball real = getRealBall();
        real.setVx((Math.random() * 2 - 1) * 600);
        real.setVy(-Math.random() * 500);
        for (int i = 0; i < n; i++) {
            Ball f = new Ball(real.getX(), real.getY(), false);
            f.setVx((Math.random() * 2 - 1) * 600);
            f.setVy(-Math.random() * 500);
            balls.add(f);
        }
    }

    public void removeFakeBalls() { balls.removeIf(b -> !b.isReal()); }

    protected void onGoal(int defenderSlot) {
        scoreboard.addGoal(1 - defenderSlot);
        characters[defenderSlot].receiveGoal();
        resetPositions();
    }

    protected void resetPositions() {
        for (Character c : characters) {
            Effect e = c.getEffect();
            if (e != null) { e.end(c, this); c.clearEffect(); }
        }
        removeFakeBalls();
        Ball real = getRealBall();
        real.clearPowerShot();
        real.resetTo(field.getCenterX() - real.getWidth() / 2, 200);
        for (int i = 0; i < 2; i++) {
            double cx = field.getWidth() * (i == 0 ? 0.25 : 0.75);
            characters[i].resetTo(cx - characters[i].getWidth() / 2, 300);
        }
    }

    protected void finish() {
        finished = true;
        for (Player p : players) p.addPoints(getPointsEarned(p));
    }

    public int getPointsEarned(Player p) {
        Player winner = isOver() ? getWinner() : null;
        if (winner == null) return 0;
        return winner == p ? 100 : 25;
    }

    public abstract boolean isOver();
    public abstract Player getWinner();

    public void pause() { paused = true; }
    public void resume() { paused = false; }
    public boolean isPaused() { return paused; }
    public boolean isFinished() { return finished; }
    public Ball getRealBall() {
        for (Ball b : balls) if (b.isReal()) return b;
        return null;
    }
    public List<Ball> getBalls() { return Collections.unmodifiableList(balls); }
    public Character getCharacterOf(int slot) { return characters[slot]; }
    public Player getPlayer(int slot) { return players[slot]; }
    public Field getField() { return field; }
    public Scoreboard getScoreboard() { return scoreboard; }
}

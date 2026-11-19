package domain.match;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import domain.DopoSoccerException;
import domain.character.Character_;
import domain.physics.Ball;
import domain.physics.Collidable;
import domain.physics.Field;
import domain.player.Player;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.dynamics.contact.SolvedContact;
import org.dyn4j.world.ContactCollisionData;
import org.dyn4j.world.listener.ContactListenerAdapter;

public abstract class Match {

    protected final Player player1;
    protected final Player player2;
    protected final Field field;
    protected final Ball mainBall;
    protected final List<Ball> activeBalls;
    protected final InGameInfo gameInfo;

    protected double goalResetTimer;
    protected boolean waitingAfterGoal;
    private ContactListenerAdapter<Body> contactListener;

    protected Match(Field field, Player player1, Player player2, InGameInfo gameInfo) {
        this.field = field;
        this.player1 = player1;
        this.player2 = player2;
        this.gameInfo = gameInfo;

        double ballRadius = field.getWidth() * 0.022;
        this.mainBall = new Ball(ballRadius);
        this.activeBalls = new ArrayList<>();
        this.activeBalls.add(this.mainBall);

        this.goalResetTimer = 0.0;
        this.waitingAfterGoal = false;

        initEntitiesOnField();
        registerContactListener();
    }

    /**
     * Posiciona al incio a los dos personajes y el balón en la cancha según la resolución.
     */
    protected void initEntitiesOnField() {
        double w = field.getWidth();
        double h = field.getHeight();
        double groundY = field.getGroundLevelY();

        double charW = w * 0.068;
        double charH = h * 0.19;
        double startY = groundY - (charH / 2.0);

        if (player1.getCharacter() != null) {
            player1.getCharacter().initPhysics(field.getWorld(), w * 0.22, startY, charW, charH, true, groundY);
        }
        if (player2.getCharacter() != null) {
            player2.getCharacter().initPhysics(field.getWorld(), w * 0.78, startY, charW, charH, false, groundY);
        }

        mainBall.initPhysics(field.getWorld(), w * 0.50, h * 0.35, mainBall.getRadius());
    }

    /**
     * Registra el listener de colisiones de dyn4j para goles, patadas, tiros de poder y suelo.
     */
    private void registerContactListener() {
        this.contactListener = new ContactListenerAdapter<Body>() {
            @Override
            public void begin(ContactCollisionData<Body> collision, Contact contact) {
                dispatchContact(collision);
            }

            public void sensed(ContactCollisionData<Body> collision, Contact contact) {
                dispatchContact(collision);
            }

            @Override
            public void postSolve(ContactCollisionData<Body> collision, SolvedContact contact) {
                Collidable c1 = (Collidable) collision.getBody1().getUserData();
                Collidable c2 = (Collidable) collision.getBody2().getUserData();

                if (c1 != null && c2 != null) {
                    c1.onCollisionPostSolve(c2, Match.this);
                    c2.onCollisionPostSolve(c1, Match.this);
                }
            }
        };
        field.getWorld().addContactListener(this.contactListener);
    }

    private void dispatchContact(ContactCollisionData<Body> collision) {
        if (waitingAfterGoal || gameInfo.isFinished()) {
            return;
        }
        Collidable c1 = (Collidable) collision.getBody1().getUserData();
        Collidable c2 = (Collidable) collision.getBody2().getUserData();

        if (c1 != null && c2 != null) {
            c1.onCollisionBegin(c2, collision.getFixture1(), collision.getFixture2(), this);
            c2.onCollisionBegin(c1, collision.getFixture2(), collision.getFixture1(), this);
        }
    }

    public void onGoalScored(boolean scoredByPlayer1) {
        //actualiza todo lo relacionado con los goles para los jugadores
        Player scorer   = scoredByPlayer1 ? player1 : player2;
        Player conceder = scoredByPlayer1 ? player2 : player1;
        scorer.addGoal();
        conceder.onGoalConceded();

        if (scoredByPlayer1) {
            gameInfo.addGoalPlayer1();
        } else {
            gameInfo.addGoalPlayer2();
        }

        clearAllFakeBalls();
        checkEndCondition();

        if (!gameInfo.isFinished()) {
            this.waitingAfterGoal = true;
            this.goalResetTimer = 1.5; // Pausa  antes de reubicar al centro
        }
    }

    private void resetPositionsAfterGoal() {
        double w = field.getWidth();
        double h = field.getHeight();
        double groundY = field.getGroundLevelY();
        double charH = h * 0.19;
        double startY = groundY - (charH / 2.0);

        if (player1.getCharacter() != null) {
            player1.getCharacter().resetPosition(w * 0.22, startY);
            player1.getCharacter().resetModifiers();
        }
        if (player2.getCharacter() != null) {
            player2.getCharacter().resetPosition(w * 0.78, startY);
            player2.getCharacter().resetModifiers();
        }

        mainBall.resetToCenter(w * 0.50, h * 0.35);
    }

    /**
     * Bucle principal del partido invocado en cada fotograma.
     */
    public void update(double dt) {
        if (gameInfo.isFinished() || gameInfo.isPaused()) {
            return;
        }

        // la pausa
        if (waitingAfterGoal) {
            goalResetTimer -= dt;
            field.stepPhysics(dt);
            if (goalResetTimer <= 0.0) {
                waitingAfterGoal = false;
                resetPositionsAfterGoal();
            }
            return;
        }

        // Actualiza stats inGame
        player1.update(dt, this);
        player2.update(dt, this);

        // Actualizar personajes
        if (player1.getCharacter() != null) player1.getCharacter().update(dt);
        if (player2.getCharacter() != null) player2.getCharacter().update(dt);


        resolveKickDamage(player1, player2);
        resolveKickDamage(player2, player1);

        // Actualizar balones
        Iterator<Ball> it = activeBalls.iterator();
        while (it.hasNext()) {
            Ball b = it.next();
            b.update(dt);
            if (b.isExpiredFake()) {
                field.getWorld().removeBody(b.getBody());
                it.remove();
            }
        }

        // Avanzar simulación física en dyn4j
        field.stepPhysics(dt);

        // Actualizar cronómetro y condición de victoria
        updateModeRules(dt);
    }

    // Se evalúa cada frame durante la patada y evalua quue aplica   y se aplica una sola vez (kickResolved).
    private void resolveKickDamage(Player attacker, Player victim) {
        Character_ a = attacker.getCharacter();
        Character_ v = victim.getCharacter();
        if (a == null || v == null || !a.isKickDamagePending()) {
            return;
        }
        if (a.isInKickRange(v)) {
            victim.takeDamage(a.getCurrentKickDamage()); // 10 × multiplicadorKick
            a.markKickResolved();
        }
    }

    //  utilidades para poderes y la Cpu

    public void spawnFakeBall(Ball fakeBall, double x, double y, double vx, double vy) {
        fakeBall.initPhysics(field.getWorld(), x, y, fakeBall.getRadius());
        fakeBall.launch(vx, vy);
        activeBalls.add(fakeBall);
    }

    public void removeFakeBall(Ball fakeBall) {
        if (fakeBall.isFake()) {
            field.getWorld().removeBody(fakeBall.getBody());
            activeBalls.remove(fakeBall);
        }
    }

    private void clearAllFakeBalls() {
        Iterator<Ball> it = activeBalls.iterator();
        while (it.hasNext()) {
            Ball b = it.next();
            if (b.isFake()) {
                field.getWorld().removeBody(b.getBody());
                it.remove();
            }
        }
    }

    public Player getOwnerOf(Character_ character) {
        if (player1.getCharacter() == character) return player1;
        if (player2.getCharacter() == character) return player2;
        return null;
    }


    public void disposeOldField() {
        field.getWorld().removeContactListener(this.contactListener);
        for (Ball b : activeBalls) {
            b.detachFromWorld();
        }
        activeBalls.clear();
        if (player1.getCharacter() != null) player1.getCharacter().detachFromWorld();
        if (player2.getCharacter() != null) player2.getCharacter().detachFromWorld();
    }


    public Player getWinner() {
        int w = gameInfo.getWinnerNumber();
        return (w == 1) ? player1 : (w == 2) ? player2 : null;
    }

    public String getResultMessage() {
        if (!gameInfo.isFinished()) {
            throw new DopoSoccerException(DopoSoccerException.MATCH_NOT_FINISHED);
        }
        Player winner = getWinner();
        StringBuilder sb = new StringBuilder();
        sb.append(winner == null ? "Empate" : "Ganador: " + winner.getName());
        sb.append(" (").append(gameInfo.getPlayer1Goals()).append(" - ").append(gameInfo.getPlayer2Goals()).append("). Puntos: ");
        boolean first = true;
        if (player1.isHuman()) {
            sb.append(player1.getName()).append(" +").append(gameInfo.getPointsFor(1));
            first = false;
        }
        if (player2.isHuman()) {
            sb.append(first ? "" : " | ").append(player2.getName()).append(" +").append(gameInfo.getPointsFor(2));
        }
        return sb.toString();
    }

    // Métodos abstractos implementados por TimeMatch y GoalMatch
    protected abstract void updateModeRules(double dt);
    protected abstract void checkEndCondition();

    // Getters
    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public Field getField() { return field; }
    public Ball getMainBall() { return mainBall; }
    public List<Ball> getActiveBalls() { return activeBalls; }
    public InGameInfo getGameInfo() { return gameInfo; }
}
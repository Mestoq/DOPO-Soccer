package domain.match;

import domain.DopoSoccerException;
import domain.character.Character_;
import domain.physics.Ball;
import domain.player.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Esta clase guarda solo datos planos del partido (marcador, tiempo, jugadores,
 * posiciones y velocidades) y sabe capturarlos y reaplicarlos sobre un Match nuevo
 * Quedan fuera por ser transitorios los efectos activos, tiros de poder en vuelo
 * y balones falsos
 */
public class MatchSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    // Estado de un jugador dentro del partido
    public static class PlayerState implements Serializable {
        private static final long serialVersionUID = 1L;

        private final boolean human;
        private final String cpuProfile;
        private final String country;
        private final int points, speed, jump, kick, survival;
        private final int goals;
        private final double health, powerBar, stunTimer;
        private final boolean aura;
        private final double x, y, vx, vy;
        private final boolean facingRight;

        private PlayerState(Player p, Character_ c) {
            this.human = p.isHuman();
            this.cpuProfile = p.getAiProfile();
            this.country = c.getCountry();
            this.points = p.getPoints();
            this.speed = p.getSpeed();
            this.jump = p.getJump();
            this.kick = p.getKick();
            this.survival = p.getSurvival();
            this.goals = p.getGoals();
            this.health = p.getCurrentHealth();
            this.powerBar = p.getPowerBar();
            this.stunTimer = p.getStunTimer();
            this.aura = p.hasPowerAura();
            this.x = c.getX();
            this.y = c.getY();
            this.vx = c.getVelocityX();
            this.vy = c.getVelocityY();
            this.facingRight = c.isFacingRight();
        }

        static PlayerState capture(Player p) throws DopoSoccerException {
            Character_ c = p.getCharacter();
            if (c == null) {
                throw new DopoSoccerException("No se puede guardar: el jugador " + p.getName() + " no tiene personaje asignado.");
            }
            return new PlayerState(p, c);
        }

        public boolean isHuman() { return human; }
        public String getCpuProfile() { return cpuProfile; }

        // Reconstruye perfil y personaje ANTES de crear el Match (initEntitiesOnField los necesita).
        public void applyProfileTo(Player p) throws DopoSoccerException {
            p.setPoints(points);
            p.setSpeed(speed);
            p.setJump(jump);
            p.setKick(kick);
            p.setSurvival(survival);
            p.setCharacter(Character_.create(country));
        }

        // Reaplica vida, barra, aura, aturdimiento, posición y velocidad DESPUÉS de crear el Match.
        void applyMatchStateTo(Player p) {
            p.restoreMatchState(goals, health, powerBar, aura, stunTimer);
            Character_ c = p.getCharacter();
            c.setFacingRight(facingRight);
            c.restoreMotion(x, y, vx, vy);
        }
    }

    private final String matchType;
    private final double limitValue;
    private final double fieldWidth, fieldHeight;
    private final int player1Goals, player2Goals;
    private final double remainingTime, elapsedTime;
    private final PlayerState player1, player2;
    private final double ballX, ballY, ballVx, ballVy;

    private MatchSnapshot(String matchType, double limitValue, Match m) {
        InGameInfo info = m.getGameInfo();
        Ball ball = m.getMainBall();
        this.matchType = matchType;
        this.limitValue = limitValue;
        this.fieldWidth = m.getField().getWidth();
        this.fieldHeight = m.getField().getHeight();
        this.player1Goals = info.getPlayer1Goals();
        this.player2Goals = info.getPlayer2Goals();
        this.remainingTime = info.getRemainingTime();
        this.elapsedTime = info.getElapsedTime();
        this.player1 = PlayerState.capture(m.getPlayer1());
        this.player2 = PlayerState.capture(m.getPlayer2());
        this.ballX = ball.getX();
        this.ballY = ball.getY();
        this.ballVx = ball.getVelocityX();
        this.ballVy = ball.getVelocityY();
    }

    public static MatchSnapshot capture(Match match, String matchType, double limitValue) throws DopoSoccerException {
        if (match == null) {
            throw new DopoSoccerException("No hay un partido para guardar.");
        }
        return new MatchSnapshot(matchType, limitValue, match);
    }

    // Reaplica el estado sobre un Match recién creado con la misma configuración.
    public void applyTo(Match match) {
        match.getGameInfo().restoreState(player1Goals, player2Goals, remainingTime, elapsedTime);
        player1.applyMatchStateTo(match.getPlayer1());
        player2.applyMatchStateTo(match.getPlayer2());
        match.getMainBall().restoreMotion(ballX, ballY, ballVx, ballVy);
    }

    // Se mantienen los nombres guardar/abrir para ser consistentes con SaveFile.
    public void guardar(File file) throws DopoSoccerException {
        if (file == null) {
            throw new DopoSoccerException("El archivo de destino no puede ser nulo.");
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(this);
        } catch (IOException e) {
            throw new DopoSoccerException("Error al guardar el partido: " + e.getMessage(), e);
        }
    }

    public static MatchSnapshot abrir(File file) throws DopoSoccerException {
        if (file == null || !file.exists()) {
            throw new DopoSoccerException("El archivo del partido no existe.");
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (MatchSnapshot) ois.readObject();
        } catch (ClassCastException e) {
            throw new DopoSoccerException("El archivo no corresponde a un partido guardado de DopoSoccer.", e);
        } catch (IOException | ClassNotFoundException e) {
            throw new DopoSoccerException("Error al abrir el partido: " + e.getMessage(), e);
        }
    }

    public String getMatchType() { return matchType; }
    public double getLimitValue() { return limitValue; }
    public double getFieldWidth() { return fieldWidth; }
    public double getFieldHeight() { return fieldHeight; }
    public PlayerState getPlayer1() { return player1; }
    public PlayerState getPlayer2() { return player2; }

}
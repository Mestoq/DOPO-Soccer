package test;

import domain.DopoSoccerException;
import domain.character.Character_;
import domain.physics.Ball;
import domain.physics.Field;
import domain.player.Cpu;
import domain.player.HumanPlayer;
import domain.player.Player;
import domain.match.GoalMatch;
import domain.match.InGameInfo;
import domain.match.Match;
import domain.match.TimeMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MatchTest {
    private static final double W = 1600;
    private static final double H = 900;

    private Field field;
    private HumanPlayer p1;
    private HumanPlayer p2;

    /**
     * Antes de cada prueba crea una cancha de 1600x900 y dos jugadores humanos.
     */
    @BeforeEach
    void setUp() {
        field = new Field(W, H);
        p1 = new HumanPlayer("Player 1");
        p2 = new HumanPlayer("Player 2");
    }

    private static boolean waiting(Match m) {
        try {
            java.lang.reflect.Field f = Match.class.getDeclaredField("waitingAfterGoal");
            f.setAccessible(true);
            return f.getBoolean(m);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static double resetTimer(Match m) {
        try {
            java.lang.reflect.Field f = Match.class.getDeclaredField("goalResetTimer");
            f.setAccessible(true);
            return f.getDouble(m);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private GoalMatch goalMatch(int target) {
        return new GoalMatch(field, p1, p2, target);
    }

    private TimeMatch timeMatch(double seconds) {
        return new TimeMatch(field, p1, p2, seconds);
    }

    /** Avanza el partido hasta completar los segundos indicados */
    private void advance(Match m, double seconds) {
        int steps = (int) Math.round(seconds / 0.1);
        for (int i = 0; i < steps; i++) {
            m.update(0.1);
        }
    }
    // adicionamos un metodo un metodo adicional que nos ayudara en algunos tests de mas adelante
    private void giveCharacters(String c1, String c2) throws DopoSoccerException {
        p1.setCharacter(Character_.create(c1));
        p2.setCharacter(Character_.create(c2));
    }

    /**
     * Crea un GoalMatch. Verifica que los getters devuelven los mismos jugadores y cancha, y que existe el InGameInfo.
     */
    @Test
    void constructorStoresCollaborators() {
        Match m = goalMatch(3);
        assertSame(p1, m.getPlayer1());
        assertSame(p2, m.getPlayer2());
        assertSame(field, m.getField());
        assertNotNull(m.getGameInfo());
    }


    /**
     * Verifica que al inicio activeBalls tiene un solo balón y que es el principal.
     */
    @Test
    void activeBallsStartsWithMainBallOnly() {
        Match m = goalMatch(3);
        assertEquals(1, m.getActiveBalls().size());
        assertSame(m.getMainBall(), m.getActiveBalls().get(0));
    }

    /**
     * Crea un partido sin asignar personajes. Verifica que no lanza excepción.
     */
    @Test
    void constructorWorksWithoutCharacters() {
        assertDoesNotThrow(() -> goalMatch(3));
    }

    /**
     * Verifica que GoalMatch queda en modo goles con su meta y TimeMatch en modo tiempo con su duración.
     */
    @Test
    void modesAreConfiguredBySubclasses() {
        assertFalse(goalMatch(4).getGameInfo().isTimeMode());
        assertEquals(4, goalMatch(4).getGameInfo().getTargetGoals());
        assertTrue(timeMatch(90).getGameInfo().isTimeMode());
        assertEquals(90.0, timeMatch(90).getGameInfo().getRemainingTime(), 1e-9);
    }

    /**
     * Simula un gol de P1. Verifica los goles de ambos, el marcador del InGameInfo y que la barra de poder de P2 sube 25.
     */
    @Test
    void goalByPlayer1UpdatesScoreAndConcederBar() {
        Match m = goalMatch(5);
        m.onGoalScored(true);
        assertEquals(1, p1.getGoals());
        assertEquals(0, p2.getGoals());
        assertEquals(1, m.getGameInfo().getPlayer1Goals());
        assertEquals(25.0, p2.getPowerBar(), 1e-9);
        assertEquals(0.0, p1.getPowerBar(), 1e-9);
    }

    /**
     * Simula un gol de P2. Verifica su marcador y que la barra de poder de P1 sube 25.
     */
    @Test
    void goalByPlayer2UpdatesScoreAndConcederBar() {
        Match m = goalMatch(5);
        m.onGoalScored(false);
        assertEquals(1, p2.getGoals());
        assertEquals(1, m.getGameInfo().getPlayer2Goals());
        assertEquals(25.0, p1.getPowerBar(), 1e-9);
    }

    /**
     * Con meta de 1 gol, verifica que el gol que termina el partido no activa la pausa de reubicación.
     */
    @Test
    void finalGoalDoesNotStartResetPause() {
        Match m = goalMatch(1);
        m.onGoalScored(true);
        assertTrue(m.getGameInfo().isFinished());
        assertFalse(waiting(m));
    }

    /**
     * Crea dos balones falsos y marca un gol. Verifica que solo queda el balón principal en activeBalls.
     */
    @Test
    void goalClearsFakeBalls() {
        Match m = goalMatch(5);
        m.spawnFakeBall(new Ball(10, true, 5.0), 400, 300, 1, 1);
        m.spawnFakeBall(new Ball(10, true, 5.0), 500, 300, 1, 1);
        assertEquals(3, m.getActiveBalls().size());
        m.onGoalScored(false);
        assertEquals(1, m.getActiveBalls().size());
        assertSame(m.getMainBall(), m.getActiveBalls().get(0));
    }

    /**
     * En un TimeMatch empatado al acabar el tiempo entra en muerte súbita; verifica que el siguiente gol termina el partido y gana quien lo marcó.
     */
    @Test
    void timeMatchSuddenDeathGoalFinishes() {
        Match m = timeMatch(1.0);
        advance(m, 1.2); // empate 0-0 al acabarse el tiempo
        assertTrue(m.getGameInfo().isSuddenDeath());
        assertFalse(m.getGameInfo().isFinished());
        m.onGoalScored(false);
        assertTrue(m.getGameInfo().isFinished());
        assertSame(p2, m.getWinner());
    }

    /**
     * Verifica que el gol cuenta para P2.
     */
    @Test
    void ballInsideLeftGoalScoresForPlayer2() {
        Match m = goalMatch(5);
        double goalWidth = W * 0.085;
        // dentro del sensor del arco izquierdo, que defiende P1
        m.getMainBall().resetPosition(goalWidth * 0.4, field.getGroundLevelY() - 60);
        advance(m, 0.5);
        assertEquals(1, p2.getGoals());
        assertEquals(0, p1.getGoals());
    }

    /**
     *  Verifica que el gol cuenta para P1.
     */
    @Test
    void ballInsideRightGoalScoresForPlayer1() {
        Match m = goalMatch(5);
        double goalWidth = W * 0.085;
        m.getMainBall().resetPosition(W - goalWidth * 0.4, field.getGroundLevelY() - 60);
        advance(m, 0.5);
        assertEquals(1, p1.getGoals());
        assertEquals(0, p2.getGoals());
    }

    /**
     * Llama a update dos veces (0.5 y 0.25). Verifica que el tiempo transcurrido suma 0.75.
     */
    @Test
    void updateAccumulatesElapsedTime() {
        Match m = goalMatch(5);
        m.update(0.5);
        m.update(0.25);
        assertEquals(0.75, m.getGameInfo().getElapsedTime(), 1e-9);
    }

    /**
     * En un TimeMatch de 10 s llama a update(0.5). Verifica que quedan 9.5 s.
     */
    @Test
    void updateConsumesRemainingTime() {
        Match m = timeMatch(10);
        m.update(0.5);
        assertEquals(9.5, m.getGameInfo().getRemainingTime(), 1e-9);
    }

    /**
     * Pausa el partido y llama a update  y mira que ni el tiempo restante ni el transcurrido cambian.
     */
    @Test
    @DisplayName("update no hace nada mientras el partido está en pausa")
    void updateDoesNothingWhenPaused() {
        Match m = timeMatch(10);
        m.getGameInfo().setPaused(true);
        m.update(1.0);
        assertEquals(10.0, m.getGameInfo().getRemainingTime(), 1e-9);
        assertEquals(0.0, m.getGameInfo().getElapsedTime(), 1e-9);
    }

    /**
     * Termina el partido y llama a update y que el reloj no avanza
     */
    @Test
    @DisplayName("update no hace nada cuando el partido ya terminó")
    void updateDoesNothingWhenFinished() {
        Match m = timeMatch(10);
        m.getGameInfo().finishMatch();
        m.update(1.0);
        assertEquals(0.0, m.getGameInfo().getElapsedTime(), 1e-9);
    }

    /**
     * Tras un gol, llama a update durante la pausay mira que el tiempo restante no se descuenta y que sigue en pausa.
     */
    @Test
    void clockStopsDuringGoalPause() {
        Match m = timeMatch(30);
        m.onGoalScored(true);
        m.update(1.0);
        assertEquals(30.0, m.getGameInfo().getRemainingTime(), 1e-9);
        assertTrue(waiting(m));
    }

    /**
     * mira que el balon vuelva al centro al meter gol.
     */
    @Test
    void ballReturnsToCenterAfterGoalPause() {
        Match m = goalMatch(5);
        m.onGoalScored(true);
        m.update(1.0); // el balón cae durante la pausa
        m.update(0.6); // total 1.6 s > 1.5 s
        assertFalse(waiting(m));
        assertEquals(W * 0.50, m.getMainBall().getX(), 1.0);
        assertEquals(H * 0.35, m.getMainBall().getY(), 1.0);
        assertEquals(0.0, m.getMainBall().getVelocityY(), 1e-9);
    }

    /**
     * Mueve a P1, marca un gol y y mira que ambos personajes vuelven a sus posiciones iniciales.
     */
    @Test
    void charactersReturnToStartAfterGoalPause() throws DopoSoccerException{
        giveCharacters("CAMERUN", "NIGERIA");
        Match m = goalMatch(5);
        p1.getCharacter().moveRight(1);
        m.update(0.3); // P1 se desplaza un poco
        m.onGoalScored(false);
        m.update(1.6);
        assertEquals(W * 0.22, p1.getCharacter().getX(), 1.0);
        assertEquals(W * 0.78, p2.getCharacter().getX(), 1.0);
    }

    /**
     * Deja agotar el tiempo con 0-0. Verifica que se activa la muerte súbita, que el partido no termina y que no hay ganador.
     */
    @Test
    void timeMatchTieStartsSuddenDeath() {
        Match m = timeMatch(1.0);
        advance(m, 1.2);
        assertTrue(m.getGameInfo().isSuddenDeath());
        assertFalse(m.getGameInfo().isFinished());
        assertNull(m.getWinner());
    }

    /**
     * P1 marca y se agota el tiempo. Verifica que el partido termina y que el ganador es P1.
     */
    @Test
    void timeMatchEndsWithWinner() {
        Match m = timeMatch(2.0);
        m.onGoalScored(true);
        advance(m, 4.5); // 1.5 s de pausa + 2 s de juego + margen
        assertTrue(m.getGameInfo().isFinished());
        assertSame(p1, m.getWinner());
    }

    /**
     * Crea un balón falso con spawnFakeBall. Verifica que entra a activeBalls y que recibe la velocidad indicada.
     */
    @Test
    void spawnFakeBallAddsToActiveBalls() {
        Match m = goalMatch(5);
        Ball fake = new Ball(10, true, 3.0);
        m.spawnFakeBall(fake, 400, 300, 2.0, -1.0);
        assertEquals(2, m.getActiveBalls().size());
        assertTrue(m.getActiveBalls().contains(fake));
        assertEquals(2.0, fake.getVelocityX(), 1e-9);
    }

    /**
     * Verifica que removeFakeBall elimina un balón falso pero nunca el balón principal.
     */
    @Test
    void removeFakeBallOnlyRemovesFakes() {
        Match m = goalMatch(5);
        Ball fake = new Ball(10, true, 3.0);
        m.spawnFakeBall(fake, 400, 300, 0, 0);
        m.removeFakeBall(fake);
        assertFalse(m.getActiveBalls().contains(fake));

        m.removeFakeBall(m.getMainBall());
        assertTrue(m.getActiveBalls().contains(m.getMainBall()));
    }

    /**
     * Pone a P2 delante de P1 y P1 patea. Verifica que la vida de P2 baja.
     */
    @Test
    void kickInRangeDamagesVictim() throws DopoSoccerException{
        giveCharacters("CAMERUN", "NIGERIA");
        Match m = goalMatch(5);
        // P1 mira a la derecha; se ubica a P2 pegado delante de P1
        p2.getCharacter().resetPosition(p1.getCharacter().getX() + 50, p1.getCharacter().getY());
        double before = p2.getCurrentHealth();
        p1.getCharacter().kick(p1.getNormalKickDamage());
        m.update(0.016);
        assertTrue(p2.getCurrentHealth() < before);
    }

    /**
     * Hace varios update seguidos tras una patada y verifica que el daño se aplica una sola vez.
     */
    @Test
    void kickDamageAppliesOnlyOnce() throws DopoSoccerException{
        giveCharacters("CAMERUN", "NIGERIA");
        Match m = goalMatch(5);
        p2.getCharacter().resetPosition(p1.getCharacter().getX() + 50, p1.getCharacter().getY());
        p1.getCharacter().kick(p1.getNormalKickDamage());
        m.update(0.016);
        double afterFirst = p2.getCurrentHealth();
        m.update(0.016);
        m.update(0.016);
        assertEquals(afterFirst, p2.getCurrentHealth(), 1e-9);
    }

    /**
     * P1 patea con P2 lejos. Verifica que la vida de P2 no cambia.
     */
    @Test
    void kickOutOfRangeDoesNoDamage() throws DopoSoccerException{
        giveCharacters("CAMERUN", "NIGERIA");
        Match m = goalMatch(5);
        double before = p2.getCurrentHealth();
        p1.getCharacter().kick(p1.getNormalKickDamage()); // P2 está a más de la mitad de la cancha
        m.update(0.016);
        assertEquals(before, p2.getCurrentHealth(), 1e-9);
    }

    /**
     * Pasa un personaje que no pertenece al partido. Verifica que devuelve null.
     */
    @Test
    void getOwnerOfUnknownCharacterIsNull() throws DopoSoccerException{
        giveCharacters("CAMERUN", "NIGERIA");
        Match m = goalMatch(5);
        assertNull(m.getOwnerOf(Character_.create("COREA DEL SUR")));
    }

    /**
     * Con el marcador en 0, verifica que getWinner sea null.
     */
    @Test
    void getWinnerIsNullOnTie() {
        assertNull(goalMatch(5).getWinner());
    }

    /**
     * Pide el mensaje de resultado con el partido en curso. Verifica que lanza DopoSoccerException.
     */
    @Test
    void resultMessageBeforeFinishThrows() {
        Match m = goalMatch(5);
        assertThrows(DopoSoccerException.class, m::getResultMessage);
    }

    /**
     * Termina un partido con victoria de P1. Verifica el mensaje: ganador, marcador y puntos de cada humano.
     */
    @Test
    void resultMessageWithWinner() {
        Match m = goalMatch(1);
        m.onGoalScored(true);
        String msg = m.getResultMessage();
        assertTrue(msg.contains("Ganador: Player 1"), msg);
        assertTrue(msg.contains("(1 - 0)"), msg);
        assertTrue(msg.contains("Player 1 +" + InGameInfo.POINTS_WINNER), msg);
        assertTrue(msg.contains("Player 2 +" + InGameInfo.POINTS_LOSER), msg);
    }

    /**
     * Termina el partido empatado. Verifica que el mensaje dice "Empate" e incluye los puntos de empate.
     */
    @Test
    void resultMessageWithDraw() {
        Match m = goalMatch(5);
        m.getGameInfo().finishMatch();
        String msg = m.getResultMessage();
        assertTrue(msg.startsWith("Empate"), msg);
        assertTrue(msg.contains("+" + InGameInfo.POINTS_DRAW), msg);
    }

    /**
     * P1 contra CPU. Verifica que el mensaje lista los puntos de P1 pero no los de la CPU.
     */
    @Test
    void resultMessageOmitsCpuPoints() {
        Cpu cpu = new Cpu("CPU", Cpu.PROFILE_CLUMSY);
        Match m = new GoalMatch(field, p1, cpu, 1);
        m.onGoalScored(true);
        String msg = m.getResultMessage();
        assertTrue(msg.contains("Player 1 +"), msg);
        assertFalse(msg.contains("CPU +"), msg);
    }

    /**
     * Corre 5s de partido contra la CPU con cada perfil (torpe, ofensiva, defensiva, experta). y  verifica que no se lanza ninguna excepción.
     */
    @Test
    void matchRunsWithCpuProfiles() throws DopoSoccerException {
        for (String profile : new String[]{Cpu.PROFILE_CLUMSY, Cpu.PROFILE_OFFENSIVE,
                Cpu.PROFILE_DEFENSIVE, Cpu.PROFILE_EXPERT}) {
            Field f = new Field(W, H);
            Player human = new HumanPlayer("Player 1");
            Cpu cpu = new Cpu("CPU", profile);
            human.setCharacter(Character_.create("CAMERUN"));
            cpu.setCharacter(Character_.create("NIGERIA"));
            Match m = new GoalMatch(f, human, cpu, 10);
            assertDoesNotThrow(() -> advance(m, 5.0), profile);
        }
    }
}

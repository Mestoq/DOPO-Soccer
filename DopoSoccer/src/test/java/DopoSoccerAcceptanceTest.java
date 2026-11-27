package test;

import domain.DopoSoccer;
import domain.DopoSoccerException;
import domain.character.KoreaCharacter;
import domain.character.NigeriaCharacter;
import domain.match.Match;
import domain.physics.Field;
import domain.player.HumanPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// basada en que el partido se complete segun los goles deseados y que el resultado  solo se aplica cuando el partido acabo.
public class DopoSoccerAcceptanceTest {
     private DopoSoccer game;
    private HumanPlayer p1;
    private HumanPlayer p2;

    @BeforeEach
    void setUp() {
        game = new DopoSoccer();
        p1 = new HumanPlayer(DopoSoccer.PLAYER_1);
        p2 = new HumanPlayer(DopoSoccer.PLAYER_2);
    }

    private Match startGoalMatch(String char1, String char2, int targetGoals) throws DopoSoccerException {
        game.preFixPlayers(p1, p2);
        game.preFixCharacters(char1, char2);
        return game.preFixMatch(Match.TYPE_GOALS, new Field(1600, 900), targetGoals);
    }

    /**
     * 
     * Player 1 (KOREA) vence 2-0 a Player 2 (NIGERIA) y recibe puntos.
     */
    @Test
    void completeGoalMatchPlayer1WinsAndEarnsPoints() throws DopoSoccerException {
        int pointsBefore = game.getPoints(DopoSoccer.PLAYER_1);

        Match match = startGoalMatch("KOREA", "NIGERIA", 2);
        assertInstanceOf(KoreaCharacter.class, p1.getCharacter());
        assertInstanceOf(NigeriaCharacter.class, p2.getCharacter());
        assertFalse(match.getGameInfo().isFinished());

        match.onGoalScored(true);
        assertFalse(match.getGameInfo().isFinished());
        match.onGoalScored(true);

        assertTrue(match.getGameInfo().isFinished());
        assertSame(p1, match.getWinner());

        String message = game.finishMatch();
        assertTrue(message.startsWith("Ganador: Player 1 (2 - 0)"), message);
        assertTrue(game.getPoints(DopoSoccer.PLAYER_1) > pointsBefore);
    }

    /**
     *  el resultado solo se aplica con el partido terminado y una sola vez.
     */
    @Test
    void resultIsAppliedOnlyWhenFinishedAndOnlyOnce() throws DopoSoccerException {
        Match match = startGoalMatch("USA", "CAMEROON", 1);

        DopoSoccerException early = assertThrows(DopoSoccerException.class, () -> game.finishMatch());
        assertEquals(DopoSoccerException.MATCH_NOT_FINISHED, early.getMessage());

        match.onGoalScored(false);
        game.finishMatch();

        DopoSoccerException twice = assertThrows(DopoSoccerException.class, () -> game.finishMatch());
        assertEquals(DopoSoccerException.RESULT_ALREADY_APPLIED, twice.getMessage());
    }
}

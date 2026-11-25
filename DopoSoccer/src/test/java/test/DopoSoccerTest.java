package test;

import domain.DopoSoccer;
import domain.DopoSoccerException;
import domain.match.GoalMatch;
import domain.match.Match;
import domain.match.SaveFile;
import domain.match.TimeMatch;
import domain.physics.Field;
import domain.player.Cpu;
import domain.player.HumanPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DopoSoccerTest {
    private DopoSoccer game;
    private HumanPlayer p1;
    private HumanPlayer p2;
    private Cpu cpu;

    @TempDir
    File tmp;

    /**
     * Antes de cada prueba creamos un DopoSoccer nuevo, dos players y una CPU en expert,
     * para que ninguna prueba dependa de otra.
     */
    @BeforeEach
    void setUp() {
        game = new DopoSoccer();
        p1 = new HumanPlayer("Player 1");
        p2 = new HumanPlayer("Player 2");
        cpu = new Cpu("CPU", Cpu.PROFILE_EXPERT);
    }

    private Field newField() {
        return new Field(1600, 900);
    }

    /**
     * Prueba la creación de perfiles del constructor.
     */
    @Test
    void constructorCreateNewprofiles() {
        DopoSoccer game = new DopoSoccer();
        assertFalse(game.isSaveLoaded("Player 1"));
        assertFalse(game.isSaveLoaded("Player 2"));
        assertFalse(game.isAnySaveLoaded());
    }

    /**
     * Con perfil nuevo intenta asignar cada uno de los 4 personajes base a p1 y verifica que no lanza excepción.
     */
    @Test
    void defaultrosterallowbasecharacter() {
        game.preFixPlayers(p1, p2);
        for (String c : List.of("KOREA", "CAMEROON", "NIGERIA", "USA")) {
            assertDoesNotThrow(() -> game.preFixCharacters(c, "NIGERIA"), c);
        }
    }

    /**
     * Configura dos humanos y asigna Camerun y Nigeria para verificar que cada jugador quedó con el personaje del país correcto.
     */
    @Test
    void prefixcharacterassignhumanvshuman() throws DopoSoccerException {
        game.preFixPlayers(p1, p2);
        game.preFixCharacters("CAMEROON", "NIGERIA");
        assertEquals("CAMEROON", p1.getCharacter().getCountry());
        assertEquals("NIGERIA", p2.getCharacter().getCountry());
    }

    /**
     * Configura humanos vs CPU de forma que se verifica que la CPU recibe el personaje del rival y p1 el suyo.
     */
    @Test
    void prefixcharacterassigncpurival() throws DopoSoccerException {
        game.preFixPlayers(p1, cpu);
        game.preFixCharacters("KOREA", "USA");
        assertEquals("KOREA", p1.getCharacter().getCountry());
        assertEquals("USA", cpu.getCharacter().getCountry());
    }

    /**
     * Sirven los nombres con espacios y en minúsculas, y se asignan igualmente.
     */
    @Test
    void prefixcharactersnormalixesNames() throws DopoSoccerException {
        game.preFixPlayers(p1, p2);
        game.preFixCharacters("  cameroon ", "  nigeria ");
        assertEquals("CAMEROON", p1.getCharacter().getCountry());
        assertEquals("NIGERIA", p2.getCharacter().getCountry());
    }

    /**
     * Llama a preFixCharacters sin haber llamado antes preFixPlayers y verifica que lanza la excepción.
     */
    @Test
    void prefixcharacterwithoutplayers() {
        assertThrows(DopoSoccerException.class, () -> game.preFixCharacters("CAMEROON", "NIGERIA"));
    }

    /**
     * Registra dos jugadores con el mismo nombre y verifica la excepción.
     */
    @Test
    void prefixcharacterswithsingleplayer() {
        game.preFixPlayers(new HumanPlayer("Player 1"), new HumanPlayer("Player 1"));
        assertThrows(DopoSoccerException.class, () -> game.preFixCharacters("CAMEROON", "NIGERIA"));
    }

    /**
     * Pide para p1 un personaje que no está en el roster y verifica que lanza la excepción.
     */
    @Test
    void prefixcharacterswithfakename() {
        game.preFixPlayers(p1, p2);
        DopoSoccerException ex = assertThrows(DopoSoccerException.class, () -> game.preFixCharacters("BRAZIL", "NIGERIA"));
        assertTrue(ex.getMessage().contains("BRAZIL"));
    }

    /**
     * Pasa null como personaje de p1 y verifica la excepción.
     */
    @Test
    void prefixcharactersnullp1() {
        game.preFixPlayers(p1, p2);
        assertThrows(DopoSoccerException.class, () -> game.preFixCharacters(null, "NIGERIA"));
    }

    /**
     * Pasa null o cadena en blanco como personaje del rival y verifica la excepción.
     */
    @Test
    void prefixcharactersinvalidrival() {
        game.preFixPlayers(p1, p2);
        assertThrows(DopoSoccerException.class, () -> game.preFixCharacters("CAMEROON", null));
        assertThrows(DopoSoccerException.class, () -> game.preFixCharacters("CAMEROON", "   "));
    }

    /**
     * País del rival no existe y verifica la excepción.
     */
    @Test
    void prefixcharacterunknownrival() {
        game.preFixPlayers(p1, p2);
        assertThrows(DopoSoccerException.class, () -> game.preFixCharacters("CAMEROON", "ATLANTIDA"));
    }

    /**
     * Llama dos veces a preFixPlayers y verifica que el partido use los nuevos.
     */
    @Test
    void prefixplayersreplacesprevious() {
        game.preFixPlayers(p1, p2);
        game.preFixPlayers(p1, cpu);
        Match m = game.preFixMatch("GOALS", newField(), 3);
        assertSame(p1, m.getPlayer1());
        assertSame(cpu, m.getPlayer2());
    }

    /**
     * Llama a preFixMatch sin configurar los jugadores y verifica la excepción.
     */
    @Test
    void prefixmatchwithoutplayers() {
        assertThrows(IllegalStateException.class, () -> game.preFixMatch("TIME", newField(), 60));
    }

    /**
     * Pide un partido TIME de 120s y verifica que devuelve un TimeMatch en modo tiempo con 120 segundos restantes.
     */
    @Test
    void prefixmatchtimecreatestimematch() {
        game.preFixPlayers(p1, p2);
        Match m = game.preFixMatch("TIME", newField(), 120);
        assertInstanceOf(TimeMatch.class, m);
        assertTrue(m.getGameInfo().isTimeMode());
        assertEquals(120.0, m.getGameInfo().getRemainingTime(), 1e-9);
    }

    /**
     * Pide un partido GOALS a 7 goles. Verifica que devuelve un GoalMatch en modo goles con meta 7.
     */
    @Test
    void preFixMatchGoalsCreatesGoalMatch() {
        game.preFixPlayers(p1, p2);
        Match m = game.preFixMatch("GOALS", newField(), 7);
        assertInstanceOf(GoalMatch.class, m);
        assertFalse(m.getGameInfo().isTimeMode());
        assertEquals(7, m.getGameInfo().getTargetGoals());
    }

    /**
     * Pide los tipos "goals" y "time" en minúsculas. Verifica que crea GoalMatch y TimeMatch igual que en mayúsculas.
     */
    @Test
    void preFixMatchTypeIsCaseInsensitive() {
        game.preFixPlayers(p1, p2);
        assertInstanceOf(GoalMatch.class, game.preFixMatch("goals", newField(), 3));
        assertInstanceOf(TimeMatch.class, game.preFixMatch("time", newField(), 30));
    }

    /**
     * Pide un tipo inexistente (PENALES). Verifica que lanza IllegalArgumentException y que el mensaje incluye el tipo.
     */
    @Test
    void preFixMatchUnsupportedTypeThrows() {
        game.preFixPlayers(p1, p2);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> game.preFixMatch("PENALES", newField(), 5));
        assertTrue(ex.getMessage().contains("PENALES"));
    }

    /**
     * Registra jugadores con nombres distintos a "Player 1"/"Player 2"/"CPU". Verifica que lanza IllegalStateException porque no encuentra los roles.
     */
    @Test
    void preFixMatchRequiresExpectedRoles() {
        game.preFixPlayers(new HumanPlayer("Ana"), new HumanPlayer("Luis"));
        assertThrows(IllegalStateException.class, () -> game.preFixMatch("TIME", newField(), 60));
    }

    /**
     * Registra los jugadores en orden invertido (P2 primero). Verifica que el partido igual deja a "Player 1" como player1 y a "Player 2" como player2.
     */
    @Test
    void preFixMatchAssignsSides() {
        game.preFixPlayers(p2, p1);
        Match m = game.preFixMatch("TIME", newField(), 60);
        assertSame(p1, m.getPlayer1());
        assertSame(p2, m.getPlayer2());
    }

    /**
     * Flujo completo: jugadores, personajes y partido. Verifica que los personajes quedan en el 22% y el 78% del ancho de la cancha.
     */
    @Test
    void fullFlowPlacesCharactersOnField() throws DopoSoccerException {
        game.preFixPlayers(p1, cpu);
        game.preFixCharacters("CAMEROON", "NIGERIA");
        Field field = newField();
        Match m = game.preFixMatch("GOALS", field, 3);
        assertEquals(field.getWidth() * 0.22, m.getPlayer1().getCharacter().getX(), 1.0);
        assertEquals(field.getWidth() * 0.78, m.getPlayer2().getCharacter().getX(), 1.0);
    }

    /**
     * Llama dos veces a preFixMatch. Verifica que se obtienen dos objetos Match distintos.
     */
    @Test
    void preFixMatchReturnsNewMatchEachTime() {
        game.preFixPlayers(p1, p2);
        Match a = game.preFixMatch("TIME", newField(), 60);
        Match b = game.preFixMatch("TIME", newField(), 60);
        assertNotSame(a, b);
    }

    /**
     * Guarda el progreso en un archivo temporal y lo carga en otro DopoSoccer. Verifica que el archivo existe, no está vacío y que isSaveLoaded() queda en true.
     */
    @Test
    void saveAndLoadRoundTrip() throws DopoSoccerException {
        File f = new File(tmp, "progreso.dat");
        game.saveProgress("Player 1", f);
        assertTrue(f.exists());
        assertTrue(f.length() > 0);
        DopoSoccer other = new DopoSoccer();
        other.loadSave("Player 1", f);
        assertTrue(other.isSaveLoaded("Player 1"));
    }

    /**
     * Intenta cargar un archivo que no existe. Verifica que lanza DopoSoccerException y que isSaveLoaded() sigue en false.
     */
    @Test
    void loadSaveMissingFileThrows() {
        assertThrows(DopoSoccerException.class, () -> game.loadSave("Player 1", new File(tmp, "no-existe.dat")));
        assertFalse(game.isSaveLoaded("Player 1"));
    }

    /**
     * Llama a loadSave(null). Verifica que lanza DopoSoccerException.
     */
    @Test
    void loadSaveNullThrows() {
        assertThrows(DopoSoccerException.class, () -> game.loadSave("Player 1", null));
    }

    /**
     * Carga un archivo de texto que no es un objeto serializado. Verifica que lanza DopoSoccerException y que no marca el save como cargado.
     */
    @Test
    void loadSaveCorruptFileThrows() throws IOException {
        File f = new File(tmp, "basura.dat");
        Files.writeString(f.toPath(), "esto no es un objeto serializado");
        assertThrows(DopoSoccerException.class, () -> game.loadSave("Player 1", f));
        assertFalse(game.isSaveLoaded("Player 1"));
    }

    /**
     * Llama a saveProgress(null). Verifica que lanza DopoSoccerException.
     */
    @Test
    void saveProgressNullThrows() {
        assertThrows(DopoSoccerException.class, () -> game.saveProgress("Player 1", null));
    }

    /**
     * Pasa un SaveFile importado con un roster propio. Verifica que solo ese roster está disponible.
     */
    @Test
    void constructorUsesRosterFromSaveFile() throws IOException, DopoSoccerException {
        File txt = new File(tmp, "save.txt");
        Files.writeString(txt.toPath(), "characters=USA_NO_EXISTE,NIGERIA\n");
        DopoSoccer g = new DopoSoccer();
        SaveFile imported = SaveFile.importar(txt);
        File dat = new File(tmp, "save.dat");
        imported.guardar(dat);

        g.loadSave("Player 1", dat);
        g.preFixPlayers(p1, p2);

        assertDoesNotThrow(() -> g.preFixCharacters("NIGERIA", "CAMEROON"));
        assertThrows(DopoSoccerException.class, () -> g.preFixCharacters("CAMEROON", "NIGERIA"));
    }
}
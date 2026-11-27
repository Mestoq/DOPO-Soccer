package domain;

import domain.character.*;
import domain.match.*;
import domain.physics.Field;
import domain.player.HumanPlayer;
import domain.player.Player;
import domain.player.Cpu;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.ToIntFunction;

import static domain.DopoSoccerException.*;

public class DopoSoccer {

    // Claves de los jugadores: son el nombre del Player y la clave de su perfil
    public static final String PLAYER_1 = "Player 1";
    public static final String PLAYER_2 = "Player 2";
    public static final String CPU = "CPU";

    private static final Map<String, ToIntFunction<SaveFile>> STAT_LEVELS = new HashMap<>();

    static {
        STAT_LEVELS.put(Player.STAT_SPEED, SaveFile::getSpeed);
        STAT_LEVELS.put(Player.STAT_JUMP, SaveFile::getJump);
        STAT_LEVELS.put(Player.STAT_KICK, SaveFile::getKick);
        STAT_LEVELS.put(Player.STAT_SURVIVAL, SaveFile::getSurvival);
    }

    private Match currentMatch;
    private final TreeMap<String, Player> players = new TreeMap<>();
    private final Map<String, SaveFile> profiles = new HashMap<>();   // un perfil por jugador humano
    private MatchSnapshot savedMatch; // perfiles cargados desde archivo
    private final Map<String, MatchFactory> matchFactories = new HashMap<>();
    private final Map<String, CharacterFactory> characterFactories = new HashMap<>();
    private boolean resultApplied; // evita sumar dos veces los puntos de un mismo partido

    public DopoSoccer() {
        this.currentMatch = null;

        matchFactories.put(Match.TYPE_TIME,  (field, p1, p2, val) -> new TimeMatch(field, p1, p2, val));
        matchFactories.put(Match.TYPE_GOALS, (field, p1, p2, val) -> new GoalMatch(field, p1, p2, (int) val));

        characterFactories.put(Character_.KOREA,    (country, power) -> new KoreaCharacter());
        characterFactories.put(Character_.CAMEROON, (country, power) -> new CameroonCharacter());
        characterFactories.put(Character_.NIGERIA,  (country, power) -> new NigeriaCharacter());
        characterFactories.put(Character_.USA,      (country, power) -> new UsaCharacter());


        profiles.put(PLAYER_1, new SaveFile());
        profiles.put(PLAYER_2, new SaveFile());
    }

    private SaveFile profileOf(String name) throws DopoSoccerException {
        SaveFile profile = profiles.get(name);
        if (profile == null) {
            throw new DopoSoccerException(INVALID_PROFILE + name);
        }
        return profile;
    }

    // Configuración del partido

    public void preFixPlayers(Player player1, Player player2) {
        players.clear(); // Importante para limpiar jugadores de partidos anteriores
        players.put(player1.getName(), player1);
        players.put(player2.getName(), player2);
    }

    public void preFixCharacters(String p1CharacterName, String p2CharacterName) throws DopoSoccerException {
        if (players.size() < 2) {
            throw new DopoSoccerException(DopoSoccerException.PLAYERS_NOT_SET);
        }

        if (p1CharacterName == null || p2CharacterName == null) {
            throw new DopoSoccerException(NULL_CHARECTER_NAME);
        }

        CharacterFactory factory1 = characterFactories.get(p1CharacterName.trim().toUpperCase());
        if (factory1 == null) {
            throw new DopoSoccerException(ROASTER_NOT_IN_ROASTER + p1CharacterName);
        }

        CharacterFactory factory2 = characterFactories.get(p2CharacterName.trim().toUpperCase());
        if (factory2 == null) {
            throw new DopoSoccerException(ROASTER_NOT_IN_ROASTER + p2CharacterName);
        }

        Player p1 = players.get(PLAYER_1);
        Player p2 = players.containsKey(PLAYER_2) ? players.get(PLAYER_2) : players.get(CPU);

        checkCharacter(p1, p1CharacterName);
        checkCharacter(p2, p2CharacterName);

        p1.setCharacter(factory1.create(p1CharacterName, null));
        p2.setCharacter(factory2.create(p2CharacterName, null));
    }
    /**
     * Un humano solo puede usar los personajes desbloqueados en su perfil; la CPU, cualquiera válido.
     */
    private void checkCharacter(Player player, String characterName) throws DopoSoccerException {
        if (characterName == null || characterName.trim().isEmpty()) {
            throw new DopoSoccerException(DopoSoccerException.CHARACTER_REQUIRED);
        }
        if (player.isHuman()
                && !profileOf(player.getName()).getUnlockedCharecters().contains(characterName.trim().toUpperCase())) {
            throw new DopoSoccerException(DopoSoccerException.CHARACTER_LOCKED + characterName);
        }
    }

    public Match preFixMatch(String type, Field field, double limitValue) {
        if (players.size() < 2) {
            throw new IllegalStateException(CHARACTERS_NOT_SET );
        }

        MatchFactory factory = matchFactories.get(type.toUpperCase());
        if (factory == null) {
            throw new IllegalArgumentException(MATCH_TYPE_UNSUPPORTED + type);
        }

        // Para determinar en qué lado jugarán
        Player p1 = players.get(PLAYER_1);
        Player p2 = players.containsKey(PLAYER_2) ? players.get(PLAYER_2) : players.get(CPU);

        if (p1 == null || p2 == null) {
            throw new IllegalStateException(INCORRECT_TREEMAP_OBJECTS);
        }

        // Cada humano arranca con el progreso de su perfil
        applySaveFileProgress(p1);
        applySaveFileProgress(p2);

        this.currentMatch = factory.create(field, p1, p2, limitValue);
        this.resultApplied = false;
        return this.currentMatch;
    }

    /**
     * Transfiere puntos y mejoras del perfil al jugador. La CPU no tiene perfil.
     */
    private void applySaveFileProgress(Player player) {
        if (!player.isHuman()) {
            return;
        }
        SaveFile profile = profileOf(player.getName());
        player.setPoints(profile.getPoints());
        player.setSpeed(profile.getSpeed());
        player.setJump(profile.getJump());
        player.setKick(profile.getKick());
        player.setSurvival(profile.getSurvival());
    }

    /**
     * Cierra un partido terminado: suma los puntos y registra las estadísticas en el perfil de
     * cada jugador humano. Solo se aplica una vez por partido.
     * @return el mensaje con el ganador y los puntos obtenidos
     */
    public String finishMatch() throws DopoSoccerException {
        if (currentMatch == null) {
            throw new DopoSoccerException(DopoSoccerException.NO_MATCH);
        }
        InGameInfo info = currentMatch.getGameInfo();
        if (!info.isFinished()) {
            throw new DopoSoccerException(DopoSoccerException.MATCH_NOT_FINISHED);
        }
        if (resultApplied) {
            throw new DopoSoccerException(DopoSoccerException.RESULT_ALREADY_APPLIED);
        }

        Player[] sides = {currentMatch.getPlayer1(), currentMatch.getPlayer2()};
        int[] goals = {info.getPlayer1Goals(), info.getPlayer2Goals()};
        for (int i = 0; i < sides.length; i++) {
            Player player = sides[i];
            if (!player.isHuman()) {
                continue;
            }
            SaveFile profile = profileOf(player.getName());
            player.addPoints(info.getPointsFor(i + 1));
            profile.recordMatchStats(goals[i], goals[1 - i], info.getWinnerNumber() == i + 1);
            profile.syncFromPlayer(player);
        }
        resultApplied = true;

        return currentMatch.getResultMessage();
    }

    // Perfiles: cargar y guardar

    /**
     * Guarda en un solo archivo el progreso de ambos jugadores y, si se indica, el partido en curso.
     */
    public void saveGame(File file, MatchSnapshot matchInProgress) throws DopoSoccerException {
        new GameSave(profileOf(PLAYER_1), profileOf(PLAYER_2), matchInProgress).guardar(file);
    }


     /**
     * Abre un archivo de guardado: restaura el progreso de ambos jugadores y deja disponible
     * el partido que tuviera guardado.
     */
    public void loadGame(File file) throws DopoSoccerException {
        GameSave save = GameSave.abrir(file);
        profiles.put(PLAYER_1, save.getPlayer1Profile());
        profiles.put(PLAYER_2, save.getPlayer2Profile());
        this.savedMatch = save.getMatch();
    }

    public Match loadMatch(MatchSnapshot snapshot, Field field) throws DopoSoccerException {
        if (snapshot == null) {
            throw new DopoSoccerException(DopoSoccerException.NO_MATCH);
        }
        if (snapshot.getMatchType() == null) {
            throw new DopoSoccerException(DopoSoccerException.MATCH_TYPE_REQUIRED);
        }
        MatchFactory factory = matchFactories.get(snapshot.getMatchType().toUpperCase());
        if (factory == null) {
            throw new DopoSoccerException(DopoSoccerException.MATCH_TYPE_UNSUPPORTED + snapshot.getMatchType());
        }

        Player p1 = buildPlayer(PLAYER_1, snapshot.getPlayer1());
        Player p2 = buildPlayer(snapshot.getPlayer2().isHuman() ? PLAYER_2 : CPU, snapshot.getPlayer2());
        preFixPlayers(p1, p2);

        this.currentMatch = factory.create(field, p1, p2, snapshot.getLimitValue());
        snapshot.applyTo(this.currentMatch); // marcador, tiempo, vida, barras, posiciones y balón
        this.resultApplied = false;
        return this.currentMatch;
    }

    private Player buildPlayer(String name, MatchSnapshot.PlayerState state) throws DopoSoccerException {
        Player player = state.isHuman() ? new HumanPlayer(name) : Cpu.create(name, state.getCpuProfile());


        String country = state.getCountry();
        if (country == null) {
            throw new DopoSoccerException(NULL_CHARECTER_NAME);
        }
        CharacterFactory factory = characterFactories.get(country.trim().toUpperCase());
        if (factory == null) {
            throw new DopoSoccerException(ROASTER_NOT_IN_ROASTER + country);
        }

        state.applyProfileTo(player, factory); // estadísticas y personaje, antes de crear el Match
        return player;
    }

    /**
     * Guarda el perfil del jugador indicado (puntos, estadísticas y personajes) en el archivo.
     */
    public void saveProgress(String profileName, File file) throws DopoSoccerException {
        profileOf(profileName).guardar(file);
    }


    public boolean hasSavedMatch() { return savedMatch != null; }
    public MatchSnapshot getSavedMatch() { return savedMatch; }

    // Tienda

    public int getPoints(String profileName) throws DopoSoccerException {
        return profileOf(profileName).getPoints();
    }

    /**
     * @param stat SPEED, JUMP, KICK o SURVIVAL
     */
    public int getStatLevel(String profileName, String stat) throws DopoSoccerException {
        ToIntFunction<SaveFile> getter = STAT_LEVELS.get(stat == null ? null : stat.trim().toUpperCase());
        if (getter == null) {
            throw new DopoSoccerException(INVALID_STAT+ stat);
        }
        return getter.applyAsInt(profileOf(profileName));
    }

    /**
     * @return el costo de subir la estadística un nivel, o -1 si ya está en el máximo
     */
    public int getUpgradeCost(String profileName, String stat) throws DopoSoccerException {
        int level = getStatLevel(profileName, stat);
        if (level >= Player.MAX_STAT_LEVEL) {
            return -1;
        }
        return new HumanPlayer(profileName).getUpgradeCost(level);
    }

    /**
     * Compra una mejora: reutiliza Player.upgrade (costo, máximo y mensajes de error)
     * sobre un jugador temporal y devuelve el resultado al perfil.
     */
    public void upgradeStat(String profileName, String stat) throws DopoSoccerException {
        SaveFile profile = profileOf(profileName);
        Player temp = new HumanPlayer(profileName);
        applySaveFileProgress(temp);
        temp.upgrade(stat);
        profile.syncFromPlayer(temp);
    }


}
package domain;

import domain.character.Character_;
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

public class DopoSoccer {

    // Claves de los jugadores: son el nombre del Player y la clave de su perfil
    public static final String PLAYER_1 = "Player 1";
    public static final String PLAYER_2 = "Player 2";
    public static final String CPU = "CPU";

    private static final Map<String, ToIntFunction<SaveFile>> STAT_LEVELS = new HashMap<>();

    static {
        STAT_LEVELS.put("SPEED", SaveFile::getSpeed);
        STAT_LEVELS.put("JUMP", SaveFile::getJump);
        STAT_LEVELS.put("KICK", SaveFile::getKick);
        STAT_LEVELS.put("SURVIVAL", SaveFile::getSurvival);
    }

    private Match currentMatch;
    private final TreeMap<String, Player> players = new TreeMap<>();
    private final Map<String, SaveFile> profiles = new HashMap<>();   // un perfil por jugador humano
    private final Set<String> loadedProfiles = new HashSet<>();       // perfiles cargados desde archivo
    private final Map<String, MatchFactory> matchFactories = new HashMap<>();
    private boolean resultApplied; // evita sumar dos veces los puntos de un mismo partido

    public DopoSoccer() {
        this.currentMatch = null;

        matchFactories.put("TIME",  (field, p1, p2, val) -> new TimeMatch(field, p1, p2, val));
        matchFactories.put("GOALS", (field, p1, p2, val) -> new GoalMatch(field, p1, p2, (int) val));

        profiles.put(PLAYER_1, new SaveFile());
        profiles.put(PLAYER_2, new SaveFile());
    }

    private SaveFile profileOf(String name) throws DopoSoccerException {
        SaveFile profile = profiles.get(name);
        if (profile == null) {
            throw new DopoSoccerException("Perfil no válido: " + name);
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
        Player p1 = players.get(PLAYER_1);
        Player p2 = players.containsKey(PLAYER_2) ? players.get(PLAYER_2) : players.get(CPU);

        checkCharacter(p1, p1CharacterName);
        checkCharacter(p2, p2CharacterName);

        // Usamos el Factory Method de Character_
        p1.setCharacter(Character_.create(p1CharacterName));
        p2.setCharacter(Character_.create(p2CharacterName));
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
            throw new IllegalStateException("Debes configurar los jugadores con preFixPlayers antes de iniciar el partido.");
        }

        MatchFactory factory = matchFactories.get(type.toUpperCase());
        if (factory == null) {
            throw new IllegalArgumentException("Tipo de partido no soportado: " + type);
        }

        // Para determinar en qué lado jugarán
        Player p1 = players.get(PLAYER_1);
        Player p2 = players.containsKey(PLAYER_2) ? players.get(PLAYER_2) : players.get(CPU);

        if (p1 == null || p2 == null) {
            throw new IllegalStateException("No se encontraron los roles esperados (Player 1 y Player 2/CPU) en el Treemap.");
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
     * Abre un archivo de progreso y lo establece como el perfil del jugador indicado.
     */
    public void loadSave(String profileName, File file) throws DopoSoccerException {
        profileOf(profileName); // valida el nombre
        SaveFile loaded = SaveFile.abrir(file);
        if (loaded == null || !loaded.getStatus()) {
            throw new DopoSoccerException("El archivo de progreso no es válido.");
        }
        profiles.put(profileName, loaded);
        loadedProfiles.add(profileName);
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

    private Player buildPlayer(String name, MatchSnapshot.PlayerState state) {
        Player player = state.isHuman() ? new HumanPlayer(name) : new Cpu(name, state.getCpuProfile());
        state.applyProfileTo(player); // estadísticas y personaje, antes de crear el Match
        return player;
    }

    /**
     * Guarda el perfil del jugador indicado (puntos, estadísticas y personajes) en el archivo.
     */
    public void saveProgress(String profileName, File file) throws DopoSoccerException {
        profileOf(profileName).guardar(file);
    }

    public boolean isSaveLoaded(String profileName) { return loadedProfiles.contains(profileName); }

    public boolean isAnySaveLoaded() { return !loadedProfiles.isEmpty(); }

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
            throw new DopoSoccerException("Estadística no válida: " + stat);
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
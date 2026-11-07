package domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Facade of the domain. The GUI talks to this class. */
public class DopoSoccer {
    private Map<String, Function<Player, Character>> catalog = new LinkedHashMap<>();
    private Player[] players = new Player[2];
    private Match match;

    public DopoSoccer() {
        catalog.put("SOUTH_KOREA", SouthKoreaCharacter::new);
        catalog.put("CAMEROON", CameroonCharacter::new);
        catalog.put("NIGERIA", NigeriaCharacter::new);
        catalog.put("USA", USACharacter::new);
    }

    public void setHuman(int slot) throws DopoSoccerException {
        checkSlot(slot);
        Human h = new Human(slot);
        for (String id : catalog.keySet()) h.unlock(id);   // no persistence yet: everything unlocked
        players[slot] = h;
    }

    public void setCPU(int slot, String profile) throws DopoSoccerException {
        throw new DopoSoccerException(DopoSoccerException.NOT_IMPLEMENTED + ": CPU players (version 3)");
    }

    public void choose(int slot, String characterId) throws DopoSoccerException {
        checkSlot(slot);
        if (players[slot] == null) throw new DopoSoccerException(DopoSoccerException.PLAYER_NOT_SET + ": slot " + slot);
        if (!catalog.containsKey(characterId))
            throw new DopoSoccerException(DopoSoccerException.UNKNOWN_CHARACTER + ": " + characterId);
        players[slot].chooseCharacter(characterId);
    }

    public void createTimeMatch(int minutes) throws DopoSoccerException {
        requireReady();
        match = new TimeMatch(players[0], players[1], new Field(), catalog, minutes);
    }

    public void createGoalsMatch(int targetGoals) throws DopoSoccerException {
        requireReady();
        match = new GoalsMatch(players[0], players[1], new Field(), catalog, targetGoals);
    }

    public void save(String path) throws DopoSoccerException {
        throw new DopoSoccerException(DopoSoccerException.NOT_IMPLEMENTED + ": save (version 2)");
    }

    public void open(String path) throws DopoSoccerException {
        throw new DopoSoccerException(DopoSoccerException.NOT_IMPLEMENTED + ": open (version 2)");
    }

    private void checkSlot(int slot) throws DopoSoccerException {
        if (slot < 0 || slot > 1) throw new DopoSoccerException(DopoSoccerException.INVALID_PARAMETER + ": slot " + slot);
    }

    private void requireReady() throws DopoSoccerException {
        for (int i = 0; i < 2; i++)
            if (players[i] == null) throw new DopoSoccerException(DopoSoccerException.PLAYER_NOT_SET + ": slot " + i);
    }

    public Match getMatch() { return match; }
    public Player getPlayer(int slot) { return players[slot]; }
    public List<String> getCatalogIds() { return new ArrayList<>(catalog.keySet()); }
}

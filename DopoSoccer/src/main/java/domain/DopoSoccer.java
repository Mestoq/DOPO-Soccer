package domain;

import domain.character.Character_;
import domain.match.*;
import domain.physics.Field;
import domain.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class DopoSoccer{
   private Match currentMatch;
   private TreeMap<String, Player> players = new TreeMap<>();
   private SaveFile currentSaveFile;
   private ArrayList <String> characterRoster = new ArrayList<>();
   private final Map<String, MatchFactory> matchFactories = new HashMap<>();

    public DopoSoccer(SaveFile loadedFile) {
        this.currentMatch = null;

        matchFactories.put("TIME",  (field, p1, p2, val) -> new TimeMatch(field, p1, p2, val));
        matchFactories.put("GOALS", (field, p1, p2, val) -> new GoalMatch(field, p1, p2, (int) val));

        if (loadedFile == null || !loadedFile.getStatus()) {
            this.currentSaveFile = new SaveFile();
            this.characterRoster = this.currentSaveFile.getUnlockedCharecters();
        } else {
            this.currentSaveFile = loadedFile;
            this.characterRoster = loadedFile.getUnlockedCharecters();
        }
    }
    public void preFixPlayers(Player player1, Player player2) {
        players.clear(); // Importante para  limpiar jugadores de partidos anteriores
        players.put(player1.getName(), player1);
        players.put(player2.getName(), player2);
    }

    public void preFixCharacters(String p1CharacterName, String p2CharacterName) throws DopoSoccerException {
        if (players.size() < 2) {
            throw new DopoSoccerException("Primero se deben configurar los jugadores en preFixPlayers.");
        }
        if (p1CharacterName == null || !characterRoster.contains(p1CharacterName.trim().toUpperCase())) {
            throw new DopoSoccerException("El personaje " + p1CharacterName + " no está desbloqueado.");
        }
        if (p2CharacterName == null || p2CharacterName.trim().isEmpty()) {
            throw new DopoSoccerException("Debe seleccionar un personaje válido para el rival.");
        }

        Player p1 = players.get("Player 1");
        Player p2 = players.containsKey("Player 2") ? players.get("Player 2") : players.get("CPU");

        // Usamos el Factory Method de Character_
        Character_ character1 = Character_.create(p1CharacterName);
        Character_ character2 = Character_.create(p2CharacterName);

        // Aplicamos las mejoras de progreso guardadas en currentSaveFile sobre los jugadores
        applySaveFileProgress(p1);
        if (players.containsKey("Player 2")) {
            applySaveFileProgress(p2);
        }

        p1.setCharacter(character1);
        p2.setCharacter(character2);
    }



    public Match preFixMatch(String type, Field field, double limitValue) {
        if (players.size() < 2) {
            throw new IllegalStateException("Debes configurar los jugadores con preFixPlayers antes de iniciar el partido.");
        }

        MatchFactory factory = matchFactories.get(type.toUpperCase());
        if (factory == null) {
            throw new IllegalArgumentException("Tipo de partido no soportado: " + type);
        }

       //Para determinar en que lado jugaran
        Player p1 = players.get("Player 1");
        Player p2 = players.containsKey("Player 2") ? players.get("Player 2") : players.get("CPU");

        if (p1 == null || p2 == null) {
            throw new IllegalStateException("No se encontraron los roles esperados (Player 1 y Player 2/CPU) en el Treemap.");
        }

        // Aplicamos el progreso guardado en SaveFile antes de arrancar
        applySaveFileProgress(p1);

        this.currentMatch = factory.create(field, p1, p2, limitValue);
        return this.currentMatch;
    }

    private void applySaveFileProgress(Player character) {
        // Aquí transferiremos las mejoras de currentSaveFile hacia player / su Character
    }


}
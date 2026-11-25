package domain.match;

import domain.DopoSoccerException;
import domain.player.Player;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import static domain.DopoSoccerException.*;

public class SaveFile implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean valid;
    private int points;

    // Niveles de mejora persistentes (1 a 10)
    private int speed;
    private int jump;
    private int kick;
    private int survival;

    // Estadísticas acumuladas
    private int matchesPlayed;
    private int matchesWon;
    private int goalsScored;
    private int goalsConceded;

    // Personajes desbloqueados
    private final ArrayList<String> unlockedCharacters;

    /**
     * Crea un perfil nuevo por defecto (niveles en 1, 0 puntos y personajes base).
     */
    public SaveFile() {
        this.valid = true;
        this.points = 0;
        this.speed = 1;
        this.jump = 1;
        this.kick = 1;
        this.survival = 1;

        this.matchesPlayed = 0;
        this.matchesWon = 0;
        this.goalsScored = 0;
        this.goalsConceded = 0;

        this.unlockedCharacters = new ArrayList<>(Arrays.asList(
                "KOREA",
                "CAMEROON",
                "NIGERIA",
                "USA"
        ));
    }

    // guardar y abrir en cifrado

    public void guardar(File file) throws DopoSoccerException {
        if (file == null) {
            throw new DopoSoccerException(NULL_SAVEFILE);
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(this);
        } catch (IOException e) {

            throw new DopoSoccerException(ERROR_SAVING + e.getMessage(), e);
        }
    }

    public static SaveFile abrir(File file) throws DopoSoccerException {
        if (file == null || !file.exists()) {
            throw new DopoSoccerException(NULL_SAVEFILE);
        }
        try (FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis)){
            return (SaveFile) ois.readObject();
        } catch (ClassCastException e) {
            throw new DopoSoccerException(INVALID_SAVEFILE_MATCH, e);
        } catch (IOException | ClassNotFoundException e) {
            throw new DopoSoccerException(ERROR_IMPORT + e.getMessage());
        }
    }

    // exportar texto plano

    public void exportAs(File file) throws DopoSoccerException {
        if (file == null) {
            throw new DopoSoccerException(NULL_EXPORT);
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("points=" + points); writer.newLine();
            writer.write("speed=" + speed); writer.newLine();
            writer.write("jump=" + jump); writer.newLine();
            writer.write("kick=" + kick); writer.newLine();
            writer.write("survival=" + survival); writer.newLine();
            writer.write("matchesPlayed=" + matchesPlayed); writer.newLine();
            writer.write("matchesWon=" + matchesWon); writer.newLine();
            writer.write("goalsScored=" + goalsScored); writer.newLine();
            writer.write("goalsConceded=" + goalsConceded); writer.newLine();
            writer.write("characters=" + String.join(",", unlockedCharacters)); writer.newLine();
        } catch (IOException e) {
            throw new DopoSoccerException(ERROR_EXPORT+ e.getMessage(), e);
        }
    }

    public static SaveFile importar(File file) throws DopoSoccerException {
        if (file == null || !file.exists()) {
            throw new DopoSoccerException(NULL_IMPORT);
        }
        SaveFile loaded = new SaveFile();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.contains("=")) {
                    continue;
                }
                String[] parts = line.split("=", 2);
                String key = parts[0].trim();
                String value = parts[1].trim();

                switch (key) {
                    case "points": loaded.points = Math.max(0, Integer.parseInt(value)); break;
                    case "speed": loaded.speed = clampStat(Integer.parseInt(value)); break;
                    case "jump": loaded.jump = clampStat(Integer.parseInt(value)); break;
                    case "kick":loaded.kick = clampStat(Integer.parseInt(value)); break;
                    case "survival":loaded.survival = clampStat(Integer.parseInt(value)); break;
                    case "matchesPlayed": loaded.matchesPlayed = Math.max(0, Integer.parseInt(value)); break;
                    case "matchesWon": loaded.matchesWon = Math.max(0, Integer.parseInt(value)); break;
                    case "goalsScored": loaded.goalsScored = Math.max(0, Integer.parseInt(value)); break;
                    case "goalsConceded": loaded.goalsConceded = Math.max(0, Integer.parseInt(value)); break;
                    case "characters":
                        loaded.unlockedCharacters.clear();
                        if (!value.isEmpty()) {
                            for (String c : value.split(",")) {
                                loaded.unlockedCharacters.add(c.trim().toUpperCase());
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
            return loaded;
        } catch (IOException | NumberFormatException e) {
            throw new DopoSoccerException(INVALID_SAVEFILE + e.getMessage(), e);
        }
    }

    private static int clampStat(int value) {
        return Math.max(1, Math.min(Player.MAX_STAT_LEVEL, value));
    }

    // actualizar puntos y stats

    public void addPoints(int earnedPoints) {
        if (earnedPoints > 0) {
            this.points += earnedPoints;
        }
    }

    public void recordMatchStats(int scored, int conceded, boolean won) {
        this.matchesPlayed++;
        if (won) {
            this.matchesWon++;
        }
        this.goalsScored += Math.max(0, scored);
        this.goalsConceded += Math.max(0, conceded);
    }

    public void unlockCharacter(String countryName) {
        if (countryName != null) {
            String normalized = countryName.trim().toUpperCase();
            if (!this.unlockedCharacters.contains(normalized)) {
                this.unlockedCharacters.add(normalized);
            }
        }
    }

    /**
     * Sincroniza los niveles y puntos actuales del Player de vuelta al SaveFile tras mejorar estadísticas.
     */
    public void syncFromPlayer(Player player) {
        if (player == null) {
            return;
        }
        this.points = player.getPoints();
        this.speed = player.getSpeed();
        this.jump = player.getJump();
        this.kick = player.getKick();
        this.survival = player.getSurvival();
    }

    // getters

    public boolean getStatus() { return valid; }
    public ArrayList<String> getUnlockedCharecters() { return new ArrayList<>(unlockedCharacters); }

    public int getPoints() { return points; }
    public int getSpeed() { return speed; }
    public int getJump() { return jump; }
    public int getKick() { return kick; }
    public int getSurvival() { return survival; }

    public int getMatchesPlayed() { return matchesPlayed; }
    public int getMatchesWon() { return matchesWon; }
    public int getGoalsScored() { return goalsScored; }
    public int getGoalsConceded() { return goalsConceded; }
}
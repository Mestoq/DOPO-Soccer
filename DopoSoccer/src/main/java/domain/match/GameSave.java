package domain.match;

import domain.DopoSoccerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static domain.DopoSoccerException.*;

/**
 * Archivo único de guardado: el progreso de los dos jugadores y,
 * si lo hay, el partido que estaba en curso.
 */
public class GameSave implements Serializable {

    private static final long serialVersionUID = 1L;

    private final SaveFile player1Profile;
    private final SaveFile player2Profile;
    private final MatchSnapshot match; // null si solo se guardó el progreso

    public GameSave(SaveFile player1Profile, SaveFile player2Profile, MatchSnapshot match) {
        this.player1Profile = player1Profile;
        this.player2Profile = player2Profile;
        this.match = match;
    }

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

    public static GameSave abrir(File file) throws DopoSoccerException {
        if (file == null || !file.exists()) {
            throw new DopoSoccerException(NULL_SAVEFILE);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            GameSave save = (GameSave) ois.readObject();
            if (save.player1Profile == null || save.player2Profile == null
                    || !save.player1Profile.getStatus() || !save.player2Profile.getStatus()) {
                throw new DopoSoccerException(INVALID_SAVEFILE);
            }
            return save;
        } catch (ClassCastException e) {
            throw new DopoSoccerException(INVALID_SAVEFILE, e);
        } catch (IOException | ClassNotFoundException e) {
            throw new DopoSoccerException(ERROR_OPENING + e.getMessage(), e);
        }
    }

    public boolean hasMatch() { return match != null; }
    public SaveFile getPlayer1Profile() { return player1Profile; }
    public SaveFile getPlayer2Profile() { return player2Profile; }
    public MatchSnapshot getMatch() { return match; }
}
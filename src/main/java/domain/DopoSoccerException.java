package domain;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/** Game-specific exception. Every instance is written to the programmer error log. */
public class DopoSoccerException extends Exception {
    public static final String CHARACTER_LOCKED = "The selected character is locked for this player";
    public static final String UNKNOWN_CHARACTER = "The selected character does not exist in the catalog";
    public static final String NOT_IMPLEMENTED = "This feature is not implemented yet";
    public static final String PLAYER_NOT_SET = "The player has not been configured";
    public static final String INVALID_PARAMETER = "Invalid parameter";

    private static final Logger LOGGER = Logger.getLogger("DopoSoccer");
    static {
        try {
            FileHandler handler = new FileHandler("doposoccer-errors.log", true);
            handler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(handler);
        } catch (IOException | SecurityException e) {
            LOGGER.log(Level.WARNING, "Could not open the error log file", e);
        }
    }

    public DopoSoccerException(String message) {
        super(message);
        LOGGER.log(Level.SEVERE, message);
    }

    public DopoSoccerException(String message, Throwable cause) {
        super(message, cause);
        LOGGER.log(Level.SEVERE, message, cause);
    }
}

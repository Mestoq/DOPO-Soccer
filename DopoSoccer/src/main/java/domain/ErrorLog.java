package domain;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public final class ErrorLog {

    public static final String LOG_FILE = "dopo-errors.log";
    private static final Logger LOGGER = Logger.getLogger("DopoSoccer");

    static {
        try {
            FileHandler handler = new FileHandler(LOG_FILE, true); // el true es para agregar no sobreescribir
            handler.setFormatter(new SimpleFormatter());           // por defecto FileHandler usa XML
            LOGGER.addHandler(handler);
        } catch (IOException | SecurityException e) {
            LOGGER.log(Level.WARNING, "No se pudo abrir " + LOG_FILE + "; se registra solo en consola.", e);
        }
    }

    private ErrorLog() { }

    public static void error(String message, Throwable cause) {
        LOGGER.log(Level.SEVERE, message, cause);
    }

    public static void warn(String message) {
        LOGGER.log(Level.WARNING, message);
    }
}
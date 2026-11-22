package presentation;

import domain.ErrorLog;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Carga la fuente Press Start 2P una sola vez y la entrega al tamaño pedido.
 */
public final class FontLoader {

    static final String FONT_PATH = "/fonts/PressStart2P.ttf";
    private static final Font BASE_FONT = loadBaseFont();

    private FontLoader() { }

    private static Font loadBaseFont() {
        try (InputStream stream = FontLoader.class.getResourceAsStream(FONT_PATH)) {
            if (stream == null) {
                ErrorLog.warn("Fuente no encontrada: " + FONT_PATH + "; se usará Arial.");
                return new Font("Arial", Font.BOLD, 12);
            }
            return Font.createFont(Font.TRUETYPE_FONT, stream);
        } catch (FontFormatException | IOException e) {
            ErrorLog.error("No se pudo cargar la fuente: " + FONT_PATH, e);
            return new Font("Arial", Font.BOLD, 12);
        }
    }

    /**
     * @param size tamaño en puntos
     * @return la fuente Press Start 2P (o Arial Bold de respaldo) al tamaño indicado
     */
    public static Font load(float size) {
        return BASE_FONT.deriveFont(size);
    }
}
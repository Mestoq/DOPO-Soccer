package presentation;

/**
 * Sonidos de efectos  y música de fondo.
 */
public interface AudioPlay {

    /**
     * Reproduce un efecto .wav una sola vez
     */
    void playSound(String soundPath);

    /**
     * Reproduce una pista .wav en bucle como sonido de fondo.
     * Si ya está sonando esa misma pista no la reinicia; si es otra, reemplaza la anterior.
     */
    void playBackground(String musicPath);

    /** Detiene la música de fondo actual, si existe. */
    void stopBackground();
}
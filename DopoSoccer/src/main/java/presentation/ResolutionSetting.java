package presentation;

/**
 * Contrato para que los paneles informen la resolución de la cancha elegida.
 */
public interface ResolutionSetting {

    /**
     * Define el tamaño de la ventana cuando se muestre el GamePanel.
     */
    void setGameResolution(int width, int height);
}
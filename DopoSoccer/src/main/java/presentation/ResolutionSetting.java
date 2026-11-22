package presentation;

import java.awt.Dimension;

/**
 * Contrato para informar y consultar la resolución de la cancha elegida.
 */
public interface ResolutionSetting {

    /** Define el tamaño de la ventana cuando se muestre el GamePanel. */
    void setGameResolution(int width, int height);

    /** @return la resolución de cancha configurada actualmente */
    Dimension getGameResolution();
}
package presentation;

/**
 * Cada botón del panel recibe esta interfaz y la usa para avanzar o volver.
 */
public interface PanelRotation {

    /** Muestra el siguiente panel del flujo (no hace nada si ya es el último). */
    void next();

    /** Muestra el panel anterior del flujo (no hace nada si ya es el primero). */
    void back();

    /**
     * Salta directamente a un panel registrado (ej. cargar partida -> GamePanel).
     * @param panelName nombre con el que se registró el panel
     */
    void goTo(String panelName);
}
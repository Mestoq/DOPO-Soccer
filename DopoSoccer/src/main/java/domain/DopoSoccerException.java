package domain;

public class DopoSoccerException extends RuntimeException {

    public static final String ROASTER_NOT_IN_ROASTER = "Personaje no existe: ";
    public static final String PLAYERS_NOT_SET = "Primero se deben configurar los jugadores.";
    public static final String CHARACTERS_NOT_SET = "Primero se deben asignar los personajes.";
    public static final String CHARACTER_LOCKED = "El personaje no está desbloqueado: ";
    public static final String CHARACTER_REQUIRED = "Debe seleccionar un personaje válido para el rival.";
    public static final String MATCH_TYPE_REQUIRED = "Debe indicar el tipo de partido (TIME o GOALS).";
    public static final String MATCH_TYPE_UNSUPPORTED = "Tipo de partido no soportado: ";
    public static final String CHARACTERS_NOT_ASSIGNED ="el jugador que no tiene personaje asignado: ";
    public static final String NULL_CHARECTER_NAME ="el jugador que no tiene personaje asignado: ";
    public static final String NULL_CHARECTER_UPGRADE ="tipo de estadística a mejorar no puede ser nulo.";
    public static final String NO_MATCH = "No hay un partido en curso.";
    public static final String NULL_MATCH = "No hay un partido para guardar.";
    public static final String NULL_SAVEFILE = "El archivo no puede estar vacio.";
    public static final String NULL_IMPORT = "El archivo importado no existe.";
    public static final String NULL_EXPORT = "El archivo a exportar no puede estar vacio.";
    public static final String ERROR_SAVING = "Error al guardar la partida: ";
    public static final String ERROR_IMPORT = "Error al importar datos de partido: ";
    public static final String ERROR_EXPORT = "Error al importar datos de partido: ";
    public static final String ERROR_OPENING = "Error al importar datos de partido: ";
    public static final String MATCH_NOT_FINISHED = "El partido aún no ha terminado.";
    public static final String RESULT_ALREADY_APPLIED = "El resultado de este partido ya fue registrado.";
    public static final String INVALID_PROFILE_DEFAULT_CLUMSY = "Perfil de CPU no válido. Se usará TORPE por defecto. Perfil recibido: ";
    public static final String INVALID_STAT= "las estadisticas no son validas: ";
    public static final String INVALID_SAVEFILE= "El archivo de progreso no es valido ";
    public static final String INVALID_SAVEFILE_MATCH= "El archivo del partido no es valido ";
    public static final String INVALID_PROFILE= "El perfil no es valido: ";
    public static final String INCORRECT_TREEMAP_OBJECTS ="No se encontraron los roles esperados (Player 1 y Player 2/CPU) en el Treemap.";

    public DopoSoccerException(String message) {
        super(message);
        ErrorLog.error(message, null);
    }


    public DopoSoccerException(String message, Throwable cause) {
        super(message, cause);
        ErrorLog.error(message, cause);
    }
}
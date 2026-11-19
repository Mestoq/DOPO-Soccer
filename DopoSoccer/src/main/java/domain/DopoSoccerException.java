package domain;

public class DopoSoccerException extends RuntimeException {

    public static final String ROASTERNOTUNLOCKED = "Personaje bloqueado, intentar con otro";
    public static final String PLAYERS_NOT_SET = "Primero se deben configurar los jugadores en preFixPlayers.";
    public static final String CHARACTERS_NOT_SET = "Primero se deben asignar los personajes en preFixCharacters.";
    public static final String CHARACTER_LOCKED = "El personaje no está desbloqueado: ";
    public static final String CHARACTER_REQUIRED = "Debe seleccionar un personaje válido para el rival.";
    public static final String MATCH_TYPE_REQUIRED = "Debe indicar el tipo de partido (TIME o GOALS).";
    public static final String MATCH_TYPE_UNSUPPORTED = "Tipo de partido no soportado: ";
    public static final String FIELD_REQUIRED = "Debe indicar la cancha del partido.";
    public static final String MATCH_LIMIT_INVALID = "El límite del partido debe ser mayor que cero.";
    public static final String GOALS_LIMIT_INVALID = "El número de goles debe ser un entero positivo.";
    public static final String NO_MATCH = "No hay un partido en curso.";
    public static final String MATCH_NOT_FINISHED = "El partido aún no ha terminado.";
    public static final String RESULT_ALREADY_APPLIED = "El resultado de este partido ya fue registrado.";
    public static final String INVALID_PROFILE_DEFAULT_CLUMSY = "Perfil de CPU no válido. Se usará TORPE por defecto. Perfil recibido: ";

    public DopoSoccerException(String message) {
        super(message);
        ErrorLog.error(message, null);
    }


    public DopoSoccerException(String message, Throwable cause) {
        super(message, cause);
        ErrorLog.error(message, cause);
    }
}
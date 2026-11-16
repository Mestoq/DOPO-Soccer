package domain;

public class DopoSoccerException extends RuntimeException {
    public static final String ROASTERNOTUNLOCKED= "Personaje bloqueado, intentar con otro";
    public DopoSoccerException(String message) {
        super(message);
    }
}

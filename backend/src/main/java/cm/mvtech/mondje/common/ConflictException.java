package cm.mvtech.mondje.common;

/**
 * Signale un conflit avec l'état actuel des données, par exemple un doublon (HTTP 409).
 */
public class ConflictException extends RuntimeException {

    /**
     * Crée l'exception avec un message lisible.
     *
     * @param message le message décrivant le conflit
     */
    public ConflictException(String message) {
        super(message);
    }
}
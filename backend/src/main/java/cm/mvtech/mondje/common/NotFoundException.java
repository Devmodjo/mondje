package cm.mvtech.mondje.common;

/**
 * Signale qu'une ressource demandée n'existe pas (HTTP 404).
 */
public class NotFoundException extends RuntimeException {

    /**
     * Crée l'exception avec un message lisible.
     *
     * @param message le message décrivant la ressource introuvable
     */
    public NotFoundException(String message) {
        super(message);
    }
}
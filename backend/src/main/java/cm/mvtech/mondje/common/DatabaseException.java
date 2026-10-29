package cm.mvtech.mondje.common;


/**
 * Enveloppe non contrôlée d'une erreur SQL.
 * Le détail technique reste dans les logs et n'est jamais renvoyé au client.
 */
public class DatabaseException extends RuntimeException{

    /**
     * Crée l'exception avec un message et la cause SQL d'origine.
     *
     * @param message le message décrivant l'opération qui a échoué
     * @param cause   l'erreur SQL d'origine
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}

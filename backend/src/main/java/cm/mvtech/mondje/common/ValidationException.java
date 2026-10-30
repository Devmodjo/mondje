package cm.mvtech.mondje.common;

import java.util.List;

/**
 * Signale qu'une donnée ne respecte pas les règles métier.
 * Elle est traduite en réponse HTTP 422 avec la liste des raisons du rejet.
 */
public class ValidationException extends RuntimeException {

    private final List<String> reasons;

    /**
     * Crée l'exception avec plusieurs raisons de rejet.
     *
     * @param reasons les raisons lisibles du rejet
     */
    public ValidationException(List<String> reasons) {
        super(String.join(" ; ", reasons));
        this.reasons = List.copyOf(reasons);
    }

    /**
     * Crée l'exception avec une seule raison de rejet.
     *
     * @param reason la raison lisible du rejet
     */
    public ValidationException(String reason) {
        this(List.of(reason));
    }

    /**
     * Retourne les raisons du rejet.
     *
     * @return la liste non modifiable des raisons
     */
    public List<String> getReasons() {
        return reasons;
    }
}
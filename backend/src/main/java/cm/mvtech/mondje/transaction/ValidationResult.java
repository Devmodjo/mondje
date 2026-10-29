package cm.mvtech.mondje.transaction;

import java.util.List;

/**
 * Résultat d'une validation : une transaction valide, ou la liste des raisons du rejet.
 *
 * @param transaction la transaction validée, {@code null} s'il y a des raisons
 * @param reasons     les raisons du rejet, vide si tout est valide
 */
public record ValidationResult(ValidatedTransaction transaction, List<String> reasons) {

    /**
     * Indique si la validation a réussi.
     *
     * @return vrai si aucune raison de rejet n'a été relevée
     */
    public boolean isValid() {
        return reasons.isEmpty();
    }
}
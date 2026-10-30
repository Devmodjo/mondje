package cm.mvtech.mondje.importing;

import cm.mvtech.mondje.transaction.ValidatedTransaction;

/**
 * Ligne valide prête à être insérée, avec sa position dans le fichier d'origine.
 *
 * @param transaction la transaction validée
 * @param sourceLine  le numéro de la ligne dans le fichier, à partir de 1
 */
public record RowToInsert(ValidatedTransaction transaction, long sourceLine) {
}
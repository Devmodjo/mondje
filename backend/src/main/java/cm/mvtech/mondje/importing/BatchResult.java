package cm.mvtech.mondje.importing;

/**
 * Bilan de l'écriture d'un lot.
 *
 * @param inserted   le nombre de lignes insérées
 * @param duplicates le nombre de lignes valides ignorées car l'UUID existait déjà
 * @param errors     le nombre de lignes rejetées enregistrées
 */
public record BatchResult(int inserted, int duplicates, int errors) {
}
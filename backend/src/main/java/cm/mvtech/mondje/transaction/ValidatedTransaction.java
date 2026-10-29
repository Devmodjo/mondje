package cm.mvtech.mondje.transaction;

import java.util.UUID;

/**
 * Transaction dont tous les champs ont été vérifiés.
 *
 * @param id          l'UUID valide
 * @param type        le sens de la transaction
 * @param amount      le montant entier strictement positif
 * @param description la description non vide, 255 caractères maximum
 */
public record ValidatedTransaction(UUID id, TransactionType type, long amount, String description) {
}
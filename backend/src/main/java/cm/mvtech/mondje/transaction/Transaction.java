package cm.mvtech.mondje.transaction;

import java.time.Instant;
import java.util.UUID;

/**
 * Transaction telle qu'elle est stockée et renvoyée par l'API.
 *
 * @param id               l'identifiant unique
 * @param type             le sens de la transaction
 * @param amount           le montant entier strictement positif, en FCFA
 * @param description      la description, 255 caractères maximum
 * @param createdAt        la date de création fixée par le serveur
 * @param importId         l'import d'origine, {@code null} pour une création manuelle
 * @param sourceLine       la ligne d'origine dans le fichier, {@code null} si manuelle
 * @param modifiedManually vrai si la transaction a été corrigée à la main
 * @param deletedAt        la date de suppression douce, {@code null} si active
 */
public record Transaction(
        UUID id,
        TransactionType type,
        long amount,
        String description,
        Instant createdAt,
        UUID importId,
        Long sourceLine,
        boolean modifiedManually,
        Instant deletedAt) {
}
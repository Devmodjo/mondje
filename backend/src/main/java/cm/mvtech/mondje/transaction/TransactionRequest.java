package cm.mvtech.mondje.transaction;

/**
 * Corps JSON d'une création de transaction.
 * Les champs sont volontairement bruts pour que la validation produise une raison précise
 * plutôt qu'une erreur de lecture du JSON.
 *
 * @param id          l'UUID au format texte
 * @param type        {@code CREDIT} ou {@code DEBIT}
 * @param amount      le montant, attendu comme un entier
 * @param description la description, 255 caractères maximum
 */
public record TransactionRequest(String id, String type, Object amount, String description) {
}
package cm.mvtech.mondje.transaction;

import java.util.regex.Pattern;

/**
 * Règles de validation d'une transaction.
 * Cette classe ne dépend ni de Spring ni de la base de données.
 */
public final class TransactionValidator {

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private TransactionValidator() {
    }

    /**
     * Indique si un texte est un UUID valide, en majuscules ou en minuscules.
     *
     * @param text le texte à vérifier
     * @return vrai si le texte respecte le format 8-4-4-4-12 en hexadécimal
     */
    public static boolean isValidUuid(String text) {
        return text != null && UUID_PATTERN.matcher(text.trim()).matches();
    }
}
package cm.mvtech.mondje.transaction;

import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    /**
     * Valide les quatre champs d'une transaction et relève toutes les erreurs d'un coup.
     *
     * @param id          l'UUID au format texte
     * @param type        {@code CREDIT} ou {@code DEBIT}
     * @param amount      le montant au format texte
     * @param description la description
     * @return la transaction validée, ou la liste complète des raisons de rejet
     */
    public static ValidationResult validate(String id, String type, String amount, String description) {
        List<String> reasons = new ArrayList<>();

        if (!isValidUuid(id)) {
            reasons.add("UUID invalide : format attendu 8-4-4-4-12 caractères hexadécimaux");
        }
        TransactionType transactionType = checkType(type, reasons);
        Long value = checkAmount(amount, reasons);
        checkDescription(description, reasons);

        if (!reasons.isEmpty()) {
            return new ValidationResult(null, reasons);
        }
        ValidatedTransaction validated = new ValidatedTransaction(
                UUID.fromString(id.trim()), transactionType, value, description);
        return new ValidationResult(validated, List.of());
    }

    private static TransactionType checkType(String type, List<String> reasons) {
        String trimmed = type == null ? "" : type.trim();
        if (trimmed.equals("CREDIT")) {
            return TransactionType.CREDIT;
        }
        if (trimmed.equals("DEBIT")) {
            return TransactionType.DEBIT;
        }
        reasons.add("type invalide : CREDIT ou DEBIT attendu");
        return null;
    }

    private static Long checkAmount(String amount, List<String> reasons) {
        try {
            long value = Long.parseLong(amount == null ? "" : amount.trim());
            if (value > 0) {
                return value;
            }
            reasons.add("montant invalide : il doit être strictement positif");
        } catch (NumberFormatException e) {
            reasons.add("montant invalide : un entier est attendu");
        }
        return null;
    }

    private static void checkDescription(String description, List<String> reasons) {
        if (description == null || description.isBlank()) {
            reasons.add("description manquante ou vide");
        } else if (description.indexOf(0) >= 0) {
            reasons.add("description invalide : caractère nul interdit");
        } else if (description.codePointCount(0, description.length()) > 255) {
            reasons.add("description trop longue : 255 caractères maximum");
        }
    }
}
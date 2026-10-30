package cm.mvtech.mondje.importing;


import cm.mvtech.mondje.transaction.TransactionValidator;
import cm.mvtech.mondje.transaction.ValidationResult;

import java.util.List;

/**
 * Transforme une ligne de fichier {@code .txt} au format {@code id;type;montant;description}
 * en transaction validée, ou en raisons de rejet.
 */
public final class TxtLineParser {

    private static final String HEADER = "id;type;montant;description";

    private TxtLineParser() {

    }

    /**
     * Indique si la ligne est l'en-tête des colonnes, à ignorer.
     *
     * @param line la ligne lue
     * @return vrai si la ligne est l'en-tête
     */
    public static boolean isHeader(String line) {
        return HEADER.equalsIgnoreCase(line.trim());
    }

    /**
     * Lit une ligne et applique les règles de validation.
     * Le texte après le troisième point-virgule est la description, même s'il contient d'autres {@code ;}.
     *
     * @param line la ligne lue
     * @return la transaction validée, ou la liste des raisons de rejet
     */
    public static ValidationResult parse(String line) {
        String[] fields = line.split(";", 4);
        if (fields.length < 4) {
            return new ValidationResult(null,
                    List.of("ligne mal formée : 4 champs séparés par « ; » attendus (id;type;montant;description)"));
        }
        return TransactionValidator.validate(fields[0], fields[1], fields[2], fields[3]);
    }


}

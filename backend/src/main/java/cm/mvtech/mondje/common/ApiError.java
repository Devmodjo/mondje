package cm.mvtech.mondje.common;

import java.util.List;

/**
 * Corps JSON de toutes les réponses d'erreur de l'API.
 *
 * @param status    le code HTTP
 * @param message   un résumé lisible de l'erreur
 * @param reasons   les raisons détaillées, vide si aucune
 * @param requestId l'identifiant de la requête, présent dans les logs
 */
public record ApiError(int status, String message, List<String> reasons, String requestId) {
}
package cm.mvtech.mondje.common;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Traduit toutes les exceptions en réponses JSON uniformes.
 * Aucune trace technique n'est renvoyée au client : elle reste dans les logs.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Donnée invalide : HTTP 422 avec la liste des raisons.
     *
     * @param ex l'exception de validation
     * @return la réponse d'erreur
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidation(ValidationException ex) {
        return build(422, "Données invalides", ex.getReasons());
    }

    /**
     * Ressource introuvable : HTTP 404.
     *
     * @param ex l'exception de ressource introuvable
     * @return la réponse d'erreur
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
        return build(404, ex.getMessage(), List.of());
    }

    /**
     * Conflit avec l'état des données : HTTP 409.
     *
     * @param ex l'exception de conflit
     * @return la réponse d'erreur
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex) {
        return build(409, ex.getMessage(), List.of());
    }

    /**
     * Corps de requête illisible ou JSON mal formé : HTTP 422.
     *
     * @param ex l'exception de lecture du corps
     * @return la réponse d'erreur
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(422, "Données invalides", List.of("corps de la requête illisible ou mal formé"));
    }

    /**
     * Paramètre d'URL ou de chemin de type incorrect : HTTP 422.
     *
     * @param ex l'exception de conversion de type
     * @return la réponse d'erreur
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(422, "Données invalides", List.of("paramètre « " + ex.getName() + " » invalide"));
    }

    /**
     * Toute autre erreur : les erreurs HTTP standard de Spring gardent leur code,
     * le reste devient un HTTP 500 sans détail technique.
     *
     * @param ex l'exception non prévue
     * @return la réponse d'erreur
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        if (ex instanceof ErrorResponse errorResponse) {
            return build(errorResponse.getStatusCode().value(), "Requête invalide", List.of());
        }
        log.error("Erreur inattendue", ex);
        return build(500, "Erreur interne du serveur", List.of());
    }

    private ResponseEntity<ApiError> build(int status, String message, List<String> reasons) {
        ApiError body = new ApiError(status, message, reasons, MDC.get("requestId"));
        return ResponseEntity.status(status).body(body);
    }
}
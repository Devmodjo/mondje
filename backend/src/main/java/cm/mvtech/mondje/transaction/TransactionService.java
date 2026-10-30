package cm.mvtech.mondje.transaction;

import cm.mvtech.mondje.common.ConflictException;
import cm.mvtech.mondje.common.NotFoundException;
import cm.mvtech.mondje.common.ValidationException;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Règles métier des transactions : validation, unicité, lecture.
 */
@Service
public class TransactionService {

    private final TransactionRepository repository;

    /**
     * Crée le service.
     *
     * @param repository l'accès aux données
     */
    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    /**
     * Crée une transaction après validation.
     *
     * @param request les données reçues
     * @return la transaction enregistrée
     * @throws ValidationException si une donnée est invalide
     * @throws ConflictException   si l'UUID existe déjà
     */
    public Transaction create(TransactionRequest request) {
        String id = request.id() == null || request.id().isBlank()
                ? UUID.randomUUID().toString()
                : request.id();
        String amount = request.amount() == null ? null : String.valueOf(request.amount());
        ValidationResult result = TransactionValidator.validate(
                id, request.type(), amount, request.description());
        if (!result.isValid()) {
            throw new ValidationException(result.reasons());
        }
        ValidatedTransaction validated = result.transaction();
        if (!repository.insert(validated)) {
            throw new ConflictException("Une transaction avec cet UUID existe déjà");
        }
        return get(validated.id());
    }

    /**
     * Lit une transaction active.
     *
     * @param id l'UUID recherché
     * @return la transaction
     * @throws NotFoundException si elle n'existe pas
     */
    public Transaction get(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction introuvable : " + id));
    }
}
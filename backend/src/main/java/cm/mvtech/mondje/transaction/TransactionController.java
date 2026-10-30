package cm.mvtech.mondje.transaction;

import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Points d'entrée HTTP des transactions.
 * Ce contrôleur ne contient aucune règle métier : il délègue tout au service.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    /**
     * Crée le contrôleur.
     *
     * @param service le service métier
     */
    public TransactionController(TransactionService service) {
        this.service = service;
    }

    /**
     * Crée une transaction.
     *
     * @param request les données de la transaction
     * @return la transaction créée, avec le code 201
     */
    @PostMapping
    public ResponseEntity<Transaction> create(@RequestBody TransactionRequest request) {
        Transaction created = service.create(request);
        return ResponseEntity.created(URI.create("/api/transactions/" + created.id())).body(created);
    }

    /**
     * Lit une transaction.
     *
     * @param id l'UUID de la transaction
     * @return la transaction
     */
    @GetMapping("/{id}")
    public Transaction get(@PathVariable("id") UUID id) {
        return service.get(id);
    }
}
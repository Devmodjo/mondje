package cm.mvtech.mondje.transaction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository repository;

    @Autowired
    private DataSource dataSource;

    private UUID id;

    @BeforeEach
    void creerUnIdentifiant() {
        id = UUID.randomUUID();
    }

    @AfterEach
    void nettoyer() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM transactions WHERE id = ?")) {
            statement.setObject(1, id);
            statement.executeUpdate();
        }
    }

    @Test
    void insereUneTransactionPuisRefuseLeDoublon() {
        ValidatedTransaction transaction =
                new ValidatedTransaction(id, TransactionType.CREDIT, 5000, "Dépôt client");

        assertTrue(repository.insert(transaction));
        assertFalse(repository.insert(transaction));
    }
}
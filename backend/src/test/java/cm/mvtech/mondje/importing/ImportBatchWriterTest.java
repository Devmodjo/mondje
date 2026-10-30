package cm.mvtech.mondje.importing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cm.mvtech.mondje.transaction.TransactionType;
import cm.mvtech.mondje.transaction.ValidatedTransaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ImportBatchWriterTest {

    @Autowired
    private ImportBatchWriter writer;

    @Autowired
    private DataSource dataSource;

    private UUID importId;

    @BeforeEach
    void creerUnImport() throws SQLException {
        importId = UUID.randomUUID();
        String hash = (UUID.randomUUID().toString() + UUID.randomUUID()).replace("-", "");
        execute("INSERT INTO imports (id, file_name, file_hash, format, status) "
                + "VALUES (?, 'test.txt', ?, 'TXT', 'RUNNING')", importId, hash);
    }

    @AfterEach
    void nettoyer() throws SQLException {
        execute("DELETE FROM transactions WHERE import_id = ?", importId);
        execute("DELETE FROM import_errors WHERE import_id = ?", importId);
        execute("DELETE FROM imports WHERE id = ?", importId);
    }

    @Test
    void ecritUnLotCompletDansUneSeuleTransaction() throws SQLException {
        List<RowToInsert> rows = List.of(row(1), row(2), row(4));
        List<LineError> errors = List.of(new LineError(3, 120, "abc;CREDIT", "UUID invalide"));

        BatchResult result = writer.write(importId, rows, errors, 4, 200);

        assertEquals(3, result.inserted());
        assertEquals(0, result.duplicates());
        assertEquals(1, result.errors());
        assertEquals(3, queryLong("SELECT count(*) FROM transactions WHERE import_id = ?", importId));
        assertEquals(1, queryLong("SELECT count(*) FROM import_errors WHERE import_id = ?", importId));
        assertEquals(200, queryLong("SELECT last_byte_offset FROM imports WHERE id = ?", importId));
    }

    @Test
    void compteLesDoublonsSansLesInsererEtGardeLaPremiereOccurrence() throws SQLException {
        UUID repeated = UUID.randomUUID();

        BatchResult first = writer.write(importId,
                List.of(rowWithId(repeated, 1), rowWithId(repeated, 2), row(3)), List.of(), 3, 90);

        assertEquals(2, first.inserted());
        assertEquals(1, first.duplicates());
        assertEquals(1, queryLong("SELECT source_line FROM transactions WHERE id = ?", repeated));

        BatchResult second = writer.write(importId, List.of(rowWithId(repeated, 4)), List.of(), 4, 120);

        assertEquals(0, second.inserted());
        assertEquals(1, second.duplicates());
        assertEquals(2, queryLong("SELECT duplicate_count FROM imports WHERE id = ?", importId));
    }

    private RowToInsert row(long line) {
        return rowWithId(UUID.randomUUID(), line);
    }

    private RowToInsert rowWithId(UUID id, long line) {
        return new RowToInsert(new ValidatedTransaction(id, TransactionType.CREDIT, 1000, "Ligne " + line), line);
    }

    private void execute(String sql, Object... params) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            statement.executeUpdate();
        }
    }

    private long queryLong(String sql, Object... params) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }
}
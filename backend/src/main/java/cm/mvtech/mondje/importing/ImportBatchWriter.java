package cm.mvtech.mondje.importing;


import cm.mvtech.mondje.common.DatabaseException;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;


/**
 * Écrit un lot de lignes d'import en une seule transaction : les transactions valides,
 * les lignes rejetées et la progression de l'import réussissent ou échouent ensemble.
 */
@Component
public class ImportBatchWriter {

    private static final int RAW_LINE_MAX_LENGTH = 100;
    private static final int REASON_MAX_LENGTH = 255;
    private static final String INSERT_TRANSACTIONS = """
            INSERT INTO transactions (id, type, amount, description, import_id, source_line)
            SELECT t.id, t.type, t.amount, t.description, ?::uuid, t.source_line
            FROM unnest(?::uuid[], ?::text[], ?::bigint[], ?::text[], ?::bigint[])
                 AS t(id, type, amount, description, source_line)
            ON CONFLICT (id) DO NOTHING
            """;

    private static final String INSERT_ERRORS = """
            INSERT INTO import_errors (import_id, line_number, byte_offset, raw_line, reason)
            SELECT ?::uuid, e.line_number, e.byte_offset, e.raw_line, e.reason
            FROM unnest(?::bigint[], ?::bigint[], ?::text[], ?::text[])
                 AS e(line_number, byte_offset, raw_line, reason)
            """;

    private static final String UPDATE_PROGRESS = """
            UPDATE imports
            SET inserted_count = inserted_count + ?,
                duplicate_count = duplicate_count + ?,
                error_count = error_count + ?,
                last_line = ?,
                last_byte_offset = ?
            WHERE id = ?
            """;

    private final DataSource dataSource;


    /**
     * Crée l'écrivain de lots.
     *
     * @param dataSource la source de données, c'est-à-dire le pool Hikari
     */
    public ImportBatchWriter(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    /**
     * Écrit un lot et met à jour la progression de l'import, en une seule transaction.
     * Les lignes valides sont triées par UUID avant l'insertion : deux imports simultanés
     * verrouillent alors les lignes dans le même ordre et ne peuvent pas se bloquer mutuellement.
     *
     * @param importId   l'identifiant de l'import, qui doit déjà exister
     * @param rows       les lignes valides du lot
     * @param errors     les lignes rejetées du lot
     * @param lastLine   le numéro de la dernière ligne traitée dans ce lot
     * @param nextOffset la position en octets où reprendre la lecture après ce lot
     * @return le bilan du lot
     */
    public BatchResult write(UUID importId, List<RowToInsert> rows, List<LineError> errors,
    long lastLine, long nextOffset) {

        try(Connection connection = dataSource.getConnection();) {
            connection.setAutoCommit(false);

            try{
                int inserted = insertTransactions(connection, importId, rows);
                insertErrors(connection, importId, errors);
                int duplicates = rows.size() - inserted;
                updateProgress(connection, importId, inserted, duplicates, errors.size(), lastLine, nextOffset);
                connection.commit();
                return new BatchResult(inserted, duplicates, errors.size());
            }catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Écriture du lot impossible", e);
        }
    }

    private int insertTransactions(Connection connection, UUID importId, List<RowToInsert> rows)
            throws SQLException {
        if (rows.isEmpty()) {
            return 0;
        }
        List<RowToInsert> sorted = new ArrayList<>(rows);
        sorted.sort(Comparator.comparing((RowToInsert row) -> row.transaction().id()));
        int size = sorted.size();
        String[] ids = new String[size];
        String[] types = new String[size];
        Long[] amounts = new Long[size];
        String[] descriptions = new String[size];
        Long[] sourceLines = new Long[size];

        for (int i = 0; i < size; i++) {
            RowToInsert row = sorted.get(i);
            ids[i] = row.transaction().id().toString();
            types[i] = row.transaction().type().name();
            amounts[i] = row.transaction().amount();
            descriptions[i] = row.transaction().description();
            sourceLines[i] = row.sourceLine();
        }
        try (PreparedStatement statement = connection.prepareStatement(INSERT_TRANSACTIONS)) {
            statement.setObject(1, importId);
            statement.setArray(2, connection.createArrayOf("uuid", ids));
            statement.setArray(3, connection.createArrayOf("text", types));
            statement.setArray(4, connection.createArrayOf("bigint", amounts));
            statement.setArray(5, connection.createArrayOf("text", descriptions));
            statement.setArray(6, connection.createArrayOf("bigint", sourceLines));
            return statement.executeUpdate();
        }
    }

    private void insertErrors(Connection connection, UUID importId, List<LineError> errors)
            throws SQLException {
        if (errors.isEmpty()) {
            return;
        }
        int size = errors.size();
        Long[] lineNumbers = new Long[size];
        Long[] byteOffsets = new Long[size];
        String[] rawLines = new String[size];
        String[] reasons = new String[size];
        for (int i = 0; i < size; i++) {
            LineError error = errors.get(i);
            lineNumbers[i] = error.lineNumber();
            byteOffsets[i] = error.byteOffset();
            rawLines[i] = clean(error.rawLine(), RAW_LINE_MAX_LENGTH);
            reasons[i] = clean(error.reason(), REASON_MAX_LENGTH);
        }
        try (PreparedStatement statement = connection.prepareStatement(INSERT_ERRORS)) {
            statement.setObject(1, importId);
            statement.setArray(2, connection.createArrayOf("bigint", lineNumbers));
            statement.setArray(3, connection.createArrayOf("bigint", byteOffsets));
            statement.setArray(4, connection.createArrayOf("text", rawLines));
            statement.setArray(5, connection.createArrayOf("text", reasons));
            statement.executeUpdate();
        }
    }

    private void updateProgress(Connection connection, UUID importId, int inserted, int duplicates,
                                int errors, long lastLine, long nextOffset) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_PROGRESS)) {
            statement.setLong(1, inserted);
            statement.setLong(2, duplicates);
            statement.setLong(3, errors);
            statement.setLong(4, lastLine);
            statement.setLong(5, nextOffset);
            statement.setObject(6, importId);
            statement.executeUpdate();
        }
    }

    private static String clean(String text, int maxLength) {
        String cleaned = text.replace('\0', '?');
        if (cleaned.length() <= maxLength) {
            return cleaned;
        }
        int end = maxLength;
        if (Character.isHighSurrogate(cleaned.charAt(end - 1))) {
            end--;
        }
        return cleaned.substring(0, end);
    }

}

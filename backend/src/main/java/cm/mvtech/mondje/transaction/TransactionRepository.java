package cm.mvtech.mondje.transaction;


import cm.mvtech.mondje.common.DatabaseException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Accès SQL aux transactions, en JDBC pur sur le pool de connexions Hikari.
 */
@Repository
public class TransactionRepository {

    private final DataSource dataSource;

    private static final String COLUMNS =
            "id, type, amount, description, created_at, import_id, source_line, modified_manually, deleted_at";

    /**
     * Crée le repository.
     * @param dataSource la source de données, c'est-à-dire le pool Hikari
     */
    public TransactionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    /**
     * Insère une transaction sans erreur si l'UUID existe déjà.
     *
     * @param transaction la transaction validée
     * @return vrai si la ligne a été insérée, faux si l'UUID existait déjà
     */
    public boolean insert(ValidatedTransaction transaction) {

        String sql = """
                    INSERT INTO transactions(id, type, amount, description) VALUES
                    (?, ? ,? ,?) ON CONFLICT (id) DO NOTHING
                """;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql);
        ) {

            stmt.setObject(1, transaction.id());
            stmt.setString(2, transaction.type().name());
            stmt.setLong(3, transaction.amount());
            stmt.setString(4, transaction.description());

            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DatabaseException("Insertion de la transaction impossible", e);
        }
    }

    public Optional<Transaction> findById(UUID id) {

        String sql = "SELECT " + COLUMNS + " FROM transactions WHERE id = ? AND deleted_at IS NULL";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)
        ) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Lecture de la transaction impossible", e);
        }

    }


    private Transaction map(ResultSet rs) throws  SQLException {
        Long sourceLine = rs.getLong("source_line");
        Long source = rs.wasNull() ? null : sourceLine;
        return new Transaction(
                rs.getObject("id", UUID.class),
                TransactionType.valueOf(rs.getString("type")),
                rs.getLong("amount"),
                rs.getString("description"),
                toInstant(rs.getObject("created_at", OffsetDateTime.class)),
                rs.getObject("import_id", UUID.class),
                source,
                rs.getBoolean("modified_manually"),
                toInstant(rs.getObject("deleted_at", OffsetDateTime.class)));
    }

    private Instant toInstant(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }

}

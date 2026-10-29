package cm.mvtech.mondje.transaction;


import cm.mvtech.mondje.common.DatabaseException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * Accès SQL aux transactions, en JDBC pur sur le pool de connexions Hikari.
 */
@Repository
public class TransactionRepository {

    private final DataSource dataSource;


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

}

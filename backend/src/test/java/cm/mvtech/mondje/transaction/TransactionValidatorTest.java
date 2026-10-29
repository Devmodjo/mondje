package cm.mvtech.mondje.transaction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionValidatorTest {

    private static final String ID_OK = "550E8400-E29B-41D4-A716-446655440000";

    @Test
    void accepteUnUuidEnMinusculesEtEnMajuscules() {
        assertTrue(TransactionValidator.isValidUuid("550e8400-e29b-41d4-a716-446655440000"));
        assertTrue(TransactionValidator.isValidUuid("550E8400-E29B-41D4-A716-446655440000"));
    }

    @Test
    void refuseUnTexteQuiNEstPasUnUuid() {
        assertFalse(TransactionValidator.isValidUuid("abc"));
        assertFalse(TransactionValidator.isValidUuid("1-1-1-1-1"));
        assertFalse(TransactionValidator.isValidUuid(""));
        assertFalse(TransactionValidator.isValidUuid(null));
    }

    @Test
    void accepteUneTransactionValide() {
        ValidationResult result = TransactionValidator.validate(ID_OK, "CREDIT", "5000", "Dépôt client");

        assertTrue(result.isValid());
        assertEquals(5000L, result.transaction().amount());
        assertEquals(TransactionType.CREDIT, result.transaction().type());
    }

    @Test
    void listeToutesLesRaisonsDuRejet() {
        ValidationResult result = TransactionValidator.validate("abc", "X", "-5", "");

        assertFalse(result.isValid());
        assertEquals(4, result.reasons().size());
    }

    @Test
    void refuseUnMontantDecimalOuNul() {
        assertFalse(TransactionValidator.validate(ID_OK, "DEBIT", "12.5", "Frais").isValid());
        assertFalse(TransactionValidator.validate(ID_OK, "DEBIT", "0", "Frais").isValid());
    }

    @Test
    void limiteLaDescriptionA255Caracteres() {
        assertTrue(TransactionValidator.validate(ID_OK, "DEBIT", "100", "x".repeat(255)).isValid());
        assertFalse(TransactionValidator.validate(ID_OK, "DEBIT", "100", "x".repeat(256)).isValid());
    }
}
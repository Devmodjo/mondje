package cm.mvtech.mondje.transaction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TransactionValidatorTest {

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
}
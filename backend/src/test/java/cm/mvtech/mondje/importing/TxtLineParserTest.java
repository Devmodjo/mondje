package cm.mvtech.mondje.importing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cm.mvtech.mondje.transaction.ValidationResult;
import org.junit.jupiter.api.Test;

class TxtLineParserTest {

    private static final String ID = "550e8400-e29b-41d4-a716-446655440000";

    @Test
    void litUneLigneValide() {
        ValidationResult result = TxtLineParser.parse(ID + ";CREDIT;5000;Dépôt client");

        assertTrue(result.isValid());
        assertEquals("Dépôt client", result.transaction().description());
    }

    @Test
    void gardeLesPointsVirgulesDeLaDescription() {
        ValidationResult result = TxtLineParser.parse(ID + ";DEBIT;100;Achat; riz; huile");

        assertEquals("Achat; riz; huile", result.transaction().description());
    }

    @Test
    void refuseUneLigneSansAssezDeChamps() {
        ValidationResult result = TxtLineParser.parse("abc;CREDIT");

        assertFalse(result.isValid());
        assertEquals(1, result.reasons().size());
    }

    @Test
    void reconnaitLEnTete() {
        assertTrue(TxtLineParser.isHeader("id;type;montant;description"));
        assertFalse(TxtLineParser.isHeader(ID + ";CREDIT;5000;x"));
    }
}
package cm.mvtech.mondje.importing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class LineReaderTest {

    private LineReader readerOf(String text) {
        return new LineReader(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void retientLeNumeroEtLaPositionEnOctetsDeChaqueLigne() throws IOException {
        LineReader reader = readerOf("ab\ncé\r\nfin");

        assertEquals("ab", reader.readLine());
        assertEquals(1, reader.lineNumber());
        assertEquals(0, reader.lineOffset());

        assertEquals("cé", reader.readLine());
        assertEquals(2, reader.lineNumber());
        assertEquals(3, reader.lineOffset());

        assertEquals("fin", reader.readLine());
        assertEquals(3, reader.lineNumber());
        assertEquals(8, reader.lineOffset());

        assertNull(reader.readLine());
    }

    @Test
    void neCreePasDeLigneVideApresLeDernierSauteDeLigne() throws IOException {
        LineReader reader = readerOf("a\nb\n");

        assertEquals("a", reader.readLine());
        assertEquals("b", reader.readLine());
        assertNull(reader.readLine());
    }

    @Test
    void gardeUneLigneVideAuMilieu() throws IOException {
        LineReader reader = readerOf("a\n\nb");

        assertEquals("a", reader.readLine());
        assertEquals("", reader.readLine());
        assertEquals("b", reader.readLine());
    }
}
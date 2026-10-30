package cm.mvtech.mondje.importing;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


/**
 * Lit un flux d'octets ligne par ligne, en mémoire constante, en retenant pour chaque ligne
 * son numéro et sa position en octets dans le fichier. Le texte est décodé en UTF-8, les octets
 * invalides sont remplacés plutôt que de faire échouer la lecture.
 */
public final class LineReader {

    private static final int BUFFER_SIZE = 64 * 1024;

    private final InputStream input;
    private final byte[] buffer = new byte[BUFFER_SIZE];
    private final ByteArrayOutputStream lineBytes = new ByteArrayOutputStream(256);
    private int position;
    private int limit;
    private long offset;
    private long currentOffset;
    private long currentLine;


    /**
     * Crée un lecteur sur un flux d'octets.
     *
     * @param input le flux à lire, fermé par l'appelant
     */
    public LineReader(InputStream input) {
        this.input = input;
    }

    /**
     * Lit la ligne suivante, sans son saut de ligne ({@code \n} ou {@code \r\n}).
     *
     * @return la ligne, ou {@code null} à la fin du fichier
     * @throws IOException si la lecture échoue
     */
    public String readLine() throws IOException {
        lineBytes.reset();
        currentOffset = offset;
        boolean consumed = false;
        while (true) {
            if (position == limit) {
                limit = input.read(buffer, 0, buffer.length);
                position = 0;
                if (limit < 0) {
                    limit = 0;
                    if (!consumed) {
                        return null;
                    }
                    break;
                }
            }
            int start = position;
            while (position < limit && buffer[position] != '\n') {
                position++;
            }
            int length = position - start;
            lineBytes.write(buffer, start, length);
            offset += length;
            consumed = consumed || length > 0;
            if (position < limit) {
                position++;
                offset++;
                consumed = true;
                break;
            }
        }
        currentLine++;
        return decode();
    }

    /**
     * Retourne le numéro, à partir de 1, de la dernière ligne lue.
     *
     * @return le numéro de ligne
     */
    public long lineNumber() {
        return currentLine;
    }

    /**
     * Retourne la position en octets, à partir de 0, du début de la dernière ligne lue.
     *
     * @return la position en octets
     */
    public long lineOffset() {
        return currentOffset;
    }

    private String decode() {
        String line = lineBytes.toString(StandardCharsets.UTF_8);
        if (currentLine == 1 && line.startsWith("\uFEFF")) {
            line = line.substring(1);
        }
        if (!line.isEmpty() && line.charAt(line.length() - 1) == '\r') {
            line = line.substring(0, line.length() - 1);
        }
        return line;
    }
}

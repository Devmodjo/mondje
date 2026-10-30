package cm.mvtech.mondje.importing;

/**
 * Ligne rejetée pendant un import.
 *
 * @param lineNumber le numéro de la ligne dans le fichier, à partir de 1
 * @param byteOffset la position en octets du début de la ligne dans le fichier
 * @param rawLine    le texte de la ligne tel qu'il a été lu
 * @param reason     la ou les raisons du rejet
 */
public record LineError(long lineNumber, long byteOffset, String rawLine, String reason) {
}
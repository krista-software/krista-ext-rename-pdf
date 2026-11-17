package app.krista.extensions.essentials.documents.renamepdf.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Confirms that the bytes handed to "Rename PDF File" really are a PDF, by checking the five-byte
 * {@code %PDF-} header every PDF document begins with.
 *
 * <p>The extension's author chose signature checking over a filename check specifically so a
 * {@code .pdf}-named file that is not actually a PDF is still caught. Pure and framework-free, so it is
 * directly unit-testable (see {@code PdfSignatureValidatorTest}).
 */
public final class PdfSignatureValidator {

    /** Number of leading bytes that identify a PDF document. */
    public static final int SIGNATURE_LENGTH = 5;

    /** The literal bytes {@code %PDF-}. */
    private static final byte[] PDF_SIGNATURE = {'%', 'P', 'D', 'F', '-'};

    private PdfSignatureValidator() {
    }

    /**
     * @param header the first bytes of a file (may be shorter than {@link #SIGNATURE_LENGTH} or null)
     * @return true only when the header starts with {@code %PDF-}
     */
    public static boolean isPdf(byte[] header) {
        if (header == null || header.length < SIGNATURE_LENGTH) {
            return false;
        }
        for (int i = 0; i < SIGNATURE_LENGTH; i++) {
            if (header[i] != PDF_SIGNATURE[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reads up to {@link #SIGNATURE_LENGTH} bytes from {@code in}. A stream that ends early yields a
     * shorter array rather than throwing — an empty or truncated file is a validation failure, not a
     * crash.
     */
    public static byte[] readHeader(InputStream in) throws IOException {
        if (in == null) {
            return new byte[0];
        }
        byte[] buffer = new byte[SIGNATURE_LENGTH];
        int read = 0;
        while (read < SIGNATURE_LENGTH) {
            int count = in.read(buffer, read, SIGNATURE_LENGTH - read);
            if (count < 0) {
                break;
            }
            read += count;
        }
        if (read == SIGNATURE_LENGTH) {
            return buffer;
        }
        byte[] truncated = new byte[read];
        System.arraycopy(buffer, 0, truncated, 0, read);
        return truncated;
    }

    /**
     * Validates that {@code in} begins with the PDF signature.
     *
     * @param fileName the original file name, used only to make the error message actionable
     * @throws IllegalArgumentException when the content is not a PDF
     */
    public static void requirePdf(InputStream in, String fileName) throws IOException {
        if (!isPdf(readHeader(in))) {
            throw new IllegalArgumentException(notAPdfMessage(fileName));
        }
    }

    /** The actionable rejection message, pointing at the conversion requests in the same domain. */
    static String notAPdfMessage(String fileName) {
        String label = (fileName == null || fileName.trim().isEmpty())
                ? "The supplied file"
                : "'" + fileName.trim() + "'";
        return label + " is not a PDF document. Rename PDF File only accepts PDFs — the file must begin "
                + "with the %PDF- signature. Convert it first (the Documents domain provides "
                + "'Convert Excel to PDF', 'Convert Docx File to PDF File' and 'Convert image to pdf'), "
                + "then rename the result.";
    }
}

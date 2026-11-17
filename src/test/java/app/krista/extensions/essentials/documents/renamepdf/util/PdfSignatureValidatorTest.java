package app.krista.extensions.essentials.documents.renamepdf.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for the pure %PDF- signature check behind "Rename PDF File". */
class PdfSignatureValidatorTest {

    /** First bytes of a real PDF 1.7 document. */
    private static final byte[] PDF_BYTES = "%PDF-1.7\n1 0 obj\n".getBytes(StandardCharsets.US_ASCII);

    /** PNG magic bytes — a plausible wrong file for someone to supply. */
    private static final byte[] PNG_BYTES = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};

    private static InputStream stream(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    @Test
    @DisplayName("accepts content whose first five bytes are %PDF-")
    void acceptsRealPdfHeader() throws IOException {
        assertTrue(PdfSignatureValidator.isPdf(PDF_BYTES));
        assertDoesNotThrow(() -> PdfSignatureValidator.requirePdf(stream(PDF_BYTES), "report.pdf"));
    }

    @Test
    @DisplayName("accepts a file that is exactly the signature and nothing more")
    void acceptsExactlyTheSignature() {
        byte[] exact = "%PDF-".getBytes(StandardCharsets.US_ASCII);
        assertTrue(PdfSignatureValidator.isPdf(exact));
    }

    @Test
    @DisplayName("rejects non-PDF content with a message naming the file and the expected type")
    void rejectsPngWithActionableMessage() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> PdfSignatureValidator.requirePdf(stream(PNG_BYTES), "logo.png"));
        assertTrue(thrown.getMessage().contains("logo.png"), thrown.getMessage());
        assertTrue(thrown.getMessage().contains("%PDF-"), thrown.getMessage());
    }

    @Test
    @DisplayName("a .pdf-named file that is not really a PDF is still rejected")
    void rejectsMislabelledFile() {
        assertThrows(IllegalArgumentException.class,
                () -> PdfSignatureValidator.requirePdf(stream(PNG_BYTES), "invoice.pdf"));
    }

    @Test
    @DisplayName("rejects empty content without throwing IndexOutOfBounds")
    void rejectsEmptyContent() {
        assertFalse(PdfSignatureValidator.isPdf(new byte[0]));
        assertThrows(IllegalArgumentException.class,
                () -> PdfSignatureValidator.requirePdf(stream(new byte[0]), "empty.pdf"));
    }

    @Test
    @DisplayName("rejects content truncated shorter than the signature")
    void rejectsTruncatedContent() {
        byte[] truncated = "%PD".getBytes(StandardCharsets.US_ASCII);
        assertFalse(PdfSignatureValidator.isPdf(truncated));
        assertThrows(IllegalArgumentException.class,
                () -> PdfSignatureValidator.requirePdf(stream(truncated), "cut-short.pdf"));
    }

    @Test
    @DisplayName("a null header is rejected rather than crashing")
    void rejectsNullHeader() {
        assertFalse(PdfSignatureValidator.isPdf(null));
    }

    @Test
    @DisplayName("the signature check is case-sensitive — '%pdf-' is not a PDF")
    void isCaseSensitive() {
        assertFalse(PdfSignatureValidator.isPdf("%pdf-1.7".getBytes(StandardCharsets.US_ASCII)));
    }

    @Test
    @DisplayName("readHeader returns exactly the signature length for a long stream")
    void readHeaderReadsSignatureLength() throws IOException {
        byte[] header = PdfSignatureValidator.readHeader(stream(PDF_BYTES));
        assertEquals(PdfSignatureValidator.SIGNATURE_LENGTH, header.length);
        assertArrayEquals("%PDF-".getBytes(StandardCharsets.US_ASCII), header);
    }

    @Test
    @DisplayName("readHeader returns a shorter array for a short stream rather than padding with zeros")
    void readHeaderHandlesShortStream() throws IOException {
        byte[] header = PdfSignatureValidator.readHeader(stream("%P".getBytes(StandardCharsets.US_ASCII)));
        assertEquals(2, header.length);
    }

    @Test
    @DisplayName("readHeader reassembles a signature delivered one byte at a time")
    void readHeaderHandlesDribblingStream() throws IOException {
        // A stream that returns a single byte per read() call — read() is allowed to do this, and a
        // naive single-read implementation would wrongly treat a real PDF as truncated.
        InputStream dribble = new InputStream() {
            private final byte[] data = "%PDF-1.7".getBytes(StandardCharsets.US_ASCII);
            private int pos;

            @Override
            public int read() {
                return pos < data.length ? data[pos++] & 0xFF : -1;
            }

            @Override
            public int read(byte[] b, int off, int len) {
                if (pos >= data.length) {
                    return -1;
                }
                b[off] = data[pos++];
                return 1;
            }
        };
        assertTrue(PdfSignatureValidator.isPdf(PdfSignatureValidator.readHeader(dribble)));
    }

    @Test
    @DisplayName("the rejection message falls back gracefully when the file name is unknown")
    void messageHandlesMissingFileName() {
        assertTrue(PdfSignatureValidator.notAPdfMessage(null).startsWith("The supplied file"));
        assertTrue(PdfSignatureValidator.notAPdfMessage("  ").startsWith("The supplied file"));
    }
}

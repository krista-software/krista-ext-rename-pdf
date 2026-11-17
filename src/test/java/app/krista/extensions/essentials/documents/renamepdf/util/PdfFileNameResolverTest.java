package app.krista.extensions.essentials.documents.renamepdf.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for the pure file-name rules behind "Rename PDF File". */
class PdfFileNameResolverTest {

    @Test
    @DisplayName("appends .pdf when the supplied name has no extension")
    void appendsPdfExtensionWhenMissing() {
        assertEquals("Invoice_2026-Q1.pdf", PdfFileNameResolver.resolve("Invoice 2026-Q1"));
    }

    @Test
    @DisplayName("leaves an existing .pdf suffix alone")
    void keepsExistingLowercaseExtension() {
        assertEquals("statement.pdf", PdfFileNameResolver.resolve("statement.pdf"));
    }

    @ParameterizedTest(name = "\"{0}\" is recognised as already ending in .pdf")
    @ValueSource(strings = {"Report.PDF", "Report.Pdf", "Report.pDf"})
    @DisplayName("detects the .pdf suffix case-insensitively, so it is never doubled")
    void doesNotDoubleExtensionRegardlessOfCase(String name) {
        assertEquals(name, PdfFileNameResolver.resolve(name));
        assertTrue(PdfFileNameResolver.endsWithPdfExtension(name));
    }

    @Test
    @DisplayName("a name ending in a different extension still gains .pdf")
    void appendsExtensionToNonPdfExtension() {
        assertEquals("scan.docx.pdf", PdfFileNameResolver.resolve("scan.docx"));
    }

    @Test
    @DisplayName("replaces unsafe filename characters with underscores")
    void sanitizesUnsafeCharacters() {
        assertEquals("a_b_c_d_e.pdf", PdfFileNameResolver.resolve("a/b\\c:d?e"));
        assertEquals("a_b_c", PdfFileNameResolver.sanitize("a b/c"));
    }

    @Test
    @DisplayName("surrounding whitespace is trimmed before the name is used")
    void trimsSurroundingWhitespace() {
        assertEquals("quarterly.pdf", PdfFileNameResolver.resolve("   quarterly.pdf   "));
    }

    @ParameterizedTest(name = "a name of \"{0}\" is rejected")
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @DisplayName("rejects a blank name with an actionable message")
    void rejectsBlankNames(String blank) {
        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> PdfFileNameResolver.resolve(blank));
        assertTrue(thrown.getMessage().contains("Output File Name is required"),
                "message should name the field: " + thrown.getMessage());
    }

    @Test
    @DisplayName("rejects a null name with an actionable message")
    void rejectsNullName() {
        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> PdfFileNameResolver.resolve(null));
        assertTrue(thrown.getMessage().contains("Output File Name is required"));
    }

    @Test
    @DisplayName("rejects a name that is nothing but the extension")
    void rejectsExtensionOnlyName() {
        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> PdfFileNameResolver.resolve(".pdf"));
        assertTrue(thrown.getMessage().contains("leaves no file name"),
                "message should explain the problem: " + thrown.getMessage());
    }

    @Test
    @DisplayName("a short name is not mistaken for an extension")
    void shortNamesAreNotTreatedAsExtensions() {
        assertFalse(PdfFileNameResolver.endsWithPdfExtension("ab"));
        assertEquals("ab.pdf", PdfFileNameResolver.resolve("ab"));
    }

    @Test
    @DisplayName("sanitize(null) is empty rather than throwing")
    void sanitizeHandlesNull() {
        assertEquals("", PdfFileNameResolver.sanitize(null));
    }
}

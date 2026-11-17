package app.krista.extensions.essentials.documents.renamepdf.util;

import java.util.regex.Pattern;

/**
 * Turns the caller-supplied "Output File Name" into the file name the renamed PDF is actually stored
 * under. Pure and framework-free, so it is directly unit-testable (see {@code PdfFileNameResolverTest}).
 *
 * <p>The rules are the ones confirmed with the extension's author:
 * <ul>
 *   <li>A blank name is rejected — renaming to nothing is never what the caller meant.</li>
 *   <li>Unsafe filename characters are replaced with {@code _} (same character class the Krista media
 *       client uses), so a name typed by a human is always storable.</li>
 *   <li>{@code .pdf} is appended automatically unless the name already ends in it. The check is
 *       case-insensitive, so {@code Report.PDF} is left alone rather than becoming
 *       {@code Report.PDF.pdf}.</li>
 *   <li>A name ending in a <em>different</em> extension still gains {@code .pdf}
 *       ({@code scan.docx} &rarr; {@code scan.docx.pdf}) — the content is a PDF, so the name must say
 *       so; silently accepting {@code .docx} would produce a file nothing can open.</li>
 * </ul>
 */
public final class PdfFileNameResolver {

    /** The extension every renamed file ends with. */
    public static final String PDF_EXTENSION = ".pdf";

    /** Anything outside this class is replaced with '_' before the name reaches the media store. */
    private static final Pattern UNSAFE_CHARACTERS = Pattern.compile("[^A-Za-z0-9._-]");

    private PdfFileNameResolver() {
    }

    /**
     * Resolve the stored file name for a caller-supplied name.
     *
     * @param requestedName the raw "Output File Name" input
     * @return a safe file name ending in a {@code .pdf} extension
     * @throws IllegalArgumentException if the name is blank, or leaves nothing before the extension
     */
    public static String resolve(String requestedName) {
        if (requestedName == null || requestedName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Output File Name is required. Please provide the name the renamed PDF should have, "
                            + "for example 'Invoice 2026-Q1'.");
        }

        String sanitized = sanitize(requestedName.trim());
        String withExtension = endsWithPdfExtension(sanitized) ? sanitized : sanitized + PDF_EXTENSION;

        if (baseNameOf(withExtension).isEmpty()) {
            throw new IllegalArgumentException(
                    "Output File Name '" + requestedName + "' leaves no file name before the .pdf extension. "
                            + "Please provide a name such as 'Invoice 2026-Q1'.");
        }
        return withExtension;
    }

    /** True when {@code name} already ends in {@code .pdf}, ignoring case. */
    public static boolean endsWithPdfExtension(String name) {
        if (name == null || name.length() < PDF_EXTENSION.length()) {
            return false;
        }
        return name.regionMatches(true, name.length() - PDF_EXTENSION.length(),
                PDF_EXTENSION, 0, PDF_EXTENSION.length());
    }

    /** Replaces every character Krista's media store will not accept in a file name with '_'. */
    public static String sanitize(String name) {
        return name == null ? "" : UNSAFE_CHARACTERS.matcher(name).replaceAll("_");
    }

    /** The part of a {@code *.pdf} name before the extension. */
    private static String baseNameOf(String nameWithPdfExtension) {
        return nameWithPdfExtension.substring(0, nameWithPdfExtension.length() - PDF_EXTENSION.length());
    }
}

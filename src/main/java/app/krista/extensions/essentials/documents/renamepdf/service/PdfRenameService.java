package app.krista.extensions.essentials.documents.renamepdf.service;

import app.krista.extensions.essentials.documents.renamepdf.integration.KristaMediaClient;
import app.krista.extensions.essentials.documents.renamepdf.util.PdfFileNameResolver;
import app.krista.extensions.essentials.documents.renamepdf.util.PdfSignatureValidator;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Orchestrates the rename: validate the name, pull the original bytes down, confirm they really are a
 * PDF, then store those same bytes back under the new name.
 *
 * <p>Keeping this out of the Area class is deliberate — the {@code @CatalogRequest} method stays a thin
 * delegator, per the extension testing policy.
 */
@Service
public class PdfRenameService {

    private static final Logger LOG = LoggerFactory.getLogger(PdfRenameService.class);

    private final KristaMediaClient media;

    @Inject
    public PdfRenameService(KristaMediaClient media) {
        this.media = media;
    }

    /**
     * Rename {@code source} to {@code requestedName}.
     *
     * <p>The returned file is a NEW Krista media reference holding byte-identical content; the original
     * is left untouched, because the platform file store offers no in-place rename.
     *
     * @throws IllegalArgumentException if the name is unusable or the content is not a PDF
     * @throws IOException              if the media store cannot be read or written
     */
    public app.krista.model.base.File rename(app.krista.model.base.File source, String requestedName)
            throws IOException {

        if (source == null) {
            throw new IllegalArgumentException(
                    "Input File is required. Please supply the PDF document to rename.");
        }

        // Validate the name BEFORE moving any bytes — a bad name should fail fast and cheaply.
        String targetName = PdfFileNameResolver.resolve(requestedName);

        File local = media.toJavaFile(source);
        File workingDir = null;
        try {
            try (InputStream in = new BufferedInputStream(new FileInputStream(local))) {
                PdfSignatureValidator.requirePdf(in, source.getFileName());
            }

            // A private temp dir keeps the copy from colliding with the download, which may already
            // sit at /tmp/<targetName> when the caller "renames" a file to the name it already has.
            workingDir = Files.createTempDirectory("renamepdf-").toFile();
            File renamed = new File(workingDir, targetName);
            Files.copy(local.toPath(), renamed.toPath(), StandardCopyOption.REPLACE_EXISTING);

            app.krista.model.base.File result = media.toKristaFile(renamed);
            LOG.debug("Renamed '{}' -> '{}' (new mediaId {})",
                    source.getFileName(), result.getFileName(), result.getMediaId());
            return result;
        } finally {
            deleteQuietly(local);
            deleteDirectoryQuietly(workingDir);
        }
    }

    private static void deleteQuietly(File file) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            LOG.debug("Could not delete temp file {}: {}", file.getName(), e.getMessage());
        }
    }

    private static void deleteDirectoryQuietly(File dir) {
        if (dir == null) {
            return;
        }
        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteQuietly(child);
            }
        }
        deleteQuietly(dir);
    }
}

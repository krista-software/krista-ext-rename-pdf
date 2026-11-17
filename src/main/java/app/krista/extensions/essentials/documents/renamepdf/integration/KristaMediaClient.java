package app.krista.extensions.essentials.documents.renamepdf.integration;

import app.krista.ksdk.files.FileHandle;
import app.krista.ksdk.files.FileRepository;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Bridge between {@code java.io.File} bytes and Krista's {@link app.krista.model.base.File} media
 * reference — the only integration this extension has, since it contacts no external system.
 *
 * <p>Krista has no in-place rename: a media reference's name is fixed when it is created by
 * {@link FileRepository#createNewFileByName(String)}. Renaming therefore means reading the original
 * bytes out ({@code toJavaFile}) and writing them back under the new name ({@code toKristaFile}),
 * which produces a NEW reference and leaves the original untouched.
 *
 * <p>Faithful to the outlook-4 / servicetrade idiom. File contents are never logged; filenames and
 * mediaId at DEBUG only.
 */
@Service
public class KristaMediaClient {

    private static final Logger LOG = LoggerFactory.getLogger(KristaMediaClient.class);
    private static final String TMP = "/tmp/";
    private static final int BUF = 4096;

    @Inject
    private FileRepository fileRepository;

    /** Krista File INPUT &rarr; local java.io.File, so its bytes can be inspected and copied. */
    public File toJavaFile(app.krista.model.base.File kfile) throws IOException {
        try (FileHandle handle = fileRepository.getFile(kfile)) {
            File out = new File(TMP + safeName(kfile.getFileName()));
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(out));
                 InputStream in = handle.getContent()) {
                byte[] b = new byte[BUF];
                int n;
                while ((n = in.read(b)) != -1) {
                    os.write(b, 0, n);
                }
            }
            return out;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Failed to read Krista file: " + e.getMessage(), e);
        }
    }

    /** local java.io.File &rarr; Krista File OUTPUT, stored under the file's (sanitized) name. */
    public app.krista.model.base.File toKristaFile(File file) throws IOException {
        try (FileHandle handle = fileRepository.createNewFileByName(safeName(file.getName()))) {
            if (handle == null) {
                throw new IOException("Null file handle for: " + file.getName());
            }
            try (FileInputStream in = new FileInputStream(file)) {
                handle.setContent(in);
            }
            app.krista.model.base.File result = handle.getFile();
            if (result == null) {
                throw new IOException("Null Krista File after upload: " + file.getName());
            }
            LOG.debug("Stored {} -> mediaId {}", result.getFileName(), result.getMediaId());
            return result;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Failed to store Krista file: " + e.getMessage(), e);
        }
    }

    /**
     * Whether the platform media store was injected and is usable. Backs the Setup-tab Test Connection
     * and the Health Check request — with no external system to reach, this is the one dependency this
     * extension genuinely has.
     */
    public boolean isAvailable() {
        return fileRepository != null;
    }

    private static String safeName(String name) {
        return name == null ? "file" : name.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}

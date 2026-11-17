package app.krista.extensions.essentials.documents.renamepdf;

import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.ChangeLog;
import app.krista.extension.impl.anno.Extension;
import app.krista.extension.impl.anno.InvokerRequest;
import app.krista.extension.impl.anno.Java;
import app.krista.extension.impl.anno.StaticResource;
import app.krista.extensions.essentials.documents.renamepdf.integration.KristaMediaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;

/**
 * Rename PDF extension entry point.
 *
 * <p><b>This extension has no authentication.</b> It contacts no external system: renaming happens
 * in-process against Krista's own file store. There are therefore deliberately NO connection
 * {@code @Field.*} declarations on this class, no secured token field, no authenticator, and no
 * Authentication tab — there is nothing for an administrator to supply. The Setup tab's Test
 * Connection button verifies the one real dependency instead: that the platform file store is
 * available.
 */
@Extension(name = "Rename PDF", version = "1.0.0",
        description = "Rename a PDF file inside a Krista workflow, returning the same PDF content "
                + "under a new file name.")
@StaticResource(path = "docs", file = "docs")
@ChangeLog(file = "docs/pages/release-notes.md")
@Java(version = Java.Version.JAVA_21)
public final class RenamePdfExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(RenamePdfExtension.class);

    private final Invoker invoker;
    private final KristaMediaClient media;

    @Inject
    public RenamePdfExtension(Invoker invoker, KristaMediaClient media) {
        this.invoker = invoker;
        this.media = media;
    }

    /** Setup-tab tabs. Documentation only — there is no Authentication tab, because there is no auth. */
    @InvokerRequest(InvokerRequest.Type.CUSTOM_TABS)
    public Map<String, String> customTabs() {
        return Map.of("Documentation", "static/docs");
    }

    /**
     * Test-Connection button. With no credentials and no external endpoint, the meaningful check is
     * that the Krista media store this extension reads and writes through is actually wired up.
     */
    @InvokerRequest(InvokerRequest.Type.TEST_CONNECTION)
    public void testConnection() throws IOException {
        if (!media.isAvailable()) {
            throw new IOException("The Krista file store (FileRepository) is not available to this "
                    + "extension, so PDFs cannot be read or written. Check the appliance logs for "
                    + "extension startup errors.");
        }
        LOGGER.info("Rename PDF test connection succeeded for invoker {}", invoker.getInvokerId());
    }
}

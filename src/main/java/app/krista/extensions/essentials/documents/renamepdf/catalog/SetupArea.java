package app.krista.extensions.essentials.documents.renamepdf.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.essentials.documents.renamepdf.catalog.support.AbstractArea;
import app.krista.extensions.essentials.documents.renamepdf.catalog.support.ExtensionResponses;
import app.krista.extensions.essentials.documents.renamepdf.catalog.support.TelemetryHelper;
import app.krista.extensions.essentials.documents.renamepdf.integration.KristaMediaClient;

import javax.inject.Inject;
import java.lang.management.ManagementFactory;

/**
 * Setup area — the standard operator-facing health check.
 *
 * <p>There is deliberately no Test Connection catalog request here: this extension authenticates
 * against nothing and connects to nothing, so the only dependency worth reporting is the platform
 * file store, which the Health Check covers.
 */
@Domain(id = "catEntryDomain_49c18799-c16b-4d33-98ac-f048f446a152",
        name = "Documents",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "12ae5e46-e6c4-4c34-bcf0-ef39bdbddac5")
public class SetupArea extends AbstractArea {

    private final KristaMediaClient media;

    @Inject
    public SetupArea(KristaMediaClient media, TelemetryHelper telemetry, Invoker invoker) {
        super(telemetry, invoker);
        this.media = media;
    }

    @CatalogRequest(
            id = "localDomainRequest_1e9f45d2-8c73-5a06-b4d8-96e2170ac35f",
            name = "Health Check",
            description = "Report extension health: whether the Krista file store this extension "
                    + "depends on is available, plus JVM heap in use.",
            area = "Setup",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Boolean(name = "Healthy", required = false, attributes = {}, options = {})
    @Field.Text(name = "Message", required = false,
            attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {})
    public ExtensionResponse healthCheck() {
        return audited("HealthCheck", () -> {
            long heapUsedMb = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / (1024 * 1024);
            boolean healthy = media.isAvailable();
            String message = healthy
                    ? "OK — Krista file store available. Heap in use: " + heapUsedMb + " MB."
                    : "The Krista file store (FileRepository) is not available, so files cannot be "
                            + "read or written. Check the appliance logs for extension startup errors.";

            var out = out();
            out.put("Healthy", healthy);
            out.put("Message", message);
            return ExtensionResponses.ok(out);
        });
    }
}

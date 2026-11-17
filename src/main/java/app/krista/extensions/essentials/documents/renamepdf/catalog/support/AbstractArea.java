package app.krista.extensions.essentials.documents.renamepdf.catalog.support;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponse.Error.ExceptionType;
import app.krista.extension.executor.Invoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared base for every Area. Holds the injected {@link TelemetryHelper} and the {@code invokerId}
 * (multi-tenant scope), and owns the {@code audited(...)} wrappers so each {@code @CatalogRequest}
 * method stays thin: validate &rarr; delegate &rarr; return. The wrapper writes the uniform
 * try/catch &rarr; classify &rarr; telemetry + timing boundary exactly once.
 */
public abstract class AbstractArea {

    protected static final String PREFIX = "renamepdf";

    private static final Logger LOG = LoggerFactory.getLogger(AbstractArea.class);

    protected final TelemetryHelper telemetry;
    protected final String invokerId;

    protected AbstractArea(TelemetryHelper telemetry, Invoker invoker) {
        this.telemetry = telemetry;
        this.invokerId = invoker.getInvokerId();
    }

    @FunctionalInterface
    protected interface CheckedSupplier<T> {
        T get() throws Exception;
    }

    /**
     * The uniform boundary for a request whose output is a field map: timing + telemetry + try/catch
     * &rarr; classified {@link ExtensionResponse}.
     */
    protected ExtensionResponse audited(String name, CheckedSupplier<ExtensionResponse> action) {
        long t0 = telemetry.start(PREFIX + "." + name);
        LOG.info("Catalog '{}' started for invoker {}", name, invokerId);
        try {
            ExtensionResponse r = action.get();
            LOG.info("Catalog '{}' ok in {}ms for invoker {}", name, System.currentTimeMillis() - t0, invokerId);
            telemetry.recordSuccess(PREFIX + "." + name, t0);
            return r;
        } catch (SecurityException e) {
            LOG.error("Catalog '{}' AUTH: {}", name, e.getMessage());
            telemetry.recordError(PREFIX + "." + name, t0, e);
            return ExtensionResponses.failure(e.getMessage(), ExceptionType.AUTHENTICATION_ERROR);
        } catch (IllegalArgumentException e) {
            LOG.warn("Catalog '{}' INPUT: {}", name, e.getMessage());
            telemetry.recordError(PREFIX + "." + name, t0, e);
            return ExtensionResponses.failure(e.getMessage(), ExceptionType.INPUT_ERROR);
        } catch (Exception e) {
            LOG.error("Catalog '{}' SYSTEM: {}", name, e.getMessage(), e);
            telemetry.recordError(PREFIX + "." + name, t0, e);
            return ExtensionResponses.failure(e.getMessage(), ExceptionType.SYSTEM_ERROR);
        }
    }

    /**
     * The same boundary for a request whose declared output is a bare value rather than a field map —
     * here, a {@code @Field.File} output returning {@code app.krista.model.base.File}.
     *
     * <p>There is no {@link ExtensionResponse} to carry a classified error in this shape, so a failure
     * is rethrown for the platform to surface rather than swallowed; the logging and telemetry
     * boundary is identical to {@link #audited}. The original message is preserved so the caller still
     * sees the actionable text produced by validation.
     */
    protected <T> T auditedValue(String name, CheckedSupplier<T> action) {
        long t0 = telemetry.start(PREFIX + "." + name);
        LOG.info("Catalog '{}' started for invoker {}", name, invokerId);
        try {
            T value = action.get();
            LOG.info("Catalog '{}' ok in {}ms for invoker {}", name, System.currentTimeMillis() - t0, invokerId);
            telemetry.recordSuccess(PREFIX + "." + name, t0);
            return value;
        } catch (IllegalArgumentException e) {
            LOG.warn("Catalog '{}' INPUT: {}", name, e.getMessage());
            telemetry.recordError(PREFIX + "." + name, t0, e);
            throw e;
        } catch (Exception e) {
            LOG.error("Catalog '{}' SYSTEM: {}", name, e.getMessage(), e);
            telemetry.recordError(PREFIX + "." + name, t0, e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    protected static Map<String, Object> out() {
        return new LinkedHashMap<>();
    }
}

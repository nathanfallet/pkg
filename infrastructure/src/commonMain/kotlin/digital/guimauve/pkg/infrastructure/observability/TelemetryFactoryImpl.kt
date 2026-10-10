package digital.guimauve.pkg.infrastructure.observability

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.metrics.Meter
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import io.opentelemetry.semconv.ServiceAttributes

/**
 * [TelemetryFactory] over the autoconfigured SDK: everything is set by the standard `OTEL_*`
 * environment variables (`OTEL_EXPORTER_OTLP_ENDPOINT`, `OTEL_EXPORTER_OTLP_PROTOCOL`…), and
 * `OTEL_SDK_DISABLED=true` turns it all off.
 *
 * [enabled] is false under test, and wherever no OTLP endpoint is configured (see [isExportConfigured]):
 * the SDK would otherwise export to `localhost:4317` and log a failure every minute where no collector
 * listens. Telemetry is then a no-op.
 */
class TelemetryFactoryImpl(
    private val enabled: Boolean,
    private val environment: String,
) : TelemetryFactory {

    private val sdk: OpenTelemetry by lazy {
        if (!enabled) return@lazy OpenTelemetry.noop()
        AutoConfiguredOpenTelemetrySdk.builder()
            .addResourceCustomizer { resource, _ ->
                resource.toBuilder()
                    .put(ServiceAttributes.SERVICE_NAME, SERVICE_NAME)
                    .put(DEPLOYMENT_ENVIRONMENT, environment)
                    .build()
            }
            // Also the global instance, for libraries that only look there.
            .setResultAsGlobal()
            .build()
            .openTelemetrySdk
    }

    override fun getOpenTelemetry(): OpenTelemetry = sdk

    override fun getTracer(): Tracer = sdk.getTracer(SERVICE_NAME)

    override fun getMeter(): Meter = sdk.getMeter(SERVICE_NAME)

    companion object {

        private const val SERVICE_NAME = "pkg"

        // Still incubating in semconv, hence spelled out rather than imported.
        private const val DEPLOYMENT_ENVIRONMENT = "deployment.environment.name"

        private val ENDPOINT_VARIABLES = listOf(
            "OTEL_EXPORTER_OTLP_ENDPOINT",
            "OTEL_EXPORTER_OTLP_TRACES_ENDPOINT",
            "OTEL_EXPORTER_OTLP_METRICS_ENDPOINT",
        )

        /** Whether [env] (the process environment) points the OTLP exporter at a collector. */
        fun isExportConfigured(env: Map<String, String>): Boolean =
            ENDPOINT_VARIABLES.any { !env[it].isNullOrBlank() }

    }

}

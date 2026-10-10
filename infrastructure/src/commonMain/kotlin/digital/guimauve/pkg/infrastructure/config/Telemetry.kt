package digital.guimauve.pkg.infrastructure.config

import digital.guimauve.pkg.infrastructure.observability.TelemetryFactory
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.opentelemetry.instrumentation.ktor.v3_0.KtorServerTelemetry
import io.opentelemetry.instrumentation.micrometer.v1_5.OpenTelemetryMeterRegistry
import org.koin.ktor.ext.inject

/**
 * Traces every HTTP call and exports Ktor's server metrics (request counts, latencies, JVM) through
 * OpenTelemetry. The traces go down to each SQL statement (see `MySQLDatabaseFactory`).
 */
fun Application.configureTelemetry() {
    val telemetryFactory by inject<TelemetryFactory>()
    install(KtorServerTelemetry) {
        setOpenTelemetry(telemetryFactory.getOpenTelemetry())
    }
    install(MicrometerMetrics) {
        registry = OpenTelemetryMeterRegistry.create(telemetryFactory.getOpenTelemetry())
    }
}

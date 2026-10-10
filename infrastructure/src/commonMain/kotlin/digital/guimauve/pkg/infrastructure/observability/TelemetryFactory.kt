package digital.guimauve.pkg.infrastructure.observability

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.metrics.Meter
import io.opentelemetry.api.trace.Tracer

/** Owns the OpenTelemetry SDK, and hands out what the instrumented components need from it. */
interface TelemetryFactory {

    /** The OpenTelemetry instance (a no-op one where telemetry is off). */
    fun getOpenTelemetry(): OpenTelemetry

    /** The tracer PKG's own spans are created with. */
    fun getTracer(): Tracer

    /** The meter PKG's own metrics are created with. */
    fun getMeter(): Meter

}

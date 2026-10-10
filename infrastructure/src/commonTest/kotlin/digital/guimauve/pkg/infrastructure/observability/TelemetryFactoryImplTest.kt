package digital.guimauve.pkg.infrastructure.observability

import io.opentelemetry.api.OpenTelemetry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TelemetryFactoryImplTest {

    @Test
    fun disabledTelemetryIsANoOp() {
        val factory = TelemetryFactoryImpl(enabled = false, environment = "test")

        assertEquals(OpenTelemetry.noop(), factory.getOpenTelemetry())
        // Nothing is recorded, nothing is exported.
        val span = factory.getTracer().spanBuilder("probe").startSpan()
        assertFalse(span.isRecording)
        assertFalse(span.spanContext.isValid)
        span.end()
        factory.getMeter().counterBuilder("probe").build().add(1)
    }

    @Test
    fun exportIsOffWithoutAnEndpoint() {
        assertFalse(TelemetryFactoryImpl.isExportConfigured(emptyMap()))
        assertFalse(TelemetryFactoryImpl.isExportConfigured(mapOf("OTEL_EXPORTER_OTLP_ENDPOINT" to " ")))
        // Other OTEL_* variables alone do not point anywhere.
        assertFalse(TelemetryFactoryImpl.isExportConfigured(mapOf("OTEL_SERVICE_NAME" to "pkg")))
    }

    @Test
    fun exportIsOnWithAnEndpoint() {
        assertTrue(TelemetryFactoryImpl.isExportConfigured(mapOf("OTEL_EXPORTER_OTLP_ENDPOINT" to "http://collector:4317")))
        assertTrue(TelemetryFactoryImpl.isExportConfigured(mapOf("OTEL_EXPORTER_OTLP_TRACES_ENDPOINT" to "http://collector:4318/v1/traces")))
        assertTrue(TelemetryFactoryImpl.isExportConfigured(mapOf("OTEL_EXPORTER_OTLP_METRICS_ENDPOINT" to "http://collector:4318/v1/metrics")))
    }

}

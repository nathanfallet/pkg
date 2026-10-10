package digital.guimauve.pkg.infrastructure.observability

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.sdk.OpenTelemetrySdk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
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
    fun enabledTelemetryIsTheAutoconfiguredSdk() {
        // What the OTEL_* variables would set; `none` so the test never tries to reach a collector.
        val properties = mapOf(
            "otel.traces.exporter" to "none",
            "otel.metrics.exporter" to "none",
            "otel.logs.exporter" to "none",
        )
        properties.forEach { (key, value) -> System.setProperty(key, value) }
        try {
            val factory = TelemetryFactoryImpl(enabled = true, environment = "staging")

            val sdk = assertIs<OpenTelemetrySdk>(factory.getOpenTelemetry())
            assertSame(sdk, factory.getOpenTelemetry(), "built once")
            val resource = sdk.sdkTracerProvider.toString()
            assertContains(resource, "service.name=\"pkg\"")
            assertContains(resource, "deployment.environment.name=\"staging\"")
            val span = factory.getTracer().spanBuilder("probe").startSpan()
            assertTrue(span.isRecording)
            span.end()
            factory.getMeter().counterBuilder("probe").build().add(1)

            sdk.shutdown().join(5, java.util.concurrent.TimeUnit.SECONDS)
        } finally {
            properties.keys.forEach(System::clearProperty)
            GlobalOpenTelemetry.resetForTest()
        }
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

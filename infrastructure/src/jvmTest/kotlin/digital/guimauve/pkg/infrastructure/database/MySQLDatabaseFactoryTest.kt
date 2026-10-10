package digital.guimauve.pkg.infrastructure.database

import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The MySQL factory instruments its pool: every statement becomes a span, and the pool reports its
 * connection metrics. Without telemetry (the default no-op) it works the same, recording nothing.
 */
class MySQLDatabaseFactoryTest {

    @Test
    fun worksWithNoOpTelemetry() {
        val factory = MySQLTestContainer.database("noop_telemetry")
        factory.migrate()

        val one = transaction(factory.getDatabase()) {
            exec("SELECT 1") { rs -> rs.next(); rs.getInt(1) }
        }

        assertEquals(1, one)
        assertTrue(factory.isHealthy())
    }

    @Test
    fun statementsAreTracedAndThePoolIsMeasured() {
        val spans = InMemorySpanExporter.create()
        val metrics = InMemoryMetricReader.create()
        val openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(spans)).build())
            .setMeterProvider(SdkMeterProvider.builder().registerMetricReader(metrics).build())
            .build()
        val factory = MySQLTestContainer.database("traced", openTelemetry)

        transaction(factory.getDatabase()) {
            exec("SELECT 1") { rs -> rs.next() }
        }

        assertTrue(
            spans.finishedSpanItems.any { it.name.startsWith("SELECT") },
            "a span per statement, got ${spans.finishedSpanItems.map { it.name }}",
        )
        val metricNames = metrics.collectAllMetrics().map { it.name }
        assertTrue(metricNames.any { it.startsWith("db.client.connection") }, "pool metrics, got $metricNames")
    }

}

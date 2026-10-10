package digital.guimauve.pkg.infrastructure.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.hikaricp.v3_0.HikariTelemetry
import io.opentelemetry.instrumentation.jdbc.datasource.JdbcTelemetry
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database

/**
 * [DatabaseFactory] for MySQL, backed by a HikariCP connection pool.
 */
class MySQLDatabaseFactory(
    private val config: DatabaseConfig,
    private val openTelemetry: OpenTelemetry = OpenTelemetry.noop(),
) : DatabaseFactory {

    private val dataSource: HikariDataSource by lazy {
        HikariDataSource(
            HikariConfig().apply {
                poolName = "hikari-${config.name}"
                jdbcUrl = "jdbc:mysql://${config.host}:${config.port}/${config.name}"
                driverClassName = "com.mysql.cj.jdbc.Driver"
                username = config.user
                password = config.password
                isAutoCommit = false
                maximumPoolSize = config.maximumPoolSize
                minimumIdle = 1
                validationTimeout = 3_000
                connectionTimeout = 30_000
                idleTimeout = 300_000
                maxLifetime = 1_800_000
                keepaliveTime = 600_000
                leakDetectionThreshold = 60_000
                // Pool metrics: connections in use, pending, and how long a connection takes to get.
                metricsTrackerFactory = HikariTelemetry.create(openTelemetry).createMetricsTrackerFactory()
            }
        )
    }

    // Every statement becomes a span, under the HTTP call that ran it.
    private val db: Database by lazy { Database.connect(JdbcTelemetry.create(openTelemetry).wrap(dataSource)) }

    override fun getDatabase(): Database = db

    override fun migrate() = Migrations.migrate(Flyway.configure().dataSource(dataSource))

    override fun isHealthy(): Boolean = !dataSource.isClosed && dataSource.isRunning

}

package digital.guimauve.pkg.infrastructure.database

import io.opentelemetry.api.OpenTelemetry
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.testcontainers.DockerClientFactory
import org.testcontainers.mysql.MySQLContainer
import java.sql.DriverManager

/** One MySQL container (Testcontainers, skipped without Docker) shared by every test class that needs it. */
object MySQLTestContainer {

    // The same major as the Bitnami MySQL chart in helm/ and the generateMigrations task.
    private val container: MySQLContainer by lazy {
        MySQLContainer("mysql:8.0").withUsername("root").withPassword("root").apply { start() }
    }

    /** A new, empty database on the shared container. */
    fun database(name: String, openTelemetry: OpenTelemetry = OpenTelemetry.noop()): MySQLDatabaseFactory {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable, "Docker is needed for the MySQL checks")
        DriverManager.getConnection(container.jdbcUrl, container.username, container.password).use {
            it.createStatement().execute("CREATE DATABASE `$name`")
        }
        return MySQLDatabaseFactory(
            DatabaseConfig(
                protocol = "mysql",
                name = name,
                host = container.host,
                port = container.firstMappedPort,
                user = container.username,
                password = container.password,
                maximumPoolSize = 2,
            ),
            openTelemetry,
        )
    }

}

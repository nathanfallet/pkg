package digital.guimauve.pkg.infrastructure.database

import digital.guimauve.pkg.infrastructure.database.tables.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.testcontainers.DockerClientFactory
import org.testcontainers.mysql.MySQLContainer
import java.sql.DriverManager
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The migrations are the schema; the Exposed tables only describe it. These replay the scripts and
 * fail when the code expects something no migration creates — the table or column added in Kotlin
 * and forgotten in `db/migration`.
 *
 * The diff runs on a real MySQL (Testcontainers, skipped without Docker): on H2 it reports every
 * timestamp and quoted column as changed, so H2 is only checked for running the scripts at all.
 */
class MigrationsTest {

    // Every table in `database.tables` — a new one belongs here too.
    private val tables = arrayOf(
        Organizations,
        Users,
        Sessions,
        Packages,
        PackageVersions,
        PackageVersionFiles,
    )

    @Test
    fun migrationsMatchTheTables() {
        val factory = mysql("fresh")
        factory.migrate()

        assertEquals(emptyList(), missingStatements(factory), "the tables expect a schema no migration creates")
    }

    @Test
    fun aDatabaseFromBeforeFlywayIsBaselinedNotRecreated() {
        // What every database created by `SchemaUtils.create` looks like on its first boot on Flyway.
        val factory = mysql("legacy")
        transaction(factory.getDatabase()) { SchemaUtils.create(*tables) }

        factory.migrate()

        assertEquals(emptyList(), missingStatements(factory))
    }

    @Test
    fun migrationsRunOnH2() {
        val factory = H2DatabaseFactory(
            DatabaseConfig(protocol = "h2", host = "", name = "migrations-test", user = "", password = "")
        )
        factory.migrate()
        // A second boot finds nothing pending.
        factory.migrate()
    }

    private fun missingStatements(factory: DatabaseFactory): List<String> =
        transaction(factory.getDatabase()) {
            MigrationUtils.statementsRequiredForDatabaseMigration(*tables, withLogs = false)
        }

    /** A new, empty database on the shared MySQL container. */
    private fun mysql(name: String): DatabaseFactory {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable, "Docker is needed for the MySQL checks")
        DriverManager.getConnection(container.jdbcUrl, container.username, container.password).use {
            it.createStatement().execute("CREATE DATABASE `$name`")
        }
        return ContainerDatabaseFactory(
            "jdbc:mysql://${container.host}:${container.firstMappedPort}/$name",
            container.username,
            container.password,
        )
    }

    /**
     * Same migrations as [MySQLDatabaseFactory], on the container's mapped port: [DatabaseConfig] has no
     * port, the production factory always dials 3306.
     */
    private class ContainerDatabaseFactory(
        private val url: String,
        private val user: String,
        private val password: String,
    ) : DatabaseFactory {

        private val db: Database by lazy { Database.connect(url, "com.mysql.cj.jdbc.Driver", user, password) }

        override fun getDatabase(): Database = db

        override fun migrate() = Migrations.migrate(Flyway.configure().dataSource(url, user, password))

        override fun isHealthy(): Boolean = true

    }

    companion object {

        // The same major as the Bitnami MySQL chart in helm/ and the generateMigrations task.
        private val container: MySQLContainer by lazy {
            MySQLContainer("mysql:8.0").withUsername("root").withPassword("root").apply { start() }
        }

    }

}

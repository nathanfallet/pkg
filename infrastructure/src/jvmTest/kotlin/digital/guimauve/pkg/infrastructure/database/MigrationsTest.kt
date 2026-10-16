package digital.guimauve.pkg.infrastructure.database

import digital.guimauve.pkg.infrastructure.database.tables.*
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils
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
        PackageDistTags,
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
        val factory = H2DatabaseFactory(DatabaseConfig(protocol = "h2", name = "migrations-test"))
        factory.migrate()
        // A second boot finds nothing pending.
        factory.migrate()
    }

    private fun missingStatements(factory: DatabaseFactory): List<String> =
        transaction(factory.getDatabase()) {
            MigrationUtils.statementsRequiredForDatabaseMigration(*tables, withLogs = false)
        }

    private fun mysql(name: String): DatabaseFactory = MySQLTestContainer.database(name)

}

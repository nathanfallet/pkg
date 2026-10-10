package digital.guimauve.pkg.infrastructure.database

import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database

/**
 * [DatabaseFactory] for H2.
 *
 * With a blank [DatabaseConfig.directory] it runs fully in-memory (what the tests use). With a
 * directory set it opens a file-backed database there, so a local run keeps its data across
 * restarts. The directory is normalized to a form H2 accepts (it rejects a bare relative path, so
 * `data` becomes `./data`).
 */
class H2DatabaseFactory(
    private val config: DatabaseConfig,
) : DatabaseFactory {

    // The scripts quote keyword columns with backticks (`name`), which H2 folds to upper case, while
    // Exposed queries them as "name": case-insensitive identifiers make both find the same column.
    // DB_CLOSE_DELAY=-1 keeps an in-memory database alive between connections; a file one needs no such flag.
    private val url: String by lazy {
        if (config.directory.isBlank()) {
            "jdbc:h2:mem:${config.name};DB_CLOSE_DELAY=-1;MODE=MySQL;CASE_INSENSITIVE_IDENTIFIERS=TRUE;"
        } else {
            "jdbc:h2:file:${normalizeDirectory(config.directory)}/${config.name};MODE=MySQL;CASE_INSENSITIVE_IDENTIFIERS=TRUE;"
        }
    }

    private val db: Database by lazy { Database.connect(url, "org.h2.Driver") }

    override fun getDatabase(): Database = db

    // Same MySQL scripts as production: H2 runs them in its MySQL compatibility mode.
    override fun migrate() = Migrations.migrate(Flyway.configure().dataSource(url, "", ""))

    // A file-backed H2 is up for as long as the process is, same as in-memory.
    override fun isHealthy(): Boolean = true

    internal companion object {

        /** H2 rejects a bare relative path: `data` becomes `./data`, absolute and home paths are kept. */
        fun normalizeDirectory(directory: String): String {
            val trimmed = directory.trimEnd('/')
            return if (trimmed.startsWith("/") || trimmed.startsWith("~") || trimmed.startsWith("./")) {
                trimmed
            } else {
                "./$trimmed"
            }
        }

    }

}

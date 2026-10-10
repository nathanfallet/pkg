package digital.guimauve.pkg.infrastructure.database

import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database

/**
 * [DatabaseFactory] for an in-memory H2 database, which is what the tests run against.
 */
class H2DatabaseFactory(
    private val config: DatabaseConfig,
) : DatabaseFactory {

    // The scripts quote keyword columns with backticks (`name`), which H2 folds to upper case, while
    // Exposed queries them as "name": case-insensitive identifiers make both find the same column.
    private val url = "jdbc:h2:mem:${config.name};DB_CLOSE_DELAY=-1;MODE=MySQL;CASE_INSENSITIVE_IDENTIFIERS=TRUE;"

    private val db: Database by lazy { Database.connect(url, "org.h2.Driver") }

    override fun getDatabase(): Database = db

    // Same MySQL scripts as production: H2 runs them in its MySQL compatibility mode.
    override fun migrate() = Migrations.migrate(Flyway.configure().dataSource(url, "", ""))

    // An in-memory database is up for as long as the process is.
    override fun isHealthy(): Boolean = true

}

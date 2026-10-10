package digital.guimauve.pkg.infrastructure.database.repositories

import digital.guimauve.pkg.infrastructure.database.DatabaseConfig
import digital.guimauve.pkg.infrastructure.database.H2DatabaseFactory
import digital.guimauve.pkg.infrastructure.database.TransactionManager
import digital.guimauve.pkg.infrastructure.database.TransactionManagerImpl
import kotlin.uuid.Uuid

/**
 * A new in-memory H2 database, migrated with the same Flyway scripts as production.
 *
 * Its name is random, so every test gets a database of its own: no test sees the rows of another,
 * and nothing has to be cleaned up between them.
 */
internal fun migratedDatabase(): TransactionManager {
    val factory = H2DatabaseFactory(
        DatabaseConfig(protocol = "h2", host = "", name = "repositories-${Uuid.random()}", user = "", password = "")
    )
    factory.migrate()
    return TransactionManagerImpl(factory)
}

package digital.guimauve.pkg.infrastructure.database.repositories

import digital.guimauve.pkg.infrastructure.database.DatabaseConfig
import digital.guimauve.pkg.infrastructure.database.H2DatabaseFactory
import digital.guimauve.pkg.infrastructure.database.TransactionManager
import digital.guimauve.pkg.infrastructure.database.TransactionManagerImpl
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Instant
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

/**
 * Asserts that [stored] is [written] as a `DATETIME(6)` keeps it. The JVM clock has nanoseconds on
 * Linux (microseconds on macOS), so what a `create` returns and what any read returns can differ by
 * less than a microsecond.
 */
internal fun assertStoredInstant(written: Instant, stored: Instant) =
    assertTrue((written - stored).absoluteValue < 1.microseconds, "$written stored as $stored")

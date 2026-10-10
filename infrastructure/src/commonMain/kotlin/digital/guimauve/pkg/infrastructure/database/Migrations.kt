package digital.guimauve.pkg.infrastructure.database

import org.flywaydb.core.api.configuration.FluentConfiguration

/**
 * Brings the schema up to date with the versioned SQL scripts in `db/migration` (Flyway). The
 * scripts are the source of truth for the schema; the Exposed tables only describe it to the code.
 * Write a new one with `./gradlew :infrastructure:generateMigrations`, then read it before keeping it.
 */
internal object Migrations {

    private const val LOCATION = "classpath:db/migration"

    /**
     * The version of `V1__baseline.sql`: the schema as it stood when the tables were still created by
     * `SchemaUtils.create`. A database from that era has tables but no Flyway history, so Flyway
     * records it at this version instead of replaying the baseline on top of it.
     */
    private const val BASELINE_VERSION = "1"

    fun migrate(configuration: FluentConfiguration) {
        configuration
            .locations(LOCATION)
            .baselineOnMigrate(true)
            .baselineVersion(BASELINE_VERSION)
            // The Exposed plugin names migrations by timestamp (V<yyyyMMddHHmmss>__…), so two parallel
            // branches each write their own. The one merged after a newer one must still be applied,
            // instead of Flyway refusing to start.
            .outOfOrder(true)
            // After a rollback, or during a rolling update, the previous image finds in the database a
            // migration it does not ship. It must still start. "*:future" is already Flyway's default;
            // "*:missing" is added.
            .ignoreMigrationPatterns("*:future", "*:missing")
            // If `db/migration` is missing from the jar (renamed, not packaged), stop here instead of
            // booting on an empty schema.
            .failOnMissingLocations(true)
            .load()
            .migrate()
    }

}

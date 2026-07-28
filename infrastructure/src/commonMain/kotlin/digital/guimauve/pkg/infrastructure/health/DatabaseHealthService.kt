package digital.guimauve.pkg.infrastructure.health

import digital.guimauve.pkg.domain.services.HealthService
import digital.guimauve.pkg.infrastructure.database.DatabaseFactory

/**
 * Implementation of [HealthService] reading the state of the connection pool.
 */
class DatabaseHealthService(
    private val databaseFactory: DatabaseFactory,
) : HealthService {

    override suspend fun isDatabaseHealthy(): Boolean = databaseFactory.isHealthy()

}

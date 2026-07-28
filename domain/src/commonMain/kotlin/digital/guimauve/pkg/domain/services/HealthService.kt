package digital.guimauve.pkg.domain.services

/**
 * Interface for the checks the readiness probe reads.
 */
interface HealthService {

    /**
     * Whether the database can be reached.
     */
    suspend fun isDatabaseHealthy(): Boolean

}

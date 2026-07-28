package digital.guimauve.pkg.presentation.routes.health

import digital.guimauve.pkg.infrastructure.database.DatabaseFactory
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Dependencies required for the health routes.
 */
data class HealthRoutesDependencies(
    val databaseFactory: DatabaseFactory,
)

/**
 * Configures the endpoints the kubernetes probes read (see `helm/pkg/values.yaml`).
 */
fun Route.healthRoutes(dependencies: HealthRoutesDependencies) = with(dependencies) {
    // Liveness: the process answers, so it must not be restarted.
    get("/healthz") {
        call.respond(mapOf("alive" to true))
    }

    // Readiness: the process can serve traffic, which means reaching the database.
    get("/readyz") {
        val database = databaseFactory.isHealthy()
        call.respond(
            if (database) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable,
            mapOf("database" to database)
        )
    }
}

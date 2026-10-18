package digital.guimauve.pkg.infrastructure.config

import io.ktor.server.application.*
import digital.guimauve.pkg.infrastructure.database.DatabaseFactory
import digital.guimauve.pkg.infrastructure.database.DatabaseUserAdministrationService
import org.koin.ktor.ext.get

/**
 * Migrates the schema at boot, before anything reads or writes. A failed migration stops the app
 * here instead of surfacing later as a missing table or column on the first request.
 */
fun Application.configureDatabase() {
    get<DatabaseFactory>().migrate()
    get<DatabaseUserAdministrationService>().bootstrap()?.let {
        log.warn("Bootstrap administrator created: username=admin temporaryPassword=${it.password}. Change it at /auth/login.")
    }
}

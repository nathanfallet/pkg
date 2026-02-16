package digital.guimauve.pkg.presentation.config

import dev.kaccelero.commons.sessions.ISessionsRepository
import digital.guimauve.pkg.models.auth.SessionPayload
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import org.koin.ktor.ext.inject

fun Application.configureSessions() {
    val sessionsRepository by inject<ISessionsRepository>()

    install(Sessions) {
        cookie<SessionPayload>("session", sessionsRepository) {
            cookie.path = "/"
        }
    }
}

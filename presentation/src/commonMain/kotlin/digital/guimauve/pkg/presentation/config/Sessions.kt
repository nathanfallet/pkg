package digital.guimauve.pkg.presentation.config

import digital.guimauve.pkg.presentation.models.SessionPayload
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import org.koin.ktor.ext.inject

fun Application.configureSessions() {
    val sessionStorage by inject<SessionStorage>()

    install(Sessions) {
        cookie<SessionPayload>("session", sessionStorage) {
            cookie.path = "/"
        }
    }
}

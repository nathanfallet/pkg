package digital.guimauve.pkg.presentation.config

import digital.guimauve.pkg.domain.exceptions.auth.InvalidCredentialsException
import digital.guimauve.pkg.domain.exceptions.auth.InvalidTokenException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

/**
 * Configures error handling for the Ktor application.
 */
fun Application.configureErrorHandling() {
    install(StatusPages) {
        // Auth
        exception<InvalidCredentialsException> { call, _ ->
            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to "Invalid credentials")
            )
        }
        exception<InvalidTokenException> { call, _ ->
            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to "Invalid token")
            )
        }

        // Generic (500)
        exception<Throwable> { call, cause ->
            cause.printStackTrace() // for debugging purposes
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Internal server error")
            )
        }
    }
}

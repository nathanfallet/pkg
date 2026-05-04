package digital.guimauve.pkg.presentation.extensions

import digital.guimauve.pkg.domain.exceptions.auth.InvalidTokenException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import kotlin.uuid.Uuid

/**
 * Retrieves the user ID from the JWT principal in the application call.
 *
 * @return The user ID as a [Uuid] if present, otherwise null.
 */
fun ApplicationCall.userIdOrNull(): Uuid? {
    return principal<JWTPrincipal>()?.payload?.subject?.let(Uuid::parse)
}

/**
 * Retrieves the user ID from the JWT principal in the application call.
 *
 * @return The user ID as a [Uuid].
 * @throws InvalidTokenException if the user ID is not present.
 */
fun ApplicationCall.userId(): Uuid {
    return userIdOrNull() ?: throw InvalidTokenException()
}

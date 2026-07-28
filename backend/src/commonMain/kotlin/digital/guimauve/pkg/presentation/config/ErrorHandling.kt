package digital.guimauve.pkg.presentation.config

import digital.guimauve.pkg.domain.exceptions.auth.InvalidCredentialsException
import digital.guimauve.pkg.domain.exceptions.auth.InvalidTokenException
import digital.guimauve.pkg.domain.exceptions.organizations.OrganizationForbiddenException
import digital.guimauve.pkg.domain.exceptions.organizations.OrganizationNotFoundException
import digital.guimauve.pkg.domain.exceptions.packages.PackageNotFoundException
import digital.guimauve.pkg.domain.exceptions.packages.versions.PackageVersionNotFoundException
import digital.guimauve.pkg.domain.exceptions.users.UserNotFoundException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

/**
 * Responds with the error key the i18n bundle and the templates already use, so that the API and
 * the dashboard name failures the same way.
 */
private suspend fun ApplicationCall.respondError(status: HttpStatusCode, key: String) =
    respond(status, mapOf("error" to key))

/**
 * Configures error handling for the Ktor application.
 */
fun Application.configureErrorHandling() {
    install(StatusPages) {
        // Auth
        exception<InvalidCredentialsException> { call, _ ->
            call.respondError(HttpStatusCode.Unauthorized, "auth_invalid_credentials")
        }
        exception<InvalidTokenException> { call, _ ->
            call.respondError(HttpStatusCode.Unauthorized, "auth_invalid_token")
        }

        // Organizations
        exception<OrganizationNotFoundException> { call, _ ->
            call.respondError(HttpStatusCode.NotFound, "organizations_not_found")
        }
        exception<OrganizationForbiddenException> { call, _ ->
            call.respondError(HttpStatusCode.Forbidden, "organizations_not_allowed")
        }

        // Users
        exception<UserNotFoundException> { call, _ ->
            call.respondError(HttpStatusCode.NotFound, "users_not_found")
        }

        // Packages
        exception<PackageNotFoundException> { call, _ ->
            call.respondError(HttpStatusCode.NotFound, "packages_not_found")
        }
        exception<PackageVersionNotFoundException> { call, _ ->
            call.respondError(HttpStatusCode.NotFound, "package_versions_not_found")
        }

        // Generic (500)
        exception<Throwable> { call, cause ->
            cause.printStackTrace() // for debugging purposes
            call.respondError(HttpStatusCode.InternalServerError, "error_internal")
        }
    }
}

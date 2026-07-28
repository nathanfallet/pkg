package digital.guimauve.pkg.presentation.config

import digital.guimauve.pkg.domain.exceptions.auth.InvalidCredentialsException
import digital.guimauve.pkg.domain.exceptions.auth.InvalidTokenException
import digital.guimauve.pkg.domain.exceptions.organizations.OrganizationForbiddenException
import digital.guimauve.pkg.domain.exceptions.organizations.OrganizationNotFoundException
import digital.guimauve.pkg.domain.exceptions.packages.PackageNotFoundException
import digital.guimauve.pkg.domain.exceptions.packages.PackagePrivateException
import digital.guimauve.pkg.domain.exceptions.packages.PackageWriteForbiddenException
import digital.guimauve.pkg.domain.exceptions.packages.maven.InvalidMavenPathException
import digital.guimauve.pkg.domain.exceptions.packages.versions.PackageVersionNotFoundException
import digital.guimauve.pkg.domain.exceptions.packages.versions.files.FileNotUploadedException
import digital.guimauve.pkg.domain.exceptions.packages.versions.files.PackageVersionFileAlreadyExistsException
import digital.guimauve.pkg.domain.exceptions.packages.versions.files.PackageVersionFileNotFoundException
import digital.guimauve.pkg.domain.exceptions.storage.StorageFileNotFoundException
import digital.guimauve.pkg.domain.exceptions.users.UserNotFoundException
import digital.guimauve.pkg.domain.services.TranslateService
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.presentation.extensions.language
import digital.guimauve.pkg.presentation.extensions.respondView
import digital.guimauve.pkg.presentation.extensions.userOrNull
import digital.guimauve.pkg.presentation.mappers.users.toUserView
import digital.guimauve.pkg.presentation.views.ErrorPageView
import digital.guimauve.pkg.presentation.views.LayoutView
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject

/**
 * A browser gets the error page of the dashboard, anything else gets the error key the i18n
 * bundle and the templates already use, so the API and the dashboard name failures the same way.
 */
private suspend fun ApplicationCall.respondError(
    status: HttpStatusCode,
    key: String,
    translateService: TranslateService,
    getUserUseCase: GetUserUseCase,
) {
    if (request.acceptItems().none { it.value == ContentType.Text.Html.toString() }) {
        return respond(status, mapOf("error" to key))
    }
    val layout = LayoutView("error_title", userOrNull(getUserUseCase)?.toUserView())
    respondView(
        "public/error.ftl",
        ErrorPageView(layout, status.value.toString(), translateService.translate(key, language())),
        status
    )
}

/**
 * Configures error handling for the Ktor application.
 */
fun Application.configureErrorHandling() {
    val translateService by inject<TranslateService>()
    val getUserUseCase by inject<GetUserUseCase>()

    suspend fun ApplicationCall.fail(status: HttpStatusCode, key: String) =
        respondError(status, key, translateService, getUserUseCase)

    install(StatusPages) {
        // Auth
        exception<InvalidCredentialsException> { call, _ ->
            call.fail(HttpStatusCode.Unauthorized, "auth_invalid_credentials")
        }
        exception<InvalidTokenException> { call, _ ->
            call.fail(HttpStatusCode.Unauthorized, "auth_invalid_token")
        }

        // Organizations
        exception<OrganizationNotFoundException> { call, _ ->
            call.fail(HttpStatusCode.NotFound, "organizations_not_found")
        }
        exception<OrganizationForbiddenException> { call, _ ->
            call.fail(HttpStatusCode.Forbidden, "organizations_not_allowed")
        }

        // Users
        exception<UserNotFoundException> { call, _ ->
            call.fail(HttpStatusCode.NotFound, "users_not_found")
        }

        // Packages
        exception<PackageNotFoundException> { call, _ ->
            call.fail(HttpStatusCode.NotFound, "packages_not_found")
        }
        exception<PackagePrivateException> { call, _ ->
            call.fail(HttpStatusCode.Unauthorized, "packages_private")
        }
        exception<PackageWriteForbiddenException> { call, _ ->
            call.fail(HttpStatusCode.Forbidden, "packages_write_forbidden")
        }
        exception<InvalidMavenPathException> { call, _ ->
            call.fail(HttpStatusCode.NotFound, "invalid_path")
        }
        exception<PackageVersionNotFoundException> { call, _ ->
            call.fail(HttpStatusCode.NotFound, "package_versions_not_found")
        }

        // Files
        exception<PackageVersionFileNotFoundException> { call, _ ->
            call.fail(HttpStatusCode.NotFound, "package_version_files_not_found")
        }
        exception<PackageVersionFileAlreadyExistsException> { call, _ ->
            call.fail(HttpStatusCode.BadRequest, "files_already_exists")
        }
        exception<FileNotUploadedException> { call, _ ->
            call.fail(HttpStatusCode.BadRequest, "file_not_uploaded")
        }
        exception<StorageFileNotFoundException> { call, _ ->
            call.fail(HttpStatusCode.NotFound, "storage_file_not_found")
        }

        // Malformed request bodies and path parameters, which Ktor wraps for us
        exception<BadRequestException> { call, _ ->
            call.fail(HttpStatusCode.BadRequest, "error_body_invalid")
        }

        // Generic (500)
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled failure on ${call.request.path()}", cause)
            call.fail(HttpStatusCode.InternalServerError, "error_internal")
        }

        // Unmatched routes
        status(HttpStatusCode.NotFound) { call, status ->
            call.fail(status, "error_not_found")
        }
    }
}

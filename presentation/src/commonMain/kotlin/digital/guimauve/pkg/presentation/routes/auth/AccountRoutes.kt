package digital.guimauve.pkg.presentation.routes.auth

import digital.guimauve.pkg.domain.exceptions.auth.InvalidCredentialsException
import digital.guimauve.pkg.domain.services.AccountFailure
import digital.guimauve.pkg.domain.services.UserAdministrationService
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.presentation.extensions.*
import digital.guimauve.pkg.presentation.mappers.users.toUserView
import digital.guimauve.pkg.presentation.mappers.organizations.toOrganizationView
import digital.guimauve.pkg.presentation.views.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray
import java.security.MessageDigest
import kotlin.uuid.Uuid

/** All mutations use a session-bound CSRF token. Passwords are never put in a URL or session. */
private suspend fun ApplicationCall.accountForm(): Parameters {
    if (request.contentType().withoutParameters() != ContentType.Application.FormUrlEncoded)
        throw AccountFailure(400, "error_body_invalid")
    val bytes = receiveChannel().readRemaining(16_385).readByteArray()
    if (bytes.size > 16_384) throw AccountFailure(413, "error_body_invalid")
    val fields = parseQueryString(bytes.decodeToString())
    val expected = session()?.csrfToken ?: throw InvalidCredentialsException()
    if (!MessageDigest.isEqual(expected.encodeToByteArray(), fields["csrf"].orEmpty().encodeToByteArray()))
        throw AccountFailure(403, "auth_form_expired")
    return fields
}

private fun ApplicationCall.accountPageHeaders() {
    response.headers.append(HttpHeaders.CacheControl, "no-store")
    response.headers.append("Content-Security-Policy", "frame-ancestors 'none'")
    response.headers.append("Referrer-Policy", "no-referrer")
}

private fun String?.accountId(): Uuid = try { Uuid.parse(this.orEmpty()) }
catch (_: IllegalArgumentException) { throw AccountFailure(400, "error_body_invalid") }

fun Route.accountRoutes(service: UserAdministrationService, getUserUseCase: GetUserUseCase) {
    route("/auth/password") {
        get {
            val user = call.userOrNull(getUserUseCase, allowTemporaryPassword = true) ?: return@get call.respondRedirect("/auth/login")
            val session = call.session() ?: throw InvalidCredentialsException()
            call.setSession(session) // Persist the CSRF token when upgrading an old session.
            call.accountPageHeaders()
            call.respondView("public/auth/password.ftl", PasswordPageView(
                LayoutView("auth_password_title", user.toUserView()), session.csrfToken,
            ))
        }
        post {
            val user = call.userOrNull(getUserUseCase, allowTemporaryPassword = true) ?: throw InvalidCredentialsException()
            val fields = call.accountForm()
            service.changePassword(user, fields["current"].orEmpty(), fields["replacement"].orEmpty())
            call.clearSession()
            call.respondRedirect("/auth/login")
        }
    }
    route("/admin/users") {
        get {
            val user = call.userOrNull(getUserUseCase, allowTemporaryPassword = true) ?: return@get call.respondRedirect("/auth/login")
            if (user.passwordChangeRequired) return@get call.respondRedirect("/auth/password")
            val overview = service.overview(user)
            call.setSession(call.session() ?: throw InvalidCredentialsException())
            call.accountPageHeaders()
            call.respondView("public/users/manage.ftl", AdministrationPageView(
                LayoutView("auth_administration_title", user.toUserView()),
                call.session()?.csrfToken ?: throw InvalidCredentialsException(),
                overview.users.map { it.toUserView() }, overview.organizations.map { it.toOrganizationView() },
            ))
        }
        post {
            val user = call.requireUser(getUserUseCase)
            val fields = call.accountForm()
            val created = service.create(user, fields["username"].orEmpty(), fields["organization"].accountId(), fields["admin"] == "on")
            call.accountPageHeaders()
            call.respondView("public/users/temporary-password.ftl", TemporaryPasswordPageView(
                LayoutView("auth_temporary_title", user.toUserView()), created.user.email, created.password,
            ))
        }
        post("/{id}/update") {
            val user = call.requireUser(getUserUseCase)
            val fields = call.accountForm()
            service.update(user, call.parameters["id"].accountId(), fields["admin"] == "on", fields["disabled"] == "on")
            call.respondRedirect("/admin/users")
        }
        post("/{id}/reset-password") {
            val user = call.requireUser(getUserUseCase)
            call.accountForm()
            val reset = service.resetPassword(user, call.parameters["id"].accountId())
            call.accountPageHeaders()
            call.respondView("public/users/temporary-password.ftl", TemporaryPasswordPageView(
                LayoutView("auth_temporary_title", user.toUserView()), reset.user.email, reset.password,
            ))
        }
    }
}

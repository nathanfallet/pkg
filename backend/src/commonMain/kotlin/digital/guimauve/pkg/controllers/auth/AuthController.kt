package digital.guimauve.pkg.controllers.auth

import dev.kaccelero.commons.exceptions.ControllerException
import dev.kaccelero.commons.responses.RedirectResponse
import digital.guimauve.pkg.domain.usecases.auth.LoginUseCase
import digital.guimauve.pkg.models.auth.LoginPayload
import digital.guimauve.pkg.models.auth.SessionPayload
import digital.guimauve.pkg.presentation.extensions.clearSession
import digital.guimauve.pkg.presentation.extensions.setSession
import io.ktor.http.*
import io.ktor.server.application.*

class AuthController(
    private val loginUseCase: LoginUseCase,
) : IAuthController {

    override fun login() {}

    override suspend fun login(call: ApplicationCall, payload: LoginPayload, redirect: String?): RedirectResponse {
        val user = loginUseCase(payload)
            ?: throw ControllerException(HttpStatusCode.Unauthorized, "auth_invalid_credentials")
        call.setSession(SessionPayload(user.id))
        return RedirectResponse(redirect ?: "/")
    }

    override suspend fun logout(call: ApplicationCall, redirect: String?): RedirectResponse {
        call.clearSession()
        return RedirectResponse(redirect ?: "/")
    }

}

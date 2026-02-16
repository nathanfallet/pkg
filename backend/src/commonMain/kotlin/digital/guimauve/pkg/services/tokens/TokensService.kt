package digital.guimauve.pkg.services.tokens

import digital.guimauve.pkg.domain.usecases.auth.LoginUseCase
import digital.guimauve.pkg.models.auth.LoginPayload
import io.ktor.server.auth.*

class TokensService(
    private val loginUseCase: LoginUseCase,
) : ITokensService {

    override val basicAuthenticationFunction: AuthenticationFunction<UserPasswordCredential> = {
        loginUseCase(LoginPayload(it.name, it.password))?.let { user ->
            UserIdPrincipal(user.id.toString())
        }
    }

    override val bearerAuthenticationFunction: AuthenticationFunction<BearerTokenCredential> = {
        // TODO (for npm)
        //UserIdPrincipal()
        null
    }

}

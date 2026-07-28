package digital.guimauve.pkg.infrastructure.config

import digital.guimauve.pkg.domain.exceptions.auth.InvalidTokenException
import digital.guimauve.pkg.domain.models.auth.LoginPayload
import digital.guimauve.pkg.domain.usecases.auth.LoginUseCase
import digital.guimauve.pkg.infrastructure.jwt.JwtTokenService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.koin.ktor.ext.inject

fun Application.configureSecurity() {
    val tokenService by inject<JwtTokenService>()
    val loginUseCase by inject<LoginUseCase>()

    authentication {
        // The API clients, which carry a token
        jwt("api-jwt") {
            verifier(tokenService.verifier)
            validate { JWTPrincipal(it.payload) }
            // Let the status pages own the response shape, rather than writing it a second time.
            challenge { _, _ -> throw InvalidTokenException() }
        }

        // The maven and PyPI clients, which send the credentials of the user
        basic("auth-basic") {
            validate { credential ->
                loginUseCase(LoginPayload(credential.name, credential.password))?.let {
                    UserIdPrincipal(it.id.toString())
                }
            }
        }

        // The npm client, which sends the token it got from `npm login`
        bearer("auth-bearer") {
            authenticate {
                // TODO: implement the npm web login flow
                null
            }
        }
    }
}

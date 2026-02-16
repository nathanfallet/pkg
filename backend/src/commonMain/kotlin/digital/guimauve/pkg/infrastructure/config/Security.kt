package digital.guimauve.pkg.infrastructure.config

import digital.guimauve.pkg.services.tokens.IJWTService
import digital.guimauve.pkg.services.tokens.ITokensService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.koin.ktor.ext.inject

fun Application.configureSecurity() {
    val jwtService by inject<IJWTService>()
    val tokensService by inject<ITokensService>()

    authentication {
        jwt("api-jwt") {
            verifier(jwtService.verifier)
            validate(jwtService.authenticationFunction)
            challenge(jwtService.challenge)
        }
        basic("auth-basic") {
            validate(tokensService.basicAuthenticationFunction)
        }
        bearer("auth-bearer") {
            authenticate(tokensService.bearerAuthenticationFunction)
        }
    }
}

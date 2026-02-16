package digital.guimauve.pkg.services.tokens

import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import kotlin.uuid.Uuid

interface IJWTService {

    val verifier: JWTVerifier
    val authenticationFunction: AuthenticationFunction<JWTCredential>
    val challenge: JWTAuthChallengeFunction

    fun generateJWT(userId: Uuid, type: String): String
    fun verifyJWT(token: String): DecodedJWT?

}

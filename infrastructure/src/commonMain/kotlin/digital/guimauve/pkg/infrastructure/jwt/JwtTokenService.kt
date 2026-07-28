package digital.guimauve.pkg.infrastructure.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.JWTVerifier
import digital.guimauve.pkg.domain.models.auth.TokenType
import digital.guimauve.pkg.domain.services.TokenService
import java.util.*
import kotlin.time.Duration.Companion.days
import kotlin.uuid.Uuid

/**
 * JWT implementation of [TokenService].
 */
class JwtTokenService(
    private val secret: String,
    private val issuer: String,
    private val audience: String,
    private val accessTokenExpiration: Long = 7.days.inWholeMilliseconds,
    private val refreshTokenExpiration: Long = 365.days.inWholeMilliseconds,
) : TokenService {

    /**
     * Exposed so that the Ktor `jwt` provider can validate the incoming tokens with it.
     */
    val verifier: JWTVerifier = JWT.require(Algorithm.HMAC256(secret))
        .withAudience(audience)
        .withIssuer(issuer)
        .build()

    override fun generateToken(userId: Uuid, type: TokenType): String {
        val expiration = when (type) {
            TokenType.ACCESS -> accessTokenExpiration
            TokenType.REFRESH -> refreshTokenExpiration
        }
        return JWT.create()
            .withSubject(userId.toString())
            .withAudience(audience)
            .withIssuer(issuer)
            .withExpiresAt(Date(System.currentTimeMillis() + expiration))
            .sign(Algorithm.HMAC256(secret))
    }

    override fun verifyToken(token: String): Uuid? = try {
        verifier.verify(token).subject?.let(Uuid::parse)
    } catch (exception: JWTVerificationException) {
        null
    }

}

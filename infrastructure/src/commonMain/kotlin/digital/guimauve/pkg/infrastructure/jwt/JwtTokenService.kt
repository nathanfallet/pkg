package digital.guimauve.pkg.infrastructure.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.JWTVerifier
import com.auth0.jwt.interfaces.Payload
import digital.guimauve.pkg.domain.exceptions.auth.InvalidCredentialsException
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
    private val credentialVersion: (Uuid) -> Int? = { 0 },
) : TokenService {

    /**
     * Exposed so that the Ktor `jwt` provider can validate the incoming tokens with it.
     */
    val verifier: JWTVerifier = JWT.require(Algorithm.HMAC256(secret))
        .withAudience(audience)
        .withIssuer(issuer)
        .build()

    override fun generateToken(userId: Uuid, type: TokenType, expectedCredentialVersion: Int?): String {
        val version = credentialVersion(userId)
        if (version == null || (expectedCredentialVersion != null && version != expectedCredentialVersion))
            throw InvalidCredentialsException()
        val expiration = when (type) {
            TokenType.ACCESS -> accessTokenExpiration
            TokenType.REFRESH -> refreshTokenExpiration
        }
        return JWT.create()
            .withSubject(userId.toString())
            .withClaim("credential_version", version)
            .withAudience(audience)
            .withIssuer(issuer)
            .withExpiresAt(Date(System.currentTimeMillis() + expiration))
            .sign(Algorithm.HMAC256(secret))
    }

    /** Called only after Ktor or [verifier] has verified the signature, issuer and expiration. */
    fun isCurrent(payload: Payload): Boolean = try {
        val id = payload.subject?.let(Uuid::parse)
        id != null && (payload.getClaim("credential_version").asInt() ?: 0) == credentialVersion(id)
    } catch (_: IllegalArgumentException) {
        false
    }

    override fun verifyToken(token: String): Uuid? = try {
        val decoded = verifier.verify(token)
        decoded.subject?.let(Uuid::parse)?.takeIf { isCurrent(decoded) }
    } catch (exception: JWTVerificationException) {
        null
    } catch (exception: IllegalArgumentException) {
        null
    }

}

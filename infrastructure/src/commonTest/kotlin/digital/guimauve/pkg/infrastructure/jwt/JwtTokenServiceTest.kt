package digital.guimauve.pkg.infrastructure.jwt

import com.auth0.jwt.JWT
import digital.guimauve.pkg.domain.models.auth.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

/** A token is only worth the user it names when this server signed it, for this audience, recently enough. */
class JwtTokenServiceTest {

    private val userId = Uuid.random()
    private val service = JwtTokenService("secret", "pkg", "pkg-clients")

    @Test
    fun `a token resolves to the user it was issued for`() {
        assertEquals(userId, service.verifyToken(service.generateToken(userId, TokenType.ACCESS)))
        assertEquals(userId, service.verifyToken(service.generateToken(userId, TokenType.REFRESH)))
    }

    @Test
    fun `a token signed with another secret is refused`() {
        val forged = JwtTokenService("other secret", "pkg", "pkg-clients").generateToken(userId, TokenType.ACCESS)

        assertNull(service.verifyToken(forged))
    }

    @Test
    fun `a token for another issuer or audience is refused`() {
        val otherIssuer = JwtTokenService("secret", "someone else", "pkg-clients").generateToken(userId, TokenType.ACCESS)
        val otherAudience = JwtTokenService("secret", "pkg", "someone else").generateToken(userId, TokenType.ACCESS)

        assertNull(service.verifyToken(otherIssuer))
        assertNull(service.verifyToken(otherAudience))
    }

    @Test
    fun `an expired token is refused`() {
        val expired = JwtTokenService("secret", "pkg", "pkg-clients", accessTokenExpiration = -1.minutes.inWholeMilliseconds)
            .generateToken(userId, TokenType.ACCESS)

        assertNull(service.verifyToken(expired))
    }

    @Test
    fun `garbage is refused rather than thrown`() {
        assertNull(service.verifyToken("not a token"))
        assertNull(service.verifyToken(""))
    }

    @Test
    fun `an access token lasts a week, a refresh token a year`() {
        val now = System.currentTimeMillis()
        val access = JWT.decode(service.generateToken(userId, TokenType.ACCESS)).expiresAt.time - now
        val refresh = JWT.decode(service.generateToken(userId, TokenType.REFRESH)).expiresAt.time - now

        // A minute of margin either way: the expiry is written in whole seconds, and the clock moves meanwhile.
        assertTrue(access in (7.days - 1.minutes).inWholeMilliseconds..(7.days + 1.minutes).inWholeMilliseconds, "access: $access")
        assertTrue(refresh in (365.days - 1.minutes).inWholeMilliseconds..(365.days + 1.minutes).inWholeMilliseconds, "refresh: $refresh")
    }

}

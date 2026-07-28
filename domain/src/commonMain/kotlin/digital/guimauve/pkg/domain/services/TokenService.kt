package digital.guimauve.pkg.domain.services

import digital.guimauve.pkg.domain.models.auth.TokenType
import kotlin.uuid.Uuid

/**
 * Interface for issuing and reading the tokens authenticating the API clients.
 */
interface TokenService {

    /**
     * Issues a token for a user.
     *
     * @param userId The ID of the user the token authenticates.
     * @param type The kind of token, which decides how long it stays valid.
     *
     * @return The token.
     */
    fun generateToken(userId: Uuid, type: TokenType): String

    /**
     * Reads the user a token authenticates.
     *
     * Note that this does not tell an access token from a refresh token: the tokens carry no
     * claim saying which they are.
     *
     * @param token The token to read.
     *
     * @return The ID of the user, or null if the token is not valid.
     */
    fun verifyToken(token: String): Uuid?

}

package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.domain.models.users.User

/**
 * Reads the user a refresh token authenticates, or null if the token is not valid or its user no
 * longer exists. The tokens carry no type, so an access token is accepted too.
 */
interface GetUserForRefreshTokenUseCase {
    suspend operator fun invoke(input: String): User?
}

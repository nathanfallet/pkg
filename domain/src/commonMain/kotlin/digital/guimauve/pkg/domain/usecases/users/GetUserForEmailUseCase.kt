package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.domain.models.users.User

/**
 * Reads a user by email address, or null if none matches. [User.password] holds the hash only when
 * `includePassword` is true.
 */
interface GetUserForEmailUseCase {
    suspend operator fun invoke(email: String, includePassword: Boolean): User?
}

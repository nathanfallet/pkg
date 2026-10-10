package digital.guimauve.pkg.domain.usecases.auth

import digital.guimauve.pkg.domain.models.auth.LoginPayload
import digital.guimauve.pkg.domain.models.users.User

/**
 * Checks an email and password. An unknown address costs as much time as a wrong password, so the
 * answer does not tell which addresses are registered.
 *
 * Returns the user (with its password hash), or null if the credentials don't match.
 */
interface LoginUseCase {
    suspend operator fun invoke(input: LoginPayload): User?
}

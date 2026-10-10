package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.domain.models.users.User
import kotlin.uuid.Uuid

/** Reads a user by its identifier, whatever its organization, or null if it doesn't exist. */
interface GetUserUseCase {
    suspend operator fun invoke(userId: Uuid): User?
}

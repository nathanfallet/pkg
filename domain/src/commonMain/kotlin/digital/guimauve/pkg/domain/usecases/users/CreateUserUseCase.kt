package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.domain.models.users.CreateUserPayload
import digital.guimauve.pkg.domain.models.users.User
import kotlin.uuid.Uuid

/**
 * Registers a user in an organization, encoding the raw password of the payload before it is stored.
 * Returns null if the user could not be created.
 */
interface CreateUserUseCase {
    suspend operator fun invoke(payload: CreateUserPayload, organizationId: Uuid): User?
}

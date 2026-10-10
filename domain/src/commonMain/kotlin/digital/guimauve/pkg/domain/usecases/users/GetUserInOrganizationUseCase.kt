package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.domain.models.users.User
import kotlin.uuid.Uuid

/** Reads a user of an organization, or null if that organization has no such user. */
interface GetUserInOrganizationUseCase {
    suspend operator fun invoke(userId: Uuid, organizationId: Uuid): User?
}

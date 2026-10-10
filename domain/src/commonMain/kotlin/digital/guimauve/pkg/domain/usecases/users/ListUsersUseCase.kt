package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.domain.models.users.User
import kotlin.uuid.Uuid

/** Lists the users of an organization. */
interface ListUsersUseCase {
    suspend operator fun invoke(organizationId: Uuid): List<User>
}

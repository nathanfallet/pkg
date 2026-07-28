package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.models.users.User
import kotlin.uuid.Uuid

interface ListUsersUseCase {
    suspend operator fun invoke(organizationId: Uuid): List<User>
}

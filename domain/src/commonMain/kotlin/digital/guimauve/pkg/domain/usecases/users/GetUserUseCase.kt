package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.domain.models.users.User
import kotlin.uuid.Uuid

interface GetUserUseCase {
    suspend operator fun invoke(userId: Uuid): User?
}

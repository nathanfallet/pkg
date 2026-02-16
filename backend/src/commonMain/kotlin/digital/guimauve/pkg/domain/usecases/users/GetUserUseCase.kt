package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.models.users.User
import kotlin.uuid.Uuid

interface GetUserUseCase {
    suspend operator fun invoke(userId: Uuid): User?
}

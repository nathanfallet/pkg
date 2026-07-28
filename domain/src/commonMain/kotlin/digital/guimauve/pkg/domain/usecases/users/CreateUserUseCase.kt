package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.models.users.CreateUserPayload
import digital.guimauve.pkg.models.users.User
import kotlin.uuid.Uuid

interface CreateUserUseCase {
    suspend operator fun invoke(payload: CreateUserPayload, organizationId: Uuid): User?
}

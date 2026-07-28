package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.domain.models.users.CreateUserPayload
import digital.guimauve.pkg.domain.models.users.User
import kotlin.uuid.Uuid

interface CreateUserUseCase {
    suspend operator fun invoke(payload: CreateUserPayload, organizationId: Uuid): User?
}

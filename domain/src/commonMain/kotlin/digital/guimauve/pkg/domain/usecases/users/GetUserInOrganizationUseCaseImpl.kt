package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.domain.models.users.User
import digital.guimauve.pkg.domain.repositories.UsersRepository
import kotlin.uuid.Uuid

class GetUserInOrganizationUseCaseImpl(
    private val repository: UsersRepository,
) : GetUserInOrganizationUseCase {
    override suspend fun invoke(userId: Uuid, organizationId: Uuid): User? = repository.get(userId, organizationId)
}

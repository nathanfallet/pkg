package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.domain.models.users.User
import digital.guimauve.pkg.domain.repositories.UsersRepository
import kotlin.uuid.Uuid

class ListUsersUseCaseImpl(
    private val repository: UsersRepository,
) : ListUsersUseCase {
    override suspend fun invoke(organizationId: Uuid): List<User> = repository.list(organizationId)
}

package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.domain.repositories.UsersRepository
import digital.guimauve.pkg.models.users.User
import kotlin.uuid.Uuid

class ListUsersUseCaseImpl(
    private val repository: UsersRepository,
) : ListUsersUseCase {
    override suspend fun invoke(organizationId: Uuid): List<User> = repository.list(organizationId)
}

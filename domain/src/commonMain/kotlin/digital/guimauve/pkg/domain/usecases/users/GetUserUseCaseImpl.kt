package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.domain.models.users.User
import digital.guimauve.pkg.domain.repositories.UsersRepository
import kotlin.uuid.Uuid

class GetUserUseCaseImpl(
    private val repository: UsersRepository,
) : GetUserUseCase {
    override suspend fun invoke(userId: Uuid): User? = repository.get(userId)
}

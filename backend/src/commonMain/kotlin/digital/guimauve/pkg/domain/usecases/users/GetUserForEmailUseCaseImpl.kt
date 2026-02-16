package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.domain.repositories.UsersRepository
import digital.guimauve.pkg.models.users.User

class GetUserForEmailUseCaseImpl(
    private val repository: UsersRepository,
) : GetUserForEmailUseCase {
    override suspend fun invoke(email: String, includePassword: Boolean): User? =
        repository.getForEmail(email, includePassword)
}

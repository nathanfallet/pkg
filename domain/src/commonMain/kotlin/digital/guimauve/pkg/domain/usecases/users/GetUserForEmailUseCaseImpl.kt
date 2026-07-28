package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.domain.models.users.User
import digital.guimauve.pkg.domain.repositories.UsersRepository

class GetUserForEmailUseCaseImpl(
    private val repository: UsersRepository,
) : GetUserForEmailUseCase {
    override suspend fun invoke(email: String, includePassword: Boolean): User? =
        repository.getForEmail(email, includePassword)
}

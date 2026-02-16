package digital.guimauve.pkg.domain.usecases.users

import dev.kaccelero.models.UUID
import digital.guimauve.pkg.domain.repositories.IUsersRepository
import digital.guimauve.pkg.models.users.User

class GetUserUseCase(
    private val repository: IUsersRepository,
) : IGetUserUseCase {

    override suspend fun invoke(input: UUID): User? = repository.get(input)

}

package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.domain.repositories.IUsersRepository
import digital.guimauve.pkg.models.users.User

class GetUserForEmailUseCase(
    private val repository: IUsersRepository,
) : IGetUserForEmailUseCase {

    override suspend fun invoke(input1: String, input2: Boolean): User? = repository.getForEmail(input1, input2)

}

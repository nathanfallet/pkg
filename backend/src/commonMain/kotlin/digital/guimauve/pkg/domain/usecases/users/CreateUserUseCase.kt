package digital.guimauve.pkg.domain.usecases.users

import dev.kaccelero.commons.auth.IHashPasswordUseCase
import dev.kaccelero.commons.repositories.ICreateChildModelSuspendUseCase
import dev.kaccelero.models.UUID
import digital.guimauve.pkg.domain.repositories.IUsersRepository
import digital.guimauve.pkg.models.users.CreateUserPayload
import digital.guimauve.pkg.models.users.User

class CreateUserUseCase(
    private val repository: IUsersRepository,
    private val hashPasswordUseCase: IHashPasswordUseCase,
) : ICreateChildModelSuspendUseCase<User, CreateUserPayload, UUID> {

    override suspend fun invoke(input1: CreateUserPayload, input2: UUID): User? =
        repository.create(input1.copy(password = hashPasswordUseCase(input1.password)), input2)

}

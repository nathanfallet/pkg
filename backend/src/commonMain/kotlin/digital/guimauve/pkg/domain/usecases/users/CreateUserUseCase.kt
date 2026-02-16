package digital.guimauve.pkg.domain.usecases.users

import dev.kaccelero.commons.auth.IHashPasswordUseCase
import dev.kaccelero.commons.repositories.ICreateChildModelSuspendUseCase
import digital.guimauve.pkg.domain.repositories.UsersRepository
import digital.guimauve.pkg.models.users.CreateUserPayload
import digital.guimauve.pkg.models.users.User
import kotlin.uuid.Uuid

class CreateUserUseCase(
    private val repository: UsersRepository,
    private val hashPasswordUseCase: IHashPasswordUseCase,
) : ICreateChildModelSuspendUseCase<User, CreateUserPayload, Uuid> {
    override suspend fun invoke(input1: CreateUserPayload, input2: Uuid): User? =
        repository.create(input1.copy(password = hashPasswordUseCase(input1.password)), input2)
}

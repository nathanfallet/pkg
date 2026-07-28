package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.domain.repositories.UsersRepository
import digital.guimauve.pkg.domain.services.PasswordEncoderService
import digital.guimauve.pkg.models.users.CreateUserPayload
import digital.guimauve.pkg.models.users.User
import kotlin.uuid.Uuid

class CreateUserUseCaseImpl(
    private val repository: UsersRepository,
    private val passwordEncoderService: PasswordEncoderService,
) : CreateUserUseCase {
    override suspend fun invoke(payload: CreateUserPayload, organizationId: Uuid): User? =
        repository.create(payload.copy(password = passwordEncoderService.encode(payload.password)), organizationId)
}

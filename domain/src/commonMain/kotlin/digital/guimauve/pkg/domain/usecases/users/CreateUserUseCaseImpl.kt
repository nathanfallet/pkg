package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.domain.models.users.CreateUserPayload
import digital.guimauve.pkg.domain.models.users.User
import digital.guimauve.pkg.domain.repositories.UsersRepository
import digital.guimauve.pkg.domain.services.PasswordEncoderService
import kotlin.uuid.Uuid

class CreateUserUseCaseImpl(
    private val repository: UsersRepository,
    private val passwordEncoderService: PasswordEncoderService,
) : CreateUserUseCase {
    override suspend fun invoke(payload: CreateUserPayload, organizationId: Uuid): User? =
        repository.create(payload.copy(password = passwordEncoderService.encode(payload.password)), organizationId)
}

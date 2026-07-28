package digital.guimauve.pkg.domain.usecases.auth

import digital.guimauve.pkg.domain.models.auth.LoginPayload
import digital.guimauve.pkg.domain.models.users.User
import digital.guimauve.pkg.domain.services.PasswordEncoderService
import digital.guimauve.pkg.domain.usecases.users.GetUserForEmailUseCase

class LoginUseCaseImpl(
    private val getUserForEmailUseCase: GetUserForEmailUseCase,
    private val passwordEncoderService: PasswordEncoderService,
) : LoginUseCase {
    override suspend fun invoke(input: LoginPayload): User? =
        getUserForEmailUseCase(input.email, true)?.takeIf {
            passwordEncoderService.matches(input.password, it.password ?: "")
        }
}

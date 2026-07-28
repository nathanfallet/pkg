package digital.guimauve.pkg.domain.usecases.auth

import digital.guimauve.pkg.domain.services.PasswordEncoderService
import digital.guimauve.pkg.domain.usecases.users.GetUserForEmailUseCase
import digital.guimauve.pkg.models.auth.LoginPayload
import digital.guimauve.pkg.models.users.User

class LoginUseCaseImpl(
    private val getUserForEmailUseCase: GetUserForEmailUseCase,
    private val passwordEncoderService: PasswordEncoderService,
) : LoginUseCase {
    override suspend fun invoke(input: LoginPayload): User? =
        getUserForEmailUseCase(input.email, true)?.takeIf {
            passwordEncoderService.matches(input.password, it.password ?: "")
        }
}

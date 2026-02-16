package digital.guimauve.pkg.domain.usecases.auth

import dev.kaccelero.commons.auth.IVerifyPasswordUseCase
import dev.kaccelero.commons.auth.VerifyPasswordPayload
import digital.guimauve.pkg.domain.usecases.users.GetUserForEmailUseCase
import digital.guimauve.pkg.models.auth.LoginPayload
import digital.guimauve.pkg.models.users.User

class LoginUseCaseImpl(
    private val getUserForEmailUseCase: GetUserForEmailUseCase,
    private val verifyPasswordUseCase: IVerifyPasswordUseCase,
) : LoginUseCase {
    override suspend fun invoke(input: LoginPayload): User? =
        getUserForEmailUseCase(input.email, true)?.takeIf {
            verifyPasswordUseCase(VerifyPasswordPayload(input.password, it.password ?: ""))
        }
}

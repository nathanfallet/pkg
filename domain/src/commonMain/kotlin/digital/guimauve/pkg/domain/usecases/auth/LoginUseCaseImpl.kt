package digital.guimauve.pkg.domain.usecases.auth

import digital.guimauve.pkg.domain.models.auth.LoginPayload
import digital.guimauve.pkg.domain.models.users.User
import digital.guimauve.pkg.domain.services.PasswordEncoderService
import digital.guimauve.pkg.domain.usecases.users.GetUserForEmailUseCase

class LoginUseCaseImpl(
    private val getUserForEmailUseCase: GetUserForEmailUseCase,
    private val passwordEncoderService: PasswordEncoderService,
) : LoginUseCase {

    /**
     * Verified against when no account matches, so that an unknown address costs the same as a
     * known one. Without it the time to answer says which addresses are registered.
     *
     * It is hashed rather than written out, so it stays valid for whatever encoder is in use, and
     * it is computed once, on the first miss.
     */
    private val dummyHash by lazy { passwordEncoderService.encode("no account will ever match this") }

    override suspend fun invoke(input: LoginPayload): User? {
        val user = getUserForEmailUseCase(input.email, true)
        val hash = user?.password ?: run {
            passwordEncoderService.matches(input.password, dummyHash)
            return null
        }
        return user.takeIf { passwordEncoderService.matches(input.password, hash) }
    }

}

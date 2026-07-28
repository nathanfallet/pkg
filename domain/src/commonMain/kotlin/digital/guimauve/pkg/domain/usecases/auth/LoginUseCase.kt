package digital.guimauve.pkg.domain.usecases.auth

import digital.guimauve.pkg.models.auth.LoginPayload
import digital.guimauve.pkg.models.users.User

interface LoginUseCase {
    suspend operator fun invoke(input: LoginPayload): User?
}

package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.domain.models.users.User

interface GetUserForEmailUseCase {
    suspend operator fun invoke(email: String, includePassword: Boolean): User?
}

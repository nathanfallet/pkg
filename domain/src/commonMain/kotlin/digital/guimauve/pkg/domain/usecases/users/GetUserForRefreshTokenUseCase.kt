package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.domain.models.users.User

interface GetUserForRefreshTokenUseCase {
    suspend operator fun invoke(input: String): User?
}

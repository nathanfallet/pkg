package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.models.users.User

interface GetUserForRefreshTokenUseCase {
    suspend operator fun invoke(input: String): User?
}

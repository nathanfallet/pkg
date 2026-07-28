package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.domain.models.users.User
import digital.guimauve.pkg.domain.services.TokenService

class GetUserForRefreshTokenUseCaseImpl(
    private val tokenService: TokenService,
    private val getUserUseCase: GetUserUseCase,
) : GetUserForRefreshTokenUseCase {
    override suspend fun invoke(input: String): User? =
        tokenService.verifyToken(input)?.let { getUserUseCase(it) }
}

package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.domain.services.TokenService
import digital.guimauve.pkg.models.users.User

class GetUserForRefreshTokenUseCaseImpl(
    private val tokenService: TokenService,
    private val getUserUseCase: GetUserUseCase,
) : GetUserForRefreshTokenUseCase {
    override suspend fun invoke(input: String): User? =
        tokenService.verifyToken(input)?.let { getUserUseCase(it) }
}

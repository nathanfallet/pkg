package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.models.users.User
import digital.guimauve.pkg.services.tokens.IJWTService
import kotlin.uuid.Uuid

class GetUserForRefreshTokenUseCaseImpl(
    private val jwtService: IJWTService,
    private val getUserUseCase: GetUserUseCase,
) : GetUserForRefreshTokenUseCase {
    override suspend fun invoke(input: String): User? = jwtService.verifyJWT(input)?.let {
        getUserUseCase(Uuid.parse(it.subject))
    }
}

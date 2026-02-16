package digital.guimauve.pkg.domain.usecases.users

import dev.kaccelero.models.UUID
import digital.guimauve.pkg.models.users.User
import digital.guimauve.pkg.services.tokens.IJWTService

class GetUserForRefreshTokenUseCase(
    private val jwtService: IJWTService,
    private val getUserUseCase: IGetUserUseCase,
) : IGetUserForRefreshTokenUseCase {

    override suspend fun invoke(input: String): User? = jwtService.verifyJWT(input)?.let {
        getUserUseCase(UUID(it.subject))
    }

}

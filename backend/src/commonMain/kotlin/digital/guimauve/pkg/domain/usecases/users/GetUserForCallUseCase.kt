package digital.guimauve.pkg.domain.usecases.users

import dev.kaccelero.commons.auth.IGetJWTPrincipalForCallUseCase
import dev.kaccelero.commons.users.IGetUserForCallUseCase
import dev.kaccelero.models.IUser
import digital.guimauve.pkg.domain.usecases.auth.IGetSessionForCallUseCase
import digital.guimauve.pkg.domain.usecases.auth.IGetUserIdPrincipalUseCase
import digital.guimauve.pkg.models.users.User
import io.ktor.server.application.*
import io.ktor.util.*
import kotlin.uuid.Uuid

class GetUserForCallUseCase(
    private val getJWTPrincipalForCall: IGetJWTPrincipalForCallUseCase,
    private val getUserIdPrincipalUseCase: IGetUserIdPrincipalUseCase,
    private val getSessionForCallUseCase: IGetSessionForCallUseCase,
    private val getUserUseCase: GetUserUseCase,
) : IGetUserForCallUseCase {

    private data class UserForCall(
        val user: User?,
    )

    private val userKey = AttributeKey<UserForCall>("user")

    override suspend fun invoke(input: ApplicationCall): IUser? {
        // Note: we cannot use `computeIfAbsent` because it does not support suspending functions
        return input.attributes.getOrNull(userKey)?.user ?: run {
            val id = getJWTPrincipalForCall(input)?.subject?.let(Uuid::parse)
                ?: getUserIdPrincipalUseCase(input)?.name?.let(Uuid::parse)
                ?: getSessionForCallUseCase(input)?.userId
            val computed = UserForCall(id?.let { getUserUseCase(it) })
            input.attributes.put(userKey, computed)
            computed.user
        }
    }

}

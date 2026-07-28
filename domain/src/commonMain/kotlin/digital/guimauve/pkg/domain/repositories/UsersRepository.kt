package digital.guimauve.pkg.domain.repositories

import digital.guimauve.pkg.domain.models.users.CreateUserPayload
import digital.guimauve.pkg.domain.models.users.User
import kotlin.uuid.Uuid

interface UsersRepository {

    suspend fun list(organizationId: Uuid): List<User>
    suspend fun get(id: Uuid): User?
    suspend fun get(id: Uuid, organizationId: Uuid): User?
    suspend fun getForEmail(email: String, includePassword: Boolean): User?
    suspend fun create(payload: CreateUserPayload, organizationId: Uuid): User?

}

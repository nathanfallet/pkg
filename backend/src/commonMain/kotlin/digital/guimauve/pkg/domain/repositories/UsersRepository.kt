package digital.guimauve.pkg.domain.repositories

import dev.kaccelero.repositories.IChildModelSuspendRepository
import digital.guimauve.pkg.models.users.CreateUserPayload
import digital.guimauve.pkg.models.users.User
import kotlin.uuid.Uuid

interface UsersRepository : IChildModelSuspendRepository<User, Uuid, CreateUserPayload, Unit, Uuid> {

    suspend fun get(id: Uuid): User?
    suspend fun getForEmail(email: String, includePassword: Boolean): User?

}

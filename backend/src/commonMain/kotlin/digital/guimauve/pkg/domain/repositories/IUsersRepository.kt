package digital.guimauve.pkg.domain.repositories

import dev.kaccelero.models.UUID
import dev.kaccelero.repositories.IChildModelSuspendRepository
import digital.guimauve.pkg.models.users.CreateUserPayload
import digital.guimauve.pkg.models.users.User

interface IUsersRepository : IChildModelSuspendRepository<User, UUID, CreateUserPayload, Unit, UUID> {

    suspend fun get(id: UUID): User?
    suspend fun getForEmail(email: String, includePassword: Boolean): User?

}

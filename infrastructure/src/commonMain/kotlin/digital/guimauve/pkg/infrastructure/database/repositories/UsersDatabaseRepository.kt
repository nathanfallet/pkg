package digital.guimauve.pkg.infrastructure.database.repositories

import digital.guimauve.pkg.domain.models.users.CreateUserPayload
import digital.guimauve.pkg.domain.models.users.User
import digital.guimauve.pkg.domain.repositories.UsersRepository
import digital.guimauve.pkg.infrastructure.database.TransactionManager
import digital.guimauve.pkg.infrastructure.database.tables.Users
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.uuid.Uuid

class UsersDatabaseRepository(
    private val transactionManager: TransactionManager,
) : UsersRepository {

    init {
        transactionManager.transaction {
            SchemaUtils.create(Users)
        }
    }

    override suspend fun list(organizationId: Uuid): List<User> =
        transactionManager.suspendTransaction {
            Users
                .selectAll()
                .where { Users.organizationId eq organizationId }
                .map(Users::toUser)
        }

    override suspend fun get(id: Uuid): User? =
        transactionManager.suspendTransaction {
            Users
                .selectAll()
                .where { Users.id eq id }
                .map(Users::toUser)
                .firstOrNull()
        }

    override suspend fun get(id: Uuid, organizationId: Uuid): User? =
        transactionManager.suspendTransaction {
            Users
                .selectAll()
                .where { Users.id eq id and (Users.organizationId eq organizationId) }
                .map(Users::toUser)
                .firstOrNull()
        }

    override suspend fun getForEmail(email: String, includePassword: Boolean): User? =
        transactionManager.suspendTransaction {
            Users
                .selectAll()
                .where { Users.email eq email }
                .map { Users.toUser(it, includePassword) }
                .firstOrNull()
        }

    override suspend fun create(payload: CreateUserPayload, organizationId: Uuid): User? =
        transactionManager.suspendTransaction {
            Users.insert {
                it[Users.organizationId] = organizationId
                it[email] = payload.email
                it[password] = payload.password
            }
        }.resultedValues?.map(Users::toUser)?.firstOrNull()

}

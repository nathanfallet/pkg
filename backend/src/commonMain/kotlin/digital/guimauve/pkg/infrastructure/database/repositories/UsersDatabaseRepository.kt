package digital.guimauve.pkg.infrastructure.database.repositories

import dev.kaccelero.database.IDatabase
import dev.kaccelero.database.eq
import dev.kaccelero.database.set
import dev.kaccelero.models.IContext
import digital.guimauve.pkg.domain.repositories.UsersRepository
import digital.guimauve.pkg.infrastructure.database.tables.Users
import digital.guimauve.pkg.models.users.CreateUserPayload
import digital.guimauve.pkg.models.users.User
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import kotlin.uuid.Uuid

class UsersDatabaseRepository(
    private val database: IDatabase,
) : UsersRepository {

    init {
        database.transaction {
            SchemaUtils.create(Users)
        }
    }

    override suspend fun list(parentId: Uuid, context: IContext?): List<User> =
        database.suspendedTransaction {
            Users
                .selectAll()
                .where { Users.organizationId eq parentId }
                .map(Users::toUser)
        }

    override suspend fun get(id: Uuid): User? =
        database.suspendedTransaction {
            Users
                .selectAll()
                .where { Users.id eq id }
                .map(Users::toUser)
                .singleOrNull()
        }

    override suspend fun get(id: Uuid, parentId: Uuid, context: IContext?): User? =
        database.suspendedTransaction {
            Users
                .selectAll()
                .where { Users.id eq id and (Users.organizationId eq parentId) }
                .map(Users::toUser)
                .singleOrNull()
        }

    override suspend fun getForEmail(email: String, includePassword: Boolean): User? =
        database.suspendedTransaction {
            Users
                .selectAll()
                .where { Users.email eq email }
                .map { Users.toUser(it, includePassword) }
                .singleOrNull()
        }

    override suspend fun create(payload: CreateUserPayload, parentId: Uuid, context: IContext?): User? =
        database.suspendedTransaction {
            Users.insert {
                it[organizationId] = parentId
                it[email] = payload.email
                it[password] = payload.password
            }
        }.resultedValues?.map(Users::toUser)?.singleOrNull()

}

package digital.guimauve.pkg.infrastructure.database.repositories

import digital.guimauve.pkg.domain.repositories.SessionsRepository
import digital.guimauve.pkg.infrastructure.database.TransactionManager
import digital.guimauve.pkg.infrastructure.database.tables.Sessions
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*

class SessionsDatabaseRepository(
    private val transactionManager: TransactionManager,
) : SessionsRepository {

    init {
        transactionManager.transaction {
            SchemaUtils.create(Sessions)
        }
    }

    override suspend fun get(id: String): String? =
        transactionManager.suspendTransaction {
            Sessions
                .selectAll()
                .where { Sessions.id eq id }
                .map { it[Sessions.value] }
                .firstOrNull()
        }

    override suspend fun set(id: String, value: String) {
        transactionManager.suspendTransaction {
            val updated = Sessions.update({ Sessions.id eq id }) {
                it[Sessions.value] = value
            }
            if (updated == 0) Sessions.insert {
                it[Sessions.id] = id
                it[Sessions.value] = value
            }
        }
    }

    override suspend fun delete(id: String) {
        transactionManager.suspendTransaction {
            Sessions.deleteWhere { Sessions.id eq id }
        }
    }

}

package digital.guimauve.pkg.infrastructure.database.repositories

import digital.guimauve.pkg.domain.models.organizations.CreateOrganizationPayload
import digital.guimauve.pkg.domain.models.organizations.Organization
import digital.guimauve.pkg.domain.repositories.OrganizationsRepository
import digital.guimauve.pkg.infrastructure.database.TransactionManager
import digital.guimauve.pkg.infrastructure.database.tables.Organizations
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.uuid.Uuid

class OrganizationsDatabaseRepository(
    private val transactionManager: TransactionManager,
) : OrganizationsRepository {

    init {
        transactionManager.transaction {
            SchemaUtils.create(Organizations)
        }
    }

    override suspend fun get(id: Uuid): Organization? =
        transactionManager.suspendTransaction {
            Organizations
                .selectAll()
                .where { Organizations.id eq id }
                .map(Organizations::toOrganization)
                .firstOrNull()
        }

    override suspend fun create(payload: CreateOrganizationPayload): Organization? =
        transactionManager.suspendTransaction {
            Organizations.insert {
                it[name] = payload.name
            }
        }.resultedValues?.map(Organizations::toOrganization)?.firstOrNull()

}

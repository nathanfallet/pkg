package digital.guimauve.pkg.infrastructure.database.repositories

import dev.kaccelero.database.IDatabase
import dev.kaccelero.models.IContext
import dev.kaccelero.models.UUID
import digital.guimauve.pkg.domain.repositories.IOrganizationsRepository
import digital.guimauve.pkg.infrastructure.database.tables.Organizations
import digital.guimauve.pkg.models.organizations.CreateOrganizationPayload
import digital.guimauve.pkg.models.organizations.Organization
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class OrganizationDatabaseRepository(
    private val database: IDatabase,
) : IOrganizationsRepository {

    init {
        database.transaction {
            SchemaUtils.create(Organizations)
        }
    }

    override suspend fun list(context: IContext?): List<Organization> =
        database.suspendedTransaction {
            Organizations
                .selectAll()
                .map(Organizations::toOrganization)
        }

    override suspend fun get(id: UUID, context: IContext?): Organization? =
        database.suspendedTransaction {
            Organizations
                .selectAll()
                .map(Organizations::toOrganization)
                .singleOrNull()
        }

    override suspend fun create(payload: CreateOrganizationPayload, context: IContext?): Organization? =
        database.suspendedTransaction {
            Organizations.insert {
                it[name] = payload.name
            }
        }.resultedValues?.map(Organizations::toOrganization)?.singleOrNull()

}

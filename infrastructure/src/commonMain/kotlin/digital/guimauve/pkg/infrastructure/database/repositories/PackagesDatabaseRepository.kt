package digital.guimauve.pkg.infrastructure.database.repositories

import digital.guimauve.pkg.domain.models.packages.CreatePackagePayload
import digital.guimauve.pkg.domain.models.packages.Package
import digital.guimauve.pkg.domain.models.packages.PackageFormat
import digital.guimauve.pkg.domain.models.packages.UpdatePackagePayload
import digital.guimauve.pkg.domain.repositories.PackagesRepository
import digital.guimauve.pkg.infrastructure.database.TransactionManager
import digital.guimauve.pkg.infrastructure.database.tables.Packages
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import kotlin.time.Clock
import kotlin.uuid.Uuid

class PackagesDatabaseRepository(
    private val transactionManager: TransactionManager,
) : PackagesRepository {

    init {
        transactionManager.transaction {
            SchemaUtils.create(Packages)
        }
    }

    override suspend fun list(organizationId: Uuid): List<Package> =
        transactionManager.suspendTransaction {
            Packages
                .selectAll()
                .where { Packages.organizationId eq organizationId }
                .map(Packages::toPackage)
        }

    override suspend fun get(id: Uuid, organizationId: Uuid): Package? =
        transactionManager.suspendTransaction {
            Packages
                .selectAll()
                .where { Packages.id eq id and (Packages.organizationId eq organizationId) }
                .map(Packages::toPackage)
                .firstOrNull()
        }

    override suspend fun getByName(name: String, format: PackageFormat): Package? =
        transactionManager.suspendTransaction {
            Packages
                .selectAll()
                .where { Packages.name eq name and (Packages.format eq format) }
                .map(Packages::toPackage)
                .firstOrNull()
        }

    override suspend fun create(payload: CreatePackagePayload, organizationId: Uuid): Package? =
        transactionManager.suspendTransaction {
            Packages.insert {
                it[name] = payload.name
                it[format] = payload.format
                it[Packages.organizationId] = organizationId
                it[isPublic] = payload.isPublic
                it[createdAt] = Clock.System.now()
            }
        }.resultedValues?.map(Packages::toPackage)?.firstOrNull()

    override suspend fun update(id: Uuid, payload: UpdatePackagePayload, organizationId: Uuid): Boolean =
        transactionManager.suspendTransaction {
            Packages.update({
                Packages.id eq id and (Packages.organizationId eq organizationId)
            }) {
                it[isPublic] = payload.isPublic
            }
        } == 1

    override suspend fun delete(id: Uuid, organizationId: Uuid): Boolean =
        transactionManager.suspendTransaction {
            Packages.deleteWhere {
                Packages.id eq id and (Packages.organizationId eq organizationId)
            }
        } == 1

}

package digital.guimauve.pkg.infrastructure.database.repositories

import dev.kaccelero.database.IDatabase
import dev.kaccelero.database.eq
import dev.kaccelero.database.set
import dev.kaccelero.models.IContext
import dev.kaccelero.models.UUID
import digital.guimauve.pkg.domain.repositories.IPackagesRepository
import digital.guimauve.pkg.infrastructure.database.tables.Packages
import digital.guimauve.pkg.models.packages.CreatePackagePayload
import digital.guimauve.pkg.models.packages.Package
import digital.guimauve.pkg.models.packages.PackageFormat
import digital.guimauve.pkg.models.packages.UpdatePackagePayload
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*

class PackagesDatabaseRepository(
    private val database: IDatabase,
) : IPackagesRepository {

    init {
        database.transaction {
            SchemaUtils.create(Packages)
        }
    }

    override suspend fun list(parentId: UUID, context: IContext?): List<Package> =
        database.suspendedTransaction {
            Packages
                .selectAll()
                .where { Packages.organizationId eq parentId }
                .map(Packages::toPackage)
        }

    override suspend fun get(id: UUID, parentId: UUID, context: IContext?): Package? =
        database.suspendedTransaction {
            Packages
                .selectAll()
                .where { Packages.id eq id and (Packages.organizationId eq parentId) }
                .map(Packages::toPackage)
                .singleOrNull()
        }

    override suspend fun getByName(name: String, format: PackageFormat): Package? =
        database.suspendedTransaction {
            Packages
                .selectAll()
                .where { Packages.name eq name and (Packages.format eq format) }
                .map(Packages::toPackage)
                .singleOrNull()
        }

    override suspend fun create(payload: CreatePackagePayload, parentId: UUID, context: IContext?): Package? =
        database.suspendedTransaction {
            Packages.insert {
                it[name] = payload.name
                it[format] = payload.format
                it[organizationId] = parentId
                it[isPublic] = payload.isPublic
                it[createdAt] = Clock.System.now()
            }
        }.resultedValues?.map(Packages::toPackage)?.singleOrNull()

    override suspend fun update(id: UUID, payload: UpdatePackagePayload, parentId: UUID, context: IContext?): Boolean =
        database.suspendedTransaction {
            Packages.update({ Packages.id eq id and (Packages.organizationId eq parentId) }) {
                it[isPublic] = payload.isPublic
            }
        } == 1

    override suspend fun delete(id: UUID, parentId: UUID, context: IContext?): Boolean =
        database.suspendedTransaction {
            Packages.deleteWhere { Packages.id eq id and (Packages.organizationId eq parentId) }
        } == 1

}

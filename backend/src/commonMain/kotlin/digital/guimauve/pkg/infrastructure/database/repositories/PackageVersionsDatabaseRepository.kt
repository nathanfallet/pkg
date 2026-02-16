package digital.guimauve.pkg.infrastructure.database.repositories

import dev.kaccelero.database.IDatabase
import dev.kaccelero.database.eq
import dev.kaccelero.database.set
import dev.kaccelero.models.IContext
import dev.kaccelero.models.UUID
import digital.guimauve.pkg.domain.repositories.IPackageVersionsRepository
import digital.guimauve.pkg.infrastructure.database.tables.PackageVersions
import digital.guimauve.pkg.models.packages.versions.CreatePackageVersionPayload
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import digital.guimauve.pkg.models.users.UserContext
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*

class PackageVersionsDatabaseRepository(
    private val database: IDatabase,
) : IPackageVersionsRepository {

    init {
        database.transaction {
            SchemaUtils.create(PackageVersions)
        }
    }

    override suspend fun list(parentId: UUID, context: IContext?): List<PackageVersion> =
        database.suspendedTransaction {
            PackageVersions
                .selectAll()
                .where { PackageVersions.packageId eq parentId }
                .orderBy(PackageVersions.publishedAt to SortOrder.DESC)
                .map(PackageVersions::toPackageVersion)
        }

    override suspend fun getByName(name: String, packageId: UUID): PackageVersion? =
        database.suspendedTransaction {
            PackageVersions
                .selectAll()
                .where { PackageVersions.version eq name and (PackageVersions.packageId eq packageId) }
                .map(PackageVersions::toPackageVersion)
                .singleOrNull()
        }

    override suspend fun getLatest(packageId: UUID): PackageVersion? =
        database.suspendedTransaction {
            PackageVersions
                .selectAll()
                .where { PackageVersions.packageId eq packageId }
                .orderBy(PackageVersions.publishedAt to SortOrder.DESC)
                .limit(1)
                .map(PackageVersions::toPackageVersion)
                .firstOrNull()
        }

    override suspend fun create(
        payload: CreatePackageVersionPayload,
        parentId: UUID,
        context: IContext?,
    ): PackageVersion? {
        val userContext = context as? UserContext ?: return null
        return database.suspendedTransaction {
            PackageVersions.insert {
                it[packageId] = parentId
                it[version] = payload.version
                it[publishedBy] = userContext.userId
                it[publishedAt] = Clock.System.now()
                it[metadata] = payload.metadata
            }
        }.resultedValues?.map(PackageVersions::toPackageVersion)?.singleOrNull()
    }

}

package digital.guimauve.pkg.infrastructure.database.repositories

import digital.guimauve.pkg.domain.models.packages.versions.CreatePackageVersionPayload
import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import digital.guimauve.pkg.domain.repositories.PackageVersionsRepository
import digital.guimauve.pkg.infrastructure.database.TransactionManager
import digital.guimauve.pkg.infrastructure.database.tables.PackageVersions
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.time.Clock
import kotlin.uuid.Uuid

class PackageVersionsDatabaseRepository(
    private val transactionManager: TransactionManager,
) : PackageVersionsRepository {

    init {
        transactionManager.transaction {
            SchemaUtils.create(PackageVersions)
        }
    }

    override suspend fun list(packageId: Uuid): List<PackageVersion> =
        transactionManager.suspendTransaction {
            PackageVersions
                .selectAll()
                .where { PackageVersions.packageId eq packageId }
                .orderBy(PackageVersions.publishedAt to SortOrder.DESC)
                .map(PackageVersions::toPackageVersion)
        }

    override suspend fun get(id: Uuid, packageId: Uuid): PackageVersion? =
        transactionManager.suspendTransaction {
            PackageVersions
                .selectAll()
                .where {
                    PackageVersions.id eq id and
                            (PackageVersions.packageId eq packageId)
                }
                .map(PackageVersions::toPackageVersion)
                .firstOrNull()
        }

    override suspend fun getByName(name: String, packageId: Uuid): PackageVersion? =
        transactionManager.suspendTransaction {
            PackageVersions
                .selectAll()
                .where {
                    PackageVersions.version eq name and
                            (PackageVersions.packageId eq packageId)
                }
                .map(PackageVersions::toPackageVersion)
                .firstOrNull()
        }

    override suspend fun getLatest(packageId: Uuid): PackageVersion? =
        transactionManager.suspendTransaction {
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
        packageId: Uuid,
        publishedBy: Uuid,
    ): PackageVersion? = transactionManager.suspendTransaction {
        PackageVersions.insert {
            it[PackageVersions.packageId] = packageId
            it[version] = payload.version
            it[PackageVersions.publishedBy] = publishedBy
            it[publishedAt] = Clock.System.now()
            it[metadata] = payload.metadata
        }
    }.resultedValues?.map(PackageVersions::toPackageVersion)?.firstOrNull()

}

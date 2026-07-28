package digital.guimauve.pkg.infrastructure.database.repositories

import digital.guimauve.pkg.domain.models.packages.versions.files.CreatePackageVersionFilePayload
import digital.guimauve.pkg.domain.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.domain.repositories.PackageVersionFilesRepository
import digital.guimauve.pkg.infrastructure.database.TransactionManager
import digital.guimauve.pkg.infrastructure.database.tables.PackageVersionFiles
import digital.guimauve.pkg.infrastructure.database.tables.PackageVersions
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.uuid.Uuid

class PackageVersionFilesDatabaseRepository(
    private val transactionManager: TransactionManager,
) : PackageVersionFilesRepository {

    init {
        transactionManager.transaction {
            SchemaUtils.create(PackageVersionFiles)
        }
    }

    override suspend fun list(versionId: Uuid): List<PackageVersionFile> =
        transactionManager.suspendTransaction {
            PackageVersionFiles
                .selectAll()
                .where { PackageVersionFiles.versionId eq versionId }
                .orderBy(PackageVersionFiles.name to SortOrder.ASC)
                .map(PackageVersionFiles::toPackageVersionFile)
        }

    override suspend fun getByName(name: String, versionId: Uuid): PackageVersionFile? =
        transactionManager.suspendTransaction {
            PackageVersionFiles
                .selectAll()
                .where {
                    PackageVersionFiles.name eq name and
                            (PackageVersionFiles.versionId eq versionId)
                }
                .map(PackageVersionFiles::toPackageVersionFile)
                .firstOrNull()
        }

    override suspend fun getLatestByName(name: String, packageId: Uuid): PackageVersionFile? =
        transactionManager.suspendTransaction {
            PackageVersionFiles
                .join(PackageVersions, JoinType.INNER, PackageVersionFiles.versionId, PackageVersions.id)
                .selectAll()
                .where {
                    PackageVersionFiles.name eq name and
                            (PackageVersions.packageId eq packageId)
                }
                .orderBy(PackageVersions.publishedAt to SortOrder.DESC)
                .limit(1)
                .map(PackageVersionFiles::toPackageVersionFile)
                .firstOrNull()
        }

    override suspend fun create(
        payload: CreatePackageVersionFilePayload,
        versionId: Uuid,
        contentType: String,
        size: Long,
    ): PackageVersionFile? = transactionManager.suspendTransaction {
        PackageVersionFiles.insert {
            it[PackageVersionFiles.versionId] = versionId
            it[name] = payload.name
            it[path] = payload.path
            it[PackageVersionFiles.contentType] = contentType
            it[PackageVersionFiles.size] = size
        }
    }.resultedValues?.map(PackageVersionFiles::toPackageVersionFile)?.firstOrNull()

}

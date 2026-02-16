package digital.guimauve.pkg.infrastructure.database.repositories

import dev.kaccelero.database.IDatabase
import dev.kaccelero.database.eq
import dev.kaccelero.database.set
import dev.kaccelero.models.IContext
import digital.guimauve.pkg.domain.repositories.PackageVersionFilesRepository
import digital.guimauve.pkg.infrastructure.database.tables.PackageVersionFiles
import digital.guimauve.pkg.infrastructure.database.tables.PackageVersions
import digital.guimauve.pkg.models.packages.versions.files.CreatePackageVersionFilePayload
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.services.storage.FileContext
import org.jetbrains.exposed.sql.*
import kotlin.uuid.Uuid

class PackageVersionFilesDatabaseRepository(
    private val database: IDatabase,
) : PackageVersionFilesRepository {

    init {
        database.transaction {
            SchemaUtils.create(PackageVersionFiles)
        }
    }

    override suspend fun getByName(name: String, parentId: Uuid): PackageVersionFile? =
        database.suspendedTransaction {
            PackageVersionFiles
                .selectAll()
                .where { PackageVersionFiles.name eq name and (PackageVersionFiles.versionId eq parentId) }
                .map(PackageVersionFiles::toPackageVersionFile)
                .singleOrNull()
        }

    override suspend fun getLatestByName(name: String, packageId: Uuid): PackageVersionFile? =
        database.suspendedTransaction {
            PackageVersionFiles
                .join(PackageVersions, JoinType.INNER, PackageVersionFiles.versionId, PackageVersions.id)
                .selectAll()
                .where { PackageVersionFiles.name eq name and (PackageVersions.packageId eq packageId) }
                .orderBy(PackageVersions.publishedAt to SortOrder.DESC)
                .limit(1)
                .map(PackageVersionFiles::toPackageVersionFile)
                .singleOrNull()
        }

    override suspend fun create(
        payload: CreatePackageVersionFilePayload,
        parentId: Uuid,
        context: IContext?,
    ): PackageVersionFile? {
        val fileContext = context as? FileContext ?: return null
        return database.suspendedTransaction {
            PackageVersionFiles.insert {
                it[versionId] = parentId
                it[name] = payload.name
                it[path] = payload.path
                it[contentType] = fileContext.contentType.toString()
                it[size] = fileContext.contentLength
            }
        }.resultedValues?.map(PackageVersionFiles::toPackageVersionFile)?.singleOrNull()
    }

}

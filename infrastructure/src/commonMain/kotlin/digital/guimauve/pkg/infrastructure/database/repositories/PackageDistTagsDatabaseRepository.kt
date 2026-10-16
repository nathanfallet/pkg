package digital.guimauve.pkg.infrastructure.database.repositories

import digital.guimauve.pkg.domain.repositories.PackageDistTagsRepository
import digital.guimauve.pkg.infrastructure.database.TransactionManager
import digital.guimauve.pkg.infrastructure.database.tables.PackageDistTags
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.uuid.Uuid

class PackageDistTagsDatabaseRepository(
    private val transactionManager: TransactionManager,
) : PackageDistTagsRepository {

    override suspend fun list(packageId: Uuid): Map<String, Uuid> =
        transactionManager.suspendTransaction {
            PackageDistTags
                .selectAll()
                .where { PackageDistTags.packageId eq packageId }
                .associate { it[PackageDistTags.tag] to it[PackageDistTags.versionId] }
        }

    override suspend fun set(packageId: Uuid, tag: String, versionId: Uuid) {
        // One statement, so two publications moving the same tag at once cannot both insert it.
        transactionManager.suspendTransaction {
            PackageDistTags.upsert {
                it[PackageDistTags.packageId] = packageId
                it[PackageDistTags.tag] = tag
                it[PackageDistTags.versionId] = versionId
            }
        }
    }

}

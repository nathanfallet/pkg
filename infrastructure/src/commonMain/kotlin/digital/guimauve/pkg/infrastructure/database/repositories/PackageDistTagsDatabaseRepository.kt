package digital.guimauve.pkg.infrastructure.database.repositories

import digital.guimauve.pkg.domain.repositories.PackageDistTagsRepository
import digital.guimauve.pkg.infrastructure.database.TransactionManager
import digital.guimauve.pkg.infrastructure.database.tables.PackageDistTags
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
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
        transactionManager.suspendTransaction {
            val updated = PackageDistTags.update({
                PackageDistTags.packageId eq packageId and (PackageDistTags.tag eq tag)
            }) {
                it[PackageDistTags.versionId] = versionId
            }
            if (updated == 0) PackageDistTags.insert {
                it[PackageDistTags.packageId] = packageId
                it[PackageDistTags.tag] = tag
                it[PackageDistTags.versionId] = versionId
            }
        }
    }

}

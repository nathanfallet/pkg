package digital.guimauve.pkg.infrastructure.database.tables

import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import kotlin.uuid.toKotlinUuid

object PackageVersionFiles : UUIDTable() {
    val versionId = uuid("version_id").index()
    val name = varchar("name", 255)
    val contentType = varchar("content_type", 255)
    val size = long("size")
    val path = varchar("path", 255)

    init {
        uniqueIndex(versionId, name)
    }

    fun toPackageVersionFile(
        row: ResultRow,
    ) = PackageVersionFile(
        row[id].value.toKotlinUuid(),
        row[versionId].toKotlinUuid(),
        row[name],
        row[contentType],
        row[size],
        row[path],
    )
}

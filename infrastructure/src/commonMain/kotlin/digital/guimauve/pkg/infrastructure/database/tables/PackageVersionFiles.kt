package digital.guimauve.pkg.infrastructure.database.tables

import digital.guimauve.pkg.domain.models.packages.versions.files.PackageVersionFile
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.UuidTable

object PackageVersionFiles : UuidTable() {

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
        row[id].value,
        row[versionId],
        row[name],
        row[contentType],
        row[size],
        row[path],
    )

}

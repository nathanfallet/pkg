package digital.guimauve.pkg.infrastructure.database.tables

import digital.guimauve.pkg.domain.models.packages.Package
import digital.guimauve.pkg.domain.models.packages.PackageFormat
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.timestamp

object Packages : UuidTable() {

    val name = varchar("name", 255)
    val format = enumerationByName<PackageFormat>("format", 255)
    val organizationId = uuid("organization_id").index()
    val isPublic = bool("is_public")
    val createdAt = timestamp("created_at")

    init {
        uniqueIndex(name, format)
    }

    fun toPackage(
        row: ResultRow,
    ) = Package(
        row[id].value,
        row[name],
        row[format],
        row[organizationId],
        row[isPublic],
        row[createdAt],
    )

}

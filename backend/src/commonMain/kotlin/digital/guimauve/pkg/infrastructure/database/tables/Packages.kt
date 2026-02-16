package digital.guimauve.pkg.infrastructure.database.tables

import dev.kaccelero.models.UUID
import digital.guimauve.pkg.models.packages.Package
import digital.guimauve.pkg.models.packages.PackageFormat
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object Packages : UUIDTable() {

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
        UUID(row[id].value),
        row[name],
        row[format],
        UUID(row[organizationId]),
        row[isPublic],
        row[createdAt],
    )

}

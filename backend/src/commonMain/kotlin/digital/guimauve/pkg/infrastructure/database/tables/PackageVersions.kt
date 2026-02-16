package digital.guimauve.pkg.infrastructure.database.tables

import dev.kaccelero.models.UUID
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object PackageVersions : UUIDTable() {

    val packageId = uuid("package_id").index()
    val version = varchar("version", 255)
    val publishedBy = uuid("published_by")
    val publishedAt = timestamp("published_at")
    val metadata = text("metadata").nullable()
    val yanked = bool("yanked").default(false)

    init {
        uniqueIndex(packageId, version)
    }

    fun toPackageVersion(
        row: ResultRow,
    ) = PackageVersion(
        UUID(row[id].value),
        UUID(row[packageId]),
        row[version],
        UUID(row[publishedBy]),
        row[publishedAt],
        row[metadata],
        row[yanked],
    )

}

package digital.guimauve.pkg.infrastructure.database.tables

import digital.guimauve.pkg.models.packages.versions.PackageVersion
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.timestamp

object PackageVersions : UuidTable() {

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
        row[id].value,
        row[packageId],
        row[version],
        row[publishedBy],
        row[publishedAt],
        row[metadata],
        row[yanked],
    )

}

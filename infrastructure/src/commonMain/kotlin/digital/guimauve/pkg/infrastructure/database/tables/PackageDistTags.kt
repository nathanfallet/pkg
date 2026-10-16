package digital.guimauve.pkg.infrastructure.database.tables

import org.jetbrains.exposed.v1.core.Table

object PackageDistTags : Table() {

    val packageId = uuid("package_id")
    val tag = varchar("tag", 255)
    val versionId = uuid("version_id")

    override val primaryKey = PrimaryKey(packageId, tag)

}

package digital.guimauve.pkg.infrastructure.database.tables

import digital.guimauve.pkg.models.organizations.Organization
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import kotlin.uuid.toKotlinUuid

object Organizations : UUIDTable() {
    val name = text("name")

    fun toOrganization(
        row: ResultRow,
    ) = Organization(
        row[id].value.toKotlinUuid(),
        row[name]
    )
}

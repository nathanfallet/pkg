package digital.guimauve.pkg.infrastructure.database.tables

import digital.guimauve.pkg.models.organizations.Organization
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.UuidTable

object Organizations : UuidTable() {

    val name = text("name")

    fun toOrganization(
        row: ResultRow,
    ) = Organization(
        row[id].value,
        row[name],
    )

}

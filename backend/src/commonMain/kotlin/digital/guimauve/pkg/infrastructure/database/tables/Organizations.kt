package digital.guimauve.pkg.infrastructure.database.tables

import dev.kaccelero.models.UUID
import digital.guimauve.pkg.models.organizations.Organization
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow

object Organizations : UUIDTable() {

    val name = text("name")

    fun toOrganization(
        row: ResultRow,
    ) = Organization(
        UUID(row[id].value),
        row[name]
    )

}

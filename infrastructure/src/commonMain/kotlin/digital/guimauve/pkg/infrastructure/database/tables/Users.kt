package digital.guimauve.pkg.infrastructure.database.tables

import digital.guimauve.pkg.models.users.User
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.UuidTable

object Users : UuidTable() {

    val organizationId = uuid("organization_id").index()
    val email = varchar("email", 255).index()
    val password = varchar("password", 255)

    fun toUser(
        row: ResultRow,
        includePassword: Boolean = false,
    ) = User(
        row[id].value,
        row[organizationId],
        row[email],
        row[password].takeIf { includePassword },
    )

}

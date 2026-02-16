package digital.guimauve.pkg.infrastructure.database.tables

import dev.kaccelero.models.UUID
import digital.guimauve.pkg.models.users.User
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow

object Users : UUIDTable() {

    val organizationId = uuid("organization_id").index()
    val email = varchar("email", 255).index()
    val password = varchar("password", 255)

    fun toUser(
        row: ResultRow,
        includePassword: Boolean = false,
    ) = User(
        UUID(row[id].value),
        UUID(row[organizationId]),
        row[email],
        row[password].takeIf { includePassword },
    )

}

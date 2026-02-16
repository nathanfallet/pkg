package digital.guimauve.pkg.infrastructure.database.tables

import digital.guimauve.pkg.models.users.User
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import kotlin.uuid.toKotlinUuid

object Users : UUIDTable() {
    val organizationId = uuid("organization_id").index()
    val email = varchar("email", 255).index()
    val password = varchar("password", 255)

    fun toUser(
        row: ResultRow,
        includePassword: Boolean = false,
    ) = User(
        row[id].value.toKotlinUuid(),
        row[organizationId].toKotlinUuid(),
        row[email],
        row[password].takeIf { includePassword },
    )
}

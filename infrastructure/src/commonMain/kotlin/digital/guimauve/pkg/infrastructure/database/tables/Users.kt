package digital.guimauve.pkg.infrastructure.database.tables

import digital.guimauve.pkg.domain.models.users.User
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.UuidTable

object Users : UuidTable() {

    val organizationId = uuid("organization_id").index()
    val email = varchar("email", 255).index()
    val password = varchar("password", 255)

    val admin = bool("admin").default(false)
    val disabled = bool("disabled").default(false)
    val passwordChangeRequired = bool("password_change_required").default(false)
    val credentialVersion = integer("credential_version").default(0)

    fun toUser(
        row: ResultRow,
        includePassword: Boolean = false,
    ) = User(
        row[id].value,
        row[organizationId],
        row[email],
        row[password].takeIf { includePassword },
        row[admin],
        row[disabled],
        row[passwordChangeRequired],
        row[credentialVersion],
    )

}

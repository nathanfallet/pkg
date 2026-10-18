package digital.guimauve.pkg.domain.models.users

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * A user, member of exactly one organization. [password] is the encoded password, and only filled
 * when explicitly requested. [email] is the historical field name for the login identifier; new
 * accounts can use a username without a mailbox. [admin] grants instance-wide user management,
 * but package access is still limited to [organizationId].
 */
@Serializable
data class User(
    val id: Uuid,
    val organizationId: Uuid,
    val email: String,
    val password: String?,
    val admin: Boolean = false,
    val disabled: Boolean = false,
    val passwordChangeRequired: Boolean = false,
    val credentialVersion: Int = 0,
) {

    companion object {

        /** Historical email format, retained for callers that validate email identifiers. */
        const val EMAIL_REGEX = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"

    }

}

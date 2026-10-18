package digital.guimauve.pkg.domain.services

import digital.guimauve.pkg.domain.models.organizations.Organization
import digital.guimauve.pkg.domain.models.users.User
import kotlin.uuid.Uuid

class AccountFailure(val status: Int, val key: String) : Exception(key)

/** Plaintext exists only in the result of creation/reset, never in a user read or diagnostic string. */
class TemporaryPassword(val user: User, val password: String) {
    override fun toString() = "TemporaryPassword([REDACTED])"
}

data class AdministrationOverview(val users: List<User>, val organizations: List<Organization>)

/** Instance administrators manage accounts; package access remains scoped to each user's organization. */
interface UserAdministrationService {
    suspend fun overview(actor: User): AdministrationOverview
    suspend fun create(actor: User, username: String, organizationId: Uuid, admin: Boolean): TemporaryPassword
    suspend fun update(actor: User, userId: Uuid, admin: Boolean, disabled: Boolean)
    suspend fun resetPassword(actor: User, userId: Uuid): TemporaryPassword
    suspend fun changePassword(actor: User, current: String, replacement: String)
}

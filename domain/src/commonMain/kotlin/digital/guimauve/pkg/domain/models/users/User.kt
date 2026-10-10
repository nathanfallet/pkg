package digital.guimauve.pkg.domain.models.users

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * A user, member of exactly one organization. [password] is the encoded password, and only filled
 * when it was asked for (the login); it is null everywhere else.
 */
@Serializable
data class User(
    val id: Uuid,
    val organizationId: Uuid,
    val email: String,
    val password: String?,
) {

    companion object {

        /** The format an email address must have; the web login form checks it before reaching the database. */
        const val EMAIL_REGEX = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"

    }

}

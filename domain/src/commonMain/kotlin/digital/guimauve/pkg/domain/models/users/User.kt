package digital.guimauve.pkg.domain.models.users

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class User(
    val id: Uuid,
    val organizationId: Uuid,
    val email: String,
    val password: String?,
) {

    companion object {

        const val EMAIL_REGEX = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"

    }

}

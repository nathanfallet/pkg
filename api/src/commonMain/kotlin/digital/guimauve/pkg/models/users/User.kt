package digital.guimauve.pkg.models.users

import digital.guimauve.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.uuid.Uuid

@Zodable
@JsExport
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

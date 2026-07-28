package digital.guimauve.pkg.models.users

import dev.kaccelero.models.IChildModel
import digital.guimauve.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.uuid.Uuid

@Zodable
@JsExport
@Serializable
data class User(
    override val id: Uuid,
    val organizationId: Uuid,
    val email: String,
    val password: String?,
) : IChildModel<Uuid, CreateUserPayload, Unit, Uuid> {

    override val parentId: Uuid
        get() = organizationId

    companion object {

        const val EMAIL_REGEX = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"

    }

}

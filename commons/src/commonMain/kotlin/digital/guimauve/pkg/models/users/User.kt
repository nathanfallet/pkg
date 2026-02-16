package digital.guimauve.pkg.models.users

import dev.kaccelero.annotations.ModelProperty
import dev.kaccelero.annotations.Schema
import dev.kaccelero.models.IChildModel
import dev.kaccelero.models.IUser
import digital.guimauve.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.uuid.Uuid

@Zodable
@JsExport
@Serializable
data class User(
    @ModelProperty("id")
    @Schema("Id of the User", "123abc")
    override val id: Uuid,
    @Schema("Id of the organization the user is in", "123abc")
    val organizationId: Uuid,
    @ModelProperty("email", "12", visibleOnUpdate = true)
    @Schema("Email of the User", "nathan@guimauve.digital")
    val email: String,
    val password: String?,
) : IChildModel<Uuid, CreateUserPayload, Unit, Uuid>, IUser {

    override val parentId: Uuid
        get() = organizationId

    companion object {

        const val EMAIL_REGEX = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"

    }

}

package digital.guimauve.pkg.api.responses.users

import dev.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.uuid.Uuid

@Zodable
@JsExport
@Serializable
data class UserResponse(
    val id: Uuid,
    val organizationId: Uuid,
    val email: String,
)

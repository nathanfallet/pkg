package digital.guimauve.pkg.models.users

import digital.guimauve.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Zodable
@JsExport
@Serializable
data class CreateUserPayload(
    val email: String,
    val password: String,
)

package digital.guimauve.pkg.models.auth

import digital.guimauve.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Zodable
@JsExport
@Serializable
data class LoginPayload(
    val email: String,
    val password: String,
)

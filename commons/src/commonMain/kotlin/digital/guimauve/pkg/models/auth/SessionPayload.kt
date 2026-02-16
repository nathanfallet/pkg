package digital.guimauve.pkg.models.auth

import dev.kaccelero.commons.auth.ISessionPayload
import digital.guimauve.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.uuid.Uuid

@Zodable
@JsExport
@Serializable
data class SessionPayload(
    val userId: Uuid,
) : ISessionPayload

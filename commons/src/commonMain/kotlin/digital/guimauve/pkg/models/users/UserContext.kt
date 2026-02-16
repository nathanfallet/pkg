package digital.guimauve.pkg.models.users

import dev.kaccelero.models.IContext
import digital.guimauve.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.uuid.Uuid

@Zodable
@JsExport
@Serializable
data class UserContext(
    val userId: Uuid,
) : IContext

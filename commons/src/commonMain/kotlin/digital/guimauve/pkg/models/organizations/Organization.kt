package digital.guimauve.pkg.models.organizations

import digital.guimauve.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.uuid.Uuid

@Zodable
@JsExport
@Serializable
data class Organization(
    val id: Uuid,
    val name: String,
)

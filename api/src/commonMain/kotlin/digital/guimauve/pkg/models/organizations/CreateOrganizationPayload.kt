package digital.guimauve.pkg.models.organizations

import digital.guimauve.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Zodable
@JsExport
@Serializable
data class CreateOrganizationPayload(
    val name: String,
)

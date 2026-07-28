package digital.guimauve.pkg.api.responses.organizations

import digital.guimauve.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.uuid.Uuid

@Zodable
@JsExport
@Serializable
data class OrganizationResponse(
    val id: Uuid,
    val name: String,
)

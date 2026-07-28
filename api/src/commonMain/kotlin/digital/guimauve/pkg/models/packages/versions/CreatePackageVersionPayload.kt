package digital.guimauve.pkg.models.packages.versions

import digital.guimauve.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Zodable
@JsExport
@Serializable
data class CreatePackageVersionPayload(
    val version: String,
    val metadata: String?,
)

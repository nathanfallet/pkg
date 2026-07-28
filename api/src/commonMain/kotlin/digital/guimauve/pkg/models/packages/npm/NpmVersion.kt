package digital.guimauve.pkg.models.packages.npm

import digital.guimauve.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Zodable
@JsExport
@Serializable
data class NpmVersion(
    val name: String,
    val version: String,
)

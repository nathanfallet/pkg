package digital.guimauve.pkg.models.packages.npm

import digital.guimauve.zodable.Zodable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Zodable
@JsExport
@Serializable
data class NpmPackage(
    @SerialName("_id") val id: String,
    val name: String,
    val description: String? = null,
    val versions: Map<String, NpmVersion>,
)

package digital.guimauve.pkg.api.models.packages.npm

import dev.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Zodable
@JsExport
@Serializable
data class NpmVersion(
    val name: String,
    val version: String,
)

package digital.guimauve.pkg.models.packages

import digital.guimauve.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Zodable
@JsExport
@Serializable
data class CreatePackagePayload(
    val name: String,
    val format: PackageFormat,
    val isPublic: Boolean,
)

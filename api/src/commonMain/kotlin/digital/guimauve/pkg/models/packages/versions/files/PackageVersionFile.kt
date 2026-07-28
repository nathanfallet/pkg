package digital.guimauve.pkg.models.packages.versions.files

import digital.guimauve.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.uuid.Uuid

@Zodable
@JsExport
@Serializable
data class PackageVersionFile(
    val id: Uuid,
    val versionId: Uuid,
    val name: String,
    val contentType: String,
    val size: Long,
    val path: String,
)

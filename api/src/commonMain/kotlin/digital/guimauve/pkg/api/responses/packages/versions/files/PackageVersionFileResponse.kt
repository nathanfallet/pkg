package digital.guimauve.pkg.api.responses.packages.versions.files

import dev.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.uuid.Uuid

@Zodable
@JsExport
@Serializable
data class PackageVersionFileResponse(
    val id: Uuid,
    val versionId: Uuid,
    val name: String,
    val contentType: String,
    val size: Long,
    val path: String,
)

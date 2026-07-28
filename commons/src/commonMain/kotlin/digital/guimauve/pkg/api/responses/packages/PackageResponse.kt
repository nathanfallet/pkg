package digital.guimauve.pkg.api.responses.packages

import digital.guimauve.pkg.models.packages.PackageFormat
import digital.guimauve.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Zodable
@JsExport
@Serializable
data class PackageResponse(
    val id: Uuid,
    val organizationId: Uuid,
    val name: String,
    val format: PackageFormat,
    val isPublic: Boolean,
    val createdAt: Instant,
)

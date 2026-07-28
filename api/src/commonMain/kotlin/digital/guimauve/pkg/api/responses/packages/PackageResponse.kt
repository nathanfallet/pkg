package digital.guimauve.pkg.api.responses.packages

import dev.zodable.Zodable
import digital.guimauve.pkg.api.models.packages.PackageFormat
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

package digital.guimauve.pkg.models.packages

import digital.guimauve.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Zodable
@JsExport
@Serializable
data class Package(
    val id: Uuid,
    val name: String,
    val format: PackageFormat,
    val organizationId: Uuid,
    val isPublic: Boolean,
    val createdAt: Instant,
)

package digital.guimauve.pkg.models.packages.versions

import digital.guimauve.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Zodable
@JsExport
@Serializable
data class PackageVersion(
    val id: Uuid,
    val packageId: Uuid,
    val version: String,
    val publishedBy: Uuid,
    val publishedAt: Instant,
    val metadata: String?,
    val yanked: Boolean,
)

package digital.guimauve.pkg.api.responses.packages.versions

import dev.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Zodable
@JsExport
@Serializable
data class PackageVersionResponse(
    val id: Uuid,
    val packageId: Uuid,
    val version: String,
    val publishedBy: Uuid,
    val publishedAt: Instant,
    val metadata: String?,
    val yanked: Boolean,
)

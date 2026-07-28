package digital.guimauve.pkg.domain.models.packages.versions

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

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

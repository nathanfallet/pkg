package digital.guimauve.pkg.domain.models.packages.versions

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * A published version of a package. [version] is the version string (e.g. `1.0.0`), unique per
 * package; [publishedBy] is the user who published it. [metadata] is what the registry hands back
 * verbatim when the version is resolved, such as an npm version manifest (null for maven). [yanked]
 * is not set by anything yet: every version is created unyanked.
 */
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

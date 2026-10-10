package digital.guimauve.pkg.domain.models.packages

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * A package published to the registry, owned by one organization. Its [name] is unique per [format]
 * across the whole registry (`group:artifact` for maven). A public package ([isPublic]) is readable
 * by anyone; a private one only by its organization. Only its organization publishes to it.
 */
@Serializable
data class Package(
    val id: Uuid,
    val name: String,
    val format: PackageFormat,
    val organizationId: Uuid,
    val isPublic: Boolean,
    val createdAt: Instant,
)

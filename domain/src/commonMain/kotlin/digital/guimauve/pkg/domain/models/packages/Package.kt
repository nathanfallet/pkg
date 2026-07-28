package digital.guimauve.pkg.domain.models.packages

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class Package(
    val id: Uuid,
    val name: String,
    val format: PackageFormat,
    val organizationId: Uuid,
    val isPublic: Boolean,
    val createdAt: Instant,
)

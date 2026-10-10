package digital.guimauve.pkg.domain.models.packages

import kotlinx.serialization.Serializable

/** What it takes to register a [Package]. */
@Serializable
data class CreatePackagePayload(
    val name: String,
    val format: PackageFormat,
    val isPublic: Boolean,
)

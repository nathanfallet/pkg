package digital.guimauve.pkg.domain.models.packages

import kotlinx.serialization.Serializable

@Serializable
data class CreatePackagePayload(
    val name: String,
    val format: PackageFormat,
    val isPublic: Boolean,
)

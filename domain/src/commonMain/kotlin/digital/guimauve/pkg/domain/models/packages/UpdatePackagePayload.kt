package digital.guimauve.pkg.domain.models.packages

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePackagePayload(
    val isPublic: Boolean,
)

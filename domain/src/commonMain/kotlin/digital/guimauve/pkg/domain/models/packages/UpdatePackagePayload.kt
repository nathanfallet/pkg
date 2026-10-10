package digital.guimauve.pkg.domain.models.packages

import kotlinx.serialization.Serializable

/** What can be changed on a [Package]: its visibility. */
@Serializable
data class UpdatePackagePayload(
    val isPublic: Boolean,
)

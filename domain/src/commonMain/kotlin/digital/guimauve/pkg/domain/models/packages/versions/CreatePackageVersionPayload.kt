package digital.guimauve.pkg.domain.models.packages.versions

import kotlinx.serialization.Serializable

@Serializable
data class CreatePackageVersionPayload(
    val version: String,
    val metadata: String?,
)

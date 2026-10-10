package digital.guimauve.pkg.domain.models.packages.versions

import kotlinx.serialization.Serializable

/** What it takes to record a [PackageVersion]. */
@Serializable
data class CreatePackageVersionPayload(
    val version: String,
    val metadata: String?,
)

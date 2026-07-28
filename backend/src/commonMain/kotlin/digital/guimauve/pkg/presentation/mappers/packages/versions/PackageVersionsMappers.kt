package digital.guimauve.pkg.presentation.mappers.packages.versions

import digital.guimauve.pkg.api.responses.packages.versions.PackageVersionResponse
import digital.guimauve.pkg.models.packages.versions.PackageVersion

/**
 * Maps a [PackageVersion] to a [PackageVersionResponse].
 *
 * @return The mapped [PackageVersionResponse].
 */
fun PackageVersion.toPackageVersionResponse() = PackageVersionResponse(
    id = id,
    packageId = packageId,
    version = version,
    publishedBy = publishedBy,
    publishedAt = publishedAt,
    metadata = metadata,
    yanked = yanked,
)

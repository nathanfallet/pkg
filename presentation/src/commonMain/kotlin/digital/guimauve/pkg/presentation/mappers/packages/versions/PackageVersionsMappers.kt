package digital.guimauve.pkg.presentation.mappers.packages.versions

import digital.guimauve.pkg.api.responses.packages.versions.PackageVersionResponse
import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import digital.guimauve.pkg.presentation.mappers.formatted
import digital.guimauve.pkg.presentation.views.PackageVersionView

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

/**
 * Maps a [PackageVersion] to a [PackageVersionView].
 *
 * @return The mapped [PackageVersionView].
 */
fun PackageVersion.toPackageVersionView() = PackageVersionView(
    id = id.toString(),
    version = version,
    publishedAt = publishedAt.formatted(),
    metadata = metadata,
    yanked = yanked,
    url = "/packages/$packageId/versions/$id",
)

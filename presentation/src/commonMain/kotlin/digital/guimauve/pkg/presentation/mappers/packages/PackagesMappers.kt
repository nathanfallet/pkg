package digital.guimauve.pkg.presentation.mappers.packages

import digital.guimauve.pkg.api.models.packages.PackageFormat
import digital.guimauve.pkg.api.responses.packages.PackageResponse
import digital.guimauve.pkg.domain.models.packages.Package
import digital.guimauve.pkg.presentation.mappers.formatted
import digital.guimauve.pkg.presentation.views.PackageView
import digital.guimauve.pkg.domain.models.packages.PackageFormat as DomainPackageFormat

/**
 * Maps a [DomainPackageFormat] to the [PackageFormat] the API exposes.
 *
 * @return The mapped [PackageFormat].
 */
fun DomainPackageFormat.toPackageFormat() = when (this) {
    DomainPackageFormat.MAVEN -> PackageFormat.MAVEN
    DomainPackageFormat.NPM -> PackageFormat.NPM
    DomainPackageFormat.PYPI -> PackageFormat.PYPI
}

/**
 * Maps a [Package] to a [PackageResponse].
 *
 * @return The mapped [PackageResponse].
 */
fun Package.toPackageResponse() = PackageResponse(
    id = id,
    organizationId = organizationId,
    name = name,
    format = format.toPackageFormat(),
    isPublic = isPublic,
    createdAt = createdAt,
)

/**
 * Maps a [Package] to a [PackageView].
 *
 * @return The mapped [PackageView].
 */
fun Package.toPackageView() = PackageView(
    id = id.toString(),
    name = name,
    format = format.name,
    createdAt = createdAt.formatted(),
    url = "/packages/$id",
    visibility = if (isPublic) "Public" else "Private",
    visibilityBadge = if (isPublic) "bg-success" else "bg-danger",
)

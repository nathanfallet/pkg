package digital.guimauve.pkg.presentation.mappers.packages

import digital.guimauve.pkg.api.responses.packages.PackageResponse
import digital.guimauve.pkg.models.packages.Package
import digital.guimauve.pkg.presentation.mappers.formatted
import digital.guimauve.pkg.presentation.views.PackageView

/**
 * Maps a [Package] to a [PackageResponse].
 *
 * @return The mapped [PackageResponse].
 */
fun Package.toPackageResponse() = PackageResponse(
    id = id,
    organizationId = organizationId,
    name = name,
    format = format,
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

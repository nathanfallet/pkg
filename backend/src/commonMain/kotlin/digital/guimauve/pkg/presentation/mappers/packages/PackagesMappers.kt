package digital.guimauve.pkg.presentation.mappers.packages

import digital.guimauve.pkg.api.responses.packages.PackageResponse
import digital.guimauve.pkg.models.packages.Package

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

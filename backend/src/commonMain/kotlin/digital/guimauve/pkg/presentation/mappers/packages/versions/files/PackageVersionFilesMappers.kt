package digital.guimauve.pkg.presentation.mappers.packages.versions.files

import digital.guimauve.pkg.api.responses.packages.versions.files.PackageVersionFileResponse
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile

/**
 * Maps a [PackageVersionFile] to a [PackageVersionFileResponse].
 *
 * @return The mapped [PackageVersionFileResponse].
 */
fun PackageVersionFile.toPackageVersionFileResponse() = PackageVersionFileResponse(
    id = id,
    versionId = versionId,
    name = name,
    contentType = contentType,
    size = size,
    path = path,
)

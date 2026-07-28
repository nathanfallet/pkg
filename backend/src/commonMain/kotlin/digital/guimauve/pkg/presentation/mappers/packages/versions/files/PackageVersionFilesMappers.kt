package digital.guimauve.pkg.presentation.mappers.packages.versions.files

import digital.guimauve.pkg.api.responses.packages.versions.files.PackageVersionFileResponse
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.presentation.mappers.formattedSize
import digital.guimauve.pkg.presentation.views.PackageVersionFileView

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

/**
 * Maps a [PackageVersionFile] to a [PackageVersionFileView].
 *
 * @return The mapped [PackageVersionFileView].
 */
fun PackageVersionFile.toPackageVersionFileView() = PackageVersionFileView(
    name = name,
    contentType = contentType,
    size = size.formattedSize(),
    url = path,
)

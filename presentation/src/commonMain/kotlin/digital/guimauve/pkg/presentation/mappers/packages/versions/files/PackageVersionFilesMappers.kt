package digital.guimauve.pkg.presentation.mappers.packages.versions.files

import digital.guimauve.pkg.api.responses.packages.versions.files.PackageVersionFileResponse
import digital.guimauve.pkg.domain.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.presentation.mappers.formattedSize
import digital.guimauve.pkg.presentation.views.PackageVersionFileView
import io.ktor.http.*
import kotlin.uuid.Uuid

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
 * @param packageId The package the file belongs to, which its download link goes through.
 *
 * @return The mapped [PackageVersionFileView].
 */
fun PackageVersionFile.toPackageVersionFileView(packageId: Uuid) = PackageVersionFileView(
    name = name,
    contentType = contentType,
    size = size.formattedSize(),
    // A filename may hold a `#`, a `?` or a `%`, which would otherwise cut the link short.
    url = "/packages/$packageId/versions/$versionId/files/${name.encodeURLPathPart()}",
)

package digital.guimauve.pkg.domain.models.packages.versions.files

import digital.guimauve.pkg.domain.models.packages.Package
import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import kotlinx.serialization.Serializable

/**
 * What it takes to record a [PackageVersionFile]. The secondary constructor derives the storage path,
 * `<organizationId>/<packageId>/<versionId>/<name>`.
 */
@Serializable
data class CreatePackageVersionFilePayload(
    val name: String,
    val path: String,
) {

    constructor(name: String, `package`: Package, version: PackageVersion) : this(
        name,
        "${`package`.organizationId}/${`package`.id}/${version.id}/$name"
    )

}

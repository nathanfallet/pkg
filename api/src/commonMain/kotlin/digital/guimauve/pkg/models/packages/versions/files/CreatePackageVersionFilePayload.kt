package digital.guimauve.pkg.models.packages.versions.files

import digital.guimauve.pkg.models.packages.Package
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import digital.guimauve.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.js.JsName

@Zodable
@JsExport
@Serializable
data class CreatePackageVersionFilePayload(
    val name: String,
    val path: String,
) {

    @JsName("fromPackageAndVersion")
    constructor(name: String, `package`: Package, version: PackageVersion) : this(
        name,
        "${`package`.organizationId}/${`package`.id}/${version.id}/$name"
    )

}

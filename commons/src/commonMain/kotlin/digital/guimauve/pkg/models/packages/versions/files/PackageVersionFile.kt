package digital.guimauve.pkg.models.packages.versions.files

import dev.kaccelero.models.IChildModel
import digital.guimauve.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.uuid.Uuid

@Zodable
@JsExport
@Serializable
data class PackageVersionFile(
    override val id: Uuid,
    val versionId: Uuid,
    val name: String,
    val contentType: String,
    val size: Long,
    val path: String,
) : IChildModel<Uuid, CreatePackageVersionFilePayload, Unit, Uuid> {

    override val parentId: Uuid
        get() = versionId

}

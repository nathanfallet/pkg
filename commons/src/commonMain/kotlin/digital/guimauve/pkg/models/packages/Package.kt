package digital.guimauve.pkg.models.packages

import dev.kaccelero.models.IChildModel
import digital.guimauve.zodable.Zodable
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.uuid.Uuid

@Zodable
@JsExport
@Serializable
data class Package(
    override val id: Uuid,
    val name: String,
    val format: PackageFormat,
    val organizationId: Uuid,
    val isPublic: Boolean,
    val createdAt: Instant,
) : IChildModel<Uuid, CreatePackagePayload, UpdatePackagePayload, Uuid> {

    override val parentId: Uuid
        get() = organizationId

}

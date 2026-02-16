package digital.guimauve.pkg.models.packages.versions

import dev.kaccelero.models.IChildModel
import digital.guimauve.zodable.Zodable
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.uuid.Uuid

@Zodable
@JsExport
@Serializable
data class PackageVersion(
    override val id: Uuid,
    val packageId: Uuid,
    val version: String,
    val publishedBy: Uuid,
    val publishedAt: Instant,
    val metadata: String?,
    val yanked: Boolean,
) : IChildModel<Uuid, CreatePackageVersionPayload, Unit, Uuid> {

    override val parentId: Uuid
        get() = packageId

}

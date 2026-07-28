package digital.guimauve.pkg.domain.models.packages.versions.files

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class PackageVersionFile(
    val id: Uuid,
    val versionId: Uuid,
    val name: String,
    val contentType: String,
    val size: Long,
    val path: String,
)

package digital.guimauve.pkg.domain.models.packages.versions.files

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * A file published for a version. [name] is unique per version; [size] is in bytes; [path] is where
 * its content lives in the storage.
 */
@Serializable
data class PackageVersionFile(
    val id: Uuid,
    val versionId: Uuid,
    val name: String,
    val contentType: String,
    val size: Long,
    val path: String,
)

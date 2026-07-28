package digital.guimauve.pkg.models.packages.maven

import digital.guimauve.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Zodable
@JsExport
@Serializable
data class MavenPath(
    val groupId: String,
    val artifactId: String,
    val version: String?,
    val filename: String,
) {

    val packageName: String
        get() = "$groupId:$artifactId"

}

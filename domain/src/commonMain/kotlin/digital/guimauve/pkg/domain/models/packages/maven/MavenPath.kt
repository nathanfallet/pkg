package digital.guimauve.pkg.domain.models.packages.maven

import kotlinx.serialization.Serializable

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

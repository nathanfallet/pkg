package digital.guimauve.pkg.domain.models.packages.maven

import kotlinx.serialization.Serializable

/**
 * A maven repository path, as
 * [ParseMavenPathUseCase][digital.guimauve.pkg.domain.usecases.packages.maven.ParseMavenPathUseCase]
 * reads it. [version] is null for a path that carries no version, such as `maven-metadata.xml`.
 */
@Serializable
data class MavenPath(
    val groupId: String,
    val artifactId: String,
    val version: String?,
    val filename: String,
) {

    val packageName: String
        get() = "$groupId:$artifactId"

    /**
     * Whether this is the `maven-metadata.xml` of the module, or one of its checksums: the only files
     * a client rewrites, each publication adding its version to the list.
     */
    val isModuleMetadata: Boolean
        get() = version == null && (filename == METADATA || filename.startsWith("$METADATA."))

    private companion object {
        const val METADATA = "maven-metadata.xml"
    }

}

package digital.guimauve.pkg.domain.repositories

import kotlin.uuid.Uuid

/** Reads and writes the dist-tags of the packages (npm), each naming one version of its package. */
interface PackageDistTagsRepository {

    /** Lists the tags of a package, each with the identifier of the version it points to. */
    suspend fun list(packageId: Uuid): Map<String, Uuid>

    /** Points a tag of a package at a version, creating the tag or moving it. */
    suspend fun set(packageId: Uuid, tag: String, versionId: Uuid)

}

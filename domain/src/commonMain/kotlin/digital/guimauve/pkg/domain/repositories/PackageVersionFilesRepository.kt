package digital.guimauve.pkg.domain.repositories

import digital.guimauve.pkg.domain.models.packages.versions.files.CreatePackageVersionFilePayload
import digital.guimauve.pkg.domain.models.packages.versions.files.PackageVersionFile
import kotlin.uuid.Uuid

/** Reads and writes the records of the files published for each version (their content lives in the storage). */
interface PackageVersionFilesRepository {

    /** Lists the files of a version, by name. */
    suspend fun list(versionId: Uuid): List<PackageVersionFile>

    /** Reads a file of a version by its name (unique per version). */
    suspend fun getByName(name: String, versionId: Uuid): PackageVersionFile?

    /**
     * Finds a file by name across every version of a package, most recently published first.
     * This is what serves a maven path that carries no version.
     */
    suspend fun getLatestByName(name: String, packageId: Uuid): PackageVersionFile?

    /**
     * Records a file published for a version. A name the version already has violates the unique
     * index and throws.
     *
     * @param contentType The MIME type the file is served with.
     * @param size The size of the content, in bytes.
     *
     * @return The created file, or null if the insert returned no row.
     */
    suspend fun create(
        payload: CreatePackageVersionFilePayload,
        versionId: Uuid,
        contentType: String,
        size: Long,
    ): PackageVersionFile?

    /**
     * Records new content for a file whose storage object was just overwritten.
     *
     * @return The updated file, or null if there is no file with that identifier.
     */
    suspend fun update(id: Uuid, contentType: String, size: Long): PackageVersionFile?

}

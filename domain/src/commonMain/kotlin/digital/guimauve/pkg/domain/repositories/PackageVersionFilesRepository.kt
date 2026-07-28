package digital.guimauve.pkg.domain.repositories

import digital.guimauve.pkg.domain.models.packages.versions.files.CreatePackageVersionFilePayload
import digital.guimauve.pkg.domain.models.packages.versions.files.PackageVersionFile
import kotlin.uuid.Uuid

interface PackageVersionFilesRepository {

    suspend fun list(versionId: Uuid): List<PackageVersionFile>
    suspend fun getByName(name: String, versionId: Uuid): PackageVersionFile?

    /**
     * Finds a file by name across every version of a package, most recently published first.
     * This is what serves a maven path that carries no version.
     */
    suspend fun getLatestByName(name: String, packageId: Uuid): PackageVersionFile?

    suspend fun create(
        payload: CreatePackageVersionFilePayload,
        versionId: Uuid,
        contentType: String,
        size: Long,
    ): PackageVersionFile?

}

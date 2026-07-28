package digital.guimauve.pkg.domain.usecases.packages.versions.files

import digital.guimauve.pkg.domain.exceptions.packages.versions.files.PackageVersionFileAlreadyExistsException
import digital.guimauve.pkg.models.packages.versions.files.CreatePackageVersionFilePayload
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.services.storage.FileSource
import kotlin.uuid.Uuid

interface CreatePackageVersionFileUseCase {

    /**
     * Publishes a file for a version.
     *
     * @param payload The name and storage path of the file.
     * @param versionId The version the file belongs to.
     * @param source Where the content comes from.
     *
     * @return The published file.
     * @throws PackageVersionFileAlreadyExistsException if the version already has that file.
     */
    suspend operator fun invoke(
        payload: CreatePackageVersionFilePayload,
        versionId: Uuid,
        source: FileSource,
    ): PackageVersionFile?

}

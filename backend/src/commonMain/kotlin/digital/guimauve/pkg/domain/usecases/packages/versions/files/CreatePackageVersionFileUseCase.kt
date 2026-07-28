package digital.guimauve.pkg.domain.usecases.packages.versions.files

import dev.kaccelero.models.IContext
import digital.guimauve.pkg.domain.exceptions.packages.versions.files.PackageVersionFileAlreadyExistsException
import digital.guimauve.pkg.models.packages.versions.files.CreatePackageVersionFilePayload
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import kotlin.uuid.Uuid

interface CreatePackageVersionFileUseCase {

    /**
     * Publishes a file for a version.
     *
     * @param payload The name and storage path of the file.
     * @param versionId The version the file belongs to.
     * @param context Where the content comes from: a stream, bytes or a URL.
     *
     * @return The published file, or null if the context carried nothing usable.
     * @throws PackageVersionFileAlreadyExistsException if the version already has that file.
     */
    // TODO: `IContext` is the last kaccelero type here; it goes away with the repository layer.
    suspend operator fun invoke(
        payload: CreatePackageVersionFilePayload,
        versionId: Uuid,
        context: IContext,
    ): PackageVersionFile?

}

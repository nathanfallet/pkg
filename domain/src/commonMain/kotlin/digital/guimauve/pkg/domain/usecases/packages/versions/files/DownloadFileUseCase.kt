package digital.guimauve.pkg.domain.usecases.packages.versions.files

import digital.guimauve.pkg.domain.exceptions.storage.StorageFileNotFoundException
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile

interface DownloadFileUseCase {

    /**
     * Reads the content of a published file from the storage.
     *
     * @return The bytes of the file.
     * @throws StorageFileNotFoundException if the storage has no such file.
     */
    suspend operator fun invoke(input: PackageVersionFile): ByteArray

}

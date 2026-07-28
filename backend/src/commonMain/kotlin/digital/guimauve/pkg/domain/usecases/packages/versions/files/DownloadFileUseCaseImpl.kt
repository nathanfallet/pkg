package digital.guimauve.pkg.domain.usecases.packages.versions.files

import digital.guimauve.pkg.domain.exceptions.storage.StorageFileNotFoundException
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.services.storage.IStorageService

class DownloadFileUseCaseImpl(
    private val storageService: IStorageService,
) : DownloadFileUseCase {
    override suspend fun invoke(input: PackageVersionFile): ByteArray =
        storageService.downloadStream(input.path)?.readBytes() ?: throw StorageFileNotFoundException()
}

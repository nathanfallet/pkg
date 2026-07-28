package digital.guimauve.pkg.domain.usecases.packages.versions.files

import digital.guimauve.pkg.domain.exceptions.storage.StorageFileNotFoundException
import digital.guimauve.pkg.domain.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.domain.services.StorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DownloadFileUseCaseImpl(
    private val storageService: StorageService,
) : DownloadFileUseCase {
    override suspend fun invoke(input: PackageVersionFile): ByteArray {
        val stream = storageService.downloadStream(input.path) ?: throw StorageFileNotFoundException()
        // `readBytes` does not close the stream, and the local storage hands out a live file handle.
        return withContext(Dispatchers.IO) { stream.use { it.readBytes() } }
    }
}

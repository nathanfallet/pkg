package digital.guimauve.pkg.domain.usecases.packages.versions.files

import digital.guimauve.pkg.domain.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.domain.models.storage.FileFromStream
import digital.guimauve.pkg.domain.repositories.PackageVersionFilesRepository
import digital.guimauve.pkg.domain.services.StorageService

class ReplacePackageVersionFileUseCaseImpl(
    private val repository: PackageVersionFilesRepository,
    private val storageService: StorageService,
) : ReplacePackageVersionFileUseCase {
    override suspend fun invoke(file: PackageVersionFile, source: FileFromStream): PackageVersionFile? {
        // Same storage path: the object is overwritten, then the record follows its new size.
        storageService.uploadStream(source, file.path) ?: return null
        return repository.update(file.id, source.contentType, source.contentLength)
    }
}

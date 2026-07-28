package digital.guimauve.pkg.domain.usecases.packages.versions.files

import dev.kaccelero.models.IContext
import digital.guimauve.pkg.domain.exceptions.packages.versions.files.PackageVersionFileAlreadyExistsException
import digital.guimauve.pkg.domain.repositories.PackageVersionFilesRepository
import digital.guimauve.pkg.models.packages.versions.files.CreatePackageVersionFilePayload
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.services.storage.FileContext
import digital.guimauve.pkg.services.storage.FileFromBytesContext
import digital.guimauve.pkg.services.storage.FileFromUrlContext
import digital.guimauve.pkg.services.storage.IStorageService
import kotlin.uuid.Uuid

class CreatePackageVersionFileUseCaseImpl(
    private val repository: PackageVersionFilesRepository,
    private val storageService: IStorageService,
) : CreatePackageVersionFileUseCase {
    override suspend fun invoke(
        payload: CreatePackageVersionFilePayload,
        versionId: Uuid,
        context: IContext,
    ): PackageVersionFile? {
        repository.getByName(payload.name, versionId)?.let {
            throw PackageVersionFileAlreadyExistsException()
        }

        (context as? FileFromBytesContext)?.let { fileFromBytes ->
            val contentLength = fileFromBytes.bytes.size.toLong()
            return fileFromBytes.bytes.inputStream().use { stream ->
                invoke(payload, versionId, FileContext(stream, fileFromBytes.contentType, contentLength))
            }
        }
        (context as? FileFromUrlContext)?.let { fileFromUrl ->
            val connection = fileFromUrl.url.openConnection()
            val contentLength = connection.contentLengthLong.takeIf { it > 0 } ?: 0L
            return connection.getInputStream().use { stream ->
                invoke(payload, versionId, FileContext(stream, fileFromUrl.contentType, contentLength))
            }
        }
        val fileContext = context as? FileContext ?: return null
        storageService.uploadStream(fileContext, payload.path)
        return repository.create(payload, versionId, fileContext)
    }
}

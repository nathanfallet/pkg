package digital.guimauve.pkg.domain.usecases.packages.versions.files

import digital.guimauve.pkg.domain.exceptions.packages.versions.files.PackageVersionFileAlreadyExistsException
import digital.guimauve.pkg.domain.models.storage.FileFromBytes
import digital.guimauve.pkg.domain.models.storage.FileFromStream
import digital.guimauve.pkg.domain.models.storage.FileFromUrl
import digital.guimauve.pkg.domain.models.storage.FileSource
import digital.guimauve.pkg.domain.repositories.PackageVersionFilesRepository
import digital.guimauve.pkg.domain.services.StorageService
import digital.guimauve.pkg.models.packages.versions.files.CreatePackageVersionFilePayload
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

class CreatePackageVersionFileUseCaseImpl(
    private val repository: PackageVersionFilesRepository,
    private val storageService: StorageService,
) : CreatePackageVersionFileUseCase {
    override suspend fun invoke(
        payload: CreatePackageVersionFilePayload,
        versionId: Uuid,
        source: FileSource,
    ): PackageVersionFile? {
        repository.getByName(payload.name, versionId)?.let {
            throw PackageVersionFileAlreadyExistsException()
        }

        // Everything ends up as a stream, which is what the storage uploads.
        val stream = when (source) {
            is FileFromStream -> source
            is FileFromBytes -> return source.bytes.inputStream().use {
                invoke(payload, versionId, FileFromStream(it, source.contentType, source.bytes.size.toLong()))
            }

            is FileFromUrl -> return withContext(Dispatchers.IO) {
                val connection = source.url.openConnection()
                val contentLength = connection.contentLengthLong.takeIf { it > 0 } ?: 0L
                connection.getInputStream().use {
                    invoke(payload, versionId, FileFromStream(it, source.contentType, contentLength))
                }
            }
        }

        storageService.uploadStream(stream, payload.path)
        return repository.create(payload, versionId, stream.contentType.toString(), stream.contentLength)
    }
}

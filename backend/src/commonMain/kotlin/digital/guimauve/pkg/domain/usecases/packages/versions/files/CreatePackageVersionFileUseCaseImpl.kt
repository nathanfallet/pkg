package digital.guimauve.pkg.domain.usecases.packages.versions.files

import digital.guimauve.pkg.domain.exceptions.packages.versions.files.PackageVersionFileAlreadyExistsException
import digital.guimauve.pkg.domain.repositories.PackageVersionFilesRepository
import digital.guimauve.pkg.models.packages.versions.files.CreatePackageVersionFilePayload
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.services.storage.*
import kotlin.uuid.Uuid

class CreatePackageVersionFileUseCaseImpl(
    private val repository: PackageVersionFilesRepository,
    private val storageService: IStorageService,
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

            is FileFromUrl -> {
                val connection = source.url.openConnection()
                val contentLength = connection.contentLengthLong.takeIf { it > 0 } ?: 0L
                return connection.getInputStream().use {
                    invoke(payload, versionId, FileFromStream(it, source.contentType, contentLength))
                }
            }
        }

        storageService.uploadStream(stream, payload.path)
        return repository.create(payload, versionId, stream.contentType.toString(), stream.contentLength)
    }
}

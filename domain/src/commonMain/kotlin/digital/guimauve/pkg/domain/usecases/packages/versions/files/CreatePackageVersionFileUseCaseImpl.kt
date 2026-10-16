package digital.guimauve.pkg.domain.usecases.packages.versions.files

import digital.guimauve.pkg.domain.exceptions.packages.versions.files.PackageVersionFileAlreadyExistsException
import digital.guimauve.pkg.domain.models.packages.versions.files.CreatePackageVersionFilePayload
import digital.guimauve.pkg.domain.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.domain.models.storage.FileFromBytes
import digital.guimauve.pkg.domain.models.storage.FileFromStream
import digital.guimauve.pkg.domain.models.storage.FileFromUrl
import digital.guimauve.pkg.domain.models.storage.FileSource
import digital.guimauve.pkg.domain.repositories.PackageVersionFilesRepository
import digital.guimauve.pkg.domain.services.StorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.Arrays
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

        repository.getByName(payload.name, versionId)?.let { existing ->
            if (hasContent(existing, stream)) return existing
            throw PackageVersionFileAlreadyExistsException()
        }

        // A file the storage did not keep must not be recorded: it would be listed but never served.
        storageService.uploadStream(stream, payload.path) ?: return null
        return repository.create(payload, versionId, stream.contentType.toString(), stream.contentLength)
    }

    /**
     * Whether [stream] carries exactly the bytes already stored for [file]. The sizes are compared
     * first, so a different file is told apart without reading the stored one.
     */
    private suspend fun hasContent(file: PackageVersionFile, stream: FileFromStream): Boolean {
        if (stream.contentLength != file.size) return false
        val stored = storageService.downloadStream(file.path) ?: return false
        return withContext(Dispatchers.IO) {
            stored.use { sameBytes(it, stream.inputStream) }
        }
    }

    private fun sameBytes(first: InputStream, second: InputStream): Boolean {
        val firstBuffer = ByteArray(BUFFER_SIZE)
        val secondBuffer = ByteArray(BUFFER_SIZE)
        while (true) {
            val read = first.readNBytes(firstBuffer, 0, BUFFER_SIZE)
            if (second.readNBytes(secondBuffer, 0, BUFFER_SIZE) != read) return false
            if (read == 0) return true
            if (!Arrays.equals(firstBuffer, 0, read, secondBuffer, 0, read)) return false
        }
    }

    private companion object {
        const val BUFFER_SIZE = 64 * 1024
    }
}

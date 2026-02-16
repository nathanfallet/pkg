package digital.guimauve.pkg.domain.usecases.packages.versions.files

import dev.kaccelero.commons.exceptions.ControllerException
import dev.kaccelero.commons.repositories.ICreateChildModelWithContextSuspendUseCase
import dev.kaccelero.models.IContext
import dev.kaccelero.models.UUID
import digital.guimauve.pkg.domain.repositories.IPackageVersionFilesRepository
import digital.guimauve.pkg.models.packages.versions.files.CreatePackageVersionFilePayload
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.services.storage.FileContext
import digital.guimauve.pkg.services.storage.FileFromBytesContext
import digital.guimauve.pkg.services.storage.FileFromUrlContext
import digital.guimauve.pkg.services.storage.IStorageService
import io.ktor.http.*

class CreatePackageVersionFileUseCase(
    private val repository: IPackageVersionFilesRepository,
    private val storageService: IStorageService,
) : ICreateChildModelWithContextSuspendUseCase<PackageVersionFile, CreatePackageVersionFilePayload, UUID> {

    override suspend fun invoke(
        input1: CreatePackageVersionFilePayload,
        input2: UUID,
        input3: IContext,
    ): PackageVersionFile? {
        repository.getByName(input1.name, input2)?.let {
            throw ControllerException(HttpStatusCode.BadRequest, "files_already_exists")
        }

        (input3 as? FileFromBytesContext)?.let { fileFromBytes ->
            val contentLength = fileFromBytes.bytes.size.toLong()
            return fileFromBytes.bytes.inputStream().use { stream ->
                invoke(input1, input2, FileContext(stream, fileFromBytes.contentType, contentLength))
            }
        }
        (input3 as? FileFromUrlContext)?.let { fileFromUrl ->
            val connection = fileFromUrl.url.openConnection()
            val contentLength = connection.contentLengthLong.takeIf { it > 0 } ?: 0L
            return connection.getInputStream().use { stream ->
                invoke(input1, input2, FileContext(stream, fileFromUrl.contentType, contentLength))
            }
        }
        val fileContext = input3 as? FileContext ?: return null
        storageService.uploadStream(fileContext, input1.path)
        return repository.create(input1, input2, fileContext)
    }

}

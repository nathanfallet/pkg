package digital.guimauve.pkg.domain.usecases.packages.versions.files

import dev.kaccelero.commons.exceptions.ControllerException
import dev.kaccelero.commons.responses.BytesResponse
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.services.storage.IStorageService
import io.ktor.http.*

class DownloadFileUseCase(
    private val storageService: IStorageService,
) : IDownloadFileUseCase {

    override suspend fun invoke(input: PackageVersionFile): BytesResponse {
        val stream = storageService.downloadStream(input.path)
            ?: throw ControllerException(HttpStatusCode.NotFound, "storage_file_not_found")
        val (contentType, contentSubtype) = input.contentType.split("/")
        return BytesResponse(
            bytes = stream.readBytes(),
            contentType = ContentType(contentType, contentSubtype),
        )
    }

}

package digital.guimauve.pkg.controllers.packages.maven

import dev.kaccelero.commons.exceptions.ControllerException
import dev.kaccelero.commons.repositories.ICreateChildModelWithContextSuspendUseCase
import dev.kaccelero.commons.responses.BytesResponse
import dev.kaccelero.commons.users.IGetUserForCallUseCase
import dev.kaccelero.commons.users.IRequireUserForCallUseCase
import digital.guimauve.pkg.domain.usecases.packages.IGetOrCreatePackageUseCase
import digital.guimauve.pkg.domain.usecases.packages.IGetPackageByNameUseCase
import digital.guimauve.pkg.domain.usecases.packages.maven.ParseMavenPathUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.IGetLatestPackageVersionUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.IGetOrCreatePackageVersionUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.IGetPackageVersionByNameUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.files.IDownloadFileUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.files.IGetLatestPackageVersionFileUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.files.IGetPackageVersionFileByNameUseCase
import digital.guimauve.pkg.models.packages.PackageFormat
import digital.guimauve.pkg.models.packages.versions.files.CreatePackageVersionFilePayload
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.models.users.User
import digital.guimauve.pkg.services.storage.FileContext
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

class MavenController(
    private val getUserUseCase: IGetUserForCallUseCase,
    private val requireUserUseCase: IRequireUserForCallUseCase,
    private val parseMavenPathUseCase: ParseMavenPathUseCase,
    private val getPackageUseCase: IGetPackageByNameUseCase,
    private val getOrCreatePackageUseCase: IGetOrCreatePackageUseCase,
    private val getPackageVersionUseCase: IGetPackageVersionByNameUseCase,
    private val getOrCreatePackageVersionUseCase: IGetOrCreatePackageVersionUseCase,
    private val getLatestVersionUseCase: IGetLatestPackageVersionUseCase,
    private val getPackageVersionFileByNameUseCase: IGetPackageVersionFileByNameUseCase,
    private val getLatestPackageVersionFileUseCase: IGetLatestPackageVersionFileUseCase,
    private val createPackageVersionFileUseCase: ICreateChildModelWithContextSuspendUseCase<PackageVersionFile, CreatePackageVersionFilePayload, Uuid>,
    private val downloadFileUseCase: IDownloadFileUseCase,
) : IMavenController {

    override suspend fun get(call: ApplicationCall): BytesResponse {
        val user = getUserUseCase(call) as? User
        val mavenPath = parseMavenPathUseCase(call.parameters.getAll("path") ?: emptyList())
        val `package` = getPackageUseCase(mavenPath.packageName, PackageFormat.MAVEN, user)
            ?: throw ControllerException(HttpStatusCode.NotFound, "packages_not_found")
        val version = mavenPath.version?.let {
            getPackageVersionUseCase(it, `package`.id)
                ?: throw ControllerException(HttpStatusCode.NotFound, "packages_versions_not_found")
        }
        val file = version?.id?.let { getPackageVersionFileByNameUseCase(mavenPath.filename, version.id) }
            ?: getLatestPackageVersionFileUseCase(mavenPath.filename, `package`.id)
            ?: throw ControllerException(HttpStatusCode.NotFound, "packages_versions_files_not_found")
        return downloadFileUseCase(file)
    }

    override suspend fun put(call: ApplicationCall): Unit = withContext(Dispatchers.IO) {
        val user = requireUserUseCase(call) as User
        val mavenPath = parseMavenPathUseCase(call.parameters.getAll("path") ?: emptyList())
        val `package` = getOrCreatePackageUseCase(mavenPath.packageName, PackageFormat.MAVEN, user)
            ?: throw ControllerException(HttpStatusCode.NotFound, "packages_not_found")
        val version = mavenPath.version?.let { getOrCreatePackageVersionUseCase(it, `package`.id, user) }
            ?: getLatestVersionUseCase(`package`.id)
            ?: throw ControllerException(HttpStatusCode.NotFound, "packages_versions_not_found")

        val inputStream = call.receiveStream()
        val contentType = call.request.contentType()
        createPackageVersionFileUseCase(
            CreatePackageVersionFilePayload(mavenPath.filename, `package`, version),
            version.id,
            FileContext(
                inputStream,
                contentType.takeIf { it != ContentType.Any } ?: ContentType.Application.OctetStream,
                call.request.contentLength() ?: 0
            )
        ) ?: throw ControllerException(HttpStatusCode.BadRequest, "file_not_uploaded")
    }

}

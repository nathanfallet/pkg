package digital.guimauve.pkg.presentation.routes.packages.maven

import digital.guimauve.pkg.domain.exceptions.packages.PackageNotFoundException
import digital.guimauve.pkg.domain.exceptions.packages.versions.PackageVersionNotFoundException
import digital.guimauve.pkg.domain.exceptions.packages.versions.files.FileNotUploadedException
import digital.guimauve.pkg.domain.exceptions.packages.versions.files.PackageVersionFileNotFoundException
import digital.guimauve.pkg.domain.models.storage.FileFromStream
import digital.guimauve.pkg.domain.usecases.packages.GetOrCreatePackageUseCase
import digital.guimauve.pkg.domain.usecases.packages.GetPackageByNameUseCase
import digital.guimauve.pkg.domain.usecases.packages.maven.ParseMavenPathUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.GetLatestPackageVersionUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.GetOrCreatePackageVersionUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.GetPackageVersionByNameUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.files.CreatePackageVersionFileUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.files.DownloadFileUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.files.GetLatestPackageVersionFileUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.files.GetPackageVersionFileByNameUseCase
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.models.packages.PackageFormat
import digital.guimauve.pkg.models.packages.versions.files.CreatePackageVersionFilePayload
import digital.guimauve.pkg.presentation.extensions.requireUser
import digital.guimauve.pkg.presentation.extensions.userOrNull
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Dependencies required for the maven registry routes.
 */
data class MavenRoutesDependencies(
    val parseMavenPathUseCase: ParseMavenPathUseCase,
    val getPackageByNameUseCase: GetPackageByNameUseCase,
    val getOrCreatePackageUseCase: GetOrCreatePackageUseCase,
    val getPackageVersionByNameUseCase: GetPackageVersionByNameUseCase,
    val getOrCreatePackageVersionUseCase: GetOrCreatePackageVersionUseCase,
    val getLatestPackageVersionUseCase: GetLatestPackageVersionUseCase,
    val getPackageVersionFileByNameUseCase: GetPackageVersionFileByNameUseCase,
    val getLatestPackageVersionFileUseCase: GetLatestPackageVersionFileUseCase,
    val createPackageVersionFileUseCase: CreatePackageVersionFileUseCase,
    val downloadFileUseCase: DownloadFileUseCase,
    val getUserUseCase: GetUserUseCase,
)

/**
 * Configures the maven registry routes.
 */
fun Route.mavenRoutes(dependencies: MavenRoutesDependencies) = with(dependencies) {
    get("/maven2/{path...}") {
        val user = call.userOrNull(getUserUseCase)
        val mavenPath = parseMavenPathUseCase(call.parameters.getAll("path").orEmpty())
        val pkg = getPackageByNameUseCase(mavenPath.packageName, PackageFormat.MAVEN, user)
            ?: throw PackageNotFoundException()
        val version = mavenPath.version?.let {
            getPackageVersionByNameUseCase(it, pkg.id) ?: throw PackageVersionNotFoundException()
        }
        val file = version?.let { getPackageVersionFileByNameUseCase(mavenPath.filename, it.id) }
            ?: getLatestPackageVersionFileUseCase(mavenPath.filename, pkg.id)
            ?: throw PackageVersionFileNotFoundException()
        call.respondBytes(downloadFileUseCase(file), ContentType.parse(file.contentType))
    }

    put("/maven2/{path...}") {
        val user = call.requireUser(getUserUseCase)
        val mavenPath = parseMavenPathUseCase(call.parameters.getAll("path").orEmpty())
        val pkg = getOrCreatePackageUseCase(mavenPath.packageName, PackageFormat.MAVEN, user)
            ?: throw PackageNotFoundException()
        val version = mavenPath.version?.let { getOrCreatePackageVersionUseCase(it, pkg.id, user) }
            ?: getLatestPackageVersionUseCase(pkg.id)
            ?: throw PackageVersionNotFoundException()

        val contentType = call.request.contentType()
            .takeIf { it != ContentType.Any } ?: ContentType.Application.OctetStream
        createPackageVersionFileUseCase(
            CreatePackageVersionFilePayload(mavenPath.filename, pkg, version),
            version.id,
            FileFromStream(
                call.receiveStream(),
                contentType.toString(),
                call.request.contentLength() ?: 0
            )
        ) ?: throw FileNotUploadedException()
        // 204, which is what the previous router answered and what maven clients expect here.
        call.respond(HttpStatusCode.NoContent)
    }
}

package digital.guimauve.pkg.presentation.routes.packages.npm

import digital.guimauve.pkg.api.models.packages.npm.NpmLoginRequest
import digital.guimauve.pkg.api.models.packages.npm.NpmLoginResponse
import digital.guimauve.pkg.api.models.packages.npm.NpmPublishRequest
import digital.guimauve.pkg.domain.exceptions.auth.InvalidCredentialsException
import digital.guimauve.pkg.domain.exceptions.packages.PackageNotFoundException
import digital.guimauve.pkg.domain.exceptions.packages.versions.PackageVersionNotFoundException
import digital.guimauve.pkg.domain.exceptions.packages.versions.files.FileNotUploadedException
import digital.guimauve.pkg.domain.exceptions.packages.versions.files.PackageVersionFileNotFoundException
import digital.guimauve.pkg.domain.models.auth.LoginPayload
import digital.guimauve.pkg.domain.models.auth.TokenType
import digital.guimauve.pkg.domain.models.packages.PackageFormat
import digital.guimauve.pkg.domain.models.packages.versions.files.CreatePackageVersionFilePayload
import digital.guimauve.pkg.domain.models.storage.FileFromBytes
import digital.guimauve.pkg.domain.services.TokenService
import digital.guimauve.pkg.domain.usecases.auth.LoginUseCase
import digital.guimauve.pkg.domain.usecases.packages.GetOrCreatePackageUseCase
import digital.guimauve.pkg.domain.usecases.packages.GetPackageByNameUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.GetOrCreatePackageVersionUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.GetPackageVersionByNameUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.ListPackageVersionsUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.files.CreatePackageVersionFileUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.files.DownloadFileUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.files.GetLatestPackageVersionFileUseCase
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.presentation.extensions.requireUser
import digital.guimauve.pkg.presentation.extensions.respondArtifact
import digital.guimauve.pkg.presentation.extensions.userOrNull
import digital.guimauve.pkg.presentation.mappers.packages.npm.tarballName
import digital.guimauve.pkg.presentation.mappers.packages.npm.toNpmPackument
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.io.encoding.Base64

// The registry protocol is not fully specified; the shapes below were captured from a real
// `npm publish` and `npm install`. See docs/npm.md.

/**
 * Dependencies required for the npm registry routes.
 */
data class NpmRoutesDependencies(
    val getPackageByNameUseCase: GetPackageByNameUseCase,
    val getOrCreatePackageUseCase: GetOrCreatePackageUseCase,
    val getPackageVersionByNameUseCase: GetPackageVersionByNameUseCase,
    val getOrCreatePackageVersionUseCase: GetOrCreatePackageVersionUseCase,
    val listPackageVersionsUseCase: ListPackageVersionsUseCase,
    val createPackageVersionFileUseCase: CreatePackageVersionFileUseCase,
    val getLatestPackageVersionFileUseCase: GetLatestPackageVersionFileUseCase,
    val downloadFileUseCase: DownloadFileUseCase,
    val getUserUseCase: GetUserUseCase,
    val loginUseCase: LoginUseCase,
    val tokenService: TokenService,
)

/**
 * Where this instance serves the npm registry from, taken from the request so that it is right
 * behind a proxy and in a local run alike.
 */
private fun ApplicationCall.npmBaseUrl(): String = with(request.origin) {
    val port = if ((scheme == "https" && serverPort == 443) || (scheme == "http" && serverPort == 80)) ""
    else ":$serverPort"
    "$scheme://$serverHost$port/npm"
}

private val json = Json { ignoreUnknownKeys = true }

/**
 * Configures the npm registry routes.
 */
fun Route.npmRoutes(dependencies: NpmRoutesDependencies) = with(dependencies) {
    // TODO: implement web login, see
    // https://marmelab.com/blog/2022/12/22/how-to-implement-web-login-in-a-private-npm-registry.html
    post("/npm/-/v1/login") {
        call.respond(emptyMap<String, String>())
    }

    // What `npm login --auth-type=legacy` calls. The username is carried in the path, in couchdb
    // form, and the answer is the token the client writes to its `.npmrc`.
    put("/npm/-/user/{userId}") {
        val credentials = call.receive<NpmLoginRequest>()
        val user = loginUseCase(LoginPayload(credentials.name, credentials.password))
            ?: throw InvalidCredentialsException()
        call.respond(
            HttpStatusCode.Created,
            NpmLoginResponse(
                id = "org.couchdb.user:${credentials.name}",
                token = tokenService.generateToken(user.id, TokenType.ACCESS),
            )
        )
    }

    put("/npm/{path...}") {
        val user = call.requireUser(getUserUseCase)
        val target = parseNpmPath(call.parameters.getAll("path").orEmpty())
        val request = call.receive<NpmPublishRequest>()

        val pkg = getOrCreatePackageUseCase(target.packageName, PackageFormat.NPM, user)
            ?: throw PackageNotFoundException()

        // A publish carries one version, but the shape allows several, and a client is free to
        // send them all.
        request.versions.forEach { (versionName, manifest) ->
            val version = getOrCreatePackageVersionUseCase(
                versionName,
                pkg.id,
                user,
                json.encodeToString(JsonObject.serializer(), manifest),
            ) ?: throw PackageVersionNotFoundException()

            // The archive is keyed by the name npm gave it, which is not the one we serve it under.
            val attachment = request.attachments.values.firstOrNull() ?: throw FileNotUploadedException()
            val bytes = runCatching { Base64.decode(attachment.data) }.getOrNull()
                ?: throw FileNotUploadedException()

            createPackageVersionFileUseCase(
                CreatePackageVersionFilePayload(tarballName(pkg.name, versionName), pkg, version),
                version.id,
                FileFromBytes(bytes, attachment.contentType),
            ) ?: throw FileNotUploadedException()
        }

        call.respond(HttpStatusCode.Created, mapOf("ok" to true))
    }

    get("/npm/{path...}") {
        val user = call.userOrNull(getUserUseCase)
        val target = parseNpmPath(call.parameters.getAll("path").orEmpty())
        val pkg = getPackageByNameUseCase(target.packageName, PackageFormat.NPM, user)
            ?: throw PackageNotFoundException()

        when (target) {
            is NpmPath.Packument -> call.respond(
                pkg.toNpmPackument(listPackageVersionsUseCase(pkg.id), call.npmBaseUrl())
            )

            is NpmPath.Version -> {
                val version = getPackageVersionByNameUseCase(target.version, pkg.id)
                    ?: throw PackageVersionNotFoundException()
                val packument = pkg.toNpmPackument(listOf(version), call.npmBaseUrl())
                call.respond(packument.versions[version.version] ?: throw PackageVersionNotFoundException())
            }

            is NpmPath.Tarball -> {
                val file = getLatestPackageVersionFileUseCase(target.fileName, pkg.id)
                    ?: throw PackageVersionFileNotFoundException()
                call.respondArtifact(file.name, downloadFileUseCase(file))
            }
        }
    }
}

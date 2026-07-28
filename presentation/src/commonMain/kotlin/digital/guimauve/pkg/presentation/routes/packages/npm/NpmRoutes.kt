package digital.guimauve.pkg.presentation.routes.packages.npm

import digital.guimauve.pkg.api.models.packages.npm.NpmPackage
import digital.guimauve.pkg.api.models.packages.npm.NpmVersion
import digital.guimauve.pkg.domain.exceptions.packages.PackageNotFoundException
import digital.guimauve.pkg.domain.exceptions.packages.versions.PackageVersionNotFoundException
import digital.guimauve.pkg.domain.models.packages.PackageFormat
import digital.guimauve.pkg.domain.usecases.packages.GetPackageByNameUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.GetPackageVersionByNameUseCase
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.presentation.extensions.requireUser
import digital.guimauve.pkg.presentation.extensions.userOrNull
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// Spec: https://github.com/npm/registry/blob/main/docs/REGISTRY-API.md

/**
 * Dependencies required for the npm registry routes.
 */
data class NpmRoutesDependencies(
    val getPackageByNameUseCase: GetPackageByNameUseCase,
    val getPackageVersionByNameUseCase: GetPackageVersionByNameUseCase,
    val getUserUseCase: GetUserUseCase,
)

/**
 * Configures the npm registry routes.
 */
fun Route.npmRoutes(dependencies: NpmRoutesDependencies) = with(dependencies) {
    // TODO: implement web login, see
    // https://marmelab.com/blog/2022/12/22/how-to-implement-web-login-in-a-private-npm-registry.html
    post("/npm/-/v1/login") {
        call.respond(emptyMap<String, String>())
    }

    put("/npm/{packageName}") {
        call.requireUser(getUserUseCase)
        val payload = call.receive<NpmPackage>()
        println(payload) // TODO: publish the tarballs the payload carries
        call.respond(HttpStatusCode.NoContent)
    }

    get("/npm/{packageName}") {
        val user = call.userOrNull(getUserUseCase)
        val packageName = call.parameters["packageName"].orEmpty()
        val pkg = getPackageByNameUseCase(packageName, PackageFormat.NPM, user)
            ?: throw PackageNotFoundException()
        call.respond(
            NpmPackage(
                id = pkg.name,
                name = pkg.name,
                description = null,
                versions = emptyMap()
            )
        )
    }

    get("/npm/{packageName}/{version}") {
        val user = call.userOrNull(getUserUseCase)
        val packageName = call.parameters["packageName"].orEmpty()
        val pkg = getPackageByNameUseCase(packageName, PackageFormat.NPM, user)
            ?: throw PackageNotFoundException()
        val packageVersion = getPackageVersionByNameUseCase(call.parameters["version"].orEmpty(), pkg.id)
            ?: throw PackageVersionNotFoundException()
        call.respond(
            NpmVersion(
                name = pkg.name,
                version = packageVersion.version
            )
        )
    }
}

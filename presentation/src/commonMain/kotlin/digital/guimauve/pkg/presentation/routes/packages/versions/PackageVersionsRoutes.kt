package digital.guimauve.pkg.presentation.routes.packages.versions

import digital.guimauve.pkg.api.resources.packages.versions.PackageVersionsApi
import digital.guimauve.pkg.domain.exceptions.packages.PackageNotFoundException
import digital.guimauve.pkg.domain.exceptions.packages.versions.PackageVersionNotFoundException
import digital.guimauve.pkg.domain.usecases.organizations.GetOrganizationUseCase
import digital.guimauve.pkg.domain.usecases.packages.GetPackageUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.GetPackageVersionUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.ListPackageVersionsUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.files.ListPackageVersionFilesUseCase
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.models.packages.Package
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import digital.guimauve.pkg.presentation.extensions.requireOrganization
import digital.guimauve.pkg.presentation.mappers.packages.versions.files.toPackageVersionFileResponse
import digital.guimauve.pkg.presentation.mappers.packages.versions.toPackageVersionResponse
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Dependencies required for package versions routes.
 */
data class PackageVersionsRoutesDependencies(
    val listPackageVersionsUseCase: ListPackageVersionsUseCase,
    val getPackageVersionUseCase: GetPackageVersionUseCase,
    val listPackageVersionFilesUseCase: ListPackageVersionFilesUseCase,
    val getPackageUseCase: GetPackageUseCase,
    val getOrganizationUseCase: GetOrganizationUseCase,
    val getUserUseCase: GetUserUseCase,
)

/**
 * Resolves the package the versions belong to, checking the caller has access to its organization.
 */
private suspend fun ApplicationCall.requirePackage(
    resource: PackageVersionsApi,
    dependencies: PackageVersionsRoutesDependencies,
): Package = with(dependencies) {
    val organization = requireOrganization(resource.organizationId, getUserUseCase, getOrganizationUseCase)
    getPackageUseCase(resource.packageId, organization.id) ?: throw PackageNotFoundException()
}

/**
 * Resolves a specific version of a package, checking the caller has access to its organization.
 */
private suspend fun ApplicationCall.requirePackageVersion(
    resource: PackageVersionsApi.Id,
    dependencies: PackageVersionsRoutesDependencies,
): PackageVersion = with(dependencies) {
    val pkg = requirePackage(resource.parent, dependencies)
    getPackageVersionUseCase(resource.versionId, pkg.id) ?: throw PackageVersionNotFoundException()
}

/**
 * Configures package versions routes.
 */
fun Route.packageVersionsRoutes(dependencies: PackageVersionsRoutesDependencies) = with(dependencies) {
    get<PackageVersionsApi> { resource ->
        val pkg = call.requirePackage(resource, dependencies)
        call.respond(listPackageVersionsUseCase(pkg.id).map { it.toPackageVersionResponse() })
    }
    get<PackageVersionsApi.Id> { resource ->
        val version = call.requirePackageVersion(resource, dependencies)
        call.respond(version.toPackageVersionResponse())
    }
    get<PackageVersionsApi.Id.Files> { resource ->
        val version = call.requirePackageVersion(resource.parent, dependencies)
        call.respond(listPackageVersionFilesUseCase(version.id).map { it.toPackageVersionFileResponse() })
    }
}

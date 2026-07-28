package digital.guimauve.pkg.presentation.routes.packages

import digital.guimauve.pkg.api.resources.packages.PackagesApi
import digital.guimauve.pkg.domain.exceptions.packages.PackageNotFoundException
import digital.guimauve.pkg.domain.usecases.organizations.GetOrganizationUseCase
import digital.guimauve.pkg.domain.usecases.packages.GetPackageUseCase
import digital.guimauve.pkg.domain.usecases.packages.ListPackagesUseCase
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.presentation.extensions.requireOrganization
import digital.guimauve.pkg.presentation.mappers.packages.toPackageResponse
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Dependencies required for packages routes.
 */
data class PackagesRoutesDependencies(
    val listPackagesUseCase: ListPackagesUseCase,
    val getPackageUseCase: GetPackageUseCase,
    val getOrganizationUseCase: GetOrganizationUseCase,
    val getUserUseCase: GetUserUseCase,
)

/**
 * Configures packages routes.
 */
fun Route.packagesRoutes(dependencies: PackagesRoutesDependencies) = with(dependencies) {
    get<PackagesApi> { resource ->
        val organization = call.requireOrganization(
            resource.organizationId,
            getUserUseCase,
            getOrganizationUseCase
        )
        call.respond(listPackagesUseCase(organization.id).map { it.toPackageResponse() })
    }
    get<PackagesApi.Id> { resource ->
        val organization = call.requireOrganization(
            resource.parent.organizationId,
            getUserUseCase,
            getOrganizationUseCase
        )
        val pkg = getPackageUseCase(resource.packageId, organization.id)
            ?: throw PackageNotFoundException()
        call.respond(pkg.toPackageResponse())
    }
}

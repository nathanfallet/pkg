package digital.guimauve.pkg.presentation.routes.organizations

import digital.guimauve.pkg.api.resources.organizations.OrganizationsApi
import digital.guimauve.pkg.domain.usecases.organizations.GetOrganizationUseCase
import digital.guimauve.pkg.domain.usecases.organizations.ListOrganizationsUseCase
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.presentation.extensions.requireOrganization
import digital.guimauve.pkg.presentation.mappers.organizations.toOrganizationResponse
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Dependencies required for organizations routes.
 */
data class OrganizationsRoutesDependencies(
    val listOrganizationsUseCase: ListOrganizationsUseCase,
    val getOrganizationUseCase: GetOrganizationUseCase,
    val getUserUseCase: GetUserUseCase,
)

/**
 * Configures organizations routes.
 */
fun Route.organizationsRoutes(dependencies: OrganizationsRoutesDependencies) = with(dependencies) {
    get<OrganizationsApi> {
        call.respond(listOrganizationsUseCase().map { it.toOrganizationResponse() })
    }
    get<OrganizationsApi.Id> { resource ->
        val organization = call.requireOrganization(
            resource.organizationId,
            getUserUseCase,
            getOrganizationUseCase
        )
        call.respond(organization.toOrganizationResponse())
    }
}

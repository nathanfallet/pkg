package digital.guimauve.pkg.presentation.routes.users

import digital.guimauve.pkg.api.resources.users.UsersApi
import digital.guimauve.pkg.domain.exceptions.users.UserNotFoundException
import digital.guimauve.pkg.domain.usecases.organizations.GetOrganizationUseCase
import digital.guimauve.pkg.domain.usecases.users.GetUserInOrganizationUseCase
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.domain.usecases.users.ListUsersUseCase
import digital.guimauve.pkg.presentation.extensions.requireOrganization
import digital.guimauve.pkg.presentation.mappers.users.toUserResponse
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Dependencies required for users routes.
 */
data class UsersRoutesDependencies(
    val listUsersUseCase: ListUsersUseCase,
    val getUserInOrganizationUseCase: GetUserInOrganizationUseCase,
    val getOrganizationUseCase: GetOrganizationUseCase,
    val getUserUseCase: GetUserUseCase,
)

/**
 * Configures users routes.
 */
fun Route.usersRoutes(dependencies: UsersRoutesDependencies) = with(dependencies) {
    get<UsersApi> { resource ->
        val organization = call.requireOrganization(
            resource.organizationId,
            getUserUseCase,
            getOrganizationUseCase
        )
        call.respond(listUsersUseCase(organization.id).map { it.toUserResponse() })
    }
    get<UsersApi.Id> { resource ->
        val organization = call.requireOrganization(
            resource.parent.organizationId,
            getUserUseCase,
            getOrganizationUseCase
        )
        val user = getUserInOrganizationUseCase(resource.userId, organization.id)
            ?: throw UserNotFoundException()
        call.respond(user.toUserResponse())
    }
}

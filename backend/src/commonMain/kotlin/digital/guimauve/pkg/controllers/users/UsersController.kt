package digital.guimauve.pkg.controllers.users

import dev.kaccelero.commons.exceptions.ControllerException
import digital.guimauve.pkg.domain.usecases.users.GetUserInOrganizationUseCase
import digital.guimauve.pkg.domain.usecases.users.ListUsersUseCase
import digital.guimauve.pkg.models.organizations.Organization
import digital.guimauve.pkg.models.users.User
import io.ktor.http.*
import io.ktor.server.application.*
import kotlin.uuid.Uuid

class UsersController(
    private val listUsersUseCase: ListUsersUseCase,
    private val getUserInOrganizationUseCase: GetUserInOrganizationUseCase,
) : IUsersController {

    override suspend fun list(call: ApplicationCall, parent: Organization): List<User> {
        return listUsersUseCase(parent.id)
    }

    override suspend fun get(call: ApplicationCall, parent: Organization, id: Uuid): Map<String, Any> {
        val user = getUserInOrganizationUseCase(id, parent.id)
            ?: throw ControllerException(HttpStatusCode.NotFound, "users_not_found")
        return mapOf(
            "item" to user,
            "organization" to parent
        )
    }

}

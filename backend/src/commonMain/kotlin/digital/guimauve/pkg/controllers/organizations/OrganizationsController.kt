package digital.guimauve.pkg.controllers.organizations

import dev.kaccelero.commons.exceptions.ControllerException
import dev.kaccelero.commons.repositories.IListModelSuspendUseCase
import digital.guimauve.pkg.domain.usecases.organizations.GetOrganizationUseCase
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.models.organizations.Organization
import digital.guimauve.pkg.presentation.extensions.requireUser
import io.ktor.http.*
import io.ktor.server.application.*
import kotlin.uuid.Uuid

class OrganizationsController(
    private val getUserUseCase: GetUserUseCase,
    private val listOrganizationsUseCase: IListModelSuspendUseCase<Organization>,
    private val getOrganizationUseCase: GetOrganizationUseCase,
) : IOrganizationsController {

    override suspend fun list(call: ApplicationCall): List<Organization> {
        return listOrganizationsUseCase()
    }

    override suspend fun get(call: ApplicationCall, id: Uuid): Organization {
        val organization = getOrganizationUseCase(id)
            ?: throw ControllerException(HttpStatusCode.NotFound, "organizations_not_found")
        val user = call.requireUser(getUserUseCase)
        if (user.organizationId != organization.id)
            throw ControllerException(HttpStatusCode.Forbidden, "organizations_not_allowed")
        return organization
    }

}

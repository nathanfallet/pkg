package digital.guimauve.pkg.domain.usecases.organizations

import dev.kaccelero.commons.exceptions.ControllerException
import dev.kaccelero.commons.users.IRequireUserForCallUseCase
import digital.guimauve.pkg.domain.repositories.IOrganizationsRepository
import digital.guimauve.pkg.models.organizations.Organization
import digital.guimauve.pkg.models.users.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.util.*

class RequireOrganizationForCallUseCase(
    private val repository: IOrganizationsRepository,
    private val requireUserForCallUseCase: IRequireUserForCallUseCase,
) : IRequireOrganizationForCallUseCase {

    private data class OrganizationForCall(
        val organization: Organization?,
    )

    private val associationKey = AttributeKey<OrganizationForCall>("organization")

    override suspend fun invoke(input: ApplicationCall): Organization {
        // Note: we cannot use `computeIfAbsent` because it does not support suspending functions
        return input.attributes.getOrNull(associationKey)?.organization ?: run {
            val user = requireUserForCallUseCase(input) as User
            val computed = OrganizationForCall(repository.get(user.organizationId))
            input.attributes.put(associationKey, computed)
            computed.organization
        } ?: throw ControllerException(HttpStatusCode.NotFound, "organizations_not_found")
    }

}

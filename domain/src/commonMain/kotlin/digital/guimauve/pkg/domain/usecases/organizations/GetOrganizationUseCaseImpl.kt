package digital.guimauve.pkg.domain.usecases.organizations

import digital.guimauve.pkg.domain.repositories.OrganizationsRepository
import digital.guimauve.pkg.models.organizations.Organization
import kotlin.uuid.Uuid

class GetOrganizationUseCaseImpl(
    private val repository: OrganizationsRepository,
) : GetOrganizationUseCase {
    override suspend fun invoke(organizationId: Uuid): Organization? = repository.get(organizationId)
}

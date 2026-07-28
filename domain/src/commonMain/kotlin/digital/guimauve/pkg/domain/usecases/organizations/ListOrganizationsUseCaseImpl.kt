package digital.guimauve.pkg.domain.usecases.organizations

import digital.guimauve.pkg.domain.models.organizations.Organization
import digital.guimauve.pkg.domain.repositories.OrganizationsRepository

class ListOrganizationsUseCaseImpl(
    private val repository: OrganizationsRepository,
) : ListOrganizationsUseCase {
    override suspend fun invoke(): List<Organization> = repository.list()
}

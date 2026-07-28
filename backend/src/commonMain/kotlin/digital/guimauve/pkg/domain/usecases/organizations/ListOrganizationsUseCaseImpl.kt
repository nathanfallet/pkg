package digital.guimauve.pkg.domain.usecases.organizations

import digital.guimauve.pkg.domain.repositories.OrganizationsRepository
import digital.guimauve.pkg.models.organizations.Organization

class ListOrganizationsUseCaseImpl(
    private val repository: OrganizationsRepository,
) : ListOrganizationsUseCase {
    override suspend fun invoke(): List<Organization> = repository.list()
}

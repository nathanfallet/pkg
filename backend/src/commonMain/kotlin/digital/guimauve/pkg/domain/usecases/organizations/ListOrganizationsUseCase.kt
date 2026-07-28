package digital.guimauve.pkg.domain.usecases.organizations

import digital.guimauve.pkg.models.organizations.Organization

interface ListOrganizationsUseCase {
    suspend operator fun invoke(): List<Organization>
}

package digital.guimauve.pkg.domain.usecases.organizations

import digital.guimauve.pkg.domain.models.organizations.Organization
import kotlin.uuid.Uuid

interface GetOrganizationUseCase {
    suspend operator fun invoke(organizationId: Uuid): Organization?
}

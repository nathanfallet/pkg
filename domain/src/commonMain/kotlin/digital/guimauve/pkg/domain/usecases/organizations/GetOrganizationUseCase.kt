package digital.guimauve.pkg.domain.usecases.organizations

import digital.guimauve.pkg.domain.models.organizations.Organization
import kotlin.uuid.Uuid

/** Reads an organization, or null if it doesn't exist. */
interface GetOrganizationUseCase {
    suspend operator fun invoke(organizationId: Uuid): Organization?
}

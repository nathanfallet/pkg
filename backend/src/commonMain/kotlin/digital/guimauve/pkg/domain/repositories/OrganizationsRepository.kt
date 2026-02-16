package digital.guimauve.pkg.domain.repositories

import dev.kaccelero.repositories.IModelSuspendRepository
import digital.guimauve.pkg.models.organizations.CreateOrganizationPayload
import digital.guimauve.pkg.models.organizations.Organization
import kotlin.uuid.Uuid

interface OrganizationsRepository : IModelSuspendRepository<Organization, Uuid, CreateOrganizationPayload, Unit>

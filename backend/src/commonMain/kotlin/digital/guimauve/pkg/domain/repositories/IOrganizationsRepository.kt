package digital.guimauve.pkg.domain.repositories

import dev.kaccelero.models.UUID
import dev.kaccelero.repositories.IModelSuspendRepository
import digital.guimauve.pkg.models.organizations.CreateOrganizationPayload
import digital.guimauve.pkg.models.organizations.Organization

interface IOrganizationsRepository : IModelSuspendRepository<Organization, UUID, CreateOrganizationPayload, Unit>
